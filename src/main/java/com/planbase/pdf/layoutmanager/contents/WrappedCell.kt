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

import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset

// TODO: This should be a private inner class of Cell
class WrappedCell(override val xyDim: XyDim, // measured on the border lines
                  val cellStyle: CellStyle,
                  private val items: List<LineWrapped>) : LineWrapped {

    override val ascent: Float
        get() = xyDim.height

    override val descentAndLeading: Float = 0f

    override val lineHeight: Float
        get() = xyDim.height

    override fun toString() = "WrappedCell($xyDim, $cellStyle, $items)"

    private val wrappedBlockDim: XyDim = {
        var dim = XyDim.ZERO
        for (row in items) {
            val rowDim = row.xyDim
            dim = XyDim(Math.max(dim.width, rowDim.width),
                        dim.height + rowDim.height)
        }
        dim
    }()

    override fun render(lp: RenderTarget, topLeft: XyOffset): XyDim {
//        println("render() topLeft=" + topLeft)
        val boxStyle = cellStyle.boxStyle
        val padding = boxStyle.padding
        val border = boxStyle.border
        // XyDim xyDim = padding.addTo(pcrs.dim);

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (boxStyle.bgColor != null) {
            //            System.out.println("\tCell.render calling putRect...");
            lp.fillRect(topLeft, xyDim, boxStyle.bgColor)
            //            System.out.println("\tCell.render back from putRect");
        }

        // Draw contents over background, but under border
        var innerTopLeft: XyOffset = padding.applyTopLeft(topLeft)
                .plusXMinusY(XyOffset(border.left.thickness / 2f, border.top.thickness / 2f))
        val innerDimensions: XyDim = padding.subtractFrom(xyDim)

        // TODO: Looks wrong!  Returns a Padding?  But we already have innerDimensions, calculated from the Padding!
        val alignPad = cellStyle.align.calcPadding(innerDimensions, wrappedBlockDim)
//        System.out.println("\tCell.render alignPad=" + alignPad);
        innerTopLeft = XyOffset(innerTopLeft.x + alignPad.left,
                                innerTopLeft.y - alignPad.top)

        var bottomY = innerTopLeft.y
        for (line in items) {
            val rowXOffset = cellStyle.align.leftOffset(wrappedBlockDim.width, line.xyDim.width)
            val (_, y) = line.render(lp, XyOffset(rowXOffset + innerTopLeft.x, bottomY))
            bottomY -= y // y is always the lowest item in the cell.
        }

        val rightX = topLeft.x + xyDim.width
        // Draw border last to cover anything that touches it?
        if (border != BorderStyle.NO_BORDERS) {
            val origX = topLeft.x
            val origY = topLeft.y

            // TODO: Fix this!
            // This breaks cell rows in order to fix rendering content after images that fall
            // mid-page-break.  Math.min() below is so that when the contents overflow the bottom
            // of the cell, we adjust the cell border downward to match.  We aren't doing the same
            // for the background color, or for the rest of the row, so that's going to look bad.
            //
            // To fix these issues, I think we need to make that adjustment in the pre-calc instead
            // of here.  Which means that the pre-calc needs to be aware of page breaking because
            // the code that causes this adjustment is PdfLayoutMgr.appropriatePage().  So we
            // probably need a fake version of that that doesn't cache anything for display on the
            // page, then refactor backward from there until we enter this code with pre-corrected
            // outerLowerRight and can get rid of Math.min.
            //
            // When we do that, we also want to check PageGrouping.drawImage() and .drawPng()
            // to see if `return y + pby.adj;` still makes sense.
            bottomY = topLeft.y - xyDim.height

            val topRight = XyOffset(rightX, origY)
            val bottomRight = XyOffset(rightX, bottomY)
            val bottomLeft = XyOffset(origX, bottomY)

            // Like CSS it's listed Top, Right, Bottom, left
            if (border.top.thickness > 0) {
                lp.drawLine(topLeft, topRight, border.top)
            }
            if (border.right.thickness > 0) {
                lp.drawLine(topRight, bottomRight, border.right)
            }
            if (border.bottom.thickness > 0) {
                lp.drawLine(bottomRight, bottomLeft, border.bottom)
            }
            if (border.left.thickness > 0) {
                lp.drawLine(bottomLeft, topLeft, border.left)
            }
        }

        return XyDim(rightX - topLeft.x,
                     topLeft.y - bottomY)
    }
}