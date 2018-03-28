package com.planbase.pdf.layoutmanager.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManual2.Companion.a6PortraitBody
import TestManualllyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.Align.TOP_LEFT
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle.Companion.NO_PAD_NO_BORDER
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding.Companion.NO_PADDING
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream

class TableTest {
    @Test fun testSingleCell() {
        val table:Table = Table(mutableListOf(twoHundred))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(hello))
                .buildRow()
                .buildPart()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dim.height)

        assertEquals(twoHundred, wrappedTable.dim.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.body.topLeft)
        assertEquals(twoHundred, ret.dim.width)
        assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.dim.height, 0.00003f)

        // TODO: Make rendered section of all items below.
    }

    @Test fun testSingleCellWrapped() {
        val table:Table = Table(mutableListOf(helloHelloWidth))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(helloHello))
                .buildRow()
                .buildPart()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dim.height)

        assertEquals(helloHelloWidth,
                     wrappedTable.dim.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.body.topLeft)
        assertEquals(helloHelloWidth, ret.dim.width, 0.00001f)
        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.dim.height, 0.00003f)
    }

    @Test fun testTwoCells() {
        val table:Table = Table(mutableListOf(twoHundred, twoHundred))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(hello))
                .cell(cellStyle, listOf(hello))
                .buildRow()
                .buildPart()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dim.height)

        assertEquals(twoHundred + twoHundred, wrappedTable.dim.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.body.topLeft)
        assertEquals(twoHundred + twoHundred, ret.dim.width)
        assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.dim.height, 0.00003f)
    }

    @Test fun testTwoCellsWrapped() {
        val table:Table = Table(mutableListOf(helloHelloWidth, helloHelloWidth))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(helloHello))
                .cell(cellStyle, listOf(helloHello))
                .buildRow()
                .buildPart()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dim.height)

        assertEquals(helloHelloWidth + helloHelloWidth, wrappedTable.dim.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.body.topLeft)
        assertEquals(helloHelloWidth + helloHelloWidth, ret.dim.width, 0.00001f)
        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.dim.height, 0.00003f)
    }

    /*
Notes: I think the issue is that there's extra vertical space after the inner-most *table* is page-broken
when nesting a second level of bullets.  The number of columns does not seem to matter.

Once the inner table is page-broken, it leaves enough room for *every* line to be on the
next page.  The bottom of the inner table is at the same point on the second page regardless of
how many lines fit on the first page.  So the more that actually goes on the second page, the
less blank space is there.

This suggests it has to do with the rendering code for the inner table.  The text inside is page-broken like other
text, but the table itself is page-broken somewhat like a chunk.

I could not get this happen with nested cells, it requires nested *tables* with a page break.

It's the outer cell that has too much space in it, the inner one is fine.

Note: very similar to CellTest.testNestedCellsAcrossPageBreak()

 */
    @Test fun testNestedTablesAcrossPageBreak() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))

        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)
        val testBorderStyle = BorderStyle(LineStyle(CMYK_BLACK, 0.1f))

        val bulletTable: Table = Table().addCellWidths(230f)
                .partBuilder()
                .rowBuilder()
                .cell(CellStyle(TOP_LEFT, BoxStyle(NO_PADDING, null, testBorderStyle)),
                      listOf(Text(BULLET_TEXT_STYLE,
                                  "Some text with a bullet. " +
                                  "Some text with a bullet. " +
                                  "Some text with a bullet. " +
                                  "Some text with a bullet. "),
                             Table().addCellWidths(230f)
                                     .partBuilder()
                                     .rowBuilder()
                                     .cell(CellStyle(TOP_LEFT, NO_PAD_NO_BORDER),
                                           listOf(Text(BULLET_TEXT_STYLE,
                                                       "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                                       "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                                       "Subtext is an under and often distinct theme in a piece of writing or convers. ")))
                                     .buildRow()
                                     .buildPart()
                      ))
                .buildRow()
                .buildPart()

        val wrappedTable: Table.WrappedTable = bulletTable.wrap()
        assertEquals(Dim(230.0f, 124.948f), wrappedTable.dim)

        val startCoord = Coord(0f, 140f)

        val after:DimAndPageNums = wrappedTable.render(lp, startCoord)
        assertEquals(Dim(230.0f, 130.74399f), after.dim)

        pageMgr.commit()
        // We're just going to write to a file.
        val os = FileOutputStream("testNestedTablesAcrossPageBreak.pdf")
        // Commit it to the output stream!
        pageMgr.save(os)
    }

    @Test fun testToString() {
        val bulletTable: Table = Table(mutableListOf(230f))
                .partBuilder()
                .rowBuilder()
                .cell(CellStyle(TOP_LEFT, BoxStyle(NO_PADDING, null, BorderStyle(LineStyle(CMYK_BLACK, 0.1f)))),
                      listOf(Text(TextStyle(HELVETICA, 12f, CMYK_BLACK), "Some text with a bullet."),
                             Table(mutableListOf(230f))
                                     .partBuilder()
                                     .rowBuilder()
                                     .cell(CellStyle(TOP_LEFT, NO_PAD_NO_BORDER), listOf(Text(TextStyle(HELVETICA, 12f, CMYK_BLACK), "Subtext is an underneath")))
                                     .buildRow()
                                     .buildPart()
                      ))
                .buildRow()
                .buildPart()
        assertEquals("Table(mutableListOf(230f))\n" +
                     ".partBuilder()\n" +
                     ".rowBuilder()\n" +
                     ".cell(CellStyle(TOP_LEFT, BoxStyle(NO_PADDING, null, BorderStyle(LineStyle(CMYK_BLACK, 0.1f)))), " +
                     "listOf(Text(TextStyle(HELVETICA, 12f, CMYK_BLACK), \"Some text with a bullet.\"), " +
                     "Table(mutableListOf(230f))\n" +
                     ".partBuilder()\n" +
                     ".rowBuilder()\n" +
                     ".cell(CellStyle(TOP_LEFT, NO_PAD_NO_BORDER), listOf(Text(TextStyle(HELVETICA, 12f, CMYK_BLACK), \"Subtext is an underneath\")))\n" +
                     ".buildRow()\n" +
                     ".buildPart()))\n" +
                     ".buildRow()\n" +
                     ".buildPart()",
                     bulletTable.toString())
    }
}