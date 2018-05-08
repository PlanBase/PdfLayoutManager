package com.planbase.pdf.layoutmanager.lineWrapping

//import kotlin.test.assertEquals
import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManualllyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.*
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import org.apache.pdfbox.pdmodel.common.PDRectangle.*
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertTrue

class MultiLineWrappedTest {
    private val floatCloseEnough = 0.000004

    @Test fun testLine() {
        val tStyle1 = TextStyle(PDType1Font.TIMES_ROMAN, 60.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.TIMES_BOLD, 100.0, CMYK_BLACK)
        val txt2 = Text(tStyle2, "gruel ")
        val txt3 = Text(tStyle1, "world!")
        val wrappedText = MultiLineWrapped()
//        println("txt1.style().lineHeight(): " + txt1.style().lineHeight())
        wrappedText.append(txt1.lineWrapper().getSomething(999.0).item)
        assertEquals(tStyle1.lineHeight, wrappedText.dim.height, floatCloseEnough)
        assertEquals(tStyle1.ascent, wrappedText.ascent, 0.0)

        wrappedText.append(txt2.lineWrapper().getSomething(999.0).item)
        assertEquals(tStyle2.lineHeight, wrappedText.dim.height, floatCloseEnough)
        assertEquals(tStyle2.ascent, wrappedText.ascent, 0.0)

        wrappedText.append(txt3.lineWrapper().getSomething(999.0).item)
        assertEquals(tStyle2.lineHeight, wrappedText.dim.height, floatCloseEnough)
        assertEquals(tStyle2.ascent, wrappedText.ascent, 0.0)

        // This is for the baseline!
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val yTop = lp.yBodyTop()
//        println("yBodyTop=$yTop")
        val yBottom = yTop - tStyle2.lineHeight
        val yBaseline = yTop - tStyle2.ascent

        val ascentDiff = tStyle2.ascent - tStyle1.ascent
        val top2 = yTop - ascentDiff
//        println("ascentDiff=$ascentDiff")
        val upperLeft = Coord(40.0, yTop)
        lp.drawLine(Coord(0.0, yTop), Coord(lp.pageWidth(), yTop), LineStyle(RGB_BLACK, 0.125), true)
        lp.drawLine(Coord(0.0, top2), Coord(lp.pageWidth(), top2), LineStyle(PDColor(floatArrayOf(0.5f, 0.5f, 0.5f), PDDeviceRGB.INSTANCE), 0.125), true)
        lp.drawLine(Coord(0.0, yBaseline), Coord(lp.pageWidth(), yBaseline), LineStyle(RGB_BLACK, 0.125), true)
        lp.drawLine(Coord(0.0, yBottom), Coord(lp.pageWidth(), yBottom), LineStyle(RGB_BLACK, 0.125), true)
        val dap:DimAndPageNums = wrappedText.render(lp, upperLeft)
//        println("tStyle1=$tStyle1")
//        println("tStyle2=$tStyle2")
        assertEquals(wrappedText.dim, dap.dim)

        pageMgr.commit()
//        val os = FileOutputStream("multiLineBaseline.pdf")
//        pageMgr.save(os)
    }

//    @Ignore

    fun verifyLine(line: MultiLineWrapped, lineHeight:Double, maxWidth:Double, text:String) {
//        println("line: " + line)
        assertEquals(lineHeight, line.dim.height, floatCloseEnough)
        assertTrue(line.width < maxWidth)
        assertEquals(text,
                     line.items
                             .fold(StringBuilder(),
                                   {acc, item -> acc.append((item as Text.WrappedText).string)})
                             .toString())
    }

    @Test fun testRenderablesToLines() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.375, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13.54166671, CMYK_BLACK)
        val txt2 = Text(tStyle2, "there ")
        val txt3 = Text(tStyle1, "world! This is great stuff.")
        val maxWidth = 60.0

        val wrappedLines: List<MultiLineWrapped> = wrapLines(listOf(txt1, txt2, txt3), maxWidth)
//        println(wrappedLines)

        assertEquals(3, wrappedLines.size)

        verifyLine(wrappedLines[0], tStyle2.lineHeight, maxWidth, "Hello there")

        verifyLine(wrappedLines[1], tStyle1.lineHeight, maxWidth, "world! This is")

        verifyLine(wrappedLines[2], tStyle1.lineHeight, maxWidth, "great stuff.")
    }

    @Test fun testRenderablesToLines2() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13.0, CMYK_BLACK)
        val txt2 = Text(tStyle2, "there ")
        val txt3 = Text(tStyle1, "world! This is great stuff.")
        val maxWidth = 90.0

        val wrappedLines: List<MultiLineWrapped> = wrapLines(listOf(txt1, txt2, txt3), maxWidth)
