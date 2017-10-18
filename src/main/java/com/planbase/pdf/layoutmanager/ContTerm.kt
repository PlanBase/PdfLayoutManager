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

package com.planbase.pdf.layoutmanager

/**
Represents a continuing or terminal FixedItem where Continuing means there could be more on this
line (no hard line break) and Terminal means a hard-coded line-break was encountered.
 */
data class ContTerm(val item: FixedItem, val foundCr: Boolean) {
    fun toContTermNone() : ContTermNone =
            if (foundCr) {
                Terminal(item)
            } else {
                Continuing(item)
            }
    companion object {
        /** Construct a new Continuing from the given object.  */
        fun continuing(continuing: FixedItem): ContTerm = ContTerm(continuing, false)

        /** Construct a new Terminal from the given object.  */
        fun terminal(terminal: FixedItem): ContTerm = ContTerm(terminal, true)
    }
}