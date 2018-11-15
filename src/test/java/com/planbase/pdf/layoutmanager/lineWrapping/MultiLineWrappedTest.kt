package com.planbase.pdf.layoutmanager.lineWrapping

//import kotlin.test.assertEquals
import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManual2.Companion.a6PortraitBody
import TestManualllyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.contents.WrappedText
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.nextDown
import kotlin.test.assertFalse
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

    fun verifyLine(line: LineWrapped, lineHeight:Double, maxWidth:Double, text:String) {
//        println("line: " + line)
        assertEquals(lineHeight, line.dim.height, floatCloseEnough)
        assertTrue(line.dim.width < maxWidth)
        assertEquals(text,
                     line.items()
                             .fold(StringBuilder(),
                                   {acc, item -> if (item is WrappedText) {
                                       acc.append(item.string)
                                   } else {
                                       acc
                                   }})
                             .toString())
    }

    @Test fun testRenderablesToLines() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.375, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13.54166671, CMYK_BLACK)
        val txt2 = Text(tStyle2, "there ")
        val txt3 = Text(tStyle1, "world! This is great stuff.")
        val maxWidth = 60.0

        val wrappedLines: List<LineWrapped> = wrapLines(listOf(txt1, txt2, txt3), maxWidth)
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

        val wrappedLines: List<LineWrapped> = wrapLines(listOf(txt1, txt2, txt3), maxWidth)
