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
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord

// TODO: This should be a private inner class of Cell
class WrappedCell(override val dim: Dim, // measured on the border lines
                  val cellStyle: CellStyle,
                  private val items: List<LineWrapped>) : LineWrapped {

    override val ascent: Float
        get() = dim.height

//    override val descentAndLeading: Float = 0f

    override val lineHeight: Float
        get() = dim.height

    override fun toString() = "WrappedCell($dim, $cellStyle, $items)"

    private val wrappedBlockDim: Dim = {
        var dim = Dim.ZERO
        for (row in items) {
            val rowDim = row.dim
            dim = Dim(Math.max(dim.width, rowDim.width),
                             dim.height + rowDim.height)
        }
        dim
    }()

    override fun render(lp: RenderTarget, topLeft: Coord): Dim {
        return tableRender(lp, topLeft, dim.height, true)
    }

    // See: CellTest.testWrapTable for issue.  But we can isolate it by testing this method.
    fun tableRender(lp: RenderTarget, topLeft: Coord, height:Float, reallyRender:Boolean): Dim {
//        println("render($topLeft, $height, $reallyRender)")
//        println("  cellStyle=$cellStyle")
        val boxStyle = cellStyle.boxStyle
        val border = boxStyle.border
        // Dim dim = padding.addTo(pcrs.dim);

        // Draw contents over background, but under border
        val tempTopLeft: Coord = boxStyle.applyTopLeft(topLeft)
//        println("  tempTopLeft=$tempTopLeft dim=$dim height=$height")
        val finalDim = if (dim.height < height) {
            dim.height(height)
        } else {
            dim
        }
//        println("  finalDim=$finalDim")
        val innerDim: Dim = boxStyle.subtractFrom(finalDim)
//        println("  innerDim=$innerDim")

        val innerTopLeft = cellStyle.align.innerTopLeft(innerDim, wrappedBlockDim, tempTopLeft)
//        println("  innerTopLeft=$innerTopLeft")

        var y = innerTopLeft.y
        for (line in items) {
            val rowXOffset = cellStyle.align.leftOffset(wrappedBlockDim.width, line.dim.width)
            val thisLineHeight = if (reallyRender) {
//                println("render")
                line.render(lp, Coord(rowXOffset + innerTopLeft.x, y)).height
            } else {
//                println("  try y=$y line=$line")
                val adjY = lp.pageBreakingTopMargin(y - line.lineHeight, line.lineHeight) + line.lineHeight
//                println("  try adjY=$adjY line.lineHeight=${line.lineHeight}")
                adjY
            }
//            println("thisLineHeight=$thisLineHeight")
            y -= thisLineHeight // y is always the lowest item in the cell.
//            println("line=$line")
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

        return Dim(rightX - topLeft.x,
                   topLeft.y - y)
    }
}