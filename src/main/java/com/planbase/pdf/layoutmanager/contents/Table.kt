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
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.listToStr
import com.planbase.pdf.layoutmanager.utils.mutableListToStr
import kotlin.math.max

/**
 * Use this to create Tables.  This strives to remind the programmer of HTML tables but because you
 * can resize and scroll a browser window, and not a piece of paper, this is fundamentally different.
 * Still familiarity with HTML may make this class easier to use.
 */
class Table(val cellWidths:MutableList<Float> = mutableListOf(),
            var cellStyle: CellStyle = CellStyle.TOP_LEFT_BORDERLESS,
            var textStyle: TextStyle? = null,
            private val parts:MutableList<TablePart> = mutableListOf()) : LineWrappable {

    override fun lineWrapper() =
            LineWrapper.preWrappedLineWrapper(WrappedTable(this.parts.map { TablePart.WrappedTablePart(it) }))

    fun wrap(): WrappedTable = WrappedTable(this.parts.map { TablePart.WrappedTablePart(it) })

    /** Sets default widths for all table parts.  */
    fun addCellWidths(x: List<Float>): Table {
        cellWidths.addAll(x)
        return this
    }

    fun addCellWidths(vararg ws: Float): Table {
        for (w in ws) {
            cellWidths.add(w)
        }
        return this
    }

//    fun addCellWidth(x: Float): TableBuilder {
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

    fun addPart(tp: TablePart): Table {
        parts.add(tp)
        return this
    }

    fun partBuilder(): TablePart {
        return TablePart(this)
    }

    override fun toString(): String =
            "Table(${mutableListToStr(cellWidths)})" +
            parts.fold(StringBuilder(""),
                       {sB, part -> sB.append("\n.partBuilder()")
                               .append(part)
                               .append("\n.buildPart()")})
                    .toString()

    data class WrappedTable(private val parts:List<TablePart.WrappedTablePart>) : LineWrapped {
        override val dim: Dim = Dim.sum(parts.map { part -> part.dim })
        override val ascent: Float = dim.height
//        override val descentAndLeading: Float = 0f
        override val lineHeight: Float = dim.height

        /*
         * Renders item and all child-items with given width and returns the x-y pair of the
         * lower-right-hand corner of the last line (e.g. of text).
         */
        override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): Dim {
            var y = topLeft.y
            var maxWidth = 0f
            for (part in parts) {
                val (width, height) = part.render(lp, topLeft.y(y), reallyRender)
                maxWidth = max(maxWidth, width)
                y -= height
            }
            return Dim(maxWidth, topLeft.y - y)
        }

        override fun toString(): String = "WrappedTable($parts)"
    }
}
