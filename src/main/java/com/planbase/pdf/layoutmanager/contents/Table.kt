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
import com.planbase.pdf.layoutmanager.contents.TablePart.WrappedTablePart
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord
import kotlin.math.max

/** Represents a table.  It used to be that you'd build a table and that act would commit it to a logical page. */
class Table(private val parts: List<TablePart>, val cellStyle: CellStyle) : LineWrappable {
    override fun lineWrapper() =
            LineWrapper.preWrappedLineWrapper(WrappedTable(this.parts.map{ WrappedTablePart(it) }))

    fun wrap():WrappedTable = WrappedTable(this.parts.map{ WrappedTablePart(it) })

    /*
    Renders item and all child-items with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).
    */
    override fun toString(): String = "Table($parts)"

    data class WrappedTable(private val parts:List<WrappedTablePart>) : LineWrapped {
        val dim: Dim = Dim.sum(parts.map { part -> part.dim })
        override val ascent: Float = dim.height
        override val descentAndLeading: Float = 0f
        override val lineHeight: Float = dim.height

        /*
        Renders item and all child-items with given width and returns the x-y pair of the
        lower-right-hand corner of the last line (e.g. of text).
        */
        override fun render(lp: RenderTarget, topLeft: Coord): Dim {
            var y = topLeft.y
            var maxWidth = 0f
            for (part in parts) {
                //            System.out.println("About to render part: " + part);
                val (width, height) = part.render(lp, topLeft.y(y))
                maxWidth = max(maxWidth, width)
                y -= height
            }
            return Dim(maxWidth, topLeft.y - y)
        }

        override fun toString(): String = "WrappedTable($parts)"
    }
}
