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

/**
 An immutable 2D coordinate in terms of X and Y measured from the lower-left corner.
 Do not confuse this with an Dimensions which represents positive width and height.
 This is called Point2d because Point2D is already a class in Java and it's mutable.
 */
data class Point2d(val x: Float, val y: Float) {

    fun x(newX: Float) = Point2d(newX, y)

    fun y(newY: Float) = Point2d(x, newY)

    //    public Point2d minus(Point2d that) { return of(this.x - that.x(), this.y - that.y()); }
    //    public Point2d plus(Point2d that) { return of(this.x + that.x(), this.y + that.y()); }

    fun plusX(offset: Float) = if (offset == 0f) { this } else { Point2d(x + offset, y) }

    fun minusY(offset: Float) = if (offset == 0f) { this } else { Point2d(x, y - offset) }

    fun plusXMinusY(that: Point2d) = Point2d(x + that.x, y - that.y)

    fun plusXMinusY(that: Dimensions) = Point2d(x + that.width, y - that.height)

    //    public Point2d maxXandY(Point2d that) {
    //        if ((this.x >= that.x()) && (this.y >= that.y())) { return this; }
    //        if ((this.x <= that.x()) && (this.y <= that.y())) { return that; }
    //        return of((this.x > that.x()) ? this.x : that.x(),
    //                  (this.y > that.y()) ? this.y : that.y());
    //    }
//    fun maxXMinY(that: Point2d): Point2d =
//            if (this.x >= that.x && this.y <= that.y) {
//                this
//            } else if (this.x <= that.x && this.y >= that.y) {
//                that
//            } else {
//                Point2d(if (this.x > that.x) this.x else that.x,
//                        if (this.y < that.y) this.y else that.y)
//            }

    /** Compares dimensions  */
//    fun lte(that: Point2d): Boolean = this.x <= that.x && this.y >= that.y

    override fun toString(): String = "Point2d(${x}f, ${y}f)"

//    companion object {
//        val ORIGIN = Point2d(0f, 0f)
//    }
}
