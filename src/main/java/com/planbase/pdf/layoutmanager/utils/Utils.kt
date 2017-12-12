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

@file:JvmName("Utils")

package com.planbase.pdf.layoutmanager.utils

import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import java.util.Arrays

@JvmField
val CMYK_BLACK = PDColor(floatArrayOf(0f, 0f, 0f, 1f), PDDeviceCMYK.INSTANCE)
@JvmField
val CMYK_WHITE = PDColor(floatArrayOf(0f, 0f, 0f, 0f), PDDeviceCMYK.INSTANCE)

@JvmField
val RGB_BLACK = PDColor(floatArrayOf(0f, 0f, 0f), PDDeviceRGB.INSTANCE)
@JvmField
val RGB_WHITE = PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)

@JvmField
val BULLET_CHAR = "\u2022"

/** For implementing briefer toString() methods */
fun colorToString(color:PDColor?) =
        when {
            color == null -> "null"
            pdColorEquator(color, CMYK_BLACK) -> "CMYK_BLACK"
            pdColorEquator(color, CMYK_WHITE) -> "CMYK_WHITE"
            pdColorEquator(color, RGB_BLACK) -> "RGB_BLACK"
            pdColorEquator(color, RGB_WHITE) -> "RGB_WHITE"
            else -> {
                if (color.patternName != null) {
                    color.toString()
                } else {
                    var ret = color.colorSpace.toString()
                    if (color.components != null) {
                        ret += color.components.asList()
                    }
                    ret
                }
            }
        }

private fun pdColorEquator(a:PDColor, b:PDColor):Boolean =
        if (a === b) { true }
        else (a.colorSpace == b.colorSpace) &&
             (a.patternName == b.patternName) &&
             Arrays.equals(a.components, b.components)

//    public static String toString(PDColor c) {
//        if (c == null) { return "null"; }
//        return new StringBuilder("#").append(twoDigitHex(c.getRed()))
//                .append(twoDigitHex(c.getGreen()))
//                .append(twoDigitHex(c.getBlue())).toString();
//    }
//    public static String twoDigitHex(int i) {
//        String h = Integer.toHexString(i);
//        return (h.length() < 2) ? "0" + h : h;
//    }
//    public static void println(CharSequence cs) { System.out.println(cs); }
