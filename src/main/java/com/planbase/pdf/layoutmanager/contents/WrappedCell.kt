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
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim

class WrappedCell(override val dim: Dim, // measured on the border lines
                  val cellStyle: CellStyle,
                  private val rows: List<LineWrapped>,
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

    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean,
                        justifyWidth:Double): DimAndPageNums =
            renderCustom(lp, topLeft, dim.height, reallyRender, preventWidows = true)

    // See: CellTest.testWrapTable for issue.  But we can isolate it by testing this method.
    fun renderCustom(lp: RenderTarget, tempTopLeft: Coord, height: Double, reallyRender: Boolean,
                     preventWidows: Boolean): DimPageNumsAndTopLeft {
//        println("WrappedCell.renderCustom(${lp.javaClass.simpleName}, $tempTopLeft, $height, $reallyRender)")
        var pageNums:IntRange = INVALID_PAGE_RANGE

        val adj:Double
        val topLeft: Coord
        if (requiredSpaceBelow == 0.0) {
            adj = 0.0
            topLeft = tempTopLeft
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
            adj = lp.pageBreakingTopMargin(tempTopLeft.y - dim.height, dim.height, requiredSpaceBelow)
            topLeft = tempTopLeft.minusY(adj)
        }

//        println("topLeft=$topLeft")
//        println("  cellStyle=$cellStyle")
        val boxStyle = cellStyle.boxStyle
        val border = boxStyle.border
        // Dim dim = padding.addTo(pcrs.dim);

        // Draw contents over background, but under border
        val finalDim = if (dim.height < height) {
            dim.withHeight(height)
        } else {
            dim
        }
//        println("  finalDim=$finalDim")
        val innerDim: Dim = boxStyle.subtractFrom(finalDim)
//        println("  innerDim=$innerDim")

        val innerTopLeft = cellStyle.align.innerTopLeft(innerDim, wrappedBlockDim, boxStyle.applyTopLeft(topLeft))
//        println("  innerTopLeft=$innerTopLeft")

        var finalTopLeft = topLeft
        var y = innerTopLeft.y
        val lastRow: LineWrapped? = if (rows.isNotEmpty()) { rows[rows.size - 1] } else { null }
        for ((index, row) in rows.withIndex()) {
//            println("row=$row")
            val rowXOffset = cellStyle.align.leftOffset(wrappedBlockDim.width, row.dim.width)

            // Widow prevention:
            // If the last line of a paragraph would end up on the next page, push the last TWO lines
            // onto that page so that neither page is left with a single line widow or orphan.
            //
            // Don't do this in a table! Nothing would look weirder than one cell in a row starting on
            // the next page!
            // TODO: Did I take care of the case where we are at the top of a page already?
            if ( preventWidows &&
                 (lastRow !is WrappedList) ) {

                // I tried this 3-row rule, but it didn't work and I became less enamored of the idea.  For one thing,
                // what if there's a heading above this paragraph and we sneak it onto the next page leaving the
                // paragraph hanging?  No, I think to start that simpler is better.  If Margaret has to fix one of
                // these, she can.
                //
                // If there are only 3 rows, splitting into 2 and 1 still leaves a lonely row.  To avoid that, push
                // all three to the next page.  Less sure about this rule.
//                if (rows.size == 3) {
//                    if (index == 0) {
//                        val penUltimateRow: MultiLineWrapped = rows[1]
//                        val lastRow: MultiLineWrapped = rows[2]
//                        println("=================== 3 FROM END y=$y")
//                        println("row=$row")
//                        println("penUltimate=$penUltimateRow")
//                        println("lastRow=$lastRow")
//                        y -= lp.pageBreakingTopMargin(y - row.lineHeight, row.lineHeight + penUltimateRow.lineHeight,
//                                                      lastRow.lineHeight)
//                        println("newY=$y\n")
//                    }
//                } else
                // Only do this if both rows will fit on one page!
                if ( (index == rows.size - 2) &&
                     ((row.dim.height + lastRow!!.dim.height) < lp.body.dim.height) ) {
                    // I thought we could call lp.pageBreakingTopMargin to see if the two lines would fit on the
                    // page, but I guess a MultiLineWrapped can have items of different ascent and descent and leading
                    // such that it could have some effect on page-breaking.  Honestly, I'm not sure why it would.
                    // But the solution, at least for now, is to instead call render(reallyRender=false).

//                    println("=================== PENULTIMATE y=$y")
//                    println("row=$row")
//                    println("lastRow=$lastRow")

//                        var tempY = y
//                        var fixable = true
//                        var dimAndPages = row.render(lp, Coord(0f, tempY), reallyRender = false)
//                        if (dimAndPages.dim.height > lp.body.dim.height) {
//                            fixable = false
//                        }
//                        tempY -= dimAndPages.dim.height
//                        dimAndPages = lastRow.render(lp, Coord(0f, tempY), reallyRender = false)
//                        if (dimAndPages.dim.height > lp.body.dim.height) {
//                            fixable = false
//                        }
//                        if (dimAndPages.pageNums.endInclusive == (pageNums.endInclusive + 1)) {
//
//                        }
//                        println("y=$y")
//                        println("lp.body=${lp.body}")

                    // Returns zero if the last 2 rows fit on this page, otherwise, returns enough offset to push both
                    // to the next page.
                    val finalTwoRowsHeight: Double = row.dim.height + lastRow.dim.height
//                        println("finalTwoRowsHeight=$finalTwoRowsHeight")
                    val adj2 = lp.pageBreakingTopMargin(y - finalTwoRowsHeight, finalTwoRowsHeight, 0.0)
//                        println("adj expected=9.890003 actaul=$adj")
                    y -= adj2
                    if (rows.size == 2) {
                        finalTopLeft = finalTopLeft.minusY(adj2)
                    }
//                        println("newY expected=37.0 actual=$y\n") // Correct!
                } // End if this row might need to go to next page to avoid a widow
            } // End if preventWidows

            val dimAndPages = row.render(lp, Coord(rowXOffset + innerTopLeft.x, y), reallyRender,
                                         if ( (index < rows.size - 1) &&
                                              (cellStyle.align == Align.TOP_LEFT_JUSTIFY) ) {
                                             // Even if we're justifying text, the last row looks better unjustified.
                                             innerDim.width
                                         } else {
                                             0.0
                                         })
//            println("dimAndPages.dim.height=${dimAndPages.dim.height}")
            y -= dimAndPages.dim.height // y is always the lowest row in the cell.
            pageNums = dimAndPages.maxExtents(pageNums)
        } // end for each row

//        println("  y=$y")
//        println("  totalHeight=${innerTopLeft.y - y}")
        // TODO: test that we add bottom padding to y
        y = minOf(y, topLeft.y - height)
//        println("height=${innerTopLeft.y - y}")

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (boxStyle.bgColor != null) {
            //            System.out.println("\tCell.render calling putRect...");
            lp.fillRect(topLeft.withY(y), dim.withHeight(topLeft.y - y), boxStyle.bgColor, reallyRender)
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
            if ( (pageNums.start == pageNums.endInclusive) && // same page
                 (border.top.thickness > 0) &&
                 (border.top == border.right) &&
                 (border.top == border.bottom) &&
                 (border.top == border.left)) {
                lp.drawLineLoop(listOf(topLeft, topLeft.withX(rightX), Coord(rightX, y),
                                       topLeft.withY(y)),
                                border.top, true)
            } else {
                if (border.top.thickness > 0) {
                    lp.drawLine(if (border.left.thickness > 0) { topLeft.plusX(border.left.thickness / -2.0) } else { topLeft },
                                if (border.right.thickness > 0) { topRight.plusX(border.right.thickness / 2.0) } else { topRight },
                                border.top, reallyRender)
                }
                if (border.right.thickness > 0) {
                    lp.drawLine(if (border.top.thickness > 0) { topRight.minusY(border.top.thickness / -2.0) } else { topRight },
                                if (border.bottom.thickness > 0) { bottomRight.minusY(border.bottom.thickness / 2.0) } else { bottomRight },
                                border.right, reallyRender)
                }
                if (border.bottom.thickness > 0) {
                    lp.drawLine(if (border.right.thickness > 0) { bottomRight.plusX(border.right.thickness / 2.0) } else { bottomRight },
                                if (border.left.thickness > 0) { bottomLeft.plusX(border.left.thickness / 2.0) } else { bottomLeft },
                                border.bottom, reallyRender)
                }
                if (border.left.thickness > 0) {
                    lp.drawLine(if (border.bottom.thickness > 0) { bottomLeft.minusY(border.bottom.thickness / 2.0) } else { bottomLeft },
                                if (border.top.thickness > 0) { topLeft.minusY(border.top.thickness / -2.0) } else { topLeft },
                                border.left, reallyRender)
                }
            }
        }

        return DimPageNumsAndTopLeft(Dim(rightX - topLeft.x,
                                         (topLeft.y - y) + adj ),
                                     pageNums,
                                     finalTopLeft)
    }

    class DimPageNumsAndTopLeft(dim: Dim, pageNums: IntRange, val topLeft: Coord) : DimAndPageNums(dim, pageNums)
}