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
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d

// TODO: This should be a private inner class of Cell
class WrappedCell(override val dimensions: Dimensions, // measured on the border lines
                  val cellStyle: CellStyle,
                  private val items: List<LineWrapped>) : LineWrapped {

    override val ascent: Float
        get() = dimensions.height

    override val descentAndLeading: Float = 0f

    override val lineHeight: Float
        get() = dimensions.height

    override fun toString() = "WrappedCell($dimensions, $cellStyle, $items)"

    private val wrappedBlockDim: Dimensions = {
        var dim = Dimensions.ZERO
        for (row in items) {
            val rowDim = row.dimensions
            dim = Dimensions(Math.max(dim.width, rowDim.width),
                        dim.height + rowDim.height)
        }
        dim
    }()

    override fun render(lp: RenderTarget, topLeft: Point2d): Dimensions {
        return tableRender(lp, topLeft, dimensions.height, true)
    }

    fun tableRender(lp: RenderTarget, topLeft: Point2d, height:Float, reallyRender:Boolean): Dimensions {
//        println("render() topLeft=" + topLeft)
        val boxStyle = cellStyle.boxStyle
        val padding = boxStyle.padding
        val border = boxStyle.border
        // Dimensions dimensions = padding.addTo(pcrs.dim);

        // Draw contents over background, but under border
        var innerTopLeft: Point2d = padding.applyTopLeft(topLeft)
                .plusXMinusY(Point2d(border.left.thickness / 2f, border.top.thickness / 2f))
        val innerDimensions: Dimensions = padding.subtractFrom(dimensions)

        // TODO: Looks wrong!  Returns a Padding?  But we already have innerDimensions, calculated from the Padding!
        val alignPad = cellStyle.align.calcPadding(innerDimensions, wrappedBlockDim)
//        System.out.println("\tCell.render alignPad=" + alignPad);
        innerTopLeft = Point2d(innerTopLeft.x + alignPad.left,
                                innerTopLeft.y - alignPad.top)

        var bottomY = innerTopLeft.y
        for (line in items) {
            val rowXOffset = cellStyle.align.leftOffset(wrappedBlockDim.width, line.dimensions.width)
            val thisLineHeight = if (reallyRender) {
//                println("render")
                line.render(lp, Point2d(rowXOffset + innerTopLeft.x, bottomY)).height
            } else {
//                println("try")
                lp.pageBreakingTopMargin(bottomY + line.descentAndLeading, line.lineHeight) + line.lineHeight
//                println("try adjY=$adjY line.lineHeight=${line.lineHeight}")
            }
//            println("thisLineHeight=$thisLineHeight")
            bottomY -= thisLineHeight // y is always the lowest item in the cell.
//            println("line=$line")
        }
        // TODO: Where do we add bottom padding to bottomY?
        bottomY = minOf(bottomY, topLeft.y - height)
//        println("height=${innerTopLeft.y - bottomY}")

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (boxStyle.bgColor != null) {
            //            System.out.println("\tCell.render calling putRect...");
            lp.fillRect(topLeft.y(bottomY), dimensions.height(topLeft.y - bottomY), boxStyle.bgColor, reallyRender)
            //            System.out.println("\tCell.render back from putRect");
        }

        val rightX = topLeft.x + dimensions.width
        // Draw border last to cover anything that touches it?
        if (border != BorderStyle.NO_BORDERS) {
            val origX = topLeft.x
            val origY = topLeft.y

            val topRight = Point2d(rightX, origY)
            val bottomRight = Point2d(rightX, bottomY)
            val bottomLeft = Point2d(origX, bottomY)

            // TODO use multi-line drawing
            // Like CSS it's listed Top, Right, Bottom, left
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

        return Dimensions(rightX - topLeft.x,
                     topLeft.y - bottomY)
    }
}