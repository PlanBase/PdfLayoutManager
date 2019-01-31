package com.planbase.pdf.lm2.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManual2.Companion.a6PortraitBody
import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.attributes.Orientation.LANDSCAPE
import com.planbase.pdf.lm2.attributes.Orientation.PORTRAIT
import com.planbase.pdf.lm2.attributes.Align.TOP_LEFT
import com.planbase.pdf.lm2.attributes.BorderStyle
import com.planbase.pdf.lm2.attributes.BoxStyle
import com.planbase.pdf.lm2.attributes.BoxStyle.Companion.NO_PAD_NO_BORDER
import com.planbase.pdf.lm2.attributes.CellStyle
import com.planbase.pdf.lm2.attributes.DimAndPageNums
import com.planbase.pdf.lm2.attributes.LineStyle
import com.planbase.pdf.lm2.attributes.Padding.Companion.NO_PADDING
import com.planbase.pdf.lm2.attributes.TextStyle
import com.planbase.pdf.lm2.pages.SinglePage
import com.planbase.pdf.lm2.utils.CMYK_BLACK
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream
import kotlin.math.nextUp

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
                     ret.dim.height, 0.00003)

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
        assertEquals(helloHelloWidth, ret.dim.width, 0.00001)
        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.dim.height, 0.00003)
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
                     ret.dim.height, 0.00003)
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
        assertEquals(helloHelloWidth + helloHelloWidth, ret.dim.width, 0.00001)
        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.dim.height, 0.00003)
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
        val testBorderStyle = BorderStyle(LineStyle(CMYK_BLACK, 0.0.nextUp().nextUp()))

        val innerTable = Table().addCellWidths(230.0)
                .partBuilder()
                .rowBuilder()
                .cell(CellStyle(TOP_LEFT, NO_PAD_NO_BORDER),
                      listOf(Text(BULLET_TEXT_STYLE,
                                  "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                  "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                  "Subtext is an under and often distinct theme in a piece of writing or convers. ")))
                .buildRow()
                .buildPart()

        val bulletTable: Table = Table().addCellWidths(230.0)
                .partBuilder()
                .rowBuilder()
                .cell(CellStyle(TOP_LEFT, BoxStyle(NO_PADDING, null, testBorderStyle)),
                      listOf(Text(BULLET_TEXT_STYLE,
                                  "Some text with a bullet. " +
                                  "Some text with a bullet. " +
                                  "Some text with a bullet. " +
                                  "Some text with a bullet. "),
                             innerTable))
                .buildRow()
                .buildPart()

        val wrappedTable: Table.WrappedTable = bulletTable.wrap()
        Dim.assertEquals(Dim(230.0, 124.848), wrappedTable.dim, 0.0)

        val outerTableHeight = wrappedTable.dim.height

        val breakingY = (lp.yBodyBottom + outerTableHeight)

        var after:DimAndPageNums = wrappedTable.render(lp, Coord(0.0, breakingY.nextUp()), reallyRender = false)
        Dim.assertEquals(Dim(230.0, outerTableHeight), after.dim, 0.0)
        assertEquals(1..1, after.pageNums)

        after = wrappedTable.render(lp, Coord(0.0, breakingY), reallyRender = true)

//        val innerTableHeight:Double = innerTable.wrap().dim.height
//        val tableHeightDiff = outerTableHeight - innerTableHeight

        // Now that this actually breaks across the page, this calculation is wrong!
//        Dim.assertEquals(Dim(230.0, tableHeightDiff + (innerTableHeight * 2)), after.dim, 0.00000001)
        assertEquals(1..2, after.pageNums)
        // Note that when we turn on widow prevention this whole test will be invalid unless we shift the page break
        // up a few rows.
        Dim.assertEquals(Dim(230.0, 138.72), after.dim, 0.00000001)

        pageMgr.commit()

        // We're just going to write to a file.
        pageMgr.save(FileOutputStream("testNestedTablesAcrossPageBreak.pdf"))
    }

    @Test fun testToString() {
        val bulletTable: Table = Table(mutableListOf(230.0))
                .partBuilder()
                .rowBuilder()
                .cell(CellStyle(TOP_LEFT, BoxStyle(NO_PADDING, null, BorderStyle(LineStyle(CMYK_BLACK, 0.1)))),
                      listOf(Text(TextStyle(HELVETICA, 12.0, CMYK_BLACK), "Some text with a bullet."),
                             Table(mutableListOf(230.0))
                                     .partBuilder()
                                     .rowBuilder()
                                     .cell(CellStyle(TOP_LEFT, NO_PAD_NO_BORDER), listOf(Text(TextStyle(HELVETICA, 12.0, CMYK_BLACK), "Subtext is an underneath")))
                                     .buildRow()
                                     .buildPart()
                      ))
                .buildRow()
                .buildPart()
        assertEquals("Table(mutableListOf(230.0))\n" +
                     ".partBuilder()\n" +
                     ".rowBuilder()\n" +
                     ".cell(CellStyle(TOP_LEFT, BoxStyle(NO_PADDING, null, BorderStyle(LineStyle(CMYK_BLACK, 0.1)))), " +
                     "listOf(Text(TextStyle(HELVETICA, 12.0, CMYK_BLACK), \"Some text with a bullet.\"),\n" +
                     "       Table(mutableListOf(230.0))\n" +
                     ".partBuilder()\n" +
                     ".rowBuilder()\n" +
                     ".cell(CellStyle(TOP_LEFT, NO_PAD_NO_BORDER), listOf(Text(TextStyle(HELVETICA, 12.0, CMYK_BLACK), \"Subtext is an underneath\")))\n" +
                     ".buildRow()\n" +
                     ".buildPart()))\n" +
                     ".buildRow()\n" +
                     ".buildPart()",
                     bulletTable.toString())
    }
}