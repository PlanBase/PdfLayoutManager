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

package com.planbase.pdf.lm2.lineWrapping

import com.planbase.pdf.lm2.attributes.DimAndPageNums
import com.planbase.pdf.lm2.pages.RenderTarget
import com.planbase.pdf.lm2.utils.Dim
import com.planbase.pdf.lm2.utils.Coord

/**
 Represents a fixed-size item.  Classes implementing this interface should be immutable.
 */
interface LineWrapped {
    /** These are the dim *without/before* page-breaking adjustments. */
    val dim: Dim
//    fun width(): Double = width
//    fun totalHeight(): Double = heightAboveBase + depthBelowBase

    /** Height above the baseline of this line */
    val ascent: Double

    /** Depth below the baseline of this line */
//    val descentAndLeading: Double

    // Removed because it entirely duplicated dim.height.
    // Was "Total vertical height this line, both above and below the baseline"
//    val lineHeight: Double

    /**
     * Sends the underlying object to PDFBox to be drawn.
     *
     * @param lp RenderTarget is the SinglePage or PageGrouping to draw to.  This will contain the paper size,
     * orientation, and body area which are necessary in order to calculate page breaking
     * @param topLeft is the offset where this item starts.
     * @param reallyRender render if true.  Otherwise, just measure without drawing anything.  This may be a little
     * awkward for the end-user, but it lets us use exactly the same logic for measuring as for drawing which
     * prevents bugs and there's a version of this method without this parameter.
     * @param justifyWidth the width of the line - non-zero if items should be alignd "justified" to stretch to both
     * sides of the width.
     * @return the adjusted Dim which may include extra (vertical) spacing required to nudge some items onto the next
     * page so they don't end up in the margin or off the page.
     */
    // TODO: Add preventWidows: Boolean after reallyRender.
    fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean, justifyWidth: Double): DimAndPageNums

    /**
     * Sends the underlying object to PDFBox to be drawn.
     *
     * @param lp RenderTarget is the SinglePage or PageGrouping to draw to.  This will contain the paper size,
     * orientation, and body area which are necessary in order to calculate page breaking
     * @param topLeft is the offset where this item starts.
     * @param reallyRender render if true.  Otherwise, just measure without drawing anything.  This may be a little
     * awkward for the end-user, but it lets us use exactly the same logic for measuring as for drawing which
     * prevents bugs and there's a version of this method without this parameter.
     * @return the adjusted Dim which may include extra (vertical) spacing required to nudge some items onto the next
     * page so they don't end up in the margin or off the page.
     */
    // TODO: Is this necessary or helpful?
    @JvmDefault
    fun render(lp: RenderTarget, topLeft: Coord, reallyRender:Boolean): DimAndPageNums =
            render(lp, topLeft, reallyRender, 0.0)

    /**
     * Sends the underlying object to PDFBox to be drawn. Use the other render() method with reallyRender=false
     * for an exact measurement after page breaking without actually drawing anything.
     *
     * @param lp RenderTarget is the SinglePage or PageGrouping to draw to.  This will contain the paper size,
     * orientation, and body area which are necessary in order to calculate page breaking
     * @param topLeft is the offset where this item starts.
     * @return the adjusted Dim which may include extra (vertical) spacing required to nudge some items onto the next
     * page so they don't end up in the margin or off the page.
     */
    // TODO: Is this necessary or helpful?
    @JvmDefault
    fun render(lp: RenderTarget, topLeft: Coord): DimAndPageNums = render(lp, topLeft, true)

//    companion object {
//
//        fun preWrappedLineWrapper(item: LineWrapped) = object : LineWrapper {
//            private var hasMore = true
//            override fun hasMore(): Boolean = hasMore
//
//            override fun getSomething(maxWidth: Double): ConTerm {
//                hasMore = false
//                return Continuing(item)
//            }
//
//            override fun getIfFits(remainingWidth: Double): ConTermNone =
//                    if (hasMore && (item.dim.width <= remainingWidth)) {
//                        hasMore = false
//                        Continuing(item)
//                    } else {
//                        None
//                    }
//        }
//    }
    /**
     * For a composite line, returns the items on the line.  For a single-item line, just returns a single-item list
     * containing `this`.
     */
    @JvmDefault
    fun items():List<LineWrapped> = listOf(this)
}