//        println(wrappedLines)

        assertEquals(2, wrappedLines.size)

        verifyLine(wrappedLines[0], tStyle2.lineHeight, maxWidth, "Hello there world!")

        verifyLine(wrappedLines[1], tStyle1.lineHeight, maxWidth, "This is great stuff.")
    }

    @Test fun testRenderablesToLines3() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello there world! This is great stuff.")
        val maxWidth = 300.0

        val wrappedLines: List<LineWrapped> = wrapLines(listOf(txt1), maxWidth)
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

        val wrappedLines: List<LineWrapped> = wrapLines(listOf(txt1), maxWidth)
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

        val wrappedLines: List<LineWrapped> = wrapLines(listOf(txt1), maxWidth)
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

        val wrappedLines: List<LineWrapped> = wrapLines(listOf(txt1), maxWidth)
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

    /**
     * Relies on [com.planbase.pdf.layoutmanager.contents.TextTest.testTooLongWordWrapping] working.
     */
    @Test fun testTooLongWordWrapping() {
        // This tests a too-long line that breaks on a hyphen (not a white-space).
        // It used to adjust the index wrong and always return index=0 and return the first half of the line.
        val times8pt = TextStyle(PDType1Font.TIMES_ROMAN, 8.0, CMYK_BLACK)
        val maxWidth = 130.0
        val lineWrapper: LineWrapper =
                Text(times8pt,
                     "www.c.ymcdn.com/sites/value-eng.site-ym.com/resource/resmgr/Standards_Documents/vmstd.pdf")
                        .lineWrapper()
        assertTrue(lineWrapper.hasMore())

        var something : ConTerm = lineWrapper.getSomething(maxWidth)
        assertTrue(something is Continuing)
        assertTrue(something.item is WrappedText)
        assertEquals(Continuing(WrappedText(times8pt, "www.c.ymcdn.com/sites/value-eng.site-"), hasMore = true),
                     something)
        assertTrue(something.item.dim.width <= maxWidth)
        assertTrue(lineWrapper.hasMore())

        something = lineWrapper.getSomething(maxWidth)
        assertTrue(something is Continuing)
        assertTrue(something.item is WrappedText)
        assertEquals(Continuing(WrappedText(times8pt, "ym.com/resource/resmgr/"), hasMore = true),
                     something)
        assertTrue(something.item.dim.width <= maxWidth)
        assertTrue(lineWrapper.hasMore())

        something = lineWrapper.getSomething(maxWidth)
        assertTrue(something is Continuing)
        assertTrue(something.item is WrappedText)
        assertEquals(Continuing(WrappedText(times8pt, "Standards_Documents/vmstd.pdf"), hasMore = false),
                     something)
        assertTrue(something.item.dim.width <= maxWidth)
        assertFalse(lineWrapper.hasMore())

    }

    @Test(expected = IllegalArgumentException::class)
    fun testRenderablesToLinesEx() {
        wrapLines(listOf(), -1.0)
    }

    @Test fun testStuff() {
        // See: TextTest.testExactLineWrapping()
        val text = Text(BULLET_TEXT_STYLE, "months showed the possible money and")
        assertEquals(214.104, text.maxWidth(), 0.0)

        val wrapped2:List<LineWrapped> = wrapLines(listOf(text), 213.0) //212.63782)
//        println("\nwrapped2: $wrapped2")
        assertEquals(2, wrapped2.size)
    }

    // Here's what this test looks like with approximate boxes around each font:
    //            _____________________________________________________ _____________________________________
    //       ^ ^ |  ____  _                                     _      |/ ^ / / / / / / / / / / / / / / / / /|
    //       | | | |  _ \(_)           /\                      | |     |  | 15.709 difference in ascent. / / |
    // ascent| | | | |_) |_  __ _     /  \   ___  ___ ___ _ __ | |_    |/ v / / / / / / / / / / / / / / / / /|
    // 20.49 | | | |  _ <| |/ _` |   / /\ \ / __|/ __/ _ \ '_ \| __|   |--_-----------_----------------------| ^ ^ascent
    //       | | | | |_) | | (_| |  / ____ \\__ \ (_|  __/ | | | |_ _  | |_) .  _    | \ _  _  _  _ __ _|_   | | |4.781
    //       v | |_|____/|_|\__, |_/_/____\_\___/\___\___|_|_|_|\__(_)_|_|_)_|_(_|___|_/(/_ > (_ (/_| | |_ ._| | v
    //   lineHt| |           __/ |                                   ^ |       __|                         ^ | |
    //   33.48 | |          |___/                    descent = 12.99 v |                                   | | |
    //         v |_____________________________________________________|                                   | | | lineHt
    //           |/ / / / / / / / / / / / / / / / / / / / / / / / /  ^ |                                   | | | 40.0
    //           | / / / / / / / / / / / / / / / / / / / / / / / / / | |                  descent = 35.219 | | |
    //           |/ / / / / / / / / / / / / / / / / / / / / / / / /  | |                                   | | |
    //           | / / / / / / / / /  difference in descent = 22.229 | |                                   | | |
    //           |/ / / / / / / / / / / / / / / / / / / / / / / / /  | |                                   | | |
    //           | / / / / / / / / / / / / / / / / / / / / / / / / / v |                                   v | v
    //           +-----------------------------------------------------+-------------------------------------+
    //
    // Notice:
    //  - The two sections of text are aligned exactly on their baseline.
    //  - The first takes up more space above the baseline by virtue of bing a bigger font.
    //  - The second takes up more space below due to a very large lineHeight value.
    //
    // Raison D'être:
    // This all works dandy when line-wrapped and rendered mid-page.  The problem came at the page break where the
    // "Big Descent" text ended up top-aligned - wrong!  Also the height with the page break should be approximately
    // double the total height (2 * 55.709 = 111.418), but it's returning an even 80.0.
    @Test fun testPageBreakingDiffAscentDescent() {
        val topHeavy = TextStyle(PDType1Font.TIMES_ROMAN, 30.0, CMYK_BLACK, "topHeavy")

        // Verify our font metrics to ensure a careful and accurate test.
        TestCase.assertEquals(20.49, topHeavy.ascent)
        TestCase.assertEquals(33.48, topHeavy.lineHeight)

        val bottomHeavy = TextStyle(PDType1Font.TIMES_ITALIC, 7.0, CMYK_BLACK, "bottomHeavy", 40.0) // ascent=4.781   lineHeight=12

        TestCase.assertEquals(4.781, bottomHeavy.ascent)
        TestCase.assertEquals(40.0, bottomHeavy.lineHeight)

        // We expect the ascent to equal the biggest ascent which is topHeavy.ascent = 20.49.
        // We expect the descent to equal the biggest descent which is
        // bottomHeavy.lineHeight - bottomHeavy.ascent = 35.219
        val biggerDescent = bottomHeavy.lineHeight - bottomHeavy.ascent
        TestCase.assertEquals(35.219, biggerDescent)

        // So the total line height is the maxAscent + maxDescent = topHeavy.ascent + biggerDescent = 55.709
        val combinedLineHeight = topHeavy.ascent + biggerDescent
        TestCase.assertEquals(55.709, combinedLineHeight)

        val multi = MultiLineWrapped(mutableListOf(WrappedText(topHeavy, "Big ascent."),
                                                   WrappedText(bottomHeavy, "Big descent.")))

//        println("multi=$multi")
//        width=167.536, ascent=20.49, lineHeight=55.709,
        val multiWidth = multi.width
//        println("multiWidth=$multiWidth")

        // The bold-italic text showed on the wrong page because the last line wasn't being dealt with as a unit.
        // A total line height is now calculated for the entire MultiLineWrapped when later inline text has a surprising
        // default lineHeight.  This test maybe belongs in MultiLineWrapped, but better here than nowhere.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)

        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), multi.dim, 0.0)

        var ret1:DimAndPageNums

        // Rendered away from the page break, the dimensions are unchanged.
        ret1 = lp.add(Coord(0.0, 300.0), multi)
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), ret1.dim, 0.0)
        assertEquals(300.0 - combinedLineHeight, lp.cursorY, 0.000001)

        ret1 = multi.render(lp, Coord(0.0, 200.0))
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), ret1.dim, 0.0)

        // This doesn't show up in the output, just going to walk closer and closer to the edge of the page
        // without going over.
        ret1 = multi.render(lp, Coord(0.0, 100.0), reallyRender = false)
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), ret1.dim, 0.0)

        val breakPoint: Double = lp.yBodyBottom + combinedLineHeight

        ret1 = multi.render(lp, Coord(0.0, breakPoint + 1.0),
                                  reallyRender = false)
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), ret1.dim, 0.0)

        ret1 = multi.render(lp, Coord(0.0, breakPoint + 0.0001),
                                  reallyRender = false)
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), ret1.dim, 0.0)

        ret1 = multi.render(lp, Coord(0.0, breakPoint + 0.0000001),
                                  reallyRender = false)
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), ret1.dim, 0.0)

        ret1 = multi.render(lp, Coord(0.0, breakPoint),
                                  reallyRender = false)
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight), ret1.dim, 0.0)

//        println("breakPoint=$breakPoint")
        ret1 = multi.render(lp, Coord(0.0, breakPoint.nextDown()),
                            reallyRender = true)

        // My theory is that we need an adjustment that pushes *both* halves of the line onto the next page and
        // that they should still be aligned on a common baseline once they get there.
        // What was actually happening was that they both go to the next page, but they are top-aligned there,
        // and the amount that pushes them there is 80.0 instead of 111.418.
        // So, 80.0 - 55.709 = 24.291
        Dim.assertEquals(Dim(multiWidth, combinedLineHeight * 2.0), ret1.dim, 0.0)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testPgBrkDiffAscDesc.pdf"))
    }

}