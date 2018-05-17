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

import com.planbase.pdf.layoutmanager.utils.Dim
import kotlin.math.max
import kotlin.math.min

/** The dimensions of a rendered item plus the start and end page numbers */
open class DimAndPageNums(val dim:Dim, val pageNums:IntRange) {

    fun maxExtents(nums: IntRange): IntRange = maxExtents(pageNums, nums)

    override fun hashCode(): Int =
            dim.hashCode() + pageNums.hashCode()

    override fun equals(other: Any?): Boolean =
            (other != null) &&
            (other is DimAndPageNums) &&
            (dim == other.dim) &&
            (pageNums == other.pageNums)

    override fun toString(): String = "DimAndPageNums($dim, $pageNums)"

    companion object {
//        const val INVALID_PAGE_NUM = Int.MIN_VALUE

        @JvmField
        val INVALID_PAGE_RANGE = IntRange.EMPTY

        fun maxExtents(nums1: IntRange, nums2: IntRange): IntRange =
                when {
                    nums1 === INVALID_PAGE_RANGE -> nums2
                    nums2 === INVALID_PAGE_RANGE -> nums1
                    nums1 == nums2 -> nums1
                    else -> IntRange(min(nums1.start, nums2.start), max(nums1.endInclusive, nums2.endInclusive))
                }
    }
}