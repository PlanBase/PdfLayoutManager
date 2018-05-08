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

import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim

//    public enum HorizAlign { LEFT, CENTER, RIGHT; }
//    public enum VertAlign { TOP, MIDDLE, BOTTOM; }

/** Horizontal and vertical alignment options for cell contents  */
enum class Align {
    TOP_LEFT {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = 0.0
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = 0.0
    },
    TOP_LEFT_JUSTIFY {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = 0.0
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = 0.0
    },
    TOP_CENTER {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = (outerWidth - innerWidth) / 2
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = 0.0
    },
    TOP_RIGHT {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = outerWidth - innerWidth
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = 0.0
    },
    MIDDLE_LEFT {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = 0.0
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = (outerHeight - innerHeight) / 2
    },
    MIDDLE_CENTER {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = (outerWidth - innerWidth) / 2
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = (outerHeight - innerHeight) / 2
    },
    MIDDLE_RIGHT {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = outerWidth - innerWidth
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = (outerHeight - innerHeight) / 2
    },
    BOTTOM_LEFT {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = 0.0
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = outerHeight - innerHeight
    },
    BOTTOM_CENTER {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = (outerWidth - innerWidth) / 2
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = outerHeight - innerHeight
    },
    BOTTOM_RIGHT {
        override fun leftOffset(outerWidth: Double, innerWidth: Double): Double = outerWidth - innerWidth
        override fun topOffset(outerHeight: Double, innerHeight: Double): Double = outerHeight - innerHeight
    };

    /**
     * Given the size of the cell (outer), the size of the contents (inner), and the
     * top-left corner of the inside of the cell, what should the top-left corner of the contents be?
     *
     * @param outer the inside dimension after taking cell borders and padding into account
     * @param inner the size of the cell contents
     * @param topLeft the coordinate of the top-left corner of the cell inside the borders and padding.
     */
    fun innerTopLeft(outer: Dim, inner: Dim, topLeft: Coord):Coord =
            topLeft.plusXMinusY(leftOffset(outer.width, inner.width),
                                topOffset(outer.height, inner.height))

    /** How far from the left do we need to start for this alignment? */
    abstract fun leftOffset(outerWidth: Double, innerWidth: Double): Double

    /** How far from the top do we need to start for this alignment? */
    abstract fun topOffset(outerHeight: Double, innerHeight: Double): Double
}
