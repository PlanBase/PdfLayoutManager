package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.ConTerm
import com.planbase.pdf.layoutmanager.lineWrapping.ConTermNone
import com.planbase.pdf.layoutmanager.lineWrapping.Continuing
import com.planbase.pdf.layoutmanager.lineWrapping.None
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import junit.framework.TestCase
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertTrue

class WrappedTextTest {

    /**
    Related to [com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrappedTest.testSpaceBeforeLastWord]
    This was a long-standing bug where if there were multiple items on a line (MultiLineWrapped) and the last one was
    text, and there was room left for one more item on the line, but only by removing the space before that item, it
    would nuke the last space before the last word.  This showed up in Chapter 3 of Alice which this test is taken
    from.
     */
    @Test fun testSpaceBeforeLastWord2() {

        val titleStyle = TextStyle(PDType1Font.TIMES_ROMAN, 16.0, CMYK_BLACK)

        // This tests a too-long line that breaks on a hyphen (not a white-space).
        // It used to adjust the index wrong and always return index=0 and return the first half of the line.
        val maxWidth = 174.0
        val txt = Text(titleStyle, "A Caucus-Race and a Long Tale")

        // Make sure that "Long" doesn't fit on the first line (the rest of this test rests on this assumption).
        assertTrue(titleStyle.stringWidthInDocUnits("A Caucus-Race and a Long") > maxWidth)
        // Test the exact length just for completeness, but doesn't matter for rest of test.
        assertEquals(175.952, titleStyle.stringWidthInDocUnits("A Caucus-Race and a Long"), 0.0005)

        val wrapper = txt.lineWrapper()
        var conTerm: ConTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")

        // This should always be true
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)

        TestCase.assertEquals("A Caucus-Race and a", // Should this have a space at the end of it?
                (conTerm.item as WrappedText).string)
        TestCase.assertEquals(138.176, conTerm.item.dim.width, 0.0005)

        // The word "Long" plus a space should not fit on the current line.
        val conTermNone: ConTermNone = wrapper.getIfFits(174.0 - (138.176 + titleStyle.stringWidthInDocUnits(" ")))
//        println("conTermNone=$conTermNone")
        assertEquals(None, conTermNone)

        // OK, "Long Tale" should come out on the next line.
        conTerm = wrapper.getSomething(maxWidth)
//        println("conTerm=$conTerm")

        // This should always be true
        assertTrue(conTerm is Continuing)
        assertTrue(conTerm.item is WrappedText)
        assertTrue(conTerm.item.dim.width <= maxWidth)
        TestCase.assertEquals("Long Tale",
                (conTerm.item as WrappedText).string)
        TestCase.assertEquals(66.208, conTerm.item.dim.width, 0.0005)
    }
}