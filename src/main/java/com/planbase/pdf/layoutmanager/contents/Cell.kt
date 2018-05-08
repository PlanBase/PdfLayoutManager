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
import com.planbase.pdf.layoutmanager.attributes.CellStyle.Companion.TOP_LEFT_BORDERLESS
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.wrapLines
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.floatToStr
import com.planbase.pdf.layoutmanager.utils.listToStr

// TODO: Consider making this an abstract class "Box" with subclasses TableCell and Div
/**
 * A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated
 * based on how the content is rendered with regard to line-breaks and page-breaks.
 * @param cellStyle the style info for this cell
 * @param width the width of this cell (in document units)
 * @param contents the contents of this cell
 * @param requiredSpaceBelow leave this much vertical space at the end of the page below this cell.
 * @param tableRow if null, this cell is a stand-alone box-model (like a div in HTML).  If non-null,
 * this cell behaves as part of the given table row.
 */
data class Cell(val cellStyle: CellStyle = TOP_LEFT_BORDERLESS,
                val width: Double,
                private var contents: List<LineWrappable>,
                private val requiredSpaceBelow : Double,
                private val tableRow: TableRow? = null) : LineWrappable {

    constructor(cellStyle: CellStyle = TOP_LEFT_BORDERLESS,
                width: Double,
                contents: List<LineWrappable>,
                tableRow: TableRow? = null) : this(cellStyle, width, contents, 0.0, tableRow)

    constructor(cellStyle: CellStyle = TOP_LEFT_BORDERLESS,
                width: Double,
                contents: List<LineWrappable>) : this(cellStyle, width, contents, 0.0, null)

    constructor(cellStyle: CellStyle = TOP_LEFT_BORDERLESS,
                width: Double,
                contents: List<LineWrappable>,
                requiredSpaceBelow : Double) : this(cellStyle, width, contents, requiredSpaceBelow, null)
    init {
        if (width < 0) {
            throw IllegalArgumentException("A cell cannot have a negative width")
        }
        if ( (tableRow != null) && (requiredSpaceBelow != 0.0) ) {
            throw IllegalArgumentException("Can either be a table cell or have required space below, not both.")
        }
    }

    fun wrap() : WrappedCell {
//        println("Wrapping: $this")
        val fixedLines: List<MultiLineWrapped> = wrapLines(contents,
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

        return WrappedCell(Dim(width, height), this.cellStyle, fixedLines, requiredSpaceBelow)
    }

    override fun lineWrapper() = LineWrapper.preWrappedLineWrapper(this.wrap())

    override fun toString() =
            "Cell($cellStyle, $width, ${listToStr(contents)})"

    fun toStringTable() =
            "\n.cell($cellStyle, ${listToStr(contents)})"

//        var i = 0
//        while (i < contents.size && i < 3) {
//            if (i > 0) {
//                sB.append(" ")
//            }
//            sB.append(contents[i])
//            i++
//        }
//        return sB.append("])").toString()
//    }
}
