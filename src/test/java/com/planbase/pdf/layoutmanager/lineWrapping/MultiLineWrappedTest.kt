package com.planbase.pdf.layoutmanager.lineWrapping

//import kotlin.test.assertEquals
import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManual2.Companion.a6PortraitBody
import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.contents.WrappedText
import com.planbase.pdf.layoutmanager.contents.WrappedTextTest
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped.Companion.wrapLines
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER
import org.apache.pdfbox.pdmodel.font.PDFont
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
     * Relies on [com.planbase.pdf.layoutmanager.contents.TextLineWrapperTest.testTooLongWordWrapping] working.
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

    /**
    Relies on [WrappedTextTest.testSpaceBeforeLastWord2]
    This was a long-standing bug where if there were multiple items on a line (MultiLineWrapped) and the last one was
    text, and there was room left for one more item on the line, but only by removing the space before that item, it
    would nuke the last space before the last word.  This showed up in Chapter 3 of Alice which this test is taken
    from.
     */
    @Test fun testSpaceBeforeLastWord() {

        val titleFont: PDFont = PDType1Font.TIMES_ROMAN

        val incipit = TextStyle(titleFont, 36.0, CMYK_BLACK)
        val heading = TextStyle(titleFont, 16.0, CMYK_BLACK)

        // Bug: there's no space between "a" and "Long".
        // Width to show bug: Min: 207.9521 Max: 211.9519
        // Difference: 3.9998
        // Width of a space in right-hand font=4.0
//        println("space=" + heading.stringWidthInDocUnits(" "))
        val wrappedItems: List<LineWrapped> = wrapLines(listOf(Text(incipit, "3. "),
                                                               Text(heading, "A Caucus-Race and a Long Tale")), 210.0)

//        println("wrappedItems=$wrappedItems")
        TestCase.assertEquals(2, wrappedItems.size) // 2 lines

        // first line has 2 items: tne number and "A Caucus-Race and a"
        val firstLine:List<LineWrapped> = wrappedItems[0].items()
//        println("firstLine=$firstLine")

        // If this fails with 3 lines here, it's probably due to a missing space between "a" and "Long".
        // That's the bug this is designed to prevent regression of.  They fit without the space, but you can't just
        // drop the space.  See if WrappedTextTest.testSpaceBeforeLastWord2() fails too.
        TestCase.assertEquals(2, firstLine.size)

        TestCase.assertEquals("3. ", (firstLine[0] as WrappedText).string)
        TestCase.assertEquals("A Caucus-Race and a", (firstLine[1] as WrappedText).string)

        val secondLine:List<LineWrapped> = wrappedItems[1].items()
        TestCase.assertEquals(1, secondLine.size)
        TestCase.assertEquals("Long Tale", (secondLine[0] as WrappedText).string)

//        lp.add(Coord(0.0, a6PortraitBody.topLeft.y), wrappedCell)
//        pageMgr.commit()
//        pageMgr.save(FileOutputStream("spaceBeforeLastWord.pdf"))
    }

    val textSize = 8.1
    val tsRegular = TextStyle(PDType1Font.TIMES_ROMAN, textSize, CMYK_BLACK, "tsRegular")
    val tsBold = TextStyle(PDType1Font.TIMES_BOLD, textSize, CMYK_BLACK, "tsBold")

    @Test
    fun spaceRemovedFromEndOfLine() {
        // This width is enough for the space at the end of the regular text, but
        // not long enough for the bold word "Improvement".  This test makes sure
        // That the final space is removed because it looks incredibly ugly with
        // justfied text otherwise.
        val wrappedLines: List<LineWrapped> =
                wrapLines(listOf(
                        Text(tsRegular, "thinking. The first basic pattern is called the "),
                        Text(tsBold, "Improvement")
                ), 189.69)

        assertEquals(2, wrappedLines.size)
        val wrapped1: WrappedText = wrappedLines[0] as WrappedText
        // Show that the final space is truncated.
        assertEquals("thinking. The first basic pattern is called the", wrapped1.string)
        assertEquals(142.6329, wrapped1.width, 0.0005)
        // This test should be paired with the following...
    }

    @Test
    fun spacePreservedAtEol() {
        // This test should be paired with the previous

        // This width is enough for the the bold word "Improvement".  This test makes sure
        // That the final space is before "Improvement" is *preserved*.
        val wrappedLines: List<LineWrapped> =
                wrapLines(listOf(
                        Text(tsRegular, "thinking. The first basic pattern is called the "),
                        Text(tsBold, "Improvement")
                ), 200.0)

        assertEquals(1, wrappedLines.size)
        val multiWrapped: LineWrapped = wrappedLines[0]

        assertEquals(2, multiWrapped.items().size)
        val wrapped1 = multiWrapped.items()[0] as WrappedText
        // Show that the final space is preserved (since there's another word on the same line).
        assertEquals("thinking. The first basic pattern is called the ", wrapped1.string)
        assertEquals(144.6579, wrapped1.width, 0.0005)
    }

    @Test fun showedErroneousExtraLineBelow() {
        // This should fit two lines, with no extra line below.
        // This test is the same as the above, with a shorter maximum width.
        // There was a bug where it added a blank line.
        val maxWidth = 189.69
        val wrappedLines = wrapLines(listOf(Text(tsRegular, "thinking. The first basic pattern is called the "),
                                            Text(tsBold, "Improvement")), maxWidth)

//        println("wrappedLines=$wrappedLines")
        assertEquals(2, wrappedLines.size)
        val totalWidth = wrappedLines.sumByDouble { it.dim.width }
        // Proves it has to be two lines...
        assertTrue(totalWidth > maxWidth)
        assertEquals(189.8721, totalWidth, 0.0005)

        val multiWrapped1: LineWrapped = wrappedLines[0]
        assertEquals(1, multiWrapped1.items().size)
        val wrapped1 = multiWrapped1.items()[0] as WrappedText
        // Show that the final space is removed since it ends the line.
        assertEquals("thinking. The first basic pattern is called the", wrapped1.string)
        assertEquals(142.6329, wrapped1.width, 0.0005)

        val multiWrapped2: LineWrapped = wrappedLines[1]
        assertEquals(1, multiWrapped2.items().size)
        val wrapped2 = multiWrapped2.items()[0] as WrappedText
        // Show that the final space is removed since it ends the line.
        assertEquals("Improvement", wrapped2.string)
        assertEquals(47.2392, wrapped2.width, 0.0005)
    }

}