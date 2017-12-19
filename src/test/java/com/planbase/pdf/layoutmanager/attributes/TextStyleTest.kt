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

        // Natural height
        assertEquals(115.6f, TextStyle(PDType1Font.HELVETICA, 100f, CMYK_BLACK).lineHeight)

        // Should take whatever height we give it!
        assertEquals(50f, TextStyle(PDType1Font.HELVETICA, 100f, CMYK_BLACK, 50f).lineHeight)
        assertEquals(200f, TextStyle(PDType1Font.HELVETICA, 100f, CMYK_BLACK, 200f).lineHeight)

        // Natural heights of other fonts
        assertEquals(119.0f, TextStyle(PDType1Font.HELVETICA_BOLD, 100f, CMYK_BLACK).lineHeight)
        assertEquals(115.6f, TextStyle(PDType1Font.HELVETICA_OBLIQUE, 100f, CMYK_BLACK).lineHeight)
        assertEquals(119.0f, TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 100f, CMYK_BLACK).lineHeight)
        assertEquals(111.6f, TextStyle(PDType1Font.TIMES_ROMAN, 100f, CMYK_BLACK).lineHeight)

        assertEquals(115.3f, TextStyle(PDType1Font.TIMES_BOLD, 100f, CMYK_BLACK).lineHeight)
        assertEquals(110f, TextStyle(PDType1Font.TIMES_ITALIC, 100f, CMYK_BLACK).lineHeight)
        assertEquals(113.9f, TextStyle(PDType1Font.TIMES_BOLD_ITALIC, 100f, CMYK_BLACK).lineHeight)
        assertEquals(105.5f, TextStyle(PDType1Font.COURIER, 100f, CMYK_BLACK).lineHeight)
        assertEquals(105.1f, TextStyle(PDType1Font.COURIER_BOLD, 100f, CMYK_BLACK).lineHeight)
        assertEquals(105.5f, TextStyle(PDType1Font.COURIER_OBLIQUE, 100f, CMYK_BLACK).lineHeight)
        assertEquals(105.1f, TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 100f, CMYK_BLACK).lineHeight)

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