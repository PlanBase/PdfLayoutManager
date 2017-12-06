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
import java.lang.Math.abs

// TODO: Rename to "Dim"
/**
 * Immutable 2D dimension in terms of non-negative width and height.
 * Do not confuse a dimension (measurement) with an Coord which represents coordinates where
 * the bottom of the page is zero and positive height is up from there.
 * Remember: an Dimensions on a Portrait orientation page has the width and height *reversed*.
 */
data class Dimensions(val width: Float, val height: Float) {
    init {
        if (width < 0 || height < 0) {
            throw IllegalArgumentException("Dimensions must be positive")
        }
    }

    /**
     * Returns an Dimensions with the width and height taken from the same-named fields on the given
     * rectangle.
     */
    constructor(rect: PDRectangle) : this(rect.width, rect.height)

    fun width(newX: Float) = Dimensions(newX, height)

    fun height(newY: Float) = Dimensions(width, newY)

    /**
     * If true, returns this, if false, returns a new Dimensions with width and height values swapped.
     * PDFs think the long dimension is always the height of the page, regardless of portrait vs.
     * landscape, so we need to conditionally adjust what we call width and height.
     */
    // This is suspicious because we're swapping width and height and the compiler thinks
    // we might be doing so by accident.
    fun swapWh() = Dimensions(height, width)

    /** Returns a PDRectangle with the given width and height (but no/0 offset)  */
    fun toRect() = PDRectangle(width, height)

    //    /** Returns a PDRectangle with the given width and height (but no/0 offset) */
    //    public PDRectangle toRect(Coord off) {
    //        return new PDRectangle(off.x(), off.y(), width, height);
    //    }

    /** Subtracts the given Dimensions from this one (remember, these can't be negative).  */
    operator fun minus(that: Dimensions) = Dimensions(this.width - that.width, this.height - that.height)

    /** Adds the given Dimensions from this one  */
    operator fun plus(that: Dimensions) = Dimensions(this.width + that.width, this.height + that.height)

    //    public XyPair plusXMinusY(XyPair that) { return of(this.width + that.width(), this.height - that.height()); }

    //    public Dimensions maxXandY(Dimensions that) {
    //        if ((this.width >= that.width()) && (this.height >= that.height())) { return this; }
    //        if ((this.width <= that.width()) && (this.height <= that.height())) { return that; }
    //        return of((this.width > that.width()) ? this.width : that.width(),
    //                  (this.height > that.height()) ? this.height : that.height());
    //    }

    /** Compares dimensions and returns true if that dimension doesn't extend beyond this one.  */
    fun lte(that: Dimensions): Boolean = this.width <= that.width &&
                                         this.height <= that.height

    override fun toString() = "Dimensions(${width}f, ${height}f)"

    companion object {
        val ZERO = Dimensions(0f, 0f)
        fun sum(xys:Iterable<Dimensions>) = xys.fold(ZERO, { acc, xy -> acc.plus(xy)})
        fun within(latitude:Float, xya: Dimensions, xyb: Dimensions):Boolean =
                (abs(xya.height - xyb.height) <= latitude) &&
                (abs(xya.width - xyb.width) <= latitude)
    }
}
