package com.planbase.pdf.lm2.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManuallyPdfLayoutMgr
import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.attributes.LineStyle
import com.planbase.pdf.lm2.attributes.Orientation
import com.planbase.pdf.lm2.attributes.TextStyle
import com.planbase.pdf.lm2.contents.TextLineWrapper.Companion.testHasMore
import com.planbase.pdf.lm2.contents.TextLineWrapper.Companion.tryGettingText
import com.planbase.pdf.lm2.lineWrapping.*
import com.planbase.pdf.lm2.lineWrapping.MultiLineWrapped.Companion.wrapLines
import com.planbase.pdf.lm2.pages.HeightAndPage
import com.planbase.pdf.lm2.utils.CMYK_BLACK
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import com.planbase.pdf.lm2.utils.RGB_BLACK
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.io.File
import kotlin.math.nextDown
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextLineWrapperTest {
    private val tStyle = TextStyle(PDType1Font.HELVETICA, 9.375, CMYK_BLACK)

    @Test fun testText() {
        val txt = Text(tStyle, "This is a long enough line of text.")
        var ri : RowIdx = tryGettingText(50.0, 0, txt)
        assertFalse(ri.foundCr)
        val wrappedText: WrappedText = ri.row
        var idx = ri.idx
        assertEquals("This is a", wrappedText.string)
        assertEquals(10, idx)
        assertEquals(34.903126, wrappedText.dim.width, 0.00001)
        assertEquals(tStyle.ascent, wrappedText.ascent)
//        assertEquals(tStyle.descent() + tStyle.leading(), wrappedText.descentAndLeading)
        assertEquals(tStyle.lineHeight, wrappedText.dim.height)

        ri = tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("long", ri.row.string)
        assertEquals(15, idx)

        ri = tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
    }

    @Test fun testTextTerminal() {
        val txt = Text(tStyle, "This is\na long enough line of text.")
        var ri = tryGettingText(50.0, 0, txt)
        assertTrue(ri.foundCr)
        val wrappedText: WrappedText = ri.row
        var idx = ri.idx
        assertEquals("This is", wrappedText.string)
        assertEquals(8, idx)
        assertEquals(27.084375, wrappedText.dim.width, 0.00000001)
        assertEquals(tStyle.ascent, wrappedText.ascent)
//        assertEquals(tStyle.descent() + tStyle.leading(), wrappedText.descentAndLeading)
        assertEquals(tStyle.lineHeight, wrappedText.dim.height)

        ri = tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("a long", ri.row.string)
        assertEquals(15, idx)

        ri = tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
    }

    @Test fun testTryGettingText() {
        assertEquals("This is a", tryGettingText(50.0, 0, Text(tStyle, "This is a test")).row.string)
        assertEquals("This is an", tryGettingText(50.0, 0, Text(tStyle, "This is an example")).row.string)
        assertEquals("This is anl", tryGettingText(50.0, 0, Text(tStyle, "This is anl example")).row.string)
        assertEquals("This is anll", tryGettingText(50.0, 0, Text(tStyle, "This is anll example")).row.string)
        assertEquals("This is anlll", tryGettingText(50.0, 0, Text(tStyle, "This is anlll example")).row.string)
        assertEquals("This is anllll", tryGettingText(50.0, 0, Text(tStyle, "This is anllll example")).row.string)
        assertEquals("This is", tryGettingText(50.0, 0, Text(tStyle, "This is anlllll example")).row.string)
        assertEquals("This is true", tryGettingText(50.0, 0, Text(tStyle, "This is true / false")).row.string)
        assertEquals("This is true/", tryGettingText(50.0, 0, Text(tStyle, "This is true/false")).row.string)
    }

    @Test fun testTryGettingText2() {
        val txt = Text(tStyle, "This is true/false")
        var ri = tryGettingText(50.0, 0, txt)
        val wrappedText: WrappedText = ri.row
        assertEquals("This is true/", wrappedText.string)

        ri = tryGettingText(50.0, ri.idx, txt)
        assertEquals("false", ri.row.string)
    }

    /**
     * This was interesting because it hung in production!  Basically, when a word is too big to fit on any line,
     * it has to overflow.  But the index for where to start looking for the next word in the Text string was
     * not updated (it had decremented down to zero checking the long word for a space).  This put
     * MultiLineWrapped.wrapLines() into a loop, adding the same too-long word to every line of wrapped text.
     * The fix was to simply fix the index to be the end of the word in that case.  Surprised it took me so long
     * to test this case, but there you have it.
     */
    @Test fun testTextWrapTooLongWord() {
        val ts = TextStyle(PDType1Font.TIMES_ROMAN, 7.6, CMYK_BLACK)
        val maxWidth = 33.0

        var txt = "foo"
        assertEquals(RowIdx(WrappedText(ts, txt),
                            idx = txt.length + 1, foundCr = false, hasMore = false),
                     tryGettingText(maxWidth, 0, Text(ts, txt)))

        txt = "foobar"
        assertEquals(RowIdx(WrappedText(ts, txt),
                            idx = txt.length + 1, foundCr = false, hasMore = false),
                     tryGettingText(maxWidth, 0, Text(ts, txt)))

        txt = "breakfast"
        assertEquals(RowIdx(WrappedText(ts, txt),
                            idx = txt.length + 1, foundCr = false, hasMore = false),
                     tryGettingText(maxWidth, 0, Text(ts, txt)))

        txt = "breakfasts"
        assertEquals(RowIdx(WrappedText(ts, txt),
                            idx = txt.length + 1, foundCr = false, hasMore = false),
                     tryGettingText(maxWidth, 0, Text(ts, txt)))

        txt = "breakfastss"
        assertEquals(RowIdx(WrappedText(ts, txt),
                            idx = txt.length + 1, foundCr = false, hasMore = false),
                     tryGettingText(maxWidth, 0, Text(ts, txt)))

        txt = "breakfastzzzzzzzzzz,"
        assertEquals(RowIdx(WrappedText(ts, txt),
                            idx = txt.length + 1, foundCr = false, hasMore = true),
                     tryGettingText(maxWidth, 0, Text(ts, "breakfastzzzzzzzzzz, lunch")))

        // Tests the actual bug.
        val txt1 = Text(ts, "breakfastzzzzzzzzzz, lunch")
        val wrappedLines: List<LineWrapped> = wrapLines(listOf(txt1), maxWidth)

        assertEquals(2, wrappedLines.size)
        assertEquals(WrappedText(ts, "breakfastzzzzzzzzzz,"), wrappedLines[0])
        assertEquals(WrappedText(ts, "lunch"), wrappedLines[1])
    }

//    @Test fun testSubstrNoLeadingSpaceUntilRet() {
//        var ret = Text.substrNoLeadingSpaceUntilRet("Hello", 0)
//        assertEquals("Hello", ret.trimmedStr)
//        assertFalse(ret.foundCr)
//        assertEquals(5, ret.totalCharsConsumed)
//
//        ret = Text.substrNoLeadingSpaceUntilRet(" Hello", 0)
//        assertEquals("Hello", ret.trimmedStr)
//        assertFalse(ret.foundCr)
//        assertEquals(6, ret.totalCharsConsumed)
//
//        ret = Text.substrNoLeadingSpaceUntilRet(" Hello\n", 0)
//        assertEquals("Hello", ret.trimmedStr)
//        assertTrue(ret.foundCr)
//        assertEquals(7, ret.totalCharsConsumed)
//
//        ret = Text.substrNoLeadingSpaceUntilRet("  Hello there\n world.", 7)
//        assertEquals("there", ret.trimmedStr)
//        assertTrue(ret.foundCr)
//        assertEquals(7, ret.totalCharsConsumed)
//    }

    @Test fun testRenderator() {
        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8.333334, CMYK_BLACK)
        val txt = Text(tStyle, "This is a long enough line of text.")
        val rend = txt.lineWrapper()
        assertTrue(rend.hasMore())
        val ri: ConTerm = rend.getSomething(40.0)
        assertFalse(ri is Terminal)
        val row = ri.item
        assertEquals(tStyle.ascent, row.ascent)
//        assertEquals(tStyle.descent() + tStyle.leading(), row.descentAndLeading)
        assertEquals(tStyle.lineHeight, row.dim.height)
        assertEquals(28.250002, row.dim.width, 0.000001)

        assertTrue(rend.getIfFits(5.0) is None)

        val ctn: ConTermNone = rend.getIfFits(20.0)
        val row3 = when(ctn) {
            is Continuing -> ctn.item
            is Terminal -> ctn.item
            None -> null
        }
        assertNotNull(row3)
        assertEquals(14.816668, row3.dim.width, 0.000001)
    }

    @Test fun testRenderator2() {
        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8.333334, CMYK_BLACK)
        val txt = Text(tStyle, "This is a long enough line of text.")
        val rend = txt.lineWrapper()
        assertTrue(rend.hasMore())
        val ri: ConTerm = rend.getSomething(40.0)
        assertFalse(ri is Terminal)
        val row = ri.item
        assertEquals(tStyle.ascent, row.ascent)
