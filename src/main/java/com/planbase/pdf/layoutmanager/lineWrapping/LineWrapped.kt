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

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.pages.PageGrouping
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset

/**
 Represents a fixed-size item, line-wrapped, but not page broken.
 If we want to render something on a single page (header, footer, page number, watermark, etc.), we can render from here.
 If we want to break across multiple pages, call pageBreak().
 Classes implementing this interface should be immutable.
 */
interface LineWrapped {
    val xyDim: XyDim
//    fun width(): Float = width
//    fun totalHeight(): Float = heightAboveBase + depthBelowBase

    /** Height above the baseline of this line */
    val ascent: Float

    /** Depth below the baseline of this line */
    val descentAndLeading: Float

    /** Total vertical height this line, both above and below the baseline */
    val lineHeight: Float

    fun renderToPage(singlePage: SinglePage, outerTopLeft: XyOffset): XyOffset

    /**
    This page-breaks line-wrapped rows in order to fix content that falls across a page-break.
    When the contents overflow the bottom of the cell, we adjust the cell border and background downward to match.

    This adjustment is calculated by calling PdfLayoutMgr.appropriatePage().

    TODO:  check PageGrouping.drawImage() and .drawPng() to see if `return y + pby.adj;` still makes sense.
     */
    fun pageBreak(mgr: PdfLayoutMgr, pageGrouping: PageGrouping, topLeft: XyOffset):PageBroken

    object ZeroLineWrapped: LineWrapped {
        override val xyDim: XyDim = XyDim.ZERO

        override val ascent: Float = 0f

        override val descentAndLeading: Float = 0f

        override val lineHeight: Float = 0f

        override fun renderToPage(singlePage: SinglePage, outerTopLeft: XyOffset): XyOffset = outerTopLeft

        override fun pageBreak(mgr: PdfLayoutMgr, pageGrouping: PageGrouping, topLeft: XyOffset): PageBroken {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}

