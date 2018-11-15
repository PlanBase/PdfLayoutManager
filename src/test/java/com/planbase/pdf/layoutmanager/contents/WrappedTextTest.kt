package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.*
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import junit.framework.TestCase
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertTrue

class WrappedTextTest {

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

    /**
    Related to [TextTest.testSpaceBeforeLastWord]
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