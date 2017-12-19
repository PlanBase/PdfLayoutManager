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
        ----    __----__
        ^     ,'  ."".  `,
        |    /   /    \   \
   Ascent   (   (      )   )
        V    \   \_  _/   /
        ____  `.__ ""  _,'
Descent/          """\ \,.
       \____          '--"




TextLine height = ascent + descent.
 */
/** Specifies font, font-size, and color. */
data class TextStyle(val font: PDFont,    // Tf
                     val fontSize: Float, // Tfs
                     val textColor: PDColor,
                     val lineHeight:Float) {
    constructor(font: PDFont,    // Tf
                fontSize: Float, // Tfs
                textColor: PDColor) : this(font, fontSize, textColor, font.fontDescriptor.fontBoundingBox.height * fontSize / 1000f)

    /** Average character width (for this font, or maybe guessed) as a positive number in document units */
    val avgCharWidth: Float = avgCharWidth(font, fontSize)

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
// Tc is in unscaled text space units (subject to scaling by the Th parameter if the writing mode is horizontal)
// characterSpacing = 0f
//
// Tw
// wordSpacing = 0f
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
                              fontSize + "f, ${colorToString(textColor)}, ${lineHeight}f)"

    /**
     Assumes ISO_8859_1 encoding
     @param text ISO_8859_1 encoded text
     @return the width of this text rendered in this font.
     */
    fun stringWidthInDocUnits(text: String): Float =
            try {
                font.getStringWidth(text) * factor
            } catch (ioe: IOException) {
                // logger.error("IOException probably means an issue reading font metrics from the underlying" +
                //              "font file used in this PDF");
                // Calculate our default if there's an exception.
                text.length * avgCharWidth
            }

    companion object {

        fun avgCharWidth(f : PDFont, sz:Float) : Float {
            var avgFontWidth = 500f
            try {
                avgFontWidth = f.averageFontWidth
            } catch (ioe: Exception) {
                //throw new IllegalStateException("IOException probably means an issue reading font
                // metrics from the underlying font file used in this PDF", ioe);
                // just use default if there's an exception.
            }

            return avgFontWidth * sz
        }
    }
}
