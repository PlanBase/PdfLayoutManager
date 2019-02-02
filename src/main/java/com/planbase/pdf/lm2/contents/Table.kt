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

import com.planbase.pdf.lm2.attributes.CellStyle
import com.planbase.pdf.lm2.attributes.DimAndPageNums
import com.planbase.pdf.lm2.attributes.TextStyle
import com.planbase.pdf.lm2.lineWrapping.LineWrappable
import com.planbase.pdf.lm2.lineWrapping.LineWrapped
import com.planbase.pdf.lm2.lineWrapping.LineWrapper
import com.planbase.pdf.lm2.pages.RenderTarget
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import com.planbase.pdf.lm2.utils.mutableListToStr
import kotlin.math.max

/*
Algorithm for choosing column sizes dynamically:

 - For each cell, figure out min and max width.
 - For each column figure out min and max width.

 - If all columns fit with max-width, then do that, shrinking total table width as needed: END.
 - If columns do not fit with min-width, then expand total table width to fit every column min-width and use that: END.

 - If any column overflows, then figure out some ratio of min to max width for the cells in that column, an average width.

colAvgWidth = (sumMinWidths + sumMaxWidths) / (numRows * 2)

Then figure proportion of average column widths

colProportion1 = colAvgWidth / sumColAvgWidths

colWidth1 = tableMaxWidth * colProportion1

 - Each column must have at least min-width size.  So check all colWidth1's and adjust any that are less than that size.
 - Proportion the remaining columns and find colProportion2 and colWidth2
 - Repeat until all columns have at least min-width.
 */

/**
 * Use this to create Tables.  This strives to remind the programmer of HTML tables but because you
 * can resize and scroll a browser window, and not a piece of paper, this is fundamentally different.
 * Still familiarity with HTML may make this class easier to use.
 */
class Table
@JvmOverloads
constructor(var cellWidths: MutableList<Double> = mutableListOf(),
            var cellStyle: CellStyle = CellStyle.TOP_LEFT_BORDERLESS,
            var textStyle: TextStyle? = null,
            private val rows: MutableList<TableRow> = mutableListOf()) : LineWrappable {
    var minRowHeight = 0.0

    fun minRowHeight(h: Double): Table {
        minRowHeight = h
        return this
    }

    private var openRow = false

    override fun lineWrapper() =
            LineWrapper.preWrappedLineWrapper(WrappedTable(this.rows.map { TableRow.WrappedTableRow(it) }))

    fun wrap(): WrappedTable {
        if (openRow) {
            throw IllegalStateException("Must end TableRow before wrapping table!")
        }
        return WrappedTable(this.rows.map { TableRow.WrappedTableRow(it) })
    }

    /** Sets default widths for all table rows.  */
    fun addCellWidths(x: Iterable<Double>): Table {
        cellWidths.addAll(x)
        return this
    }

    fun addCellWidths(vararg ws: Double): Table {
        for (w in ws) {
            cellWidths.add(w)
        }
        return this
    }

//    fun addCellWidth(x: Double): TableBuilder {
//        cellWidths.add(x)
//        return this
//    }

    fun cellStyle(x: CellStyle): Table {
        cellStyle = x
        return this
    }

    fun textStyle(x: TextStyle): Table {
        textStyle = x
        return this
    }

    fun addRow(trb: TableRow): Table {
        rows.add(trb)
        return this
    }

    fun startRow(): TableRow {
        if (openRow) {
            throw IllegalStateException("Must end current TableRow before starting a new one!")
        }
        openRow = true
        return TableRow(this, { openRow = false })
    }

    override fun toString(): String =
            "Table(${mutableListToStr(0, cellWidths)})" +
            rows.fold(StringBuilder(""),
                       {sB, row -> sB.append("\n.startRow()")
                               .append(row)
                               .append("\n.endRow()")})
                    .toString()

    data class WrappedTable(private val rows:List<TableRow.WrappedTableRow>) : LineWrapped {
        override val dim: Dim = Dim.sum(rows.map { row -> row.dim })
        override val ascent: Double = dim.height

        /*
         * Renders item and all child-items with given width and returns the x-y pair of the
         * lower-right-hand corner of the last line (e.g. of text).
         */
        override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean,
                            justifyWidth:Double): DimAndPageNums {
            var y = topLeft.y
            var maxWidth = 0.0
            var pageNums:IntRange = DimAndPageNums.INVALID_PAGE_RANGE
            for (row in rows) {
                val dimAndPageNums: DimAndPageNums = row.render(lp, topLeft.withY(y), reallyRender)
                maxWidth = max(maxWidth, dimAndPageNums.dim.width)
                y -= dimAndPageNums.dim.height
                pageNums = dimAndPageNums.maxExtents(pageNums)
            }
            return DimAndPageNums(Dim(maxWidth, topLeft.y - y), pageNums)
        }

        override fun toString(): String = "WrappedTable($rows)"
    }
}
