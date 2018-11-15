package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.organicdesign.indented.StringUtils.stringify

/**
 * Represents a unit of wrapped text.  MultiLineWrapped can hold multiple WrappedText or other "Wrapped" objects
 * per line, but a WrappedText only holds one contiguously styled section of one line of text.  A single
 * WrappedText could cover a whole line, but no more.
 *
 * @param textStyle the style
 * @param string the actual text that fits in this line
 * @param width the width of that string in this textStyle
 */
data class WrappedText(val textStyle: TextStyle,
                       val string: String,
                       val width: Double = textStyle.stringWidthInDocUnits(string)) : LineWrapped {

    override val dim = Dim(width, textStyle.lineHeight)

    override val ascent: Double = textStyle.ascent

    fun withoutTrailingSpace(): WrappedText =
            when {
                string.isNotEmpty() && Character.isWhitespace(string.last()) -> WrappedText(textStyle, string.trimEnd())
                else -> this
            }

    /** Returns the number of literal space characters in this WrappedText */
    val numSpaces: Int by lazy { string.count { it == ' ' } }

    fun withWordSpacing(spacing: Double): WrappedText {
        val newTextStyle = textStyle.withWordSpacing(spacing)
        return WrappedText(newTextStyle, string)
    }

//        override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): DimAndPageNums {
//            = lp.drawStyledText(topLeft.minusY(ascent), string, textStyle, reallyRender)
//                    .dimAndPagesFromWidth(dim)

    // Text rendering calculation spot 1/3
    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean,
                        justifyWidth:Double): DimAndPageNums {
        //            println("      WrappedText.render(topLeft=$topLeft)")

        var tempTextStyle = textStyle

        // Text justification calculation 1/2
        // Do we need to justify text?
        if (justifyWidth > 0.0) {
            val contentWidth: Double = dim.width
            // Justified text only looks good if the line is long enough, so only justify when the text covers
            // at least 75% of the line already.  MultiLineWrapped handles justification for lines that contain
            // different fonts, or text and other entities, so it only uses wordSpacing for justification.  But here,
            // we should be dealing with a pure line of text in a single font and can play with the character
            // spacing just enough to minimize the rivers caused by massive spaces between words.
            if (contentWidth > justifyWidth * 0.70) {
                val widthDiff = justifyWidth - contentWidth
                // Character spacing should be less than word spacing.  The ratio of 1:2 character to word
                // seems to look the best to me today, but 2:3 (0.25 and 0.75) looked good as well.
                // I also tried adding more character spacing as the lines got shorter, but the consistent
                // approach was less noticeable to my eye.
                val charSpacing = (widthDiff * 0.333333333) / string.length
                val wordSpacing = (widthDiff * 0.666666667) / numSpaces

                // Second thought, we want this to look as similar to what we're used to as possible.
//                val charSpacing = (widthDiff * 0.25) / string.length
//                val wordSpacing = (widthDiff * 0.75) / numSpaces
                tempTextStyle = textStyle.withCharWordSpacing(charSpacing, wordSpacing)
//                val wordSpacing = widthDiff / numSpaces
//                tempTextStyle = textStyle.withWordSpacing(wordSpacing)
            }
        }

        val dap:DimAndPageNums =
                lp.drawStyledText(topLeft.minusY(ascent), string, tempTextStyle, reallyRender)
                        .dimAndPagesFromWidth(dim)

        //            println("      => ascent=$ascent dim.height=${dim.height} dap.height=${dap.dim.height} ")
        return dap
    }

    override fun toString() = "WrappedText($textStyle, ${stringify(string)}, $width)"
}