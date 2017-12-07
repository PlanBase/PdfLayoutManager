package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.contents.Text.Companion.cleanStr
import com.planbase.pdf.layoutmanager.contents.Text.RowIdx
import com.planbase.pdf.layoutmanager.contents.Text.WrappedText
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.ConTerm
import com.planbase.pdf.layoutmanager.lineWrapping.ConTermNone
import com.planbase.pdf.layoutmanager.lineWrapping.Continuing
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.None
import com.planbase.pdf.layoutmanager.lineWrapping.Terminal
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert
import org.junit.Test
import java.io.FileOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextTest {
    @Test fun testText() {
        val tStyle = TextStyle(PDType1Font.HELVETICA, 9f, CMYK_BLACK)
        val txt = Text(tStyle, "This is a long enough line of text.")
        var ri : RowIdx = Text.tryGettingText(50f, 0, txt)
        assertFalse(ri.foundCr)
        val wrappedText: WrappedText = ri.row
        var idx = ri.idx
        assertEquals("This is a", wrappedText.string)
        assertEquals(10, idx)
        assertEquals(34.903126f, wrappedText.dimensions.width)
        assertEquals(tStyle.ascent(), wrappedText.ascent)
        assertEquals(tStyle.descent() + tStyle.leading(), wrappedText.descentAndLeading)
        assertEquals(tStyle.lineHeight(), wrappedText.dimensions.height)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("long", ri.row.string)
        assertEquals(15, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
    }

    @Test fun testTextTerminal() {
        val tStyle = TextStyle(PDType1Font.HELVETICA, 9f, CMYK_BLACK)
        val txt = Text(tStyle, "This is\na long enough line of text.")
        var ri = Text.tryGettingText(50f, 0, txt)
        assertTrue(ri.foundCr)
        val wrappedText: WrappedText = ri.row
        var idx = ri.idx
        assertEquals("This is", wrappedText.string)
        assertEquals(8, idx)
        assertEquals(27.084375f, wrappedText.dimensions.width)
        assertEquals(tStyle.ascent(), wrappedText.ascent)
        assertEquals(tStyle.descent() + tStyle.leading(), wrappedText.descentAndLeading)
        assertEquals(tStyle.lineHeight(), wrappedText.dimensions.height)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("a long", ri.row.string)
        assertEquals(15, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
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
        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8f, CMYK_BLACK)
        val txt = Text(tStyle, "This is a long enough line of text.")
        val rend = txt.lineWrapper()
        assertTrue(rend.hasMore())
        val ri: ConTerm = rend.getSomething(40f)
        assertFalse(ri is Terminal)
        val row = ri.item
        assertEquals(tStyle.ascent(), row.ascent)
        assertEquals(tStyle.descent() + tStyle.leading(),
                row.descentAndLeading)
        assertEquals(tStyle.lineHeight(), row.lineHeight)
        assertEquals(tStyle.lineHeight(), row.dimensions.height)
        assertEquals(28.250002f, row.dimensions.width)

        assertTrue(rend.getIfFits(5f) is None)

        val ctn: ConTermNone = rend.getIfFits(20f)
        val row3 = when(ctn) {
            is Continuing -> ctn.item
            is Terminal -> ctn.item
            None -> null
        }
        assertNotNull(row3)
        assertEquals(14.816668f, row3!!.dimensions.width)
    }

    @Test fun testRenderator2() {
        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8f, CMYK_BLACK)
        val txt = Text(tStyle, "This is a long enough line of text.")
        val rend = txt.lineWrapper()
        assertTrue(rend.hasMore())
        val ri: ConTerm = rend.getSomething(40f)
        assertFalse(ri is Terminal)
        val row = ri.item
        assertEquals(tStyle.ascent(), row.ascent)
        assertEquals(tStyle.descent() + tStyle.leading(),
                     row.descentAndLeading)
        assertEquals(tStyle.lineHeight(), row.lineHeight)
        assertEquals(tStyle.lineHeight(), row.dimensions.height)
        assertEquals(28.250002f, row.dimensions.width)

        assertTrue(rend.getIfFits(5f) is None)

        val ctn: ConTermNone = rend.getIfFits(40f)
        val row3 = when(ctn) {
            is Continuing -> ctn.item
            is Terminal -> ctn.item
            None -> null
        }
        assertNotNull(row3)
        assertEquals(14.816668f, row3!!.dimensions.width)
        assertEquals(tStyle.lineHeight(), row3.dimensions.height)
    }

//    @Test fun testCalcDimensions() {
//        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8f, CMYK_BLACK)
//        val txt = Text.of(tStyle, "This is a long enough line of text.")
//
//        val dim: Dimensions = txt.calcDimensions(40f)
//        println(dim)
//        assertEquals(tStyle.lineHeight() * 2, dim.height())
//        assertEquals(28.250002f, dim.width())
//    }

    @Test fun testCleanStr() {
        assertEquals("\n\nHello\n\n\n   There\nWorld\n\n\n   ",
                     cleanStr("  \n\n\tHello  \n\n\n   There\r\nWorld   \n\n\n   "))
    }

    @Test fun testBaseline() {
        val tStyle1 = TextStyle(PDType1Font.TIMES_ROMAN, 100f, CMYK_BLACK)
        val txt1 = Text(tStyle1, "Hello dNgjpqy")
        val thinLine = LineStyle(RGB_BLACK, 0.125f)

        // This is for the baseline!
        val mgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val logicalPage = mgr.logicalPageStart()
        mgr.ensurePageIdx(0)
        val lp = mgr.page(0)
        val margin = 40f
        val pageDim = mgr.pageDim.swapWh()
        val yTop = pageDim.height - margin
        val pageRightMargin = pageDim.width - margin
        val yBaseline = yTop - tStyle1.ascent()
        val yBottom = yTop - (tStyle1.lineHeight() - tStyle1.leading())
        val upperLeft = Coord(margin, yTop)
        lp.drawLine(Coord(margin, yTop), Coord(pageRightMargin, yTop), thinLine, true)
        lp.drawLine(Coord(margin, yBaseline), Coord(pageRightMargin, yBaseline), thinLine, true)
        lp.drawLine(Coord(margin, yBottom), Coord(pageRightMargin, yBottom), thinLine, true)
        lp.drawStyledText(upperLeft.minusY(tStyle1.ascent()), txt1.text, tStyle1, true)

        // TODO: Figure this out!
//        assertEquals(Coord(upperLeft.x + line.width, upperLeft.y - tStyle2.lineHeight()), xyOff)

        logicalPage.commit()
        val os = FileOutputStream("textBaseline.pdf")
        mgr.save(os)
    }

}