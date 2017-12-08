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

package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.ConTerm
import com.planbase.pdf.layoutmanager.lineWrapping.ConTermNone
import com.planbase.pdf.layoutmanager.lineWrapping.Continuing
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.lineWrapping.None
import com.planbase.pdf.layoutmanager.lineWrapping.Terminal
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord

/**
 * Represents styled text kind of like a #Text node in HTML.
 */
data class Text(val textStyle: TextStyle,
                private val initialText: String = "") : LineWrappable {
    constructor(textStyle: TextStyle) : this(textStyle, "")

    // This removes all tabs, transforms all line-terminators into "\n", and removes all runs of spaces that
    // precede line terminators.  This should simplify the subsequent line-breaking algorithm.
    val text = cleanStr(initialText)

    data class WrappedText(val textStyle: TextStyle,
                           val string: String,
                           override val dim: Dim,
                           val source: LineWrappable) : LineWrapped {

        constructor(s: String, x: Float, ts: TextStyle,
                    src: LineWrappable): this(ts, s, Dim(x, ts.lineHeight()), src)

        override val ascent: Float = textStyle.ascent()

        override val descentAndLeading: Float = textStyle.descent() + textStyle.leading()

        override val lineHeight: Float = textStyle.lineHeight()

        override fun render(lp: RenderTarget, topLeft: Coord): Dim =
                dim.height(lp.drawStyledText(topLeft.minusY(textStyle.ascent()), string, textStyle, true))

        override fun toString() = "WrappedText(\"$string\", $dim, $textStyle)"
    }

    fun style(): TextStyle = textStyle

    fun avgCharsForWidth(width: Float): Int = (width * 1220 / textStyle.avgCharWidth).toInt()

    fun maxWidth(): Float = textStyle.stringWidthInDocUnits(text.trim())

    override fun toString(): String {
        return "Text($textStyle, \"" +
                (if (text.length > 25) {
                    text.substring(0, 22) + "..."
                } else {
                    text
                }) + "\")"
    }

    override fun lineWrapper(): LineWrapper {
        return TextLineWrapper(this)
    }

    internal data class RowIdx(val row: WrappedText,
                               val idx: Int,
                               val foundCr: Boolean) {

        fun toContTerm() : ConTerm =
                if (foundCr) {
                    Terminal(row)
                } else {
                    Continuing(row)
                }
    }

    class TextLineWrapper(private val txt: Text) : LineWrapper {
        private var idx = 0

        override fun hasMore(): Boolean = idx < txt.text.length

        override fun getSomething(maxWidth: Float): ConTerm {
            if (maxWidth < 0) {
                throw IllegalArgumentException("Illegal negative width: " + maxWidth)
            }
            val rowIdx = tryGettingText(maxWidth, idx, txt)
            idx = rowIdx.idx
            return rowIdx.toContTerm()
        }

        override fun getIfFits(remainingWidth: Float): ConTermNone {
            if (remainingWidth <= 0) {
                return None
            }
            val ctri = tryGettingText(remainingWidth, idx, txt)
            val row = ctri.row
            return if (row.dim.width <= remainingWidth) {
                idx = ctri.idx
                ctri.toContTerm() as ConTermNone
            } else {
                None
            }
        }
    }

    companion object {
        private val CR = '\n'

        internal fun tryGettingText(maxWidth: Float, startIdx: Int, txt: Text): RowIdx {
            if (maxWidth < 0) {
                throw IllegalArgumentException("Can't meaningfully wrap text with a negative width: " + maxWidth)
            }

            // Already removed all tabs, transformed all line-terminators into "\n", and removed all runs of spaces
            // that precede line terminators.
            val row = txt.text
            if (row.length <= startIdx) {
                throw IllegalStateException("text length must be greater than startIdx")
            }

            var crIdx = row.indexOf(CR, startIdx)
            val foundCr =
                    if (crIdx < 0) {
                        crIdx = row.length
                        false
                    } else {
                        true
                    }

            val text = row.substring(startIdx, crIdx)
//            println("text:" + text)

            val charWidthGuess = txt.avgCharsForWidth(maxWidth)

            val textLen = text.length
            //        System.out.println("text=[" + text + "] len=" + textLen);
            // Knowing the average width of a character lets us guess and generally be near
            // the word where the line break will occur.  Since the font reports a narrow average,
            // (possibly due to the predominance of spaces in text) we widen it a little for a
            // better first guess.
            var idx = charWidthGuess
            if (idx > textLen) {
                idx = textLen
            }
            var substr = text.substring(0, idx)
            var strWidth = txt.textStyle.stringWidthInDocUnits(substr)

            //        System.out.println("(strWidth=" + strWidth + " < maxWidth=" + maxWidth + ") && (idx=" + idx + " < textLen=" + textLen + ")");
            // If too short - find shortest string that is too long.
            // int idx = idx;
            // int maxTooShortIdx = -1;
            while (strWidth < maxWidth && idx < textLen) {
                //                System.out.println("find shortest string that is too long");
                // Consume any whitespace.
                while (idx < textLen && Character.isWhitespace(text[idx])) {
                    idx++
                }
                // Find last non-whitespace character
                while (idx < textLen && !Character.isWhitespace(text[idx])) {
                    idx++
                }
                // Test new width
                substr = text.substring(0, idx)
                strWidth = txt.textStyle.stringWidthInDocUnits(substr)
            }

            idx--
            //        System.out.println("(strWidth=" + strWidth + " > maxWidth=" + maxWidth + ") && (idx=" + idx + " > 0)");
            // Too long.  Find longest string that is short enough.
            while (strWidth > maxWidth && idx > 0) {
                //            System.out.println("find longest string that is short enough");
                //            System.out.println("strWidth: " + strWidth + " maxWidth: " + maxWidth + " idx: " + idx);
                // Find previous whitespace run
                while (idx > -1 && !Character.isWhitespace(text[idx])) {
                    idx--
                }
                // Find last non-whitespace character before whitespace run.
                while (idx > -1 && Character.isWhitespace(text[idx])) {
                    idx--
                }
                if (idx < 1) {
                    break // no spaces - have to put whole thing in cell and let it run over.
                }
                // Test new width
                substr = text.substring(0, idx + 1)
                strWidth = txt.textStyle.stringWidthInDocUnits(substr)
            }

            idx++
            val eolIdx = substr.indexOf(char= CR)
            if (eolIdx > -1) {
                substr = substr.substring(0, eolIdx)
                strWidth = txt.textStyle.stringWidthInDocUnits(substr)
                if (strWidth > maxWidth) {
                    throw IllegalStateException("strWidth=$strWidth > maxWidth=$maxWidth")
                }
                return RowIdx(WrappedText(substr, strWidth, txt.textStyle, txt), idx + startIdx + 1, true)
            }
            // Need to test trailing whitespace.
//            println("idx=" + idx + " substr=\"" + substr + "\"")

            return RowIdx(WrappedText(substr, strWidth, txt.textStyle, txt), idx + startIdx + 1,
                          if (substr == text) {
                                                                           foundCr
                                                                       } else {
                                                                           false
                                                                       })
        }

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
