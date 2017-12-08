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
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord
import java.util.ArrayList
import kotlin.math.max

/**
 * Unsynchronized mutable class which is not thread-safe.  The internal tracking of cells and widths
 * allows you to make a cell builder for a cell at a given column, add cells in subsequent columns,
 * then complete (buildCell()) the cell and have it find its proper (now previous) column.
 */
class TableRow(private val tablePart: TablePart) {
    var textStyle: TextStyle? = tablePart.textStyle
    private var cellStyle: CellStyle = tablePart.cellStyle
    private val cells: MutableList<Cell?> = ArrayList(tablePart.cellWidths.size)
    var minRowHeight = tablePart.minRowHeight
    private var nextCellIdx = 0

//    fun textStyle(x: TextStyle): TableRow {
//        textStyle = x
//        return this
//    }

    fun align(a: Align) : TableRow {
        cellStyle = cellStyle.align(a)
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

    fun minRowHeight(f: Float): TableRow {
        minRowHeight = f
        return this
    }

    fun cell(cs: CellStyle = cellStyle,
             contents: List<LineWrappable>): TableRow {
        if (tablePart.cellWidths.size < (nextCellIdx + 1)) {
            throw IllegalStateException("Can't add another cell because there are only ${tablePart.cellWidths.size}" +
                                        " cell widths and already $nextCellIdx cells")
        }
        cells.add(Cell(cs, tablePart.cellWidths[nextCellIdx++], contents, this))
        return this
    }

    fun buildRow(): TablePart {
        // Do we want to fill out the row with blank cells?
        if (cells.contains(null)) {
            throw IllegalStateException("Cannot build row when some TableRowCellBuilders have been" +
                                        " created but the cells not built and added back to the row.")
        }
        return tablePart.addRow(this)
    }

    fun finalRowHeight():Float {
        cells.map { c -> c?.wrap() ?: LineWrapped.ZeroLineWrapped }
                .forEach{ c -> minRowHeight = Math.max(minRowHeight, c.dim.height)}
//        println("finalRowHeight() returns: ${minRowHeight}")
        return minRowHeight
    }

    class WrappedTableRow(row: TableRow) {
        private val minRowHeight:Float = row.minRowHeight
        private val fixedCells:List<WrappedCell> =
                row.cells.map { c -> c!!.wrap() }
                        .toList()

        fun render(lp: RenderTarget, topLeft: Coord): Dim {
//        cells.map { c -> c?.wrap() ?: LineWrapped.ZeroLineWrapped }
//                .forEach{ c -> minRowHeight = Math.max(minRowHeight, c.dim.height)}

            var x = topLeft.x
            var maxRowHeight = minRowHeight
            println("minRowHeight=$minRowHeight")
            // Find the height of the tallest cell before rendering any cells.
            for (fixedCell in fixedCells) {
                val (width, height) = fixedCell.tableRender(lp, topLeft.x(x), maxRowHeight, false)
                println("height=$height")
                maxRowHeight = max(maxRowHeight, height)
                println("maxRowHeight=$maxRowHeight")
                x += width
            }
            val maxWidth = x - topLeft.x

            // Now render the cells
            x = topLeft.x
            for (fixedCell in fixedCells) {
                val (width, _) = fixedCell.tableRender(lp, topLeft.x(x), maxRowHeight, true)
                x += width
            }

            return Dim(maxWidth, maxRowHeight)
        }
    }

    override fun toString(): String = "TableRow($cells)"
}
