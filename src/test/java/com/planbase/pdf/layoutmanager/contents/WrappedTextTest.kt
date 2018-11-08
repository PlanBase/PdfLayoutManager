package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.wrapLines
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Test

class WrappedTextTest {

    val regular = PDType1Font.TIMES_ROMAN
    val bold = PDType1Font.TIMES_BOLD
    val textSize = 8.1
    val tsRegular = TextStyle(regular, textSize, CMYK_BLACK)
    val tsBold = TextStyle(bold, textSize, CMYK_BLACK)

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
}