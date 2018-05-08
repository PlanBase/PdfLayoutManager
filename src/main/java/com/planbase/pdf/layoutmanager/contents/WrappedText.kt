package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.escapeStr

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


    /** Returns the number of literal space characters in this WrappedText */
    fun numSpaces(): Int = string.count { it == ' ' }

    fun withWordSpacing(spacing: Double): WrappedText {
        val newTextStyle = textStyle.withWordSpacing(spacing)
        return WrappedText(newTextStyle, string)
    }

//        override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): DimAndPageNums {
//            = lp.drawStyledText(topLeft.minusY(ascent), string, textStyle, reallyRender)
//                    .dimAndPagesFromWidth(dim)

    // Text rendering calculation spot 1/3
    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): DimAndPageNums {
        //            println("      WrappedText.render(topLeft=$topLeft)")
        val dap:DimAndPageNums =
                lp.drawStyledText(topLeft.minusY(ascent), string, textStyle, reallyRender)
                        .dimAndPagesFromWidth(dim)

        //            println("      => ascent=$ascent dim.height=${dim.height} dap.height=${dap.dim.height} ")
        return dap
    }

    override fun toString() = "WrappedText($textStyle, \"${escapeStr(string)}\", $width)"
}