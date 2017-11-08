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
                  private val pcls: List<LineWrapped>) : LineWrapped {

    override val ascent: Float
        get() = xyDim.height

    override val descentAndLeading: Float = 0f

    override val lineHeight: Float
        get() = xyDim.height

    override fun toString() = "WrappedCell($xyDim, $cellStyle, $pcls)"

    override fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        println("render() outerTopLeft=" + outerTopLeft)
        val boxStyle = cellStyle.boxStyle
        val padding = boxStyle.padding
        val border = boxStyle.border
        // XyDim xyDim = padding.addTo(pcrs.dim);

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (boxStyle.bgColor != null) {
            //            System.out.println("\tCell.render calling putRect...");
            lp.fillRect(outerTopLeft, xyDim, boxStyle.bgColor)
            //            System.out.println("\tCell.render back from putRect");
        }

        // Draw contents over background, but under border
        var innerTopLeft: XyOffset = padding.applyTopLeft(outerTopLeft)
                .plusXMinusY(XyOffset(border.left.thickness / 2f, border.top.thickness / 2f))
        val innerDimensions: XyDim = padding.subtractFrom(xyDim)

//        val wrappedBlockDim = xyDim
//        System.out.println("\tCell.render cellStyle.align()=" + cellStyle.align());
//        System.out.println("\tCell.render xyDim=" + xyDim);
//        System.out.println("\tCell.render padding=" + padding);
//        System.out.println("\tCell.render innerDimensions=" + innerDimensions);
//        System.out.println("\tCell.render wrappedBlockDim=" + wrappedBlockDim);
        val alignPad = cellStyle.align.calcPadding(innerDimensions, xyDim)
//        System.out.println("\tCell.render alignPad=" + alignPad);
        innerTopLeft = XyOffset(innerTopLeft.x + alignPad.left,
                                innerTopLeft.y - alignPad.top)

        var outerLowerRight = innerTopLeft
        var bottomY = innerTopLeft.y
        println("(inner) bottomY starts at top:" + bottomY)
        for (line in pcls) {
            val rowXOffset = cellStyle.align.leftOffset(xyDim.width, line.xyDim.width)
            outerLowerRight = line.render(lp, XyOffset(rowXOffset + innerTopLeft.x, bottomY))
            println("outerLowerRight:" + outerLowerRight)
            bottomY -= outerLowerRight.y // y is always the lowest item in the cell.
            //            innerTopLeft = outerLowerRight.x(innerTopLeft.x);
        }
        println("(inner) bottomY after rendering contents:" + bottomY)

        // Draw border last to cover anything that touches it?
        if (border != BorderStyle.NO_BORDERS) {
            val origX = outerTopLeft.x
            val origY = outerTopLeft.y
            val rightX = outerTopLeft.x + xyDim.width

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
            bottomY -= padding.bottom
            println("bottomY after adding padding:" + bottomY)

            // Like CSS it's listed Top, Right, Bottom, left
            if (border.top.thickness > 0) {
                lp.drawLine(origX, origY, rightX, origY, border.top)
            }
            if (border.right.thickness > 0) {
                lp.drawLine(rightX, origY, rightX, bottomY, border.right)
            }
            if (border.bottom.thickness > 0) {
                lp.drawLine(origX, bottomY, rightX, bottomY, border.bottom)
            }
            if (border.left.thickness > 0) {
                lp.drawLine(origX, origY, origX, bottomY, border.left)
            }
        }

        return outerLowerRight
    }
}