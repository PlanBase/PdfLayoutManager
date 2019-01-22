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

package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.LineJoinStyle
import com.planbase.pdf.layoutmanager.utils.LineJoinStyle.MITER
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.lang.StringBuilder

/**
 * Holds the LineStyles for the top, right, bottom, and left borders of a PdfItem.  For an equal
 * border on all sides, use:
 * <pre>`BorderStyle b = new BorderStyle(color, width);`</pre>
 * For an unequal border, use named parameters in Kotlin.
 * This class works just like styles in CSS in terms of specifying one style, then
 * overwriting it with another.  This example sets all borders except to black and the default width,
 * then removes the top border:
 * <pre>`BorderStyle topBorderStyle = BorderStyle(LineStyle(PDColor.BLACK))
 * .top(LineStyle(PDColor.RED, 2.0));`</pre>
 * If neighboring cells in a cell-row have the same border, only one will be printed.  If different,
 * the left-border of the right cell will override.  You have to manage your own top borders
 * manually.
 *
 */
// Like CSS it's listed Top, Right, Bottom, left
data class BorderStyle(val top: LineStyle = LineStyle.NO_LINE,
                       val right: LineStyle = LineStyle.NO_LINE,
                       val bottom: LineStyle = LineStyle.NO_LINE,
                       val left: LineStyle = LineStyle.NO_LINE,
                       val lineJoinStyle: LineJoinStyle = MITER) {

    /** Creates a BorderStyle with [MITER]ed corners */
    constructor(top: LineStyle, right: LineStyle,
                bottom: LineStyle, left: LineStyle) : this(top, right, bottom, left, MITER)

    /**
     * Returns equal top and bottom borders and equal right and left borders and [MITER]ed corners
     * @param topBottom the line style for top and bottom lines
     * @param rightLeft the line style for right and left lines.
     * @return a new immutable border object
     */
    constructor(topBottom: LineStyle, rightLeft: LineStyle) : this(topBottom, rightLeft, topBottom, rightLeft, MITER)

    /**
     * Returns an equal border on all sides with [MITER]ed corners
     * @param allSides the line style
     * @return a new immutable border object
     */
    constructor(allSides: LineStyle) : this(allSides, allSides, allSides, allSides, MITER)

//    /**
//     * Returns an equal border on all sides
//     * @param c the border color
//     * @param w the width of the border.
//     * @return a new immutable border object
//     */
//    constructor(c: PDColor, w: Double) : this (LineStyle(c, w))

    /**
     * Returns an equal border on all sides
     * @param c the border color
     * @return a new immutable border object with default width
     */
    constructor(c: PDColor) : this (LineStyle(c))

    fun withTop(ls: LineStyle) = BorderStyle(ls, right, bottom, left, lineJoinStyle)
    fun withRight(ls: LineStyle) = BorderStyle(top, ls, bottom, left, lineJoinStyle)
    fun withBottom(ls: LineStyle) = BorderStyle(top, right, ls, left, lineJoinStyle)
    fun withLeft(ls: LineStyle) = BorderStyle(top, right, bottom, ls, lineJoinStyle)

    fun allSame(): Boolean = top == right &&
                             right == bottom &&
                             bottom == left

    fun hasAllBorders(): Boolean =
            top.thickness > 0.0 &&
            right.thickness > 0.0 &&
            bottom.thickness > 0.0 &&
            left.thickness > 0.0

    fun topBottomThickness(): Double = top.thickness + bottom.thickness

    fun leftRightThickness(): Double = left.thickness + right.thickness

    override fun toString(): String {

        if (this == NO_BORDERS) {
            return "NO_BORDERS"
        }
        val sB = StringBuilder("BorderStyle(")

        if (allSame()) {
            sB.append("$top")
        } else {
            sB.append("$top, $right, $bottom, $left")
        }
        if (lineJoinStyle != MITER) {
            sB.append(", $lineJoinStyle")
        }
        return sB.append(")").toString()
    }

    companion object {
        @JvmField
        val NO_BORDERS = BorderStyle(LineStyle.NO_LINE)
    }
}
