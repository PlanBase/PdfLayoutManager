package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class TextStyleTest {
    @Test fun basics() {
        assertEquals(92.5f, TextStyle(PDType1Font.HELVETICA, 100f, CMYK_BLACK).lineHeight)
        assertEquals(92.5f, TextStyle(PDType1Font.HELVETICA_BOLD, 100f, CMYK_BLACK).lineHeight)
        assertEquals(92.5f, TextStyle(PDType1Font.HELVETICA_OBLIQUE, 100f, CMYK_BLACK).lineHeight)
        assertEquals(92.5f, TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 100f, CMYK_BLACK).lineHeight)
        assertEquals(90.0f, TextStyle(PDType1Font.TIMES_ROMAN, 100f, CMYK_BLACK).lineHeight)
        assertEquals(90.0f, TextStyle(PDType1Font.TIMES_BOLD, 100f, CMYK_BLACK).lineHeight)
        assertEquals(90.0f, TextStyle(PDType1Font.TIMES_ITALIC, 100f, CMYK_BLACK).lineHeight)
        assertEquals(90.0f, TextStyle(PDType1Font.TIMES_BOLD_ITALIC, 100f, CMYK_BLACK).lineHeight)
        assertEquals(78.6f, TextStyle(PDType1Font.COURIER, 100f, CMYK_BLACK).lineHeight)
        assertEquals(78.6f, TextStyle(PDType1Font.COURIER_BOLD, 100f, CMYK_BLACK).lineHeight)
        assertEquals(78.6f, TextStyle(PDType1Font.COURIER_OBLIQUE, 100f, CMYK_BLACK).lineHeight)
        assertEquals(78.6f, TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 100f, CMYK_BLACK).lineHeight)
//        assertEquals(f, TextStyle(PDType1Font.SYMBOL, 100f, CMYK_BLACK).lineHeight)
//        assertEquals(f, TextStyle(PDType1Font.ZAPF_DINGBATS, 100f, CMYK_BLACK).lineHeight)

        val fontFile = File("target/test-classes/LiberationMono-Bold.ttf")
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val liberationFont: PDType0Font = pageMgr.loadTrueTypeFont(fontFile)
        assertEquals(113.28125f, TextStyle(liberationFont, 100f, CMYK_BLACK).lineHeight)

//        val fd = PDType1Font.HELVETICA_BOLD.fontDescriptor
//        println("fd=${fd}")
//        println("ascent=${fd.ascent}")
//        println("capHeight=${fd.capHeight}")
//        println("descent=${fd.descent}")
//        println("fontBoundingBox=${fd.fontBoundingBox}")
//        println("leading=${fd.leading}")
//        println("maxWidth=${fd.maxWidth}")
//        println("xHeight=${fd.xHeight}")
//        println("stemV=${fd.stemV}")
//        println("stemH=${fd.stemH}")
//
//        val ts = TextStyle(PDType1Font.HELVETICA_BOLD, 10f, CMYK_BLACK)
//        println("ts=$ts")
    }
}