package com.planbase.pdf.layoutmanager

import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Test

class TextLineKtTest {
    @Test fun testLine() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text.of(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text.of(tStyle2, "there ")
        val txt3 = Text.of(tStyle1, "world!")
        val line = TextLine()
//        println("txt1.style().lineHeight(): " + txt1.style().lineHeight())
        line.append(txt1.layouter().getSomething(999f).item)
        assertEquals(tStyle1.lineHeight(), line.height(), 0.000002f)

        line.append(txt2.layouter().getSomething(999f).item)
        assertEquals(tStyle2.lineHeight(), line.height(), 0.000002f)

        line.append(txt3.layouter().getSomething(999f).item)
        assertEquals(tStyle2.lineHeight(), line.height(), 0.000002f)
    }

//    @Ignore

    @Test fun testRenderablesToLines() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text.of(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text.of(tStyle2, "there ")
        val txt3 = Text.of(tStyle1, "world! This is great stuff.")

        val textLines: List<TextLine> = renderablesToTextLines(listOf(txt1, txt2, txt3), 60f)
//        println(textLines)
        val line1 = textLines[0]
        assertEquals(tStyle2.lineHeight(), line1.height())
    }

//    @Test fun testRenderablesToLines2() {
//        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
//        val txt1 = Text.of(tStyle1, "Hello ")
//        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
//        val txt2 = Text.of(tStyle2, "there ")
//        val txt3 = Text.of(tStyle1, "world! This is great stuff.")
//
//        val textLines: List<TextLine> = renderablesToTextLines(listOf(txt1, txt2, txt3), 50f)
////        println(textLines)
//
//        assertEquals(4, textLines.size)
//
//        val line1 = textLines[0]
//        println("line1: " + line1)
//        assertEquals(tStyle2.lineHeight(), line1.height())
//    }

}