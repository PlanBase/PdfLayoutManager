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
 * Represents styled text kind of like a #Text node in HTML.
 */
data class Text(val textStyle: TextStyle, val text: String = "") : Layoutable {
    constructor(textStyle: TextStyle) : this(textStyle, "")

    private val dims = HashMap<Float, WrappedBlock>()

    internal data class WrappedRow(val string: String,
                                   override val xyDim: XyDim,
                                   val textStyle: TextStyle) : FixedItem {

        constructor(s: String, x: Float, ts: TextStyle): this(s, XyDim(x, ts.lineHeight()), ts)

        //        float width() { return xyDim.width(); }
        //        float totalHeight() { return xyDim.height(); }

        override val ascent: Float = textStyle.ascent()

        override val descentAndLeading: Float = textStyle.descent() + textStyle.leading()

        override val lineHeight: Float = textStyle.lineHeight()

        override fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
            lp.drawStyledText(outerTopLeft.x, outerTopLeft.y, string, textStyle)
            return XyOffset(outerTopLeft.x + xyDim.width,
                            outerTopLeft.y - xyDim.height - textStyle.leading())
        }
    }

    private class WrappedBlock(var rows: MutableList<WrappedRow> = ArrayList(), var blockDim: XyDim? = null)

    fun text(): String = text

    fun style(): TextStyle = textStyle

    fun avgCharsForWidth(width: Float): Int = (width * 1220 / textStyle.avgCharWidth).toInt()

    fun maxWidth(): Float = textStyle.stringWidthInDocUnits(text.trim())

    private fun calcDimensionsForReal(maxWidth: Float): XyDim {
        if (maxWidth < 0) {
            throw IllegalArgumentException("Can't meaningfully wrap text with a negative width: " + maxWidth)
        }
        throw Exception("calcDimForReal")

//        val wb = WrappedBlock()
//        val x = 0f
//        var y = 0f
//        var maxX = x
//        val txt = this
//
//        val rend = layouter()
//        val line:TextLine = TextLine()
//
//        // TODO: This is fundamentally wrong - need to change how things are rendered.
//        while (rend.hasMore()) {
//
//        }
//
//
//
//        val row = txt.text() //PdfLayoutMgr.convertJavaStringToWinAnsi(txt.text());
//
//        var text = substrNoLeadingWhitespace(row, 0)
//        val charWidthGuess = txt.avgCharsForWidth(maxWidth)
//
//        while (text.isNotEmpty()) {
//            val textLen = text.length
//            //            System.out.println("text=[" + text + "] len=" + textLen);
//            // Knowing the average width of a character lets us guess and generally be near
//            // the word where the line break will occur.  Since the font reports a narrow average,
//            // (possibly due to the predominance of spaces in text) we widen it a little for a
//            // better first guess.
//            var idx = charWidthGuess
//            if (idx > textLen) {
//                idx = textLen
//            }
//            var substr = text.substring(0, idx)
//            var strWidth = textStyle.stringWidthInDocUnits(substr)
//
//            //            System.out.println("(strWidth=" + strWidth + " < maxWidth=" + maxWidth + ") && (idx=" + idx + " < textLen=" + textLen + ")");
//            // If too short - find shortest string that is too long.
//            // int idx = idx;
//            // int maxTooShortIdx = -1;
//            while (strWidth < maxWidth && idx < textLen) {
//                //                System.out.println("find shortest string that is too long");
//                // Consume any whitespace.
//                while (idx < textLen && Character.isWhitespace(text[idx])) {
//                    idx++
//                }
//                // Find last non-whitespace character
//                while (idx < textLen && !Character.isWhitespace(text[idx])) {
//                    idx++
//                }
//                // Test new width
//                substr = text.substring(0, idx)
//                strWidth = textStyle.stringWidthInDocUnits(substr)
//            }
//
//            idx--
//            //            System.out.println("(strWidth=" + strWidth + " > maxWidth=" + maxWidth + ") && (idx=" + idx + " > 0)");
//            // Too long.  Find longest string that is short enough.
//            while (strWidth > maxWidth && idx > 0) {
//                //                System.out.println("find longest string that is short enough");
//                //logger.info("strWidth: " + strWidth + " cell.width: " + cell.width + " idx: " + idx);
//                // Find previous whitespace run
//                while (idx > -1 && !Character.isWhitespace(text[idx])) {
//                    idx--
//                }
//                // Find last non-whatespace character before whitespace run.
//                while (idx > -1 && Character.isWhitespace(text[idx])) {
//                    idx--
//                }
//                if (idx < 1) {
//                    break // no spaces - have to put whole thing in cell and let it run over.
//                }
//                // Test new width
//                substr = text.substring(0, idx + 1)
//                strWidth = textStyle.stringWidthInDocUnits(substr)
//            }
//
//            wb.rows.add(WrappedRow.of(substr, strWidth, textStyle))
//            //            System.out.println("added row");
//            y -= textStyle.lineHeight()
//            //            System.out.println("y=" + y);
//
//            // Chop off section of substring that we just wrote out.
//            text = substrNoLeadingWhitespace(text, substr.length)
//            if (strWidth > maxX) {
//                maxX = strWidth
//            }
//            //            System.out.println("maxX=" + maxX);
//        }
//        //        // Not sure what to do if passed "".  This used to mean to insert a blank line, but I'd
//        //        // really like to make that "\n" instead, but don't have the time.  *sigh*
//        //        if (y == 0) {
//        //            y -= textStyle.lineHeight();
//        //        }
//        wb.blockDim = XyDim.of(maxX, 0 - y)
//        dims.put(maxWidth, wb)
//        //        System.out.println("\tcalcWidth(" + maxWidth + ") on " + this.toString());
//        //        System.out.println("\t\ttext calcDim() blockDim=" + wb.blockDim);
//        return wb.blockDim!!
    }

    private fun ensureWrappedBlock(maxWidth: Float): WrappedBlock {
        var wb: WrappedBlock? = dims[maxWidth]
        if (wb == null) {
            calcDimensionsForReal(maxWidth)
            wb = dims[maxWidth]
        }
        return wb!!
    }

