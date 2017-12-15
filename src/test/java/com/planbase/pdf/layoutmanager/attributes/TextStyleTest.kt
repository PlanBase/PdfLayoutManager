package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Test

class TextStyleTest {
    @Test fun basics() {
        // TODO: Re-enable and maybe make factorFromFontSize always be 1000.
        // Just testing some default values before potentially merging changes that could make
        // these variable.
//        assertEquals(9.5f, TextStyle(PDType1Font.HELVETICA_BOLD, 9.5f, CMYK_BLACK).lineHeight)
//        assertEquals(9.5f, TextStyle(PDType1Font.TIMES_ROMAN, 9.5f, CMYK_BLACK).lineHeight,
//                     0.00000001f)
//        assertEquals(0.981249988079071f, TextStyle(PDType1Font.COURIER, 12f, CMYK_BLACK).lineHeight,
//                     0.00000001f)
//        assertEquals(0.981249988079071f, TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, CMYK_BLACK).lineHeight,
//                     0.00000001f)
//        assertEquals(0.754687488079071f, TextStyle(PDType1Font.HELVETICA, 7f, CMYK_BLACK).lineHeight,
//                     0.00000001f)
    }

    @Test fun testLeading() {
//        assertEquals(1.0242186784744263f, TextStyle(PDType1Font.HELVETICA_BOLD, 9.5f, CMYK_BLACK).lineHeight,
//                     0.00000001f)
//
//        assertEquals(1.0242186784744263f, TextStyle(PDType1Font.HELVETICA_BOLD, 9.5f, CMYK_BLACK).lineHeight,
//                     0.00000001f)
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

//        assertEquals(10.177864074707031f, TextStyle(PDType1Font.HELVETICA_BOLD, 9.5f, CMYK_BLACK).lineHeight,
//                     0.00000001f)
    }
}