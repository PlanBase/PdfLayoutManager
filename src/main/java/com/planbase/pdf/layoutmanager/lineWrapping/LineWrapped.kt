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

package com.planbase.pdf.layoutmanager.lineWrapping

import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord

/**
 Represents a fixed-size item.  Classes implementing this interface should be immutable.
 */
interface LineWrapped {
    /** These are the dim *without/before* page-breaking adjustments. */
    val dim: Dim
//    fun width(): Float = width
//    fun totalHeight(): Float = heightAboveBase + depthBelowBase

    /** Height above the baseline of this line */
    val ascent: Float

    /** Depth below the baseline of this line */
//    val descentAndLeading: Float

    /** Total vertical height this line, both above and below the baseline */
    val lineHeight: Float

    /**
     Sends the underlying object to PDFBox to be drawn.

     @param lp RenderTarget is the SinglePage or PageGrouping to draw to.  This will contain the paper size,
     orientation, and body area which are necessary in order to calculate page breaking
     @param topLeft is the offset where this item starts.
     @param reallyRender render if true.  Otherwise, just measure without drawing anything.  This may be a little
     awkward for the end-user, but it lets us use exactly the same logic for measuring as for drawing which
     prevents bugs and there's a version of this method without this parameter.
     @return the adjusted Dim which may include extra (vertical) spacing required to nudge some items onto the next
     page so they don't end up in the margin or off the page.
     */
    fun render(lp: RenderTarget, topLeft: Coord, reallyRender:Boolean): Dim

    /**
    Sends the underlying object to PDFBox to be drawn. Use [@link render(RenderTarget, Coord, Boolean)] if
    you just want an exact measurement after page breaking without actually drawing anything.

    @param lp RenderTarget is the SinglePage or PageGrouping to draw to.  This will contain the paper size,
    orientation, and body area which are necessary in order to calculate page breaking
    @param topLeft is the offset where this item starts.
    @return the adjusted Dim which may include extra (vertical) spacing required to nudge some items onto the next
    page so they don't end up in the margin or off the page.
     */
    fun render(lp: RenderTarget, topLeft: Coord) = render(lp, topLeft, true)

    object ZeroLineWrapped: LineWrapped {
        override val dim: Dim = Dim.ZERO

        override val ascent: Float = 0f

//        override val descentAndLeading: Float = 0f

        override val lineHeight: Float = 0f

        override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): Dim = dim
    }

//    companion object {
//
//        fun preWrappedLineWrapper(item: LineWrapped) = object : LineWrapper {
//            private var hasMore = true
//            override fun hasMore(): Boolean = hasMore
//
//            override fun getSomething(maxWidth: Float): ConTerm {
//                hasMore = false
//                return Continuing(item)
//            }
//
//            override fun getIfFits(remainingWidth: Float): ConTermNone =
//                    if (hasMore && (item.dim.width <= remainingWidth)) {
//                        hasMore = false
//                        Continuing(item)
//                    } else {
//                        None
//                    }
//        }
//    }
}

