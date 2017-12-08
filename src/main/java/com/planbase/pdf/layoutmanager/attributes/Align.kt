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

//    /** Horizontal alignment options for cell contents */
//    public enum HorizAlign { LEFT, CENTER, RIGHT; }
//    public enum VertAlign { TOP, MIDDLE, BOTTOM; }

/** Horizontal and vertical alignment options for cell contents  */
enum class Align {
    TOP_LEFT {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord = xy

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float = 0f
    },
    TOP_CENTER {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord =
                xy.plusX(leftOffset(outer.width, inner.width))

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float =
                (outerWidth - innerWidth) / 2
    },
    TOP_RIGHT {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord =
                xy.plusX(leftOffset(outer.width, inner.width))

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float =
                outerWidth - innerWidth
    },
    MIDDLE_LEFT {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord =
                xy.minusY((outer.height - inner.height) / 2)

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float = 0f
    },
    MIDDLE_CENTER {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord =
                xy.plusXMinusY(leftOffset(outer.width, inner.width),
                               (outer.height - inner.height) / 2)

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float =
                (outerWidth - innerWidth) / 2
    },
    MIDDLE_RIGHT {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord =
                xy.plusXMinusY(leftOffset(outer.width, inner.width),
                               (outer.height - inner.height) / 2)

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float =
                outerWidth - innerWidth
    },
    BOTTOM_LEFT {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord =
                xy.minusY(outer.height - inner.height)

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float = 0f
    },
    BOTTOM_CENTER {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord =
                xy.plusXMinusY(leftOffset(outer.width, inner.width),
                               outer.height - inner.height)

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float =
                (outerWidth - innerWidth) / 2
    },
    BOTTOM_RIGHT {
        override fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord  =
                xy.plusXMinusY(leftOffset(outer.width, inner.width),
                               outer.height - inner.height)

        override fun leftOffset(outerWidth: Float, innerWidth: Float): Float =
                outerWidth - innerWidth
    };

    abstract fun innerTopLeft(outer: Dim, inner: Dim, xy: Coord):Coord

    /** How far from the left do we need to start for this alignment? */
    abstract fun leftOffset(outerWidth: Float, innerWidth: Float): Float
}