//        assertEquals(tStyle.descent() + tStyle.leading(), row.descentAndLeading)
        assertEquals(tStyle.lineHeight, row.dim.height)
        assertEquals(28.250002, row.dim.width, 0.000001)

        assertTrue(rend.getIfFits(5.0) is None)

        val ctn: ConTermNone = rend.getIfFits(40.0)
        val row3 = when(ctn) {
            is Continuing -> ctn.item
            is Terminal -> ctn.item
            None -> null
        }
        assertNotNull(row3)
        assertEquals(14.816668, row3.dim.width, 0.000001)
        assertEquals(tStyle.lineHeight, row3.dim.height)
    }

//    @Test fun testCalcDimensions() {
//        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8.0, CMYK_BLACK)
//        val txt = Text.of(tStyle, "This is a long enough line of text.")
//
//        val dim: Dim = txt.calcDimensions(40.0)
//        println(dim)
//        assertEquals(tStyle.lineHeight() * 2, dim.height())
//        assertEquals(28.250002, dim.width())
//    }

    @Test fun testBaseline() {
        val tStyle1 = TextStyle(PDType1Font.TIMES_ROMAN, 100.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hi 'dNlgjpqy,!$")
        val thinLine = LineStyle(RGB_BLACK, 0.125)

        // This is for the baseline!
        val mgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        mgr.startPageGrouping(Orientation.LANDSCAPE, letterLandscapeBody)
        mgr.ensurePageIdx(0, letterLandscapeBody)
        val lp = mgr.page(0)
        val margin = 40.0
        val pageDim = mgr.pageDim.swapWh()
        val yTop = pageDim.height - margin
        val pageRightMargin = pageDim.width - margin
        val yBaseline = yTop - tStyle1.ascent
        val yBottom = yTop - tStyle1.lineHeight
        val upperLeft = Coord(margin, yTop)
        lp.drawLine(Coord(margin, yTop), Coord(pageRightMargin, yTop), thinLine)
        lp.drawLine(Coord(margin, yBaseline), Coord(pageRightMargin, yBaseline), thinLine)
        lp.drawLine(Coord(margin, yBottom), Coord(pageRightMargin, yBottom), thinLine)
        val heightAndPage: HeightAndPage = lp.drawStyledText(upperLeft.minusY(tStyle1.ascent), tStyle1, txt1.text)
        assertEquals(tStyle1.lineHeight, heightAndPage.height)

        mgr.commit()
//        val os = FileOutputStream("textBaseline.pdf")
//        mgr.save(os)
    }

    @Test fun testExactLineWrapping() {
        val text = Text(BULLET_TEXT_STYLE, "months showed the possible money and")
        assertEquals(214.104, text.maxWidth(), 0.0)

        // All should fit
        var cont = text.lineWrapper().getSomething(214.104)
        assertEquals(text.text,
                     (cont.item as WrappedText).string)
        assertEquals(214.104,
                     (cont.item as WrappedText).width)

        // Slightly less should line wrap
        cont = text.lineWrapper().getSomething(214.104.nextDown())
        assertEquals("months showed the possible money",
                     (cont.item as WrappedText).string)
        assertEquals(190.752,
                     BULLET_TEXT_STYLE.stringWidthInDocUnits("months showed the possible money"))
        assertEquals(190.752,
                     (cont.item as WrappedText).width)
    }

    @Test
    fun testMultiLineEndsWithComma() {
        val maxWidth = 138.980
        val txt=Text(TextStyle(PDType1Font.TIMES_ITALIC, 12.0, CMYK_BLACK), "They must go by the carrier- ")
        val wrapper = txt.lineWrapper()
        val conTerm: ConTerm = wrapper.getSomething(maxWidth)

        // This should always be true
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)

        assertEquals("They must go by the carrier-", // Should this have a space at the end of it?
                     (conTerm.item as WrappedText).string)
        assertEquals(136.98, conTerm.item.dim.width, 0.0005)
    }

    /**
     * This has to work in order for [WrappedCellTest.testFirstGuessWayTooSmall] to work
     */
    @Test fun testFirstGuessTooShort() {
        val maxWidth = 190.0
        val txt=Text(TextStyle(PDType1Font.TIMES_ROMAN, 8.0, CMYK_BLACK),
                     "lll,,,lll,,,lll/lll,,/lll,,-englsite-ym.com/resource/resmgr/Standards_Documents/vmstd.pdf")
        val wrapper = txt.lineWrapper()
        var conTerm: ConTerm = wrapper.getSomething(maxWidth)

        // This should always be true
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)

        // This probably won't change, but could be subject to rounding errors or font differences or something.
        assertEquals("lll,,,lll,,,lll/lll,,/lll,,-englsite-ym.com/resource/resmgr/",
                     (conTerm.item as WrappedText).string)
        assertEquals(170.008, conTerm.item.dim.width, 0.0005)

        conTerm = wrapper.getSomething(maxWidth)

        // This should always be true
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)

        // This may be subject to rounding errors or different line-breaking characters
        assertEquals("Standards_Documents/vmstd.pdf",
                     (conTerm.item as WrappedText).string)
        assertEquals(106.44, conTerm.item.dim.width, 0.0005)
    }

    /**
     * This has to work in order for [WrappedCellTest.testFirstGuessWayTooSmall] to work
     */
    @Test fun testFirstGuessTooLong() {
        val maxWidth = 190.0
        val txt=Text(TextStyle(PDType1Font.TIMES_ROMAN, 8.0, CMYK_BLACK),
                     "WWW@@@WWW@@@WWW/WWW@@/WWW@@-engWsite-ym.com/resource/resmgr/Standards_Documents/vmstd.pdf")
        val wrapper = txt.lineWrapper()
        var conTerm: ConTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")

        // This should always be true
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)

        // TODO: Check actual text like test above!

        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")

        // This should always be true
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
    }

    /**
     * This has to work in order for
     * [com.planbase.pdf.lm2.lineWrapping.MultiLineWrappedTest.testTooLongWordWrapping]
     * to be a valid test.
     */
    @Test fun testTooLongWordWrapping() {
        // This tests a too-long line that breaks on a hyphen (not a white-space).
        // It used to adjust the index wrong and always return index=0 and return the first half of the line.
        val maxWidth = 100.0
        val txt = Text(TextStyle(PDType1Font.TIMES_ROMAN, 8.0, CMYK_BLACK),
                       "www.c.ymcdn.com/sites/value-eng.site-ym.com/resource/resmgr/Standards_Documents/vmstd.pdf")

        val wrapper = txt.lineWrapper()
        var conTerm: ConTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")

        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("www.c.ymcdn.com/sites/",
                     (conTerm.item as WrappedText).string)
        assertEquals(81.104, conTerm.item.dim.width, 0.0005)

        conTerm = wrapper.getSomething(maxWidth)
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("value-eng.site-ym.com/",
                     (conTerm.item as WrappedText).string)
        assertEquals(75.544, conTerm.item.dim.width, 0.0005)

        conTerm = wrapper.getSomething(maxWidth)
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("resource/resmgr/",
                     (conTerm.item as WrappedText).string)
        assertEquals(53.76, conTerm.item.dim.width, 0.0005)

        conTerm = wrapper.getSomething(maxWidth)
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("Standards_Documents/",
                     (conTerm.item as WrappedText).string)
        assertEquals(74.216, conTerm.item.dim.width, 0.0005)

        conTerm = wrapper.getSomething(maxWidth)
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("vmstd.pdf",
                     (conTerm.item as WrappedText).string)
        assertEquals(32.224, conTerm.item.dim.width, 0.0005)
    }

    @Ignore // I'm skipping this because the font is only installed on my machine.
    @Test fun testTooLongWordWrapping2() {
        // This tests a too-long line that breaks on a hyphen (not a white-space).
        // It used to adjust the index wrong and always return index=0 and return the first half of the line.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(300.0, 450.0), null)
        val font = pageMgr.loadTrueTypeFont(File("NovaresePro-Book.ttf"))
        val times8pt = TextStyle(font, 8.0, CMYK_BLACK)
//        val times8pt = TextStyle(TIMES_ROMAN, 8.3207190645, CMYK_BLACK)
//        val times8pt = TextStyle(TIMES_ROMAN, 8.34, CMYK_BLACK)
        val maxWidth = 180.0
        val lineWrapper: LineWrapper = Text(times8pt,
                                            "www.c.ymcdn.com/sites/value-eng.site-ym.com/resource/resmgr/Standards_Documents/vmstd.pdf")
                .lineWrapper()

        assertTrue(lineWrapper.hasMore())

        var something : ConTerm = lineWrapper.getSomething(maxWidth)
//        println("something=$something")
        assertTrue(something is Continuing)
        assertTrue(something.item is WrappedText)
        Assert.assertEquals(Continuing(WrappedText(times8pt, "www.c.ymcdn.com/sites/value-eng.site-ym.com/"), true),
                            something)
        Assert.assertEquals(162.928, something.item.dim.width, 0.0005)
        assertTrue(lineWrapper.hasMore())

        something = lineWrapper.getSomething(maxWidth)
//        println("something=$something")
        assertTrue(something is Continuing)
        assertTrue(something.item is WrappedText)
        Assert.assertEquals(Continuing(WrappedText(times8pt, "resource/resmgr/Standards_Documents/vmstd.pdf"), false),
                            something)
        Assert.assertEquals(171.432, something.item.dim.width, 0.0005)
        assertFalse(lineWrapper.hasMore())
    }

    @Ignore // I'm skipping this because the font is only installed on my machine.
    @Test fun testTooLongWordWrapping3() {
        // This tests a too-long line that breaks on a hyphen (not a white-space).
        // It used to adjust the index wrong and always return index=0 and return the first half of the line.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(300.0, 450.0), null)
        val font = pageMgr.loadTrueTypeFont(File("NovaresePro-Book.ttf"))
        val times8pt = TextStyle(font, 8.0, CMYK_BLACK)
//        val times8pt = TextStyle(TIMES_ROMAN, 8.3207190645, CMYK_BLACK)
//        val times8pt = TextStyle(TIMES_ROMAN, 8.34, CMYK_BLACK)
        val maxWidth = 180.0
        val lineWrapper: LineWrapper = Text(times8pt,
                                            "resource/resmgr/Standards_Documents/vmstd.pdf")
                .lineWrapper()

        assertTrue(lineWrapper.hasMore())

        val something : ConTerm = lineWrapper.getSomething(maxWidth)
//        println("something=$something")
        assertTrue(something is Continuing)
        assertTrue(something.item is WrappedText)
        Assert.assertEquals(Continuing(WrappedText(times8pt, "resource/resmgr/Standards_Documents/vmstd.pdf"), false),
                            something)
        Assert.assertEquals(171.432, something.item.dim.width, 0.0005)
        assertFalse(lineWrapper.hasMore())
    }

    /**
     * Note that this needs to work before [com.planbase.pdf.lm2.pages.SinglePageTest] or
     * [com.planbase.pdf.lm2.pages.PageGroupingTest] can work.
     */
    @Test fun quickBrownFox() {
        val maxWidth = 65.0
        val txt = Text(TextStyle(PDType1Font.TIMES_ROMAN, 15.0, RGB_BLACK),
                       "The quick brown fox jumps over the lazy dog")

        val wrapper = txt.lineWrapper()
        var conTerm: ConTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("The quick",
                     (conTerm.item as WrappedText).string)
        assertEquals(60.405, conTerm.item.dim.width, 0.0005)
        assertTrue(wrapper.hasMore())

        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("brown fox",
                     (conTerm.item as WrappedText).string)
        assertEquals(62.07, conTerm.item.dim.width, 0.0005)
        assertTrue(wrapper.hasMore())

        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("jumps",
                     (conTerm.item as WrappedText).string)
        assertEquals(36.675, conTerm.item.dim.width, 0.0005)
        assertTrue(wrapper.hasMore())

        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("over the",
                     (conTerm.item as WrappedText).string)
        assertEquals(48.735, conTerm.item.dim.width, 0.0005)
        assertTrue(wrapper.hasMore())

        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("lazy dog",
                     (conTerm.item as WrappedText).string)
        assertEquals(51.240, conTerm.item.dim.width, 0.0005)
        assertFalse(wrapper.hasMore())
    }

    /**
     * Note that this needs to work before page 3 of [TestManuallyPdfLayoutMgr.testPdf] can work.
     */
    @Test fun ohSayCanYouSee() {
        val maxWidth = 185.0
        val txt = Text(TextStyle(PDType1Font.HELVETICA, 9.5, RGB_BLACK),
                       "O say can you see by the dawn's early light,\n" +
                       "What so proudly we hailed...\n")

        val wrapper = txt.lineWrapper()
        var conTerm: ConTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("O say can you see by the dawn's early",
                     (conTerm.item as WrappedText).string)
        assertEquals(162.336, conTerm.item.dim.width, 0.0005)
        assertTrue(wrapper.hasMore())

        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Terminal)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("light,",
                     (conTerm.item as WrappedText).string)
        assertEquals(20.064, conTerm.item.dim.width, 0.0005)
        assertTrue(wrapper.hasMore())

        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Terminal)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("What so proudly we hailed...",
                     (conTerm.item as WrappedText).string)
        assertEquals(119.3295, conTerm.item.dim.width, 0.0005)
        assertFalse(wrapper.hasMore())
    }

    @Test fun testTwoItemsOnOneLinePreservesSpaceBetween() {
        // Bigger than necessary (text is 66-75 units wide)
        var maxWidth = 300.0

        // Here, wrap "Hello" without a trailing space, to prove that it's roughly 67 units wide
        // so that we can see that adding a trailing space increases the width.
        val helloNoSpace = Text(TextStyle(PDType1Font.TIMES_ROMAN, 30.0, CMYK_BLACK), "Hello")
        val helloNoSpaceWrapper = helloNoSpace.lineWrapper()
        val conNoSpaceTerm: ConTerm = helloNoSpaceWrapper.getSomething(maxWidth)
//        println("conNoSpaceTerm=$conNoSpaceTerm")
        assertTrue(conNoSpaceTerm is Continuing)
        assertTrue(conNoSpaceTerm.item is WrappedText)
        assertTrue(conNoSpaceTerm.item.dim.width <= maxWidth)
        assertEquals("Hello",
                     (conNoSpaceTerm.item as WrappedText).string)
        assertEquals(66.66, conNoSpaceTerm.item.dim.width, 0.0005)
        assertFalse(helloNoSpaceWrapper.hasMore())

        // Same as above, but with a trailing space.
        val helloSpace = Text(TextStyle(PDType1Font.TIMES_ROMAN, 30.0, CMYK_BLACK), "Hello ")
        var helloWrapper = helloSpace.lineWrapper()
        var conTerm: ConTerm = helloWrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("Hello ",
                     (conTerm.item as WrappedText).string)
        assertEquals(74.16, conTerm.item.dim.width, 0.0005)
        // In a sense, this is the essence of this test right here:
        assertTrue(conTerm.item.dim.width > conNoSpaceTerm.item.dim.width)
        assertFalse(helloWrapper.hasMore())

        // Now we have the exact widths:
        // "Hello"  = 66.66
        // "Hello " = 74.16
        // So we know it counts the space in the width.  Let's set maxWidth to just a little bigger than 74.16
        // and see if it still works (it breaks as of 2018-11-02).
//        println("========================================== ABOUT TO BREAK ==========================================")
        maxWidth = 74.161
        helloWrapper = helloSpace.lineWrapper()
        conTerm = helloWrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        assertEquals("Hello ",
                     (conTerm.item as WrappedText).string)
        assertEquals(74.16, conTerm.item.dim.width, 0.0005)
        // In a sense, this is the essence of this test right here:
        assertTrue(conTerm.item.dim.width > conNoSpaceTerm.item.dim.width)
        assertFalse(helloWrapper.hasMore())
    }

    @Test
    fun testLastWordEndsCommaSpace() {
        val timesItalic12 = TextStyle(PDType1Font.TIMES_ITALIC, 12.0, CMYK_BLACK)
        val byCarrierNoSpace = "They must go by the carrier,"
        val byCarrierWithSpace = "They must go by the carrier, "
        assertEquals(135.984, timesItalic12.stringWidthInDocUnits(byCarrierNoSpace))
        assertEquals(138.984, timesItalic12.stringWidthInDocUnits(byCarrierWithSpace))
        // "They must go by the carrier," all fits within the first line.
        // But the bug produced a missing space before the last word "by thecarrier,"
        // Because it somehow walked back from "... the carrier," which fit down to "... the"
        // Then tried again to put "carrier, " on the next line, but it still fit on the current line.
        val rowIdx: RowIdx = tryGettingText(137.64581738281248, 0, Text(timesItalic12, byCarrierWithSpace))
//        println("rowIdx=$rowIdx")

        assertEquals(byCarrierNoSpace, rowIdx.row.string)
        assertEquals(135.984, rowIdx.row.width)
        assertEquals(29, rowIdx.idx)
        assertFalse(rowIdx.foundCr)
    }

    // This is the use case for testLastWordEndsCommaSpace() above.  It's not a *test*, but it's useful to understand
    // the context.
