// Copyright 2013-03-03 PlanBase Inc.
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
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapper
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.renderablesToMultiLineWrappeds
import com.planbase.pdf.layoutmanager.utils.Dim

/**
 A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated
 based on how the content is rendered with regard to line-breaks and page-breaks.
 */
data class Cell(val cellStyle: CellStyle = CellStyle.Default, // contents can override this style
                val width: Float,
                // A list of the contents.  It's pretty limiting to have one item per row.
                private var contents: List<LineWrappable>,
                private val tableRow: TableRowBuilder? = null) : LineWrappable {
    init {
        if (width < 0) {
            throw IllegalArgumentException("A cell cannot have a negative width")
        }
    }

    fun wrap() : WrappedCell {
        val fixedLines: List<MultiLineWrapped> =
                renderablesToMultiLineWrappeds(contents,
                                               width - cellStyle.boxStyle.leftRightInteriorSp())
//        var maxWidth = cellStyle.boxStyle.leftRightThickness()
        var height = cellStyle.boxStyle.topBottomInteriorSp()

        for (line in fixedLines) {
            height += line.dim.height
//            println("height=$height")
//            maxWidth = maxOf(line.dim.width, maxWidth)
        }

//        if ( (tableRow != null) &&
//             (height < tableRow.minRowHeight) ) {
//            height = tableRow.minRowHeight
//        }

        return WrappedCell(Dim(width, height), this.cellStyle, fixedLines)
    }

    override fun lineWrapper() = MultiLineWrapper(contents.iterator())

    /** {@inheritDoc}  */
    override fun toString(): String {
        val sB = StringBuilder("Cell(").append(cellStyle).append(" width=")
                .append(width).append(" contents=[")

        var i = 0
        while (i < contents.size && i < 3) {
            if (i > 0) {
                sB.append(" ")
            }
            sB.append(contents[i])
            i++
        }
        return sB.append("])").toString()
    }
}
