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

package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d

/**
 * Represents minimum spacing of the top, right, bottom, and left sides of PDF Page Items.
 */
data class Padding(val top: Float,
                   val right: Float,
                   val bottom: Float,
                   val left: Float) {

    /** Sets all padding values equally  */
    constructor(a:Float) : this(a, a, a, a)

//    fun topLeftPadDim(): Dimensions = Dimensions(left, top)
//
//    fun botRightPadDim(): Dimensions = Dimensions(right, bottom)

    fun subtractFrom(outer: Dimensions): Dimensions =
            Dimensions(outer.width - (left + right),
                  outer.height - (top + bottom))

    fun addTo(outer: Dimensions): Dimensions =
            Dimensions(outer.width + (left + right),
                  outer.height + (top + bottom))

    fun applyTopLeft(orig: Point2d): Point2d = Point2d(orig.x + left, orig.y - top)

    fun topBottomPadding() = top + bottom

    fun leftRightPadding() = left + right

    override fun toString() =
            if ((top == right) && (top == bottom) && (top == left)) {
                "Padding($top)"
            } else {
                super.toString()
            }

    //    public Point2d topLeftPadOffset() { return Point2d(left, -top); }
    //    public Point2d botRightPadOffset() { return Point2d(right, -bottom); }

    companion object {
        /**
         * Default padding of 1.5, 1.5, 2. 1.5 (top, right, bottom, left)
         */
        val DEFAULT_TEXT_PADDING = Padding(1.5f, 1.5f, 2f, 1.5f)
        val NO_PADDING = Padding(0f, 0f, 0f, 0f)
    }
}