//        println(wrappedLines)

        assertEquals(2, wrappedLines.size)

        verifyLine(wrappedLines[0], tStyle2.lineHeight, maxWidth, "Hello there world!")

        verifyLine(wrappedLines[1], tStyle1.lineHeight, maxWidth, "This is great stuff.")
    }

    @Test fun testRenderablesToLines3() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello there world! This is great stuff.")
        val maxWidth = 300.0

        val wrappedLines: List<MultiLineWrapped> = wrapLines(listOf(txt1), maxWidth)
//        println(wrappedLines)

        assertEquals(1, wrappedLines.size)

        verifyLine(wrappedLines[0], tStyle1.lineHeight, maxWidth, "Hello there world! This is great stuff.")
    }


    @Test fun testRenderablesToLinesTerminal() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello\nthere world! This\nis great stuff.")
        // This is 300 just like previous test, showing this can fit on one line
        // So we know the line breaks are due to the \n characters.
        val maxWidth = 300.0

        val wrappedLines: List<MultiLineWrapped> = wrapLines(listOf(txt1), maxWidth)
//        println(wrappedLines)

        assertEquals(3, wrappedLines.size)

        verifyLine(wrappedLines[0], tStyle1.lineHeight, maxWidth, "Hello")
        verifyLine(wrappedLines[1], tStyle1.lineHeight, maxWidth, "there world! This")
        verifyLine(wrappedLines[2], tStyle1.lineHeight, maxWidth, "is great stuff.")
    }

    @Test fun testRenderablesToLinesTerminal2() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello\nthere world! This\nis great stuff.\n")
        // This is 300 just like previous test, showing this can fit on one line
        // So we know the line breaks are due to the \n characters.
        val maxWidth = 300.0

        val wrappedLines: List<MultiLineWrapped> = wrapLines(listOf(txt1), maxWidth)
//        println(wrappedLines)

        assertEquals(4, wrappedLines.size)

//        println("line3: " + wrappedLines[3])

        verifyLine(wrappedLines[0], tStyle1.lineHeight, maxWidth, "Hello")
        verifyLine(wrappedLines[1], tStyle1.lineHeight, maxWidth, "there world! This")
        verifyLine(wrappedLines[2], tStyle1.lineHeight, maxWidth, "is great stuff.")
        // Additional blank line has same height as previous one.
        verifyLine(wrappedLines[3], tStyle1.lineHeight, maxWidth, "")
    }

    @Test fun testRenderablesToLinesMultiReturn() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, " Hello    \n\n\n  there world! This\n\nis great stuff.     \n\n")
        // This is 300 just like previous test, showing this can fit on one line
        // So we know the line breaks are due to the \n characters.
        val maxWidth = 300.0

        val wrappedLines: List<MultiLineWrapped> = wrapLines(listOf(txt1), maxWidth)
//        println(wrappedLines)

        assertEquals(8, wrappedLines.size)
//        println("line3: " + wrappedLines[3])

        verifyLine(wrappedLines[0], tStyle1.lineHeight, maxWidth, " Hello")
        verifyLine(wrappedLines[1], tStyle1.lineHeight, maxWidth, "")
        verifyLine(wrappedLines[2], tStyle1.lineHeight, maxWidth, "")
        verifyLine(wrappedLines[3], tStyle1.lineHeight, maxWidth, "  there world! This")
        verifyLine(wrappedLines[4], tStyle1.lineHeight, maxWidth, "")
        verifyLine(wrappedLines[5], tStyle1.lineHeight, maxWidth, "is great stuff.")
        verifyLine(wrappedLines[6], tStyle1.lineHeight, maxWidth, "")
        verifyLine(wrappedLines[7], tStyle1.lineHeight, maxWidth, "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRenderablesToLinesEx() {
        wrapLines(listOf(), -1.0)
    }

    @Test fun testStuff() {
        // See: TextTest.testExactLineWrapping()
        val text = Text(BULLET_TEXT_STYLE, "months showed the possible money and")
        assertEquals(214.104, text.maxWidth(), 0.0)

        val wrapped2:List<MultiLineWrapped> = wrapLines(listOf(text), 213.0) //212.63782)
//        println("\nwrapped2: $wrapped2")
        assertEquals(2, wrapped2.size)
    }
}