//    override fun calcDimensions(maxWidth: Float): XyDim {
//        // I'd like to try to make calcDimensionsForReal() handle this situation before throwing an exception here.
//        //        if (maxWidth < 0) {
//        //            throw new IllegalArgumentException("maxWidth must be positive, not " + maxWidth);
//        //        }
//        return ensureWrappedBlock(maxWidth).blockDim!!
//    }

//    /** {@inheritDoc}  */
//    override fun render(lp: RenderTarget, outerTopLeft: XyOffset,
//                        outerDimensions: XyDim): XyOffset {
//
//        //        System.out.println("\tText.render(" + this.toString());
//        //        System.out.println("\t\ttext.render(outerTopLeft=" + outerTopLeft +
//        //                           ", outerDimensions=" + outerDimensions);
//
//        val maxWidth = outerDimensions.width()
//        val wb = ensureWrappedBlock(maxWidth)
//
//        var x = outerTopLeft.x()
//        var y = outerTopLeft.y()
//        val innerPadding = align.calcPadding(outerDimensions, wb.blockDim)
//        //        System.out.println("\t\ttext align.calcPadding() returns: " + innerPadding);
//        if (innerPadding != null) {
//            x += innerPadding.left()
//            //y -= innerPadding.top();
//        }
//
//        for (wr in wb.rows) {
//            // Here we're done whether it fits or not.
//            //final float xVal = x + align.leftOffset(wb.blockDim.x(), wr.xyDim.x());
//
//            y -= textStyle.ascent()
//            //            if (allPages) {
//            //                lp.borderStyledText(x, y, wr.string, textStyle);
//            //            } else {
//
//            // TODO: Probably want this!
//            //            wr.render(lp, XyOffset.of(x, y));
//            lp.drawStyledText(x, y, wr.string, textStyle)
//            //            }
//            y -= textStyle.descent()
//            y -= textStyle.leading()
//        }
//        return XyOffset.of(outerTopLeft.x() + wb.blockDim!!.width(),
//                           outerTopLeft.y() - wb.blockDim!!.height())
//    }

    internal data class Thing(val trimmedStr: String,
                              val totalCharsConsumed: Int,
                              val foundCr: Boolean)

    override fun toString(): String {
        return "Text(\"" +
                (if (text.length > 25) {
                    text.substring(0, 22) + "..."
                } else {
                    text
                }) + "\")"
    }

    override fun layouter(): Layouter {
        return TextLayouter(this)
    }

    internal data class RowIdx(val row: WrappedRow,
                               val idx: Int,
                               val foundCr: Boolean) {
        fun toContTerm() : ContTerm =
                if (foundCr) {
                    Terminal(row)
                } else {
                    Continuing(row)
                }
//        fun toContTermNone() : ContTermNone =
//                if (foundCr) {
//                    ContTermNone.Companion.terminal(row)
//                } else {
//                    ContTermNone.Companion.continuing(row)
//                }
    }

    internal inner class TextLayouter(private val txt: Text) : Layouter {
        private var idx = 0

        override fun hasMore(): Boolean = idx < txt.text.length

        override fun getSomething(maxWidth: Float): ContTerm {
            if (maxWidth < 0) {
                throw IllegalArgumentException("Illegal negative width: " + maxWidth)
            }
            val rowIdx = tryGettingText(maxWidth, idx, txt)
            idx = rowIdx.idx
            return rowIdx.toContTerm()
        }

        override fun getIfFits(remainingWidth: Float): ContTermNone {
            if (remainingWidth <= 0) {
                return None
            }
            val ctri = tryGettingText(remainingWidth, idx, txt)
            val row = ctri.row
            return if (row.xyDim.width <= remainingWidth) {
                idx = ctri.idx
                if (ctri.foundCr) {
                    Terminal(row)
                } else {
                    Continuing(row)
                }
            } else {
                None
            }
        }
    }

    companion object {

        fun of(style: TextStyle, text: String?): Text =
                if (text == null) {
                    Text(style, "")
                } else {
                    Text(style, text)
                }

        private val CR = '\n'

        private fun substrNoLeadingWhitespace(text: String, startIdx: Int): String {
            var tempStartIdx = startIdx
            // Drop any opening whitespace.
            while (tempStartIdx < text.length && Character.isWhitespace(text[tempStartIdx])) {
                tempStartIdx++
            }
            if (tempStartIdx > 0) {
                return text.substring(tempStartIdx)
            }
            return text
        }

        internal fun substrNoLeadingSpaceUntilRet(text: String, origStartIdx: Int): Thing {
            var veryBeginningIdx = origStartIdx
            var startIdx = origStartIdx
            // Drop any opening whitespace.
            while (startIdx < text.length && Character.isWhitespace(text[startIdx])) {
                startIdx++
            }
            var crIdx = text.indexOf(CR, startIdx)
            var foundCr = true
            if (crIdx < 0) {
                crIdx = text.length
                foundCr = false
            } else {
                // decrement here effectively adds one to the total length to consume the CR.
                veryBeginningIdx--
            }
            val charsConsumed = crIdx - veryBeginningIdx
            return Thing(text.substring(startIdx, crIdx), charsConsumed, foundCr)

            //        return xformChars(text)
            //                       .drop(startIdx)
            //                       .dropWhile(Character::isWhitespace)
            //                       .takeWhile(c -> CR != c)
            //                       .fold(new StringBuilder(), StringBuilder::append)
            //                       .toString();
        }

        internal fun tryGettingText(maxWidth: Float, startIdx: Int, txt: Text): RowIdx {
            if (maxWidth < 0) {
                throw IllegalArgumentException("Can't meaningfully wrap text with a negative width: " + maxWidth)
            }
            val row = txt.text() //PdfLayoutMgr.convertJavaStringToWinAnsi(txt.text());
            if (row.length <= startIdx) {
                throw IllegalStateException("text length must be greater than startIdx")
            }

            // String text = substrNoLeadingWhitespace(row, startIdx);
            val thing = substrNoLeadingSpaceUntilRet(row, startIdx)
            val text = thing.trimmedStr

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
                // Find last non-whatespace character before whitespace run.
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
            val eolIdx = substr.indexOf(char=CR)
            if (eolIdx > -1) {
                substr = substr.substring(0, eolIdx)
                strWidth = txt.textStyle.stringWidthInDocUnits(substr)
                if (strWidth > maxWidth) {
                    throw IllegalStateException("strWidth=$strWidth > maxWidth=$maxWidth")
                }
                return RowIdx(WrappedRow(substr, strWidth, txt.textStyle), idx + startIdx + 1, true)
            }
            return RowIdx(WrappedRow(substr, strWidth, txt.textStyle), idx + startIdx + 1, false)
        }
    }

}
