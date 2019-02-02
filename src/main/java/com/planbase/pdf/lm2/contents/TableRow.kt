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

package com.planbase.pdf.lm2.contents

import com.planbase.pdf.lm2.attributes.Align
import com.planbase.pdf.lm2.attributes.CellStyle
import com.planbase.pdf.lm2.attributes.DimAndPageNums
import com.planbase.pdf.lm2.attributes.TextStyle
import com.planbase.pdf.lm2.lineWrapping.LineWrappable
import com.planbase.pdf.lm2.pages.RenderTarget
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import java.util.ArrayList
import kotlin.math.max

/**
 * Unsynchronized mutable class which is not thread-safe.  The internal tracking of cells and widths
 * allows you to make a cell builder for a cell at a given column, add cells in subsequent columns,
 * then complete (buildCell()) the cell and have it find its proper (now previous) column.
 */
class TableRow
@JvmOverloads
constructor(private val table: Table,
            private val rowClosedCallback: (() -> Unit)? = null) {
    var textStyle: TextStyle? = table.textStyle
    private var cellStyle: CellStyle = table.cellStyle
    private val cells: MutableList<Cell> = ArrayList(table.cellWidths.size)
    var minRowHeight = table.minRowHeight
    private var nextCellIdx = 0

//    fun textStyle(x: TextStyle): TableRow {
//        textStyle = x
//        return this
//    }

    fun align(a: Align) : TableRow {
        cellStyle = cellStyle.withAlign(a)
        return this
    }

    fun addTextCells(vararg ss: String): TableRow {
        if (textStyle == null) {
            throw IllegalStateException("Tried to add a text cell without setting a default text style")
        }
        for (s in ss) {
            cell(cellStyle, listOf(Text(textStyle!!, s)))
        }
        return this
    }

    fun minRowHeight(f: Double): TableRow {
        minRowHeight = f
        return this
    }

    fun cell(cs: CellStyle = cellStyle,
             contents: List<LineWrappable>): TableRow {
        if (table.cellWidths.size < (nextCellIdx + 1)) {
            throw IllegalStateException("Can't add another cell because there are only ${table.cellWidths.size}" +
                                        " cell widths and already $nextCellIdx cells")
        }
        cells.add(Cell(cs, table.cellWidths[nextCellIdx++], contents))
        return this
    }

    fun endRow(): Table {
        // Do we want to fill out the row with blank cells?
//        if (cells.contains(null)) {
//            throw IllegalStateException("Cannot build row when some TableRowCellBuilders have been" +
//                                        " created but the cells not built and added back to the row.")
//        }
        val table = table.addRow(this)
        if (rowClosedCallback != null) {
            rowClosedCallback.invoke()
        }
        return table
    }

    fun finalRowHeight(): Double {
//        cells.map { c -> c?.wrap() ?: LineWrapped.ZeroLineWrapped }
        cells.map { c -> c.wrap() }
                .forEach{ c -> minRowHeight = Math.max(minRowHeight, c.dim.height)}
//        println("finalRowHeight() returns: ${minRowHeight}")
        return minRowHeight
    }

    class WrappedTableRow(row: TableRow) {
        val dim = Dim(row.table.cellWidths.sum(),
                      row.finalRowHeight())
        private val minRowHeight: Double = row.minRowHeight
        private val fixedCells:List<WrappedCell> =
                row.cells.map { c -> c.wrap() }
                        .toList()

        fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): DimAndPageNums {
//        cells.map { c -> c?.wrap() ?: LineWrapped.ZeroLineWrapped }
//                .forEach{ c -> minRowHeight = Math.max(minRowHeight, c.dim.height)}
            var pageNums:IntRange = DimAndPageNums.INVALID_PAGE_RANGE
            var x = topLeft.x
            var maxRowHeight = minRowHeight
//            println("    minRowHeight=$minRowHeight")
            // Find the height of the tallest cell before rendering any cells.
            for (fixedCell in fixedCells) {
//                println("    beforeRender height=${fixedCell.dim.height}")
                val dimAndPageNums: DimAndPageNums = fixedCell.renderCustom(lp, topLeft.withX(x), maxRowHeight,
                                                                            reallyRender = false,
                                                                            preventWidows = false)
//                println("    afterRender height=$height") // Size is wrong here!
                maxRowHeight = max(maxRowHeight, dimAndPageNums.dim.height)
//                println("    maxRowHeight=$maxRowHeight")
                x += dimAndPageNums.dim.width
                pageNums = dimAndPageNums.maxExtents(pageNums)
            }
            val maxWidth = x - topLeft.x

            if (reallyRender) {
                // Now render the cells
                x = topLeft.x
                for (fixedCell in fixedCells) {
                    val width = fixedCell.renderCustom(lp, topLeft.withX(x), maxRowHeight, reallyRender = true,
                                                       preventWidows = false).dim.width
                    x += width
                }
            }

            return DimAndPageNums(Dim(maxWidth, maxRowHeight), pageNums)
        }
    }

    override fun toString(): String =
//            "TableRow($cells)"
            cells.fold(StringBuilder(""),
                       {sB, cell -> sB.append(cell.toStringTable())})
                    .toString()
}
