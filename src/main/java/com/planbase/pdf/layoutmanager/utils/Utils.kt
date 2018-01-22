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

import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import java.awt.Transparency
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorModel
import java.util.Arrays

@JvmField
val CMYK_BLACK = PDColor(floatArrayOf(0f, 0f, 0f, 1f), PDDeviceCMYK.INSTANCE)
@JvmField
val CMYK_WHITE = PDColor(floatArrayOf(0f, 0f, 0f, 0f), PDDeviceCMYK.INSTANCE)

@JvmField
val RGB_BLACK = PDColor(floatArrayOf(0f, 0f, 0f), PDDeviceRGB.INSTANCE)
@JvmField
val RGB_WHITE = PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)

const val BULLET_CHAR = "\u2022"

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
                    "PDColor(${collectionToStr("floatArrayOf", color.components.asList())}," +
                    " PD${color.colorSpace}.INSTANCE)"
                }
            }
        }

private fun pdColorEquator(a:PDColor, b:PDColor):Boolean =
        if (a === b) { true }
        else (a.colorSpace == b.colorSpace) &&
             (a.patternName == b.patternName) &&
             Arrays.equals(a.components, b.components)

private val colorSpaceStrs =
        mapOf(Pair(ColorSpace.TYPE_XYZ, "TYPE_XYZ"),
              Pair(ColorSpace.TYPE_Lab, "TYPE_Lab"),
              Pair(ColorSpace.TYPE_Luv, "TYPE_Luv"),
              Pair(ColorSpace.TYPE_YCbCr, "TYPE_YCbCr"),
              Pair(ColorSpace.TYPE_Yxy, "TYPE_Yxy"),
              Pair(ColorSpace.TYPE_RGB, "TYPE_RGB"),
              Pair(ColorSpace.TYPE_GRAY, "TYPE_GRAY"),
              Pair(ColorSpace.TYPE_HSV, "TYPE_HSV"),
              Pair(ColorSpace.TYPE_HLS, "TYPE_HLS"),
              Pair(ColorSpace.TYPE_CMYK, "TYPE_CMYK"),
              Pair(ColorSpace.TYPE_CMY, "TYPE_CMY"),
              Pair(ColorSpace.TYPE_2CLR, "TYPE_2CLR"),
              Pair(ColorSpace.TYPE_3CLR, "TYPE_3CLR"),
              Pair(ColorSpace.TYPE_4CLR, "TYPE_4CLR"),
              Pair(ColorSpace.TYPE_5CLR, "TYPE_5CLR"),
              Pair(ColorSpace.TYPE_6CLR, "TYPE_6CLR"),
              Pair(ColorSpace.TYPE_7CLR, "TYPE_7CLR"),
              Pair(ColorSpace.TYPE_8CLR, "TYPE_8CLR"),
              Pair(ColorSpace.TYPE_9CLR, "TYPE_9CLR"),
              Pair(ColorSpace.TYPE_ACLR, "TYPE_ACLR"),
              Pair(ColorSpace.TYPE_BCLR, "TYPE_BCLR"),
              Pair(ColorSpace.TYPE_CCLR, "TYPE_CCLR"),
              Pair(ColorSpace.TYPE_DCLR, "TYPE_DCLR"),
              Pair(ColorSpace.TYPE_ECLR, "TYPE_ECLR"),
              Pair(ColorSpace.TYPE_FCLR, "TYPE_FCLR"))

val transparencyStrs = mapOf(Pair(Transparency.OPAQUE, "OPAQUE"),
                             Pair(Transparency.BITMASK, "BITMASK"),
                             Pair(Transparency.TRANSLUCENT, "TRANSLUCENT"))

fun colorSpaceToStr(cs: ColorSpace):String {
    val type: Int = cs.type
    val typeStr = colorSpaceStrs[type]
    return if (typeStr == null) {
        cs.toString()
    } else {
        "$typeStr"
    }
}

fun colorModelToStr(cm: ColorModel) =
        "ColorModel(" +
//        "${cm.pixelSize}, " +
        "${Arrays.toString(cm.componentSize)}," +
        " ${colorSpaceToStr(cm.colorSpace)}," +
        if (cm.hasAlpha()) { " α," } else { "" } +
//        " preMult=${cm.isAlphaPremultiplied}," +
        " ${transparencyStrs[cm.transparency]}" +
//        ", transIdx=${cm.transferType}" +
        ")"

fun buffImgToStr(bi: BufferedImage) =
        "BufferedImage(${colorModelToStr(bi.colorModel)}," +
        " ${bi.raster.width}x${bi.raster.height}" +
//        " ${bi.raster}," +
//        " preMult=${bi.isAlphaPremultiplied}, " +
        if (bi.propertyNames != null) {
            " HashTable(${bi.propertyNames.map{"$it=${bi.getProperty(it)},"}})"
        } else {
            ""
        } +
        ")"

fun floatToStr(f:Float):String {
    val str = f.toString()
    return if (str.endsWith(".0")) {
        str.substring(0, str.length - 2)
    } else {
        str
    } + "f"
}

fun objToStr(item:Any):String = when (item) {
    is String -> "\"$item\""
    is Char   -> "'$item'"
    is Float  -> floatToStr(item)
    else      -> item.toString()
}

fun collectionToStr(collName: String, ls: Iterable<Any>) =
        ls.fold(StringBuilder(collName).append("("),
                { sB, item ->
                    if (sB.length > collName.length + 1) {
                        sB.append(", ")
                    }
                    sB.append(objToStr(item))
                })
                .append(")")
                .toString()

fun mutableListToStr(ls: List<Any>) = collectionToStr("mutableListOf", ls)

fun listToStr(ls: List<Any>) = collectionToStr("listOf", ls)

private val fontStrs =
        mapOf(Pair(PDType1Font.TIMES_ROMAN, "TIMES_ROMAN"),
              Pair(PDType1Font.TIMES_BOLD, "TIMES_BOLD"),
              Pair(PDType1Font.TIMES_ITALIC, "TIMES_ITALIC"),
              Pair(PDType1Font.TIMES_BOLD_ITALIC, "TIMES_BOLD_ITALIC"),
              Pair(PDType1Font.HELVETICA, "HELVETICA"),
              Pair(PDType1Font.HELVETICA_BOLD, "HELVETICA_BOLD"),
              Pair(PDType1Font.HELVETICA_OBLIQUE, "HELVETICA_OBLIQUE"),
              Pair(PDType1Font.HELVETICA_BOLD_OBLIQUE, "HELVETICA_BOLD_OBLIQUE"),
              Pair(PDType1Font.COURIER, "COURIER"),
              Pair(PDType1Font.COURIER_BOLD, "COURIER_BOLD"),
              Pair(PDType1Font.COURIER_OBLIQUE, "COURIER_OBLIQUE"),
              Pair(PDType1Font.COURIER_BOLD_OBLIQUE, "COURIER_BOLD_OBLIQUE"),
              Pair(PDType1Font.SYMBOL, "SYMBOL"),
              Pair(PDType1Font.ZAPF_DINGBATS, "ZAPF_DINGBATS"))

fun fontToStr(font: PDFont): String = fontStrs[font] ?: "\"$font\""

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
