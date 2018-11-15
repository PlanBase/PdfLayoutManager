package com.planbase.pdf.layoutmanager.attributes

import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.*
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.CMYK_WHITE
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import java.io.File
import java.io.FileOutputStream
import kotlin.test.Test

class TextStyleTest {
    private val quickBrownFox = "The quick brown fox jumps over the lazy dog"

    @Test fun basics() {

        // Natural height
        assertEquals(115.6, TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK).lineHeight, 0.0)

        // Should take whatever height we give it!
        assertEquals(50.0, TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 50.0).lineHeight, 0.0)
        assertEquals(200.0, TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 200.0).lineHeight, 0.0)

        // Natural heights of other fonts
        assertEquals(119.0, TextStyle(PDType1Font.HELVETICA_BOLD, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(115.6, TextStyle(PDType1Font.HELVETICA_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(119.0, TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(111.6, TextStyle(PDType1Font.TIMES_ROMAN, 100.0, CMYK_BLACK).lineHeight, 0.0)

        assertEquals(115.3, TextStyle(PDType1Font.TIMES_BOLD, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(110.0, TextStyle(PDType1Font.TIMES_ITALIC, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(113.9, TextStyle(PDType1Font.TIMES_BOLD_ITALIC, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.5, TextStyle(PDType1Font.COURIER, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.1, TextStyle(PDType1Font.COURIER_BOLD, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.5, TextStyle(PDType1Font.COURIER_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.1, TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)

        val fontFile = File("target/test-classes/EmilysCandy-Regular.ttf")
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val liberationFont: PDType0Font = pageMgr.loadTrueTypeFont(fontFile)
        assertEquals(125.19531, TextStyle(liberationFont, 100.0, CMYK_BLACK).lineHeight, 0.00001)

        assertEquals(TextStyle(HELVETICA, 100.0, CMYK_BLACK, null, TextStyle.defaultLineHeight(HELVETICA, 100.0)),
                     TextStyle(liberationFont, 100.0, CMYK_BLACK).withFontAndLineHeight(HELVETICA))

        // TODO: Test character spacing and word spacing!

        val helvetica100 = TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK)
        val basicWidth = 516.7

        assertEquals(basicWidth, helvetica100.stringWidthInDocUnits("Hello World"), 0.0)

        assertEquals(basicWidth + 10.0,
                     TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 100.0, 0.0, 0.0, 10.0)
                             .stringWidthInDocUnits("Hello World"), 0.0)

        assertEquals(basicWidth + ("Hello World".length * 1.0),
                     TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 100.0, 0.0, 1.0, 0.0)
                             .stringWidthInDocUnits("Hello World"), 0.0)

        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val page: SinglePage = pageMgr.page(0)


        val times20 = TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK)
        val leading = times20.lineHeight
        page.drawStyledText(lp.body.topLeft.minusY(leading), quickBrownFox, times20)

        page.drawStyledText(lp.body.topLeft.minusY(leading * 2), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, 1.0, 0.0))

        page.drawStyledText(lp.body.topLeft.minusY(leading * 3), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, -1.0, 0.0))

        page.drawStyledText(lp.body.topLeft.minusY(leading * 4), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, 0.0, 2.0))

        page.drawStyledText(lp.body.topLeft.minusY(leading * 5), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, 0.0, -2.0))

        val helloOff = lp.body.topLeft.minusY(leading * 6)
        page.drawStyledText(helloOff, "Hello", times20)
        page.drawStyledText(helloOff.plusX(times20.stringWidthInDocUnits("Hello")), "subscript",
                            TextStyle(PDType1Font.TIMES_ROMAN, 11.0, CMYK_BLACK, null, 11.0, -4.0, 0.0, 0.0))

        val stuffOff = helloOff.plusX(times20.stringWidthInDocUnits("hellosubscript"))
        page.drawStyledText(stuffOff, "Stuff", times20)
        page.drawStyledText(stuffOff.plusX(times20.stringWidthInDocUnits("Stuff") + 1.0), "superscript",
                            TextStyle(PDType1Font.TIMES_ROMAN, 11.0, CMYK_BLACK, null, 11.0, 10.0, 0.0, 0.0))

        pageMgr.commit()
        val os = FileOutputStream("textStyleTest.pdf")
        pageMgr.save(os)


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
//        val ts = TextStyle(PDType1Font.HELVETICA_BOLD, 10.0, CMYK_BLACK)
//        println("ts=$ts")
    }

    @Test fun testToString() {
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_WHITE)",
                     TextStyle(HELVETICA, 11.125, CMYK_WHITE).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_WHITE, 13.5)",
                     TextStyle(HELVETICA, 11.125, CMYK_WHITE, null, 13.5).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_BLACK, 13.5, 0.25, -0.75, 1.0)",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, null, 13.5, 0.25, -0.75, 1.0).toString())
        assertEquals("hello",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, "hello", 13.5, 0.25, -0.75, 1.0).toString())
        assertEquals("hello+cSpace=0.11+wSpace=0.22",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, "hello", 13.5, 0.25, -0.75, 1.0)
                             .withCharWordSpacing(0.11, 0.22).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_BLACK, 13.5, 0.25, 0.11, 0.22)",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, null, 13.5, 0.25, -0.75, 1.0)
                             .withCharWordSpacing(0.11, 0.22).toString())
        assertEquals("hello+wSpace=0.33",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, "hello", 13.5, 0.25, -0.75, 1.0)
                             .withWordSpacing(0.33).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_BLACK, 13.5, 0.25, -0.75, 0.33)",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, null, 13.5, 0.25, -0.75, 1.0)
                             .withWordSpacing(0.33).toString())
    }
}