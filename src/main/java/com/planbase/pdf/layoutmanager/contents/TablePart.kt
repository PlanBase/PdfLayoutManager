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

import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.TableRowBuilder.WrappedTableRow
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord
import java.util.ArrayList
import kotlin.math.max

/**
 * A set of styles to be the default for a table header or footer, or whatever other kind of group of table rows you
 * dream up.
 */
class TablePart(private val tableBuilder: TableBuilder) {
    val cellWidths:List<Float> = tableBuilder.cellWidths.toList()
    var cellStyle: CellStyle = tableBuilder.cellStyle
    var textStyle: TextStyle? = tableBuilder.textStyle
    var minRowHeight = 0f
    private val rows = ArrayList<TableRowBuilder>(1)

    fun finalXyDim() = Dim(cellWidths.sum(),
                           rows.map{ r -> r.finalRowHeight()}.sum())

    fun cellStyle(x: CellStyle): TablePart {
        cellStyle = x
        return this
    }

    //    public TablePart boxStyle(BoxStyle x) { return new Builder().boxStyle(boxStyle).build(); }

    fun textStyle(x: TextStyle): TablePart {
        textStyle = x
        return this
    }

    fun minRowHeight(f: Float): TablePart {
        minRowHeight = f
        return this
    }

    fun rowBuilder() = TableRowBuilder(this)

    fun addRow(trb: TableRowBuilder): TablePart {
        rows.add(trb)
        return this
    }

    fun buildPart(): TableBuilder = tableBuilder.addPart(this)

//    fun calcDimensions(): Dim {
//        var maxDim = Dim.ZERO
//        for (row in rows) {
//            val (width, height) = row.calcDimensions()
//            maxDim = Dim(Math.max(width, maxDim.width),
//                           maxDim.height + height)
//        }
//        return maxDim
//    }

    class WrappedTablePart(part:TablePart) {
        val dim: Dim = part.finalXyDim()
//        val ascent:Float = dim.height
//        val lineHeight: Float = dim.height

        private val rows: List<WrappedTableRow> =
                part.rows.map { WrappedTableRow(it) }
                        .toList()

        fun render(lp: RenderTarget, topLeft: Coord): Dim {
            var y = topLeft.y
            var maxWidth = 0f
            for (row in rows) {
                //            System.out.println("\tAbout to render row: " + row);
                val (width, height) = row.render(lp, topLeft.y(y))
                maxWidth = max(maxWidth, width)
                y -= height
            }
            return Dim(maxWidth, topLeft.y - y)
        }
    }

    override fun toString(): String = "TablePart($cellWidths, minRowHeight=$minRowHeight, $rows)"

    //    public static Builder builder(TableBuilder t) { return new Builder(t); }
    //
    //    public static class Builder {
    //        private final TableBuilder tableBuilder;
    //        private float[] cellWidths;
    //        private BoxStyle boxStyle;
    //        private TextStyle textStyle;
    //
    //        private Builder(TableBuilder t) { tableBuilder = t; }
    //
    //        public Builder cellWidths(float[] x) { cellWidths = x; return this; }
    //        public Builder boxStyle(BoxStyle x) { boxStyle = x; return this; }
    //        public Builder textStyle(TextStyle x) { textStyle = x; return this; }
    //
    //        public TablePart build() { return new TablePart(tableBuilder, cellWidths, boxStyle, textStyle); }
    //    } // end of class Builder
}