//    @Test
//    fun testBreakBeforeLastWordThatEndsWithAComma() {
//        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
//
//        val bodyCellStyle = CellStyle(Align.TOP_LEFT_JUSTIFY, BoxStyle(Padding(10.0, 0.0, 0.0, 0.0), null,
//                                                                       BorderStyle.NO_BORDERS))
//        val bodyText = TextStyle(PDType1Font.TIMES_ROMAN, 12.0, CMYK_BLACK)
//        val thought = TextStyle(PDType1Font.TIMES_ITALIC, 12.0, CMYK_BLACK)
//
//        val lp = pageMgr.startPageGrouping(
//                Orientation.PORTRAIT,
//                a6PortraitBody,
//                { pageNum:Int, pb: SinglePage ->
//                    val isLeft = pageNum % 2 == 1
//                    val leftMargin:Double = if (isLeft) 37.0 else 45.0
//                    pb.drawStyledText(Coord(leftMargin + (a6PortraitBody.dim.width / 2), 20.0), "$pageNum.",
//                                      TextStyle(PDType1Font.TIMES_ROMAN, 8.0, CMYK_BLACK), true)
//                    leftMargin })
//
//        // The first line should read, "would manage it. They must go by the carrier,"
//        // But the bug produced a missing space before the last word "by thecarrier,"
//        val dap: DimAndPageNums =
//                lp.appendCell(0.0, bodyCellStyle,
//                              listOf(Text(bodyText, "would manage it. "),
//                                     Text(thought, "They must go by the carrier, "),
//                                     Text(bodyText, "she thought; ")))
//
//        println("dap=$dap")
//
//
//        pageMgr.commit()
//
//        // We're just going to write to a file.
//        pageMgr.save(FileOutputStream("spaceBeforeLastWordCommaSpace.pdf"))
//    }

    @Test fun testTestHasMore() {
        assertFalse(testHasMore("Hello", "Hello"))
        assertTrue(testHasMore("Hello", "Hello Z"))
        assertFalse(testHasMore("Hello", "Hello    "))
    }
}