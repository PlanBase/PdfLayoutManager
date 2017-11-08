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

import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.PdfItem
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.ScaledImage.WrappedImage
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.IOException
import java.util.SortedSet
import java.util.TreeSet

/**
 * Caches the contents of a specific, single page for later drawing.  Inner classes are what's added
 * to the cache and what controlls the drawing.  You generally want to use [PageGrouping] when
 * you want automatic page-breaking.  SinglePage is for when you want to force something onto a
 * specific page only.
 */
class SinglePage(val pageNum: Int,
                 private val mgr: PdfLayoutMgr,
                 pageReactor: ((Int, SinglePage) -> Float)?) : RenderTarget {
    private var items : SortedSet<PdfItem> = TreeSet()
    private var lastOrd: Long = 0
    // The x-offset for the body section of this page (left-margin-ish)
    // THIS MUST COME LAST as items will not be initialized if it comes before.
    private val xOff: Float = pageReactor?.invoke(pageNum, this) ?: 0f

    private fun fillRect(x: Float, y: Float, width: Float, height: Float, c: PDColor, zIdx: Float) {
        items.add(FillRect(x + xOff, y, width, height, c, lastOrd++, zIdx))
    }

    /** {@inheritDoc}  */
    override fun fillRect(outerTopLeft: XyOffset, outerDim: XyDim, c: PDColor): SinglePage {
        fillRect(outerTopLeft.x, outerTopLeft.y, outerDim.width, outerDim.height, c, -1f)
        return this
    }
    //        public void fillRect(final float xVal, final float yVal, final float w, final PDColor c,
    //                             final float h) {
    //            fillRect(xVal, yVal, w, h, c, PdfItem.DEFAULT_Z_INDEX);
    //        }
    //
    //        public void drawImage(final float xVal, final float yVal, final BufferedImage bi,
    //                             final PdfLayoutMgr mgr, final float z) {
    //            items.add(DrawImage.of(xVal, yVal, bi, mgr, lastOrd++, z));
    //        }

    /** {@inheritDoc}  */
    override fun drawImage(x: Float, y: Float, wi: WrappedImage): Float {
        items.add(DrawImage(x + xOff, y, wi, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX))
        // This does not account for a page break because this class represents a single page.
        return wi.xyDim.height
    }

    private fun drawLine(xa: Float, ya: Float, xb: Float, yb: Float, ls: LineStyle, z: Float) {
        items.add(DrawLine(xa + xOff, ya, xb + xOff, yb, ls, lastOrd++, z))
    }

    /** {@inheritDoc}  */
    override fun drawLine(x1: Float, y1: Float, x2: Float, y2: Float, lineStyle: LineStyle): SinglePage {
        drawLine(x1, y1, x2, y2, lineStyle, PdfItem.DEFAULT_Z_INDEX)
        return this
    }

    private fun drawStyledText(x: Float, y: Float, text: String, s: TextStyle, z: Float) {
        items.add(Text(x + xOff, y, text, s, lastOrd++, z))
    }

    /** {@inheritDoc}  */
    override fun drawStyledText(x: Float, y: Float, text: String, textStyle: TextStyle): SinglePage {
        drawStyledText(x, y, text, textStyle, PdfItem.DEFAULT_Z_INDEX)
        return this
    }

    @Throws(IOException::class)
    fun commit(stream: PDPageContentStream) {
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (item in items) {
            item.commit(stream)
        }
    }

    private class DrawLine(private val x1: Float,
                           private val y1: Float,
                           private val x2: Float,
                           private val y2: Float,
                           private val style: LineStyle,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setStrokingColor(style.color)
            stream.setLineWidth(style.thickness)
            stream.moveTo(x1, y1)
            stream.lineTo(x2, y2)
            stream.stroke()
        }
    }

    private class FillRect(val x: Float,
                           val y: Float,
                           val width: Float,
                           val height: Float,
                           val color: PDColor,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setNonStrokingColor(color)
            stream.addRect(x, y, width, height)
            stream.fill()
        }
    }

    internal class Text(val x: Float, val y: Float, val t: String, val style: TextStyle,
                        ord: Long, z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.beginText()
            stream.setNonStrokingColor(style.textColor)
            stream.setFont(style.font, style.fontSize)
            stream.newLineAtOffset(x, y)
            stream.showText(t)
            stream.endText()
        }
    }

    private class DrawImage(val x: Float,
                            val y: Float,
                            val scaledImage: WrappedImage,
                            mgr: PdfLayoutMgr,
                            ord: Long, z: Float) : PdfItem(ord, z) {
        private val img: PDImageXObject = mgr.ensureCached(scaledImage)

        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            // stream.drawImage(jpeg, x, y);
            val (width, height) = scaledImage.xyDim
            stream.drawImage(img, x, y, width, height)
        }
    }
}