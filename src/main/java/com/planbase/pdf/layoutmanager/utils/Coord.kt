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

package com.planbase.pdf.layoutmanager.utils

import kotlin.math.abs
/**
 An immutable 2D Coordinate, offset, or point in terms of X and Y.  Often measured from the lower-left corner.
 Do not confuse this with an Dim which represents positive width and height.
 This is called Coord because Point and Point2D are already classes in Java and they are mutable.
 It's pronounced "co-ward" as in, "coordinate."  It's not called Xy because that's too easy to confuse
 with width and height, which this is not - it's an offset from the origin.
 */
data class Coord(val x: Float, val y: Float) {

    fun x(newX: Float) = Coord(newX, y)

    fun y(newY: Float) = Coord(x, newY)

    fun plusX(offset: Float) = if (offset == 0f) { this } else { Coord(x + offset, y) }

    fun plusY(offset: Float) = if (offset == 0f) { this } else { Coord(x, y + offset) }

    fun minusY(offset: Float) = if (offset == 0f) { this } else { Coord(x, y - offset) }

//    fun plusXMinusY(that: Coord) = Coord(x + that.x, y - that.y)

    fun plusXMinusY(that: Dim) = Coord(x + that.width, y - that.height)

    fun dimensionTo(that: Coord) = Dim(abs(x - that.x), abs(y - that.y))

    override fun toString(): String = "Coord(${x}f, ${y}f)"
}
