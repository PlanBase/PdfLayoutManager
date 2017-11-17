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
import com.planbase.pdf.layoutmanager.utils.XyOffset
import java.util.ArrayList

/**
 * Unsynchronized mutable class which is not thread-safe.  The internal tracking of cells and widths
 * allows you to make a cell builder for a cell at a given column, add cells in subsequent columns,
 * then complete (buildCell()) the cell and have it find its proper (now previous) column.
 */
class TableRowBuilder(private val tablePart: TablePart) {
    var textStyle: TextStyle? = tablePart.textStyle
    private var cellStyle: CellStyle = tablePart.cellStyle
    private val cells: MutableList<Cell?> = ArrayList(tablePart.cellWidths.size)
    var minRowHeight = tablePart.minRowHeight
    private var nextCellIdx = 0

//    fun textStyle(x: TextStyle): TableRowBuilder {
//        textStyle = x
//        return this
//    }

    fun align(a: Align) : TableRowBuilder {
        cellStyle = cellStyle.align(a)
        return this
    }

    fun addTextCells(vararg ss: String): TableRowBuilder {
        if (textStyle == null) {
            throw IllegalStateException("Tried to add a text cell without setting a default text style")
        }
        for (s in ss) {
            cell(cellStyle, listOf(Text(textStyle!!, s)))
        }
        return this
    }

    fun minRowHeight(f: Float): TableRowBuilder {
        minRowHeight = f
        return this
    }

    fun cell(cs: CellStyle = cellStyle,
             contents: List<LineWrappable>): TableRowBuilder {
        cells.add(Cell(cs, tablePart.cellWidths[nextCellIdx++], contents, this))
        return this
    }

    fun buildRow(): TablePart {
        // Do we want to fill out the row with blank cells?
        if (cells.contains(null)) {
            throw IllegalStateException("Cannot build row when some TableRowCellBuilders have been created but the cells not built and added back to the row.")
        }
        return tablePart.addRow(this)
    }

    fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        cells.map { c -> c?.wrap() ?: LineWrapped.ZeroLineWrapped }
                .forEach{ c -> minRowHeight = Math.max(minRowHeight, c.xyDim.height)}

        val fixedCells = cells.map { c -> c?.wrap() ?: LineWrapped.ZeroLineWrapped }

        var x = outerTopLeft.x
        for (fixedCell in fixedCells) {
            // TODO: Account for page breaks!
            fixedCell.render(lp, XyOffset(x, outerTopLeft.y))
            x += fixedCell.xyDim.width
        }
        return XyOffset(x, outerTopLeft.y - minRowHeight)
    }

    override fun toString(): String = "TableRowBuilder($cells)"
}