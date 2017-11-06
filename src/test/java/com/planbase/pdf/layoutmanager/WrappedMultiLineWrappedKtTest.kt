package com.planbase.pdf.layoutmanager

import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.lineWrapping.WrappedMultiLineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.renderablesToWrappedMultiLineWrappeds
import com.planbase.pdf.layoutmanager.utils.Utils
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Test
//import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WrappedMultiLineWrappedKtTest {
    private val floatCloseEnough = 0.000002f

    @Test fun testLine() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text(tStyle2, "there ")
        val txt3 = Text(tStyle1, "world!")
        val line = WrappedMultiLineWrapped()
//        println("txt1.style().lineHeight(): " + txt1.style().lineHeight())
        line.append(txt1.lineWrapper().getSomething(999f).item)
        assertEquals(tStyle1.lineHeight(), line.xyDim.height, floatCloseEnough)

        line.append(txt2.lineWrapper().getSomething(999f).item)
        assertEquals(tStyle2.lineHeight(), line.xyDim.height, floatCloseEnough)

        line.append(txt3.lineWrapper().getSomething(999f).item)
        assertEquals(tStyle2.lineHeight(), line.xyDim.height, floatCloseEnough)
    }

//    @Ignore

    fun verifyLine(line: WrappedMultiLineWrapped, lineHeight:Float, maxWidth:Float, text:String) {
//        println("line: " + line)
        assertEquals(lineHeight, line.xyDim.height, floatCloseEnough)
        assertTrue(line.width < maxWidth)
        assertEquals(text,
                     line.items
                             .fold(StringBuilder(),
                                   {acc, item -> acc.append((item as Text.WrappedText).string)})
                             .toString())
    }

    @Test fun testRenderablesToLines() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text(tStyle2, "there ")
        val txt3 = Text(tStyle1, "world! This is great stuff.")
        val maxWidth = 60f

        val WrappedMultiLineWrappeds: List<WrappedMultiLineWrapped> = renderablesToWrappedMultiLineWrappeds(listOf(txt1, txt2, txt3), maxWidth)
//        println(WrappedMultiLineWrappeds)

        assertEquals(3, WrappedMultiLineWrappeds.size)

        verifyLine(WrappedMultiLineWrappeds[0], tStyle2.lineHeight(), maxWidth, "Hello there")

        verifyLine(WrappedMultiLineWrappeds[1], tStyle1.lineHeight(), maxWidth, "world! This is")

        verifyLine(WrappedMultiLineWrappeds[2], tStyle1.lineHeight(), maxWidth, "great stuff.")
    }

    @Test fun testRenderablesToLines2() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text(tStyle2, "there ")
        val txt3 = Text(tStyle1, "world! This is great stuff.")
        val maxWidth = 90f

        val WrappedMultiLineWrappeds: List<WrappedMultiLineWrapped> = renderablesToWrappedMultiLineWrappeds(listOf(txt1, txt2, txt3), maxWidth)
//        println(WrappedMultiLineWrappeds)

        assertEquals(2, WrappedMultiLineWrappeds.size)

        verifyLine(WrappedMultiLineWrappeds[0], tStyle2.lineHeight(), maxWidth, "Hello there world!")

        verifyLine(WrappedMultiLineWrappeds[1], tStyle1.lineHeight(), maxWidth, "This is great stuff.")
    }

    @Test fun testRenderablesToLines3() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello there world! This is great stuff.")
        val maxWidth = 300f

        val WrappedMultiLineWrappeds: List<WrappedMultiLineWrapped> = renderablesToWrappedMultiLineWrappeds(listOf(txt1), maxWidth)
//        println(WrappedMultiLineWrappeds)

        assertEquals(1, WrappedMultiLineWrappeds.size)

        verifyLine(WrappedMultiLineWrappeds[0], tStyle1.lineHeight(), maxWidth, "Hello there world! This is great stuff.")
    }


    @Test fun testRenderablesToLinesTerminal() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello\nthere world! This\nis great stuff.")
        // This is 300 just like previous test, showing this can fit on one line
        // So we know the line breaks are due to the \n characters.
        val maxWidth = 300f

        val WrappedMultiLineWrappeds: List<WrappedMultiLineWrapped> = renderablesToWrappedMultiLineWrappeds(listOf(txt1), maxWidth)
//        println(WrappedMultiLineWrappeds)

        assertEquals(3, WrappedMultiLineWrappeds.size)

        verifyLine(WrappedMultiLineWrappeds[0], tStyle1.lineHeight(), maxWidth, "Hello")
        verifyLine(WrappedMultiLineWrappeds[1], tStyle1.lineHeight(), maxWidth, "there world! This")
        verifyLine(WrappedMultiLineWrappeds[2], tStyle1.lineHeight(), maxWidth, "is great stuff.")
    }

    @Test fun testRenderablesToLinesTerminal2() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello\nthere world! This\nis great stuff.\n")
        // This is 300 just like previous test, showing this can fit on one line
        // So we know the line breaks are due to the \n characters.
        val maxWidth = 300f

        val WrappedMultiLineWrappeds: List<WrappedMultiLineWrapped> = renderablesToWrappedMultiLineWrappeds(listOf(txt1), maxWidth)
//        println(WrappedMultiLineWrappeds)

        assertEquals(4, WrappedMultiLineWrappeds.size)

//        println("line3: " + WrappedMultiLineWrappeds[3])

        verifyLine(WrappedMultiLineWrappeds[0], tStyle1.lineHeight(), maxWidth, "Hello")
        verifyLine(WrappedMultiLineWrappeds[1], tStyle1.lineHeight(), maxWidth, "there world! This")
        verifyLine(WrappedMultiLineWrappeds[2], tStyle1.lineHeight(), maxWidth, "is great stuff.")
        // Additional blank line has same height as previous one.
        verifyLine(WrappedMultiLineWrappeds[3], tStyle1.lineHeight(), maxWidth, "")
    }

    @Test fun testRenderablesToLinesMultiReturn() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text(tStyle1, " Hello    \n\n\n  there world! This\n\nis great stuff.     \n\n")
        // This is 300 just like previous test, showing this can fit on one line
        // So we know the line breaks are due to the \n characters.
        val maxWidth = 300f

        val WrappedMultiLineWrappeds: List<WrappedMultiLineWrapped> = renderablesToWrappedMultiLineWrappeds(listOf(txt1), maxWidth)
//        println(WrappedMultiLineWrappeds)

        assertEquals(8, WrappedMultiLineWrappeds.size)
//        println("line3: " + WrappedMultiLineWrappeds[3])

        verifyLine(WrappedMultiLineWrappeds[0], tStyle1.lineHeight(), maxWidth, " Hello")
        verifyLine(WrappedMultiLineWrappeds[1], tStyle1.lineHeight(), maxWidth, "")
        verifyLine(WrappedMultiLineWrappeds[2], tStyle1.lineHeight(), maxWidth, "")
        verifyLine(WrappedMultiLineWrappeds[3], tStyle1.lineHeight(), maxWidth, "  there world! This")
        verifyLine(WrappedMultiLineWrappeds[4], tStyle1.lineHeight(), maxWidth, "")
        verifyLine(WrappedMultiLineWrappeds[5], tStyle1.lineHeight(), maxWidth, "is great stuff.")
        verifyLine(WrappedMultiLineWrappeds[6], tStyle1.lineHeight(), maxWidth, "")
        verifyLine(WrappedMultiLineWrappeds[7], tStyle1.lineHeight(), maxWidth, "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRenderablesToLinesEx() {
        renderablesToWrappedMultiLineWrappeds(listOf(), -1f)
    }
}