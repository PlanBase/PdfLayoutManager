package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class TextStyleTest {
    val quickBrownFox = "The quick brown fox jumps over the lazy dog"

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

        // TODO: Test character spacing and word spacing!

        val helvetica100 = TextStyle(PDType1Font.HELVETICA, 100f, CMYK_BLACK)
        val basicWidth = 516.7f

        assertEquals(basicWidth, helvetica100.stringWidthInDocUnits("Hello World"))

        assertEquals(basicWidth + 10f,
                     TextStyle(PDType1Font.HELVETICA, 100f, CMYK_BLACK, 100f, 0f, 0f, 10f)
                             .stringWidthInDocUnits("Hello World"))

        assertEquals(basicWidth + ("Hello World".length * 1f),
                     TextStyle(PDType1Font.HELVETICA, 100f, CMYK_BLACK, 100f, 0f, 1f, 0f)
                             .stringWidthInDocUnits("Hello World"))

        val lp = pageMgr.startPageGrouping()
        val page: SinglePage = pageMgr.page(0)


        val times20 = TextStyle(PDType1Font.TIMES_ROMAN, 20f, CMYK_BLACK)
        val leading = times20.lineHeight
        page.drawStyledText(lp.bodyTopLeft().minusY(leading), quickBrownFox, times20)

        page.drawStyledText(lp.bodyTopLeft().minusY(leading * 2), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20f, CMYK_BLACK, 20f, 0f, 1f, 0f))

        page.drawStyledText(lp.bodyTopLeft().minusY(leading * 3), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20f, CMYK_BLACK, 20f, 0f, -1f, 0f))

        page.drawStyledText(lp.bodyTopLeft().minusY(leading * 4), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20f, CMYK_BLACK, 20f, 0f, 0f, 2f))

        page.drawStyledText(lp.bodyTopLeft().minusY(leading * 5), quickBrownFox,
                            TextStyle(PDType1Font.TIMES_ROMAN, 20f, CMYK_BLACK, 20f, 0f, 0f, -2f))

        val helloOff = lp.bodyTopLeft().minusY(leading * 6)
        page.drawStyledText(helloOff, "Hello", times20)
        page.drawStyledText(helloOff.plusX(times20.stringWidthInDocUnits("Hello")), "subscript",
                            TextStyle(PDType1Font.TIMES_ROMAN, 11f, CMYK_BLACK, 11f, -4f, 0f, 0f))

        val stuffOff = helloOff.plusX(times20.stringWidthInDocUnits("hellosubscript"))
        page.drawStyledText(stuffOff, "Stuff", times20)
        page.drawStyledText(stuffOff.plusX(times20.stringWidthInDocUnits("Stuff") + 1f), "superscript",
                            TextStyle(PDType1Font.TIMES_ROMAN, 11f, CMYK_BLACK, 11f, 10f, 0f, 0f))

        lp.commit()
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
//        val ts = TextStyle(PDType1Font.HELVETICA_BOLD, 10f, CMYK_BLACK)
//        println("ts=$ts")
    }
}