// Copyright 2013-03-03 PlanBase Inc.
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

package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapperWrapper
import com.planbase.pdf.layoutmanager.lineWrapping.WrappedMultiLineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.renderablesToWrappedMultiLineWrappeds
import com.planbase.pdf.layoutmanager.utils.XyDim

/**
 A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated
 based on how the content is rendered with regard to line-breaks and page-breaks.
 */
data class Cell(val cellStyle: CellStyle = CellStyle.Default, // contents can override this style
                val width: Float,
                // A list of the contents.  It's pretty limiting to have one item per row.
                private var contents: List<LineWrappable>) : LineWrappable {
    constructor(cs: CellStyle,
                w: Float,
                textStyle: TextStyle,
                text:List<String>) : this(cs, w, text.map{s -> Text(textStyle, s) }.toList())
//    private constructor(b:Builder) : this(b.cellStyle ?: CellStyle.DEFAULT, b.width, b.rows)

    // Caches XyDims for all content textLines, indexed by desired width (we only have to lay-out again
    // when the width changes.
//    private val widthCache = HashMap<Float, PreCalcLines>(0)

    // A cache for all pre-calculated textLines.
//    class PreCalcLines(val textLines: List<TextLine> = ArrayList(1),
//                       val totalDim: XyDim)

    init {
        if (width < 0) {
            throw IllegalArgumentException("A cell cannot have a negative width")
        }
    }

    fun addAll(lrs:List<LineWrappable>): Cell {
        val tempItems = contents.toMutableList()
        tempItems.addAll(lrs)
        contents = tempItems.toList()
        return this
    }

//    private fun calcDimensionsForReal(maxWidth: Float) {
//        val padding = cellStyle.padding
//        var innerWidth = maxWidth
//        if (padding != null) {
//            innerWidth -= padding.left + padding.right
//        }
//        val textLines: List<TextLine> = renderablesToTextLines(contents, innerWidth)
//        var width = 0f
//        var height = 0f
//        for (line in textLines) {
//            width = max(width, line.width)
//            height += line.xyDim.height
//        }
//        widthCache.put(maxWidth, PreCalcLines(textLines, XyDim(width, height)))
//    }

//    private fun ensurePreCalcLines(maxWidth: Float): PreCalcLines {
//        var pcl: PreCalcLines? = widthCache[maxWidth]
//        if (pcl == null) {
//            calcDimensionsForReal(maxWidth)
//            pcl = widthCache[maxWidth]
//        }
//        return pcl!!
//    }

    fun fix() : WrappedCell {
        val fixedLines: List<WrappedMultiLineWrapped> = renderablesToWrappedMultiLineWrappeds(contents, width)
//        var maxWidth = cellStyle.boxStyle.leftRightThickness()
        var height = cellStyle.boxStyle.topBottomInteriorSp()
        for (line in fixedLines) {
            height += line.xyDim.height
//            maxWidth = maxOf(line.xyDim.width, maxWidth)
        }
        return WrappedCell(XyDim(width, height), this.cellStyle, fixedLines)
    }

    override fun lineWrapper() = MultiLineWrapperWrapper(contents.iterator())

    //    fun calcDimensions(maxWidth: Float): XyDim {
//        // I think zero or negative width cells might be OK to ignore.  I'd like to try to make
//        // Text.calcDimensionsForReal() handle this situation before throwing an error here.
//        //        if (maxWidth < 0) {
//        //            throw new IllegalArgumentException("maxWidth must be positive, not " + maxWidth);
//        //        }
//        val blockDim = ensurePreCalcLines(maxWidth).totalDim
//        return if (cellStyle.padding == null) blockDim else cellStyle.padding.addTo(blockDim)
//        //        System.out.println("Cell.calcDimensions(" + maxWidth + ") dim=" + dim +
//        //                           " returns " + ret);
//    }


//    /*
//    Renders item and all child-textLines with given width and returns the x-y pair of the
//    lower-right-hand corner of the last line (e.g. of text).
//
//    {@inheritDoc}
//    */
//    fun render(lp: RenderTarget, outerTopLeft: XyOffset, outerDimensions: XyDim): XyOffset {
//        //        System.out.println("Cell.render(" + this.toString());
//        //        new Exception().printStackTrace();
//
//        val maxWidth:Float = outerDimensions.width
//        val pcls = ensurePreCalcLines(maxWidth)
//        val padding = cellStyle.padding
//        // XyDim outerDimensions = padding.addTo(pcrs.dim);
//
//        // Draw background first (if necessary) so that everything else ends up on top of it.
//        if (cellStyle.bgColor != null) {
//            //            System.out.println("\tCell.render calling putRect...");
//            lp.fillRect(outerTopLeft, outerDimensions, cellStyle.bgColor)
//            //            System.out.println("\tCell.render back from putRect");
//        }
//
//        // Draw contents over background, but under border
//        var innerTopLeft: XyOffset
//        val innerDimensions: XyDim
//        if (padding == null) {
//            innerTopLeft = outerTopLeft
//            innerDimensions = outerDimensions
//        } else {
//            //            System.out.println("\tCell.render outerTopLeft before padding=" + outerTopLeft);
//            innerTopLeft = padding.applyTopLeft(outerTopLeft)
//            //            System.out.println("\tCell.render innerTopLeft after padding=" + innerTopLeft);
//            innerDimensions = padding.subtractFrom(outerDimensions)
//        }
//        val wrappedBlockDim = pcls.totalDim
//        //        System.out.println("\tCell.render cellStyle.align()=" + cellStyle.align());
//        //        System.out.println("\tCell.render outerDimensions=" + outerDimensions);
//        //        System.out.println("\tCell.render padding=" + padding);
//        //        System.out.println("\tCell.render innerDimensions=" + innerDimensions);
//        //        System.out.println("\tCell.render wrappedBlockDim=" + wrappedBlockDim);
//        val alignPad = cellStyle.align.calcPadding(innerDimensions, wrappedBlockDim)
//        //        System.out.println("\tCell.render alignPad=" + alignPad);
//        innerTopLeft = XyOffset(innerTopLeft.x + alignPad.left,
//                                innerTopLeft.y - alignPad.top)
//
//        var outerLowerRight = innerTopLeft
//        var y:Float = innerTopLeft.y
//        for (line in pcls.textLines) {
//            val rowXOffset = cellStyle.align
//                    .leftOffset(wrappedBlockDim.width, line.width)
//            outerLowerRight = line.render(lp,
//                                          XyOffset(rowXOffset + innerTopLeft.x, y))
//            y -= line.height()
//            //            innerTopLeft = outerLowerRight.x(innerTopLeft.x());
//        }
//
//        // Draw border last to cover anything that touches it?
//        val border = cellStyle.borderStyle
//        if (border != null) {
//            val origX:Float = outerTopLeft.x
//            val origY:Float = outerTopLeft.y
//            val rightX:Float = outerTopLeft.x + outerDimensions.width
//
//            // This breaks cell rows in order to fix rendering content after images that fall
//            // mid-page-break.  Math.min() below is so that when the contents overflow the bottom
//            // of the cell, we adjust the cell border downward to match.  We aren't doing the same
//            // for the background color, or for the rest of the row, so that's going to look bad.
//            //
//            // To fix these issues, I think we need to make that adjustment in the pre-calc instead
//            // of here.  Which means that the pre-calc needs to be aware of page breaking because
//            // the code that causes this adjustment is PdfLayoutMgr.appropriatePage().  So we
//            // probably need a fake version of that that doesn't cache anything for display on the
//            // page, then refactor backward from there until we enter this code with pre-corrected
//            // outerLowerRight and can get rid of Math.min.
//            //
//            // When we do that, we also want to check PageGrouping.drawImage() and .drawPng()
//            // to see if `return y + pby.adj;` still makes sense.
//            val bottomY = Math.min(outerTopLeft.y - outerDimensions.height,
//                                   outerLowerRight.y)
//
//            // Like CSS it's listed Top, Right, Bottom, left
//            if (border.top != null) {
//                lp.drawLine(origX, origY, rightX, origY, border.top)
//            }
//            if (border.right != null) {
//                lp.drawLine(rightX, origY, rightX, bottomY, border.right)
//            }
//            if (border.bottom != null) {
//                lp.drawLine(origX, bottomY, rightX, bottomY, border.bottom)
//            }
//            if (border.left != null) {
//                lp.drawLine(origX, origY, origX, bottomY, border.left)
//            }
//        }
//
//        return outerLowerRight
//    }

    // Replaced with TableRow.CellBuilder()
    //    /**
    //     Be careful when adding multiple cell builders at once because the cell size is based upon
    //     a pointer into the list of cell sizes.  That pointer gets incremented each time a cell is
    //     added, not each time nextCellSize() is called.  Is this a bug?  Or would fixing it create
    //     too many other bugs?
    //     @param trb
    //     @return
    //     */
    //    public static Builder builder(TableRowBuilder trb) {
    //        Builder b = new Builder(trb.cellStyle(), trb.nextCellSize()).textStyle(trb.textStyle());
    //        b.trb = trb;
    //        return b;
    //    }

    /** {@inheritDoc}  */
    override fun toString(): String {
        val sB = StringBuilder("Cell(").append(cellStyle).append(" width=")
                .append(width).append(" contents=[")

        var i = 0
        while (i < contents.size && i < 3) {
            if (i > 0) {
                sB.append(" ")
            }
            sB.append(contents[i])
            i++
        }
        return sB.append("])").toString()
    }
}
