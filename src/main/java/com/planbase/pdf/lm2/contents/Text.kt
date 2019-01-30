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

package com.planbase.pdf.lm2.contents

import com.planbase.pdf.lm2.attributes.TextStyle
import com.planbase.pdf.lm2.lineWrapping.LineWrappable
import com.planbase.pdf.lm2.lineWrapping.LineWrapper
import org.organicdesign.indented.StringUtils.stringify

/**
 Represents styled text kind of like a #Text node in HTML.
 */
data class Text(val textStyle: TextStyle,
                private val initialText: String = "") : LineWrappable {
    constructor(textStyle: TextStyle) : this(textStyle, "")

    // This removes all tabs, transforms all line-terminators into "\n", and removes all runs of spaces that
    // precede line terminators.  This should simplify the subsequent line-breaking algorithm.
    val text = cleanStr(initialText)

    fun avgCharsForWidth(width: Double): Int = (width * 1220.0 / textStyle.avgCharWidth).toInt()

    fun maxWidth(): Double = textStyle.stringWidthInDocUnits(text.trim())

    override fun toString() = "Text($textStyle, ${stringify(text)})"

    override fun lineWrapper(): LineWrapper = TextLineWrapper(this)

    companion object {
        // From: https://docs.google.com/document/d/1vpbFYqfW7XmJplSwwLLo7zSPOztayO7G4Gw5_EHfpfI/edit#
        //
        // 1. Remove all tabs.  There is no way to make a good assumption about how to turn them into spaces.
        //
        // 2. Transform all line-terminators into "\n".  Also remove spaces before every line terminator (hard break).
        //
        // The wrapping algorithm will remove all consecutive spaces on an automatic line-break.  Otherwise, we want
        // to preserve consecutive spaces within a line.
        //
        // Non-breaking spaces are ignored here.
        fun cleanStr(s:String):String = s.replace(Regex("\t"), "")
                .replace(Regex("[ ]*(\r\n|[\r\n\u0085\u2028\u2029])"), "\n")
    }
}
