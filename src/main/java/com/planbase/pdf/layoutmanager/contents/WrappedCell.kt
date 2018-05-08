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

package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums.Companion.INVALID_PAGE_RANGE
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim

// TODO: This should be a private inner class of Cell
class WrappedCell(override val dim: Dim, // measured on the border lines
                  val cellStyle: CellStyle,
                  private val rows: List<MultiLineWrapped>,
                  private val requiredSpaceBelow : Double) : LineWrapped {

    override val ascent: Double
        get() = dim.height

    override fun toString() = "WrappedCell($dim, $cellStyle, $rows)"

    private val wrappedBlockDim: Dim = {
        var width = 0.0
        var height = 0.0
        for (row in rows) {
            val rowDim = row.dim
            width = Math.max(width, rowDim.width)
            height += rowDim.height
        }
        if (cellStyle.align == Align.TOP_LEFT_JUSTIFY) {
            width = dim.width - cellStyle.boxStyle.leftRightInteriorSp()
        }
        Dim(width, height)
    }()

    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): DimAndPageNums =
            renderCustom(lp, topLeft, dim.height, reallyRender)

    // See: CellTest.testWrapTable for issue.  But we can isolate it by testing this method.
    fun renderCustom(lp: RenderTarget, tempTopLeft: Coord, height: Double, reallyRender:Boolean): DimAndPageNums {
        var pageNums:IntRange = INVALID_PAGE_RANGE

        val adj = if (requiredSpaceBelow == 0.0) {
            0.0
        } else {
            lp.pageBreakingTopMargin(tempTopLeft.y - dim.height, dim.height, requiredSpaceBelow)
        }

        val topLeft: Coord = if (requiredSpaceBelow == 0.0) {
            tempTopLeft
        } else {
            // Not part of a table.
            //
            // Given topLeft.y and dim, is there room for this cell plus requiredSpaceBelow on this page?
            // - Yes: Return topLeft.y unchanged.
            // - No: Is there room on any page?
            // - No: Return topLeft.y unchanged
            // - Yes: Return the y value of the top of the next page.
            //
            // If not, return the new innerTopLeft.y.
            //
            // NO:
            // Given innerTopLeft.y and wrappedBlockDim, is there room for the block plus requiredSpaceBelow
            // on this page? If not, return the new innerTopLeft.y.
            tempTopLeft.minusY(adj)
        }

//        println("render($topLeft, $height, $reallyRender)")
//        println("  cellStyle=$cellStyle")
        val boxStyle = cellStyle.boxStyle
        val border = boxStyle.border
        // Dim dim = padding.addTo(pcrs.dim);

        // Draw contents over background, but under border
        val finalDim = if (dim.height < height) {
            dim.height(height)
        } else {
            dim
        }
//        println("  finalDim=$finalDim")
        val innerDim: Dim = boxStyle.subtractFrom(finalDim)
//        println("  innerDim=$innerDim")

        val innerTopLeft = cellStyle.align.innerTopLeft(innerDim, wrappedBlockDim, boxStyle.applyTopLeft(topLeft))
//        println("  innerTopLeft=$innerTopLeft")

        var y = innerTopLeft.y
        for ((index, row) in rows.withIndex()) {
//            println("row=$row")
            val rowXOffset = cellStyle.align.leftOffset(wrappedBlockDim.width, row.dim.width)

            val dimAndPages = row.render(lp, Coord(rowXOffset + innerTopLeft.x, y), reallyRender,
                                         if ( (index < rows.size - 1) &&
                                              (cellStyle.align == Align.TOP_LEFT_JUSTIFY) ) {
                                             // Even if we're justifying text, the last row looks better unjustified.
                                             innerDim.width
                                         } else {
                                             0.0
                                         })
//            println("thisLineHeight=$thisLineHeight")
            y -= dimAndPages.dim.height // y is always the lowest row in the cell.
            pageNums = dimAndPages.maxExtents(pageNums)
        }
//        println("  y=$y")
//        println("  totalHeight=${innerTopLeft.y - y}")
        // TODO: test that we add bottom padding to y
        y = minOf(y, topLeft.y - height)
//        println("height=${innerTopLeft.y - y}")

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (boxStyle.bgColor != null) {
            //            System.out.println("\tCell.render calling putRect...");
            lp.fillRect(topLeft.y(y), dim.height(topLeft.y - y), boxStyle.bgColor, reallyRender)
            //            System.out.println("\tCell.render back from putRect");
        }

        val rightX = topLeft.x + dim.width
        // Draw border last to cover anything that touches it?
        if (border != BorderStyle.NO_BORDERS) {
            val origX = topLeft.x
            val origY = topLeft.y

            val topRight = Coord(rightX, origY)
            val bottomRight = Coord(rightX, y)
            val bottomLeft = Coord(origX, y)

            // I'm not using multi-line drawing here (now/yet).
            // It's complicated, and if there's page breaking it won't work anyway.
            if (border.top.thickness > 0) {
                lp.drawLine(topLeft, topRight, border.top, reallyRender)
            }
            if (border.right.thickness > 0) {
                lp.drawLine(topRight, bottomRight, border.right, reallyRender)
            }
            if (border.bottom.thickness > 0) {
                lp.drawLine(bottomRight, bottomLeft, border.bottom, reallyRender)
            }
            if (border.left.thickness > 0) {
                lp.drawLine(bottomLeft, topLeft, border.left, reallyRender)
            }
        }

        return DimAndPageNums(Dim(rightX - topLeft.x,
                                (topLeft.y - y) + adj ),
                              pageNums)
    }
}