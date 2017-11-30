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
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Companion.DEFAULT_MARGIN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.ScaledImage.WrappedImage
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.io.IOException
import java.util.TreeSet


/**
 *
 * Maybe better called a "DocumentSection" this represents a group of Renderables that logically
 * belong on the same page, but may spill over multiple subsequent pages as necessary in order to
 * fit.  Headers and footers are tied to this Logical Page / Document Section.
 *
 *
 * Here is a typical page layout:
 * <pre>`
 * +---------------------+ -.
 * | M  Margin Header  M |  |
 * | a +-------------+ a |   > Margin body top
 * | r |    Header   | r |  |
 * | g +-------------+ g | -'
 * |   |             |   |
 * | B |             | B |
 * | o |     Body    | o |
 * | d |             | d |
 * | y |             | y |
 * |   +-------------+   | -.
 * | L |    Footer   | R |  |
 * | e +-------------+ t |   > Margin body bottom
 * | f  Margin Footer    |  |
 * +---------------------+ -'
`</pre> *
 *
 *
 * Here is our model
 * <pre>`
 * +--------------------+
 * |                    |
 * |                    |
 * |   +------------+   | <- yBodyTop()
 * |   |           h|   |
 * |   |           e|   |
 * |   |           i|   |
 * |   |    Body   g|   |
 * |   |           h|   |
 * |   |w i d t h  t|   |
 * |   #------------+   | <- yBodyBottom()
 * |   ^                |
 * | Body               |
 * | Offset             |
 * #--------------------+
 * (0,0)
`</pre> *
 *
 *
 * Put header/footer content wherever you want.  We move the body as a unit as needed.
 *
 * Constructor
 * @param mgr the PdfLayoutMgr you are using.
 * @param orientation page orientation for this logical page grouping.
 * @param bodyOff the offset (in document units) from the lower-left hand corner of the page to
 * the lower-left of the body area.
 * @param bodyDim the dimensions of the body area.
 * @return a new PageGrouping with the given settings.
 */
