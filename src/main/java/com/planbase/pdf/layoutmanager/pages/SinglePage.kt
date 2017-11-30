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
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.Point2
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

    private fun fillRect(bottomLeft: Point2, xyDim: XyDim, c: PDColor, zIdx: Float) {
        items.add(FillRect(bottomLeft.plusX(xOff), xyDim, c, lastOrd++, zIdx))
    }

    /** {@inheritDoc}  */
    override fun fillRect(bottomLeft: Point2, xyDim: XyDim, c: PDColor): Float {
        fillRect(bottomLeft, xyDim, c, -1f)
        return xyDim.height
    }

    /** {@inheritDoc}  */
    override fun drawImage(bottomLeft: Point2, wi: WrappedImage): Float {
        items.add(DrawImage(bottomLeft.plusX(xOff), wi, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX))
        // This does not account for a page break because this class represents a single page.
        return wi.xyDim.height
    }

    private fun drawLine(start: Point2, end: Point2, ls: LineStyle, z: Float) {
        items.add(DrawLine(start.plusX(xOff), end.plusX(xOff), ls, lastOrd++, z))
    }

    /** {@inheritDoc}  */
    override fun drawLine(start: Point2, end: Point2, lineStyle: LineStyle): SinglePage {
        drawLine(start, end, lineStyle, PdfItem.DEFAULT_Z_INDEX)
        return this
    }

    private fun drawStyledText(bottomLeft: Point2, text: String, s: TextStyle, z: Float) {
        items.add(Text(bottomLeft.plusX(xOff), text, s, lastOrd++, z))
    }

    /** {@inheritDoc}  */
    override fun drawStyledText(bottomLeft: Point2, text: String, textStyle: TextStyle): Float {
        drawStyledText(bottomLeft, text, textStyle, PdfItem.DEFAULT_Z_INDEX)
        return textStyle.lineHeight()
    }

    @Throws(IOException::class)
    fun commit(stream: PDPageContentStream) {
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (item in items) {
            item.commit(stream)
        }
    }

    override fun toString(): String = "SinglePage($pageNum)"

    private class DrawLine(private val start: Point2,
                           private val end: Point2,
                           private val style: LineStyle,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setStrokingColor(style.color)
            stream.setLineWidth(style.thickness)
            stream.moveTo(start.x, start.y)
            stream.lineTo(end.x, end.y)
            stream.stroke()
        }
    }

    private class FillRect(val bottomLeft: Point2,
                           val xyDim: XyDim,
                           val color: PDColor,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setNonStrokingColor(color)
            stream.addRect(bottomLeft.x, bottomLeft.y, xyDim.width, xyDim.height)
            stream.fill()
        }
    }

    internal class Text(private val bottomLeft: Point2, val t: String, val style: TextStyle,
                        ord: Long, z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.beginText()
            stream.setNonStrokingColor(style.textColor)
            stream.setFont(style.font, style.fontSize)
            stream.newLineAtOffset(bottomLeft.x, bottomLeft.y)
            stream.showText(t)
            stream.endText()
        }
    }

    private class DrawImage(val bottomLeft: Point2,
                            val scaledImage: WrappedImage,
                            mgr: PdfLayoutMgr,
                            ord: Long, z: Float) : PdfItem(ord, z) {
        private val img: PDImageXObject = mgr.ensureCached(scaledImage)

        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            // stream.drawImage(jpeg, x, y);
            val (width, height) = scaledImage.xyDim
            stream.drawImage(img, bottomLeft.x, bottomLeft.y, width, height)
        }
    }
}