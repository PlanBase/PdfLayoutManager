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

import com.planbase.pdf.layoutmanager.utils.colorToString
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.io.IOException

import org.apache.pdfbox.pdmodel.font.PDFont

/*
 Represents the attributes of some text.
             __
            /           ----    __----__
            |           ^     ,'  ."".  `,
            |           |    /   /    \   \
            |      Ascent   (   (      )   )
lineHeight <            V    \   \_  _/   /
   AKA      |           ____  `.__ ""  _,'
 "leading"  |   Descent/          """\ \,.
            |          \____          '--"
            |
            \__ Extra space here is included in the leading, but is NOT leading!

 @param font "Tf" in the PDF spec, the font
 @param fontSize "Tfs" in the PDF spec, the font size in document units
 @param textColor the color
 @param lineHeight "TL" in the PDF spec, is the distance from baseline of one row of text to baseline of the next in
 document units.  By default this is the height of the bounding box for the font (translated into document units).
 PDF spec calls this "leading."
 @param characterSpacing "Tc" in the PDF spec, is the amount of extra space to put between characters
 (negative removes) in unscaled text space units.  Subject to scaling by the Th parameter if the writing mode is
 horizontal.
 @param wordSpacing "Tw" in the PDF spec, is like characterSpacing, but only affects the ASCII SPACE character
 (0x20 or 32 decimal) and is in document units (PdfLayoutMgr emulates this instead of falling through to PDFBox).
 */
/** Specifies font, font-size, and color. */
data class TextStyle(val font: PDFont,    // Tf
                     val fontSize: Float, // Tfs
                     val textColor: PDColor,
                     val lineHeight:Float,
                     val characterSpacing:Float,
                     val wordSpacing:Float) {
    constructor(font: PDFont,
                fontSize: Float,
                textColor: PDColor,
                lineHeight:Float) : this(font, fontSize, textColor, lineHeight, 0f, 0f)

    constructor(font: PDFont,
                fontSize: Float,
                textColor: PDColor) : this(font, fontSize, textColor, font.fontDescriptor.fontBoundingBox.height * fontSize / 1000f)

    /** Average character width (for this font, or maybe guessed) as a positive number in document units */
    val avgCharWidth: Float = avgCharWidth(font, fontSize, characterSpacing)

    // Somewhere it says that font units are 1000 times page units, but my tests with
    // PDType1Font.HELVETICA and PDType1Font.HELVETICA_BOLD from size 5-200 show that 960x is
    // pretty darn good.  If we find a font this doesn't work for, we'll have to adjust.
    private val factor = fontSize / 1000f

    // Characters look best with the descent size both above and below.  Also acts as a good
    // default leading.
    val ascent = font.fontDescriptor.ascent * fontSize / 1000f

// Below taken from Section 9.3 page 243 of PDF 32000-1:2008
//
// Some of these parameters are expressed in unscaled text space units. This means that they shall be specified
// in a coordinate system that shall be defined by the text matrix, T m but shall not be scaled by the font size
// parameter, T fs.
//
// Th
// horizontalScaling = scale / 100
//
// Tz
// scale = 100
//
// Tl Text leading (aka lineHeight) shall be used only by the T*, ', and " operators.  The vertical distance between the baselines of adjacent lines of text
// leading = 0f
//
// Tmode
// renderingMode = 0 // integer
//
// Trise or Ts adjusts the baseline for superscript (positive) or subscript (negative) effects.
// rise = 0f
//
// Tk
// knockout

    override fun toString() = "TextStyle(\"" + font.toString().replace("PDType1Font", "T1") + "\" " +
                              fontSize + "f, ${colorToString(textColor)}, ${lineHeight}f" +
                              if (characterSpacing != 0f) { ", ${characterSpacing}f" } else { "" } +
                              if (wordSpacing != 0f) { ", ${wordSpacing}f" } else { "" } +
                              ")"

    /**
     Assumes ISO_8859_1 encoding
     @param text ISO_8859_1 encoded text
     @return the width of this text rendered in this font.
     */
    fun stringWidthInDocUnits(text: String): Float  {
        var ret = try {
            font.getStringWidth(text) * factor

        } catch (ioe: IOException) {
            // logger.error("IOException probably means an issue reading font metrics from the underlying" +
            //              "font file used in this PDF");
            // Calculate our default if there's an exception.
            text.length * avgCharWidth
        }
        if (characterSpacing != 0f) {
            ret += text.length * characterSpacing
        }
        if (wordSpacing != 0f) {
//            println("ret before wordspacing = $ret   text='$text'  count=${text.count{ it == ' ' }}")
            ret += text.count{ it == ' ' } * wordSpacing
//            println("ret after wordspacing = $ret")
        }
        return ret
    }

    companion object {

        fun avgCharWidth(f : PDFont, sz:Float, csp: Float) : Float {
            var avgFontWidth = 500f
            try {
                avgFontWidth = f.averageFontWidth
            } catch (ioe: Exception) {
                //throw new IllegalStateException("IOException probably means an issue reading font
                // metrics from the underlying font file used in this PDF", ioe);
                // just use default if there's an exception.
            }

            return (avgFontWidth * sz) + csp
        }
    }
}