class PageGrouping(private val mgr: PdfLayoutMgr,
                   val orientation: Orientation,
                   bodyOff: Point2d,
                   bodyDim: Dimensions) : RenderTarget { // AKA Document Section

    /**
     * Create a PageGrouping with default margins for body top and bottom.
     * @param m the PdfLayoutMgr you are using.
     * @param orientation page orientation for this logical page grouping.
     * @return a new PageGrouping with the given settings.
     */
    constructor(m: PdfLayoutMgr, orientation: Orientation):
            this(m, orientation, Point2d(DEFAULT_MARGIN, DEFAULT_MARGIN),
                 if (orientation == PORTRAIT)
                     m.pageDim.minus(DEFAULT_DOUBLE_MARGIN_DIM)
                 else
                     m.pageDim.swapWh().minus(DEFAULT_DOUBLE_MARGIN_DIM))


    private val bodyRect: PDRectangle = PDRectangle(bodyOff.x, bodyOff.y,
                                                    bodyDim.width, bodyDim.height)
    // borderItems apply to a logical section
    private val borderItems = TreeSet<PdfItem>()
//    private var borderOrd = 0
    private var valid = true

    // ===================================== Instance Methods =====================================

    fun bodyTopLeft() = Point2d(bodyRect.lowerLeftX, bodyRect.upperRightY)

    /** The Y-value for top of the body section (in document units)  */
    fun yBodyTop(): Float = bodyRect.upperRightY

    /**
     * The Y-value for the bottom of the body section (in document units).  The bottom of the page is
     * always zero, so this is always equivalent to the margin body bottom.
     */
    fun yBodyBottom(): Float = bodyRect.lowerLeftY

    /** Height (dimension, not offset) of the body section (in document units)  */
    // part of public interface
    fun bodyHeight(): Float = bodyRect.height

    /**
     * Width of the entire page (in document units).  This is the short dimension for portrait,
     * the long dimension for landscape.
     */
    fun pageWidth(): Float {
        return if (orientation == PORTRAIT)
            mgr.pageDim.width
        else
            mgr.pageDim.height
    }

    //    /**
    //     Height of the entire page (in document units).  This is the long dimension for portrait,
    //     the short dimension for landscape.
    //     */
    //    public float pageHeight() {
    //        return portrait ? mgr.pageDim().height()
    //                        : mgr.pageDim().width();
    //    }

    /** Ends this logical page grouping and invalidates it for further operations.  */
    @Throws(IOException::class)
    fun commit(): PdfLayoutMgr {
        mgr.logicalPageEnd(this)
        valid = false
        return mgr
    }

    /** {@inheritDoc}  */
    override fun drawStyledText(bottomLeft: Point2d, text: String, textStyle: TextStyle): Float {
        if (!valid) {
            throw IllegalStateException("Logical page accessed after commit")
        }
        val pby = appropriatePage(bottomLeft.y, 0f)
        pby.pb.drawStyledText(bottomLeft.y(pby.y), text, textStyle)
        // TODO: Is this right?
        return textStyle.lineHeight() + pby.adj
    }

    /** {@inheritDoc}  */
    override fun drawImage(bottomLeft: Point2d, wi: WrappedImage): Float {
        if (!valid) {
            throw IllegalStateException("Logical page accessed after commit")
        }
        // Calculate what page image should start on
        val pby = appropriatePage(bottomLeft.y, wi.dimensions.height)
        // draw image based on baseline and decrement y appropriately for image.
        pby.pb.drawImage(bottomLeft.y(pby.y), wi)
        return wi.dimensions.height - pby.adj
    }

    /** {@inheritDoc}  */
    override fun fillRect(bottomLeft: Point2d, dimensions: Dimensions, c: PDColor): Float {
        if (!valid) {
            throw IllegalStateException("Logical page accessed after commit")
        }
        //        System.out.println("putRect(" + outerTopLeft + " " + outerDimensions + " " +
        //                           Utils.toString(c) + ")");
        val left = bottomLeft.x
        val topY = bottomLeft.y + dimensions.height
        val width = dimensions.width
        val maxHeight = dimensions.height
        val bottomY = bottomLeft.y

        if (topY < bottomY) {
            throw IllegalStateException("height must be positive")
        }
        // logger.info("About to put line: (" + x1 + "," + y1 + "), (" + x2 + "," + y2 + ")");
        val pby1 = appropriatePage(topY, 0f)
        val pby2 = appropriatePage(bottomY, 0f)
        if (pby1 == pby2) {
            pby1.pb.fillRect(Point2d(left, pby1.y), Dimensions(width, maxHeight), c)
        } else {
            val totalPages = pby2.pb.pageNum - pby1.pb.pageNum + 1

            var currPage = pby1.pb
            // The first x and y are correct for the first page.  The second x and y will need to
            // be adjusted below.
            var ya:Float
            var yb:Float

            for (pageNum in 1..totalPages) {
                // On all except the first page the first y will start at the top of the page.
                // lt or equals, because can never be greater than
                ya = if (pby1.pb.pageNum < currPage.pageNum) {
                    yBodyTop()
                } else {
                    pby1.y
                }

                // the second Y must be adjusted by the height of the pages already printed.
                // On all except the last page, the second-y will end at the bottom of the page.
                yb = if (pageNum == totalPages) {
                    pby2.y
                } else {
                    yBodyBottom()
                }

                currPage.fillRect(Point2d(left, yb), Dimensions(width, ya - yb), c)

                // pageNum is one-based while get is zero-based, so passing get the current
                // pageNum actually gets the next page.  Don't get another one after we already
                // processed the last page!
                if (pageNum < totalPages) {
                    currPage = mgr.page(currPage.pageNum)
                }
            }
        }

        return maxHeight + pby2.adj
    }

    /** {@inheritDoc}  */
    override fun drawLine(start: Point2d, end: Point2d, lineStyle: LineStyle): PageGrouping {
        if (!valid) {
            throw IllegalStateException("Logical page accessed after commit")
        }

//        println("About to put line: start=$start end=$end")
        val flip:Boolean = end.y > start.y

        val pby1 = appropriatePage(if (flip) { end.y } else { start.y }, 0f)
        val pby2 = appropriatePage(if (flip) { start.y } else { end.y }, 0f)
//        println("pby1=$pby1, pby2=$pby2")
        if (pby1 == pby2) {
            if (flip) {
                pby1.pb.drawLine(end.y(pby1.y), start.y(pby2.y), lineStyle)
            } else {
                pby1.pb.drawLine(start.y(pby1.y), end.y(pby2.y), lineStyle)
            }
        } else {
            val totalPages = pby2.pb.pageNum - pby1.pb.pageNum + 1
            val xDiff = end.x - start.x
            val yDiff = start.y - end.y
//            println("xDiff=$xDiff")
//            println("yDiff=$yDiff")

            var currPage = pby1.pb
            // The first x and y are correct for the first page.  The second x and y will need to
            // be adjusted below.
            var xa = if (flip) { end.x } else { start.x }
            var xb = 0f // left of page.

            for (pageNum in 1..totalPages) {
                if (pageNum > 1) {
                    // The x-value at the start of the new page will be the same as
                    // it was on the bottom of the previous page.
                    xa = xb
                }

                val ya = if (pby1.pb.pageNum < currPage.pageNum) {
                    // On all except the first page the first y will start at the top of the page.
                    yBodyTop()
                } else { // equals, because can never be greater than
                    pby1.y
                }

                val yb:Float
                if (pageNum == totalPages) {
                    xb = if (flip) { start.x } else { end.x }
                    // the second Y must be adjusted by the height of the pages already printed.
                    yb = pby2.y
                } else {
                    // On all except the last page, the second-y will end at the bottom of the page.
                    yb = yBodyBottom()

                    // This represents the x-value of the line at the bottom of one page and later
                    // becomes the x-value for the top of the next page.  It should work whether
                    // slope is negative or positive, because the sign of xDiff will reflect the
                    // slope.
                    //
                    // x1 is the starting point.
                    // xDiff is the total deltaX over all pages so it needs to be scaled by:
                    // (ya - yb) / yDiff is the proportion of the line shown on this page.
                    xb = xa + xDiff * ((ya - yb) / yDiff)
                }

//                println("(xa=$xa, ya=$ya), (xb=$xb, yb=$yb)")

                // This may look silly, but if we're doing mitering, the direction of lines is important.
                // In that case, the last endpoint of the previous line must equal the starting point of this line.
                // So if we detected that we had to flip the line to break it across pages, flip it back here!
                if (flip) {
                    currPage.drawLine(Point2d(xb, yb), Point2d(xa, ya), lineStyle)
                } else {
                    currPage.drawLine(Point2d(xa, ya), Point2d(xb, yb), lineStyle)
                }

                // pageNum is one-based while get is zero-based, so passing get the current
                // pageNum actually gets the next page.  Don't get another one after we already
                // processed the last page!
                if (pageNum < totalPages) {
                    currPage = mgr.page(currPage.pageNum)
                }
            }
        }

        return this
    }

    /**
     Returns the correct page for the given value of y.  This lets the user use any Y value and
     we continue extending their canvas downward (negative) by adding extra pages.
     @param origY the un-adjusted y value.
     @return the proper page and adjusted y value for that page.
     */
    private fun appropriatePage(origY: Float, height: Float): PageBufferAndY {
        var y = origY
        if (!mgr.hasAnyPages()) {
            throw IllegalStateException("Cannot work with the any pages until one has been" +
                                        " created by calling mgr.ensurePageIdx(1).")
        }
        var idx = mgr.unCommittedPageIdx()
        // Get the first possible page.  Just keep moving to the top of the next page until it's in
        // the printable area.
        while (y < yBodyBottom()) {
            y += bodyHeight()
            idx++
            mgr.ensurePageIdx(idx)
        }
        val ps = mgr.page(idx)
        var adj = 0f
        if (y + height > yBodyTop()) {
            val oldY = y
            y = yBodyTop() - height
            adj = y - oldY
        }
        return PageBufferAndY(ps, y, adj)
    }

    /*
     * You can draw a cell without a table (for a heading, or paragraph of same-format text, or
     * whatever).
     */
//    fun drawCell(x: Float, y: Float, cell: WrappedCell): Dimensions =
//            // render the row with that maxHeight.
//            cell.render(this, Point2d(x, y))

//    /**
//     * Shows the given cells plus either a background or an outline as appropriate.
//     *
//     * @param initialX the left-most x-value.
//     * @param origY the starting y-value
//     * @param cells the Cells to display
//     * @return the final y-value
//     * @throws IOException if there is an error writing to the underlying stream.
//     */
//    @Throws(IOException::class)
//    fun putRow(initialX: Float, origY: Float, vararg cells: Cell): Float {
//        if (!valid) {
//            throw IllegalStateException("Logical page accessed after commit")
//        }
//
//        // Similar to TableBuilder and TableRowBuilder.calcDimensions().  Should be combined?
//        var maxDim = Dimensions.ZERO
//        for (cell in cells) {
//            val wh = cell.calcDimensions(cell.width)
//            maxDim = Dimensions(wh!!.width + maxDim.width,
//                           Math.max(maxDim.height, wh.height))
//        }
//        val maxHeight = maxDim.height
//
//        //        System.out.println("putRow: maxHeight=" + maxHeight);
//
//        // render the row with that maxHeight.
//        var x = initialX
//        for (cell in cells) {
//            cell.render(this, Point2d(x, origY), Dimensions(cell.width, maxHeight))
//            x += cell.width
//        }
//
//        return origY - maxHeight
//    }

    //    /**
    //     Header and footer in this case means anything that doesn't have to appear within the body
    //     of the page.  Most commonly used for headers and footers, but could be watermarks, background
    //     images, or anything outside the normal page flow.  I believe these get drawn first so
    //     the body text will render over the top of them.  Items put here will *not* wrap to the next
    //     page.
    //
    //     @param x the x-value on all pages (often set outside the normal margins)
    //     @param origY the y-value on all pages (often set outside the normal margins)
    //     @param cell the cell containing the styling and text to render
    //     @return the bottom Y-value of the rendered cell (on all pages)
    //     */
    //    public float drawCellAsWatermark(float x, float origY, Cell cell) {
    //        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
    //        float outerWidth = cell.width();
    //        Dimensions innerDim = cell.calcDimensions(outerWidth);
    //        PageBufferAndY pby = appropriatePage(origY);
    //        return cell.render(pby.pb, Point2d.of(x, pby.y), innerDim.width(outerWidth)).y();
    //    }

    @Throws(IOException::class)
    fun commitBorderItems(stream: PDPageContentStream) {
        if (!valid) {
            throw IllegalStateException("Logical page accessed after commit")
        }
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (item in borderItems) {
            item.commit(stream)
        }
    }

//    /**
//     * Adds items to every page in page grouping.  You should not need to use this directly.  It only
//     * has package scope so that Text can access it for one thing.  It may become private in the
//     * future.
//     */
//    internal fun borderStyledText(bottomLeft: Point2d, text: String, s: TextStyle) {
//        if (!valid) {
//            throw IllegalStateException("Logical page accessed after commit")
//        }
//        borderItems.add(SinglePage.Text(bottomLeft, text, s, borderOrd++.toLong(),
//                                        PdfItem.DEFAULT_Z_INDEX))
//    }

    companion object {
        private val DEFAULT_DOUBLE_MARGIN_DIM = Dimensions(DEFAULT_MARGIN * 2, DEFAULT_MARGIN * 2)

        /**
        @param pb specific page item will be put on
        @param y the y-value on that page
        @param adj the height of the adjustment used to keep the line on one page.
         */
        private data class PageBufferAndY(val pb: SinglePage,
                                          val y: Float,
                                          val adj: Float)
    }
}
