package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.Dim
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test

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
        val lp = pageMgr.startPageGrouping()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(twoHundred, ret.width)
        assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)

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
        val lp = pageMgr.startPageGrouping()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(helloHelloWidth, ret.width)
        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)
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
        val lp = pageMgr.startPageGrouping()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(twoHundred + twoHundred, ret.width)
        assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)
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
        val lp = pageMgr.startPageGrouping()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(helloHelloWidth + helloHelloWidth, ret.width)
        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)
    }
}