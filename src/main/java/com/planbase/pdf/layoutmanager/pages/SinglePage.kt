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

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPages
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.PageArea
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.ScaledImage.WrappedImage
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.IOException
import java.util.SortedSet
import java.util.TreeSet

/**
 * Caches the contents of a specific, single page for later drawing.  Inner classes are what's added
 * to the cache and what controls the drawing.  You generally want to use [PageGrouping] when
 * you want automatic page-breaking.  SinglePage is for when you want to force something onto a
 * specific page only.
 * @param body the offset and size of the body area.
 */
class SinglePage(val pageNum: Int,
                 private val mgr: PdfLayoutMgr,
                 pageReactor: ((Int, SinglePage) -> Float)?,
                 override val body: PageArea) : RenderTarget {
    private var items : SortedSet<PdfItem> = TreeSet()
    private var lastOrd: Long = 0
    // The x-offset for the body section of this page (left-margin-ish)
    // THIS MUST COME LAST as items will not be initialized if it comes before.
    private val xOff: Float = pageReactor?.invoke(pageNum, this) ?: body.topLeft.x

    private fun fillRect(bottomLeft: Coord, dim: Dim, c: PDColor, zIdx: Float) {
        items.add(FillRect(bottomLeft.plusX(xOff), dim, c, lastOrd++, zIdx))
    }

    override fun fillRect(bottomLeft: Coord, dim: Dim, c: PDColor, reallyRender: Boolean): Float {
        if (reallyRender) {
            fillRect(bottomLeft, dim, c, -1f)
        }
        return dim.height
    }

    override fun drawImage(bottomLeft: Coord, wi: WrappedImage, reallyRender: Boolean): HeightAndPage {
        if (reallyRender) {
            items.add(DrawImage(bottomLeft.plusX(xOff), wi, mgr, lastOrd++, DEFAULT_Z_INDEX))
        }
        // This does not account for a page break because this class represents a single page.
        return HeightAndPage(wi.dim.height, pageNum)
    }

    private fun drawLineStrip(points: List<Coord>, ls: LineStyle, z: Float) {
        items.add(DrawLine(points.map{ it.plusX(xOff) }.toList(), ls, lastOrd++, z))
    }

    override fun drawLineStrip(points: List<Coord>, lineStyle: LineStyle, reallyRender: Boolean): IntRange {
        if (reallyRender) {
            drawLineStrip(points, lineStyle, DEFAULT_Z_INDEX)
        }
        return IntRange(pageNum, pageNum)
    }

    private fun drawStyledText(baselineLeft: Coord, text: String, s: TextStyle, z: Float) {
        items.add(Text(baselineLeft.plusX(xOff), text, s, lastOrd++, z))
    }

    override fun drawStyledText(baselineLeft: Coord, text: String, textStyle: TextStyle, reallyRender: Boolean): HeightAndPage {
        if (reallyRender) {
            drawStyledText(baselineLeft, text, textStyle, DEFAULT_Z_INDEX)
        }
        return HeightAndPage(textStyle.lineHeight, pageNum)
    }

    var cursorY:Float = body.topLeft.y

    /**
     * Add LineWrapped items directly to the page grouping at the specified coordinate.  This is a little more
     * work than adding an entire chapter to a cell and calling Cell.render(), but it allows each top level item
     * to return a page range.  These pages can later be used to create a table of contents or an index.
     *
     * @param topLeft the coordinate to add the item at.  Might want to make a convenience version of this method
     * that internally updates a cursor so you never have to specify this.
     * @param block the LineWrapped item to display
     */
    fun add(topLeft: Coord, block: LineWrapped): DimAndPages {
        this.pageBreakingTopMargin(topLeft.y - body.dim.height, body.dim.height, 0f)
        val dap: DimAndPages = block.render(this, topLeft)
        cursorY = topLeft.y - dap.dim.height
        return dap
    }

    /**
     * Add LineWrapped items directly to the page grouping at the current cursor and body-left.
     * The cursor is always at the left-hand side of the body at the bottom of the last item put on the page.
     *
     * @param block the LineWrapped item to display
     */
    fun append(block: LineWrapped): DimAndPages =
            add(Coord(xOff, cursorY), block)

    /**
     * Cell goes at bodyTopLeft.x and cursorY.  Cell width is bodyDim.width.
     *
     * @param cellStyle the style for the cell to make
     * @param contents the contents of the cell
     */
    fun appendCell(cellStyle: CellStyle, contents:List<LineWrappable>): DimAndPages =
            add(Coord(xOff, cursorY), Cell(cellStyle, body.dim.width, contents).wrap())

    /**
     * Returns the top margin necessary to push this item onto a new page if it won't fit on this one.
     * A single page always returns zero suggesting that something won't flow onto another page, but it may
     * still be truncated when it goes off the edge of this one.
     */
    override fun pageBreakingTopMargin(bottomY:Float, height:Float, requiredSpaceBelow:Float):Float = 0f

    @Throws(IOException::class)
    fun commit(stream: PDPageContentStream) {
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (item in items) {
            item.commit(stream)
        }
    }

    override fun toString(): String = "SinglePage($pageNum)"

    companion object {
        const val DEFAULT_Z_INDEX = 0f
    }

    /**
     * An internal class representing items to be later drawn to the page of a PDF file.
     * The z-index allows items to be drawn
     * from back (lower-z-values) to front (higher-z-values).  When the z-index of two items is the same
     * they will be drawn in the order they were created.  Implementing classes should give PdfItems
     * ascending serialNumbers as they are created by calling super(num, z);  PdfItems are comparable
     * and their natural ordering is the same order as they will be drawn: ascending by z-index,
     * then by creation order.  The default z-index is zero.
     */
    internal abstract class PdfItem(private val serialNumber: Long,
                                    private val z: Float) : Comparable<PdfItem> {
        //    public static PdfItem of(final long ord, final float zIndex) {
        //        return new PdfItem(ord, zIndex);
        //    }

        @Throws(IOException::class)
        abstract fun commit(stream: PDPageContentStream)

        // @Override
        override fun compareTo(other: PdfItem): Int {
            // Ascending by Z (draw the lower-order background items first)
            val zDiff = this.z.compareTo(other.z)
            if (zDiff != 0) {
                return zDiff
            }

            // Ascending by creation order
            val oDiff = this.serialNumber - other.serialNumber
            if (oDiff > 0) {
                return 1
            } else if (oDiff < 0) {
                return -1
            }
            return 0
        }

        override fun equals(other: Any?): Boolean {
            // Cheapest operation first...
            if (this === other) {
                return true
            }
            // Return false if can't be equal
            if (other == null ||
                other !is PdfItem ||
                this.hashCode() != other.hashCode()) {
                return false
            }
            // Details...
            val that = other as PdfItem?
            return compareTo(that!!) == 0
        }

        override fun hashCode(): Int {
            return (z * 1000).toInt() + serialNumber.toInt()
        }
    }


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

    /* Text is drawn from the baseline up. */
    internal class Text(private val baselineLeft: Coord,
                        val t: String,
                        private val style: TextStyle,
                        ord: Long, z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            // TODO: Just adding the rise to the y value here is kind of a cop-out - could run into the line above or below.
            stream.setNonStrokingColor(style.textColor)
            stream.setFont(style.font, style.fontSize)
            val characterSpacing = style.characterSpacing
            if (characterSpacing != 0f) {
                stream.setCharacterSpacing(characterSpacing)
            }
            val wordSpacing = style.wordSpacing
            if (wordSpacing == 0f) {
                stream.beginText()
                stream.newLineAtOffset(baselineLeft.x, baselineLeft.y + style.rise)
                stream.showText(t)
                stream.endText()
            } else {
                var x = baselineLeft.x
                var word = StringBuilder()
                for (c in t) {
                    if (c == ' ') {
                        if (word.isEmpty()) {
//                            println("x1=$x")
                            x += style.stringWidthInDocUnits(" ")
//                            println("  x1=$x")
                        } else {
                            val str = word.toString()
                            stream.beginText()
                            stream.newLineAtOffset(x, baselineLeft.y + style.rise)
                            stream.showText(str)
                            stream.endText()
//                            println("x2=$x")
                            x += style.stringWidthInDocUnits(str)
                            x += style.stringWidthInDocUnits(" ")
//                            println("  x2=$x")
                            word = StringBuilder()
                        }
                    } else {
                        word.append(c)
                    }
                }
                if (word.isNotEmpty()) {
                    stream.beginText()
                    stream.newLineAtOffset(x, baselineLeft.y + style.rise)
                    stream.showText(word.toString())
                    stream.endText()
                }
            }
            if (characterSpacing != 0f) {
                stream.setCharacterSpacing(0f)
            }
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