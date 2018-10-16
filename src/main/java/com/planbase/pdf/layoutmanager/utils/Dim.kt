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

import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.lang.AssertionError
import java.lang.Math.abs

/**
 Immutable 2D dimension in terms of non-negative width and height.
 Do not confuse a dimension (measurement) with a Coord(inate) which represents an offset from
 (0, 0) at the bottom of the page.
 Remember: a Dimensions on a Portrait orientation may have the width and height *reversed*.
 */
data class Dim(val width: Double, val height: Double) {
    init {
        if (width < 0.0 || height < 0.0) {
            throw IllegalArgumentException("Dimensions must be positive: width=$width height=$height")
        }
    }

    /**
     * Returns an Dim with the width and height taken from the same-named fields on the given
     * rectangle.
     */
    constructor(rect: PDRectangle) : this(rect.width.toDouble(), rect.height.toDouble())

    fun withWidth(newX: Double) = Dim(newX, height)

    fun withHeight(newY: Double) = Dim(width, newY)

    /**
     * If true, returns this, if false, returns a new Dim with width and height values swapped.
     * PDFs think the long dimension is always the height of the page, regardless of portrait vs.
     * landscape, so we need to conditionally adjust what we call width and height.
     */
    // This is suspicious because we're swapping width and height and the compiler thinks
    // we might be doing so by accident.
    fun swapWh() = Dim(height, width)

    /** Returns a PDRectangle with the given width and height (but no/0 offset)  */
    fun toRect() = PDRectangle(width.toFloat(), height.toFloat())

    //    /** Returns a PDRectangle with the given width and height (but no/0 offset) */
    //    public PDRectangle toRect(Coord off) {
    //        return new PDRectangle(off.x(), off.y(), width, height);
    //    }

    /** Subtracts the given Dim from this one (remember, these can't be negative).  */
    operator fun minus(that: Dim) = Dim(this.width - that.width, this.height - that.height)

    /** Adds the given Dim from this one  */
    operator fun plus(that: Dim) = Dim(this.width + that.width, this.height + that.height)

    //    public XyPair plusXMinusY(XyPair that) { return of(this.width + that.width(), this.height - that.height()); }

    //    public Dim maxXandY(Dim that) {
    //        if ((this.width >= that.width()) && (this.height >= that.height())) { return this; }
    //        if ((this.width <= that.width()) && (this.height <= that.height())) { return that; }
    //        return of((this.width > that.width()) ? this.width : that.width(),
    //                  (this.height > that.height()) ? this.height : that.height());
    //    }

    /** Compares dim and returns true if that dimension doesn't extend beyond this one.  */
    fun lte(that: Dim): Boolean = this.width <= that.width &&
                                  this.height <= that.height

    override fun toString() = "Dim($width, $height)"

    companion object {
        /** Zero-dimension (zero width, zero height) */
        @JvmField
        val ZERO = Dim(0.0, 0.0)

        fun sum(xys:Iterable<Dim>) = xys.fold(ZERO, { acc, xy -> acc.plus(xy)})

        fun assertEquals(xya: Dim, xyb: Dim, latitude: Double) {
            if ((abs(xya.height - xyb.height) > latitude) ||
                (abs(xya.width - xyb.width) > latitude)) {
                throw AssertionError("\n" +
                                     "Expected: $xya\n" +
                                     "Actual:   $xyb")
            }
        }
    }
}
