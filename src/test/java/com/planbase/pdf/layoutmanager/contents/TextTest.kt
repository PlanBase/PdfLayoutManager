package com.planbase.pdf.layoutmanager.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManualllyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Text.Companion.cleanStr
import com.planbase.pdf.layoutmanager.contents.Text.RowIdx
import com.planbase.pdf.layoutmanager.lineWrapping.ConTerm
import com.planbase.pdf.layoutmanager.lineWrapping.ConTermNone
import com.planbase.pdf.layoutmanager.lineWrapping.Continuing
import com.planbase.pdf.layoutmanager.lineWrapping.None
import com.planbase.pdf.layoutmanager.lineWrapping.Terminal
import com.planbase.pdf.layoutmanager.pages.HeightAndPage
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import kotlin.math.nextDown
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextTest {
    val tStyle = TextStyle(PDType1Font.HELVETICA, 9.375, CMYK_BLACK)

    @Test fun testText() {
        val txt = Text(tStyle, "This is a long enough line of text.")
        var ri : RowIdx = Text.tryGettingText(50.0, 0, txt)
        assertFalse(ri.foundCr)
        val wrappedText: WrappedText = ri.row
        var idx = ri.idx
        assertEquals("This is a", wrappedText.string)
        assertEquals(10, idx)
        assertEquals(34.903126, wrappedText.dim.width, 0.00001)
        assertEquals(tStyle.ascent, wrappedText.ascent)
//        assertEquals(tStyle.descent() + tStyle.leading(), wrappedText.descentAndLeading)
        assertEquals(tStyle.lineHeight, wrappedText.dim.height)

        ri = Text.tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("long", ri.row.string)
        assertEquals(15, idx)

        ri = Text.tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = Text.tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
    }

    @Test fun testTextTerminal() {
        val txt = Text(tStyle, "This is\na long enough line of text.")
        var ri = Text.tryGettingText(50.0, 0, txt)
        assertTrue(ri.foundCr)
        val wrappedText: WrappedText = ri.row
        var idx = ri.idx
        assertEquals("This is", wrappedText.string)
        assertEquals(8, idx)
        assertEquals(27.084375, wrappedText.dim.width, 0.00000001)
        assertEquals(tStyle.ascent, wrappedText.ascent)
//        assertEquals(tStyle.descent() + tStyle.leading(), wrappedText.descentAndLeading)
        assertEquals(tStyle.lineHeight, wrappedText.dim.height)

        ri = Text.tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("a long", ri.row.string)
        assertEquals(15, idx)

        ri = Text.tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = Text.tryGettingText(50.0, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
    }

    @Test fun testTryGettingText() {
        assertEquals("This is a", Text.tryGettingText(50.0, 0, Text(tStyle, "This is a test")).row.string)
        assertEquals("This is an", Text.tryGettingText(50.0, 0, Text(tStyle, "This is an example")).row.string)
        assertEquals("This is anl", Text.tryGettingText(50.0, 0, Text(tStyle, "This is anl example")).row.string)
        assertEquals("This is anll", Text.tryGettingText(50.0, 0, Text(tStyle, "This is anll example")).row.string)
        assertEquals("This is anlll", Text.tryGettingText(50.0, 0, Text(tStyle, "This is anlll example")).row.string)
        assertEquals("This is anllll", Text.tryGettingText(50.0, 0, Text(tStyle, "This is anllll example")).row.string)
        assertEquals("This is", Text.tryGettingText(50.0, 0, Text(tStyle, "This is anlllll example")).row.string)
        assertEquals("This is true", Text.tryGettingText(50.0, 0, Text(tStyle, "This is true / false")).row.string)
        assertEquals("This is true/", Text.tryGettingText(50.0, 0, Text(tStyle, "This is true/false")).row.string)
    }

    @Test fun testTryGettingText2() {
        val txt = Text(tStyle, "This is true/false")
        var ri = Text.tryGettingText(50.0, 0, txt)
        val wrappedText: WrappedText = ri.row
        assertEquals("This is true/", wrappedText.string)

        ri = Text.tryGettingText(50.0, ri.idx, txt)
        assertEquals("false", ri.row.string)
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
        assertEquals(14.816668, row3!!.dim.width, 0.000001)
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
        assertEquals(14.816668, row3!!.dim.width, 0.000001)
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

    @Test fun testCleanStr() {
        assertEquals("\n\nHello\n\n\n   There\nWorld\n\n\n   ",
                     cleanStr("  \n\n\tHello  \n\n\n   There\r\nWorld   \n\n\n   "))
    }

    @Test fun testBaseline() {
        val tStyle1 = TextStyle(PDType1Font.TIMES_ROMAN, 100.0, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hi 'dNlgjpqy,!$")
        val thinLine = LineStyle(RGB_BLACK, 0.125)

        // This is for the baseline!
        val mgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        mgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        mgr.ensurePageIdx(0, letterLandscapeBody)
        val lp = mgr.page(0)
        val margin = 40.0
        val pageDim = mgr.pageDim.swapWh()
        val yTop = pageDim.height - margin
        val pageRightMargin = pageDim.width - margin
        val yBaseline = yTop - tStyle1.ascent
        val yBottom = yTop - tStyle1.lineHeight
        val upperLeft = Coord(margin, yTop)
        lp.drawLine(Coord(margin, yTop), Coord(pageRightMargin, yTop), thinLine, true)
        lp.drawLine(Coord(margin, yBaseline), Coord(pageRightMargin, yBaseline), thinLine, true)
        lp.drawLine(Coord(margin, yBottom), Coord(pageRightMargin, yBottom), thinLine, true)
        val heightAndPage: HeightAndPage = lp.drawStyledText(upperLeft.minusY(tStyle1.ascent), txt1.text, tStyle1, true)
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
}