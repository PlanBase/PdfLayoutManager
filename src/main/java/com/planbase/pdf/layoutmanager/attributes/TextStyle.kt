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
import com.planbase.pdf.layoutmanager.utils.fontToStr
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.io.IOException

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
 @param rise "Trise" or "Ts" in the PDF spec, adjusts the baseline positive for superscript or negative for subscript.
 You probably want to use a smaller font for this.
 @param characterSpacing "Tc" in the PDF spec, is the amount of extra space to put between characters
 (negative removes) in unscaled text space units.  Subject to scaling by the Th parameter if the writing mode is
 horizontal.
 @param wordSpacing "Tw" in the PDF spec, is like characterSpacing, but only affects the ASCII SPACE character
 (0x20 or 32 decimal) and is in document units (PdfLayoutMgr emulates this instead of falling through to PDFBox).
 */
/** Specifies font, font-size, and color. */
data class TextStyle(val font: PDFont,    // Tf
                     val fontSize: Double, // Tfs
                     val textColor: PDColor,
                     val lineHeight: Double = defaultLineHeight(font, fontSize),
                     val rise: Double = 0.0,
                     val characterSpacing: Double = 0.0,
                     val wordSpacing: Double = 0.0) {
    constructor(font: PDFont,
                fontSize: Double,
                textColor: PDColor,
                lineHeight: Double) : this(font, fontSize, textColor, lineHeight, 0.0, 0.0, 0.0)

    constructor(font: PDFont,
                fontSize: Double,
                textColor: PDColor) : this(font, fontSize, textColor, defaultLineHeight(font, fontSize))

    /** Average character width (for this font, or maybe guessed) as a positive number in document units */
    val avgCharWidth: Double = avgCharWidth(font, fontSize, characterSpacing)

    // Somewhere it says that font units are 1000 times page units, but my tests with
    // PDType1Font.HELVETICA and PDType1Font.HELVETICA_BOLD from size 5-200 show that 960x is
    // pretty darn good.  If we find a font this doesn't work for, we'll have to adjust.
    private val factor: Double = fontSize / 1000.0

    // Characters look best with the descent size both above and below.  Also acts as a good
    // default leading.
    val ascent = font.fontDescriptor.ascent * fontSize / 1000.0

    fun withCharWordSpacing(cSpace: Double, wSpace:Double) =
            TextStyle(font, fontSize, textColor, lineHeight, rise, characterSpacing + cSpace, wSpace)

    fun withWordSpacing(spacing: Double) =
            TextStyle(font, fontSize, textColor, lineHeight, rise, characterSpacing, spacing)

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
// leading = 0.0
//
// Tmode
// renderingMode = 0 // integer
//
// Tk
// knockout

    override fun toString():String {
        val sB = StringBuilder("TextStyle(").append(fontToStr(font)).append(", ")
                         .append(fontSize).append(", ${colorToString(textColor)}")
        if (defaultLineHeight(font, fontSize) != lineHeight) {
            sB.append(", $lineHeight")
        }
        // We don't have separate constructors for all of these, so show all or none.
        if ( (rise != 0.0) ||
             (characterSpacing != 0.0) ||
             (wordSpacing != 0.0) ) {
            sB.append(", $rise, $characterSpacing, $wordSpacing")
        }
        return sB.append(")").toString()
    }

    /**
     Assumes ISO_8859_1 encoding
     @param text ISO_8859_1 encoded text
     @return the width of this text rendered in this font.
     */
    fun stringWidthInDocUnits(text: String): Double  {
        var ret: Double = try {
            font.getStringWidth(text).toDouble() * factor

        } catch (ioe: IOException) {
            // logger.error("IOException probably means an issue reading font metrics from the underlying" +
            //              "font file used in this PDF");
            // Calculate our default if there's an exception.
            text.length * avgCharWidth
        }
        if (characterSpacing != 0.0) {
            ret += text.length * characterSpacing
        }
        if (wordSpacing != 0.0) {
//            println("ret before wordspacing = $ret   text='$text'  count=${text.count{ it == ' ' }}")
            ret += text.count{ it == ' ' } * wordSpacing
//            println("ret after wordspacing = $ret")
        }
        return ret
    }

    companion object {

        fun avgCharWidth(f : PDFont, sz: Double, csp: Double) : Double {
            var avgFontWidth = 500.0
            try {
                avgFontWidth = f.averageFontWidth.toDouble()
            } catch (ioe: Exception) {
                //throw new IllegalStateException("IOException probably means an issue reading font
                // metrics from the underlying font file used in this PDF", ioe);
                // just use default if there's an exception.
            }

            return (avgFontWidth * sz) + csp
        }

        fun defaultLineHeight(font: PDFont, fontSize: Double) =
                font.fontDescriptor.fontBoundingBox.height * fontSize / 1000.0
    }
}
