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
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import java.util.ArrayList
import java.util.Collections

/**
 * Unsynchronized mutable class which is not thread-safe.  The internal tracking of cells and widths
 * allows you to make a cell builder for a cell at a given column, add cells in subsequent columns,
 * then complete (buildCell()) the cell and have it find its proper (now previous) column.
 */
class TableRowBuilder(private val tablePart: TablePart) {
    var textStyle: TextStyle? = tablePart.textStyle
    val cellStyle: CellStyle = tablePart.cellStyle
    private val cells: MutableList<Cell?> = ArrayList(tablePart.cellWidths.size)
    var minRowHeight = tablePart.minRowHeight
    private var nextCellIdx = 0

//    private TableRow(TablePart tp, float[] a, Cell[] b, BoxStyle c, TextStyle d) {
//        tablePart = tp; cellWidths = a; cells = b; boxStyle = c; textStyle = d;
//    }

    fun nextCellSize(): Float {
        if (tablePart.cellWidths.size <= nextCellIdx) {
            throw IllegalStateException("Tried to add more cells than you set sizes for")
        }
        return tablePart.cellWidths[nextCellIdx]
    }

//    fun textStyle(x: TextStyle): TableRowBuilder {
//        textStyle = x
//        return this
//    }

    fun addCells(vararg cs: Cell): TableRowBuilder {
        Collections.addAll(cells, *cs)
        nextCellIdx += cs.size
        return this
    }

    fun addTextCells(vararg ss: String): TableRowBuilder {
        if (textStyle == null) {
            throw IllegalStateException("Tried to add a text cell without setting a default text style")
        }
        for (s in ss) {
            addCellAt(Cell(cellStyle, nextCellSize(), listOf(Text(textStyle!!, s)), this), nextCellIdx)
            nextCellIdx++
        }
        return this
    }

    // TODO: This should be add LineWrappable Cells.
    fun addImageCells(vararg js: ScaledImage): TableRowBuilder {
        for (j in js) {
            addCellAt(Cell(cellStyle, nextCellSize(), listOf(j), this), nextCellIdx)
            nextCellIdx++
        }
        return this
    }

    // Because cells are renderable, this would accept one which could result in duplicate cells
    // when Cell.buildCell() creates a cell and passes it in here.
    //    public TableRowBuilder addCell(BoxStyle.Align align, LineWrappable... things) {
    //            cells.add(Cell.builder(this).add(things).build());
    //        return this;
    //    }

    fun addCell(c: Cell): TableRowBuilder {
        cells.add(c)
        nextCellIdx++
        return this
    }

    fun addCellAt(c: Cell, idx: Int): TableRowBuilder {
        // Ensure capacity in the list.
        while (cells.size < idx + 1) {
            cells.add(null)
        }
        if (cells[idx] != null) {
            // System.out.println("Existing cell was: " + cells.get(idx) + "\n Added cell was: " + c);
            throw IllegalStateException("Tried to add a cell built from a table row back to the row after adding a free cell in its spot.")
        }
        cells[idx] = c
        return this
    }

    fun minRowHeight(f: Float): TableRowBuilder {
        minRowHeight = f
        return this
    }

    fun cellBuilder(): RowCellBuilder {
        val cb = RowCellBuilder(this)
        nextCellIdx++
        return cb
    }

    fun buildRow(): TablePart {
        // Do we want to fill out the row with blank cells?
        if (cells.contains(null)) {
            throw IllegalStateException("Cannot build row when some TableRowCellBuilders have been created but the cells not built and added back to the row.")
        }
        return tablePart.addRow(this)
    }

//    fun calcDimensions(): XyDim {
//        var maxDim = XyDim.ZERO
//        // Similar to PdfLayoutMgr.putRow().  Should be combined?
//        for (cell in cells) {
//            if (cell != null) {
//                val wh = cell.calcDimensions(cell.width)
//                maxDim = XyDim(wh!!.width + maxDim.width,
//                               Math.max(maxDim.height, wh.height))
//            }
//        }
//        return maxDim
//    }

    fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        var maxDim = XyDim.ZERO.height(minRowHeight)
        val fixedCells = cells.map { c -> c?.wrap() ?: LineWrapped.ZeroLineWrapped }
        for (fixedCell in fixedCells) {
            val wh = fixedCell.xyDim
            maxDim = XyDim(maxDim.width + wh.width,
                                                                Math.max(maxDim.height, wh.height))
        }

        var x = outerTopLeft.x
        for (fixedCell in fixedCells) {
            // TODO: Account for page breaks!
            fixedCell.render(lp, XyOffset(x, outerTopLeft.y))
            x += fixedCell.xyDim.width
        }
        return XyOffset(x, outerTopLeft.y - maxDim.height)
    }

    override fun toString(): String =
            "TableRowBuilder($cells)"

    class RowCellBuilder(private val tableRowBuilder: TableRowBuilder) : CellBuilder {
        /** {@inheritDoc}  */
        override val width: Float = tableRowBuilder.nextCellSize() // Both require this.
        private var cellStyle: CellStyle = tableRowBuilder.cellStyle // Both require this.
        private val rows = ArrayList<LineWrappable>()
        private var textStyle: TextStyle? = tableRowBuilder.textStyle
        private val colIdx: Int = tableRowBuilder.nextCellIdx

        // I think setting the width after creation is a pretty bad idea for this class since so much
        // is put into getting the width and column correct.
        // public TableRowCellBuilder width(float w) { width = w; return this; }

        /** {@inheritDoc}  */
        override fun cellStyle(cs: CellStyle): RowCellBuilder {
            cellStyle = cs
            return this
        }

//        fun borderStyle(bs: BorderStyle): RowCellBuilder {
//            boxStyle = boxStyle.borderStyle(bs)
//            return this
//        }

        /** {@inheritDoc}  */
        override fun align(a: Align): RowCellBuilder {
            cellStyle = cellStyle.align(a)
            return this
        }

        /** {@inheritDoc}  */
        override fun textStyle(x: TextStyle): RowCellBuilder {
            textStyle = x
            return this
        }

        /** {@inheritDoc}  */
        override fun add(rs: LineWrappable): RowCellBuilder {
            // TODO: Is this correct???  Adding rows and returning a row cell builder???
            Collections.addAll(rows, rs)
            return this
        }

        /** {@inheritDoc}  */
        override fun addAll(js: Collection<LineWrappable>): RowCellBuilder {
            rows.addAll(js)
            return this
        }

        /** {@inheritDoc}  */
        override fun addStrs(vararg ss: String): RowCellBuilder {
            if (textStyle == null) {
                throw IllegalStateException("Must set a default text style before adding" + " raw strings")
            }
            for (s in ss) {
                rows.add(Text(textStyle!!, s))
            }
            return this
        }

        /** {@inheritDoc}  */
        override fun add(ts: TextStyle, ls: Iterable<String>): RowCellBuilder {
            for (s in ls) {
                rows.add(Text(ts, s))
            }
            return this
        }

        fun buildCell(): TableRowBuilder {
            val c = Cell(cellStyle, width, rows, tableRowBuilder)
            return tableRowBuilder.addCellAt(c, colIdx)
        }

        override fun toString(): String {
            return StringBuilder("RowCellBuilder(").append(tableRowBuilder).append(" colIdx=")
                    .append(colIdx).append(")").toString()
        }

        override fun hashCode(): Int {
            return tableRowBuilder.hashCode() + colIdx
        }

        override fun equals(other: Any?): Boolean {
            // Cheapest operation first...
            if (this === other) {
                return true
            }

            if (other == null ||
                other !is RowCellBuilder ||
                this.hashCode() != other.hashCode()) {
                return false
            }
            // Details...
            val that = other as RowCellBuilder?

            return this.colIdx == that!!.colIdx && tableRowBuilder == that.tableRowBuilder
        }
    }

    companion object {

        fun of(tp: TablePart): TableRowBuilder {
            return TableRowBuilder(tp)
        }
    }
}
