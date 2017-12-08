// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr2
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager.pages

import com.planbase.pdf.layoutmanager.PdfItem
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.ScaledImage.WrappedImage
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.IOException
import java.util.SortedSet
import java.util.TreeSet

/**
 Caches the contents of a specific, single page for later drawing.  Inner classes are what's added
 to the cache and what controls the drawing.  You generally want to use [PageGrouping] when
 you want automatic page-breaking.  SinglePage is for when you want to force something onto a
 specific page only.
 */
class SinglePage(val pageNum: Int,
                 private val mgr: PdfLayoutMgr,
                 pageReactor: ((Int, SinglePage) -> Float)?) : RenderTarget {
    private var items : SortedSet<PdfItem> = TreeSet()
    private var lastOrd: Long = 0
    // The x-offset for the body section of this page (left-margin-ish)
    // THIS MUST COME LAST as items will not be initialized if it comes before.
    private val xOff: Float = pageReactor?.invoke(pageNum, this) ?: 0f

    private fun fillRect(bottomLeft: Coord, dim: Dim, c: PDColor, zIdx: Float) {
        items.add(FillRect(bottomLeft.plusX(xOff), dim, c, lastOrd++, zIdx))
    }

    /** {@inheritDoc}  */
    override fun fillRect(bottomLeft: Coord, dim: Dim, c: PDColor, reallyRender: Boolean): Float {
        if (reallyRender) {
            fillRect(bottomLeft, dim, c, -1f)
        }
        return dim.height
    }

    /** {@inheritDoc}  */
    override fun drawImage(bottomLeft: Coord, wi: WrappedImage, reallyRender: Boolean): Float {
        if (reallyRender) {
            items.add(DrawImage(bottomLeft.plusX(xOff), wi, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX))
        }
        // This does not account for a page break because this class represents a single page.
        return wi.dim.height
    }

    private fun drawLineStrip(points: List<Coord>, ls: LineStyle, z: Float) {
        items.add(DrawLine(points.map{ it.plusX(xOff) }.toList(), ls, lastOrd++, z))
    }

    /** [@inheritDoc]  */
    override fun drawLineStrip(points: List<Coord>, lineStyle: LineStyle, reallyRender: Boolean): SinglePage {
        if (reallyRender) {
            drawLineStrip(points, lineStyle, PdfItem.DEFAULT_Z_INDEX)
        }
        return this
    }

    private fun drawStyledText(baselineLeft: Coord, text: String, s: TextStyle, z: Float) {
        items.add(Text(baselineLeft.plusX(xOff), text, s, lastOrd++, z))
    }

    /** {@inheritDoc}  */
    override fun drawStyledText(baselineLeft: Coord, text: String, textStyle: TextStyle, reallyRender: Boolean): Float {
        if (reallyRender) {
            drawStyledText(baselineLeft, text, textStyle, PdfItem.DEFAULT_Z_INDEX)
        }
        return textStyle.lineHeight()
    }

    /**
    Returns the top margin necessary to push this item onto a new page if it won't fit on this one.
    A single page always returns zero suggesting that something won't flow onto another page, but it may
    still be truncated when it goes off the edge of this one.
     */
    override fun pageBreakingTopMargin(bottomY:Float, height:Float):Float = 0f

    @Throws(IOException::class)
    fun commit(stream: PDPageContentStream) {
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (item in items) {
            item.commit(stream)
        }
    }

    override fun toString(): String = "SinglePage($pageNum)"

    private class DrawLine(private val points:List<Coord>,
                           private val style: LineStyle,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setStrokingColor(style.color)
            stream.setLineWidth(style.thickness)
            var point = points[0]
            stream.moveTo(point.x, point.y)
            for (i in 1..points.lastIndex) {
                point = points[i]
                stream.lineTo(point.x, point.y)
            }
            stream.stroke()
        }
    }

    private class FillRect(val bottomLeft: Coord,
                           val dim: Dim,
                           val color: PDColor,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setNonStrokingColor(color)
            stream.addRect(bottomLeft.x, bottomLeft.y, dim.width, dim.height)
            stream.fill()
        }
    }

    /*
    Text is drawn from the baseline up.
     */
    internal class Text(private val baselineLeft: Coord, val t: String, val style: TextStyle,
                        ord: Long, z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.beginText()
            stream.setNonStrokingColor(style.textColor)
            stream.setFont(style.font, style.fontSize)
            stream.newLineAtOffset(baselineLeft.x, baselineLeft.y)
            stream.showText(t)
            stream.endText()
        }
    }

    private class DrawImage(val bottomLeft: Coord,
                            val scaledImage: WrappedImage,
                            mgr: PdfLayoutMgr,
                            ord: Long, z: Float) : PdfItem(ord, z) {
        private val img: PDImageXObject = mgr.ensureCached(scaledImage)

        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            // stream.drawImage(jpeg, x, y);
            val (width, height) = scaledImage.dim
            stream.drawImage(img, bottomLeft.x, bottomLeft.y, width, height)
        }
    }
}