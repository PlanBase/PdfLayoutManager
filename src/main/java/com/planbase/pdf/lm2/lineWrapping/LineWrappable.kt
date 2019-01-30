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

/**
 Implementing LineWrappable means being suitable for use with a two-pass layout manager whose first
 pass says, "given this width, what is your height?" and second pass says, "Given these dimensions,
 draw yourself as best you can."  Classes implementing LineWrapper are generally mutable (builders).
 */
interface LineWrappable {
//    val boxStyle: BoxStyle

    fun lineWrapper(): LineWrapper

    companion object ZeroLineWrappable: LineWrappable {
//        override val boxStyle = BoxStyle.NONE
        override fun lineWrapper() = LineWrapper.EmptyLineWrapper
    }
}