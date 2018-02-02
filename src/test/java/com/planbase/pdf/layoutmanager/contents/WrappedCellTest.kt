package com.planbase.pdf.layoutmanager.contents

import TestManualllyPdfLayoutMgr.Companion.RGB_BLUE_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_YELLOW_BRIGHT
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPages
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import kotlin.test.assertEquals

class WrappedCellTest {

    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()
        val cellWidth = 200f
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals(textStyle.lineHeight + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val dimAndPages: DimAndPages = wrappedCell.render(lp, upperLeft)
        Dim.assertEquals(wrappedCell.dim, dimAndPages.dim, 0.00002f)

        lp.commit()
    }

    @Test fun testMultiLine() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()
        val cellWidth = 300f
        val hello = Text(textStyle, "Hello\nThere\nWorld!")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals((textStyle.lineHeight * 3) + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        wrappedCell.render(lp, upperLeft)
        lp.commit()

//        val os = FileOutputStream("test4.pdf")
//        pageMgr.save(os)
    }

    @Test fun testRightAlign() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()

        val cellWidth = 200f
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.TOP_RIGHT, boxStyle),
                        cellWidth, listOf(hello), null)

        val wrappedCell =
                WrappedCell(Dim(cellWidth,
                                  textStyle.lineHeight + boxStyle.topBottomInteriorSp()),
                            CellStyle(Align.TOP_RIGHT, boxStyle),
                            listOf({
                                       val mlw = MultiLineWrapped()
                                       mlw.width = hello.maxWidth()
                                       mlw.ascent = textStyle.ascent
                                       mlw.lineHeight = textStyle.lineHeight
                                       mlw.append(Text.WrappedText(textStyle, hello.text, hello.maxWidth()))
                                       mlw
                                   }.invoke()), 0f)
//        val wrappedCell = cell.wrap()
//        println("cell.wrap()=${cell.wrap()}")

        kotlin.test.assertEquals(textStyle.lineHeight + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val dimAndPages: DimAndPages = wrappedCell.render(lp, upperLeft)
//        println("upperLeft=" + upperLeft)
//        println("xyOff=" + xyOff)

        assertEquals(cellWidth, dimAndPages.dim.width)

        Dim.assertEquals(wrappedCell.dim, dimAndPages.dim, 0.00002f)

        lp.commit()

//        // We're just going to write to a file.
//        // Commit it to the output stream!
//        val os = FileOutputStream("wrappedCellRight.pdf")
//        pageMgr.save(os)
    }

    @Test fun testCellHeightBug() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()
        val textStyle = TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, RGB_YELLOW_BRIGHT)
        val cellStyle = CellStyle(Align.BOTTOM_CENTER, BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK)))

        val tB = Table()
                .addCellWidths(listOf(120f))
                .textStyle(textStyle)
                .partBuilder()
                .cellStyle(cellStyle)
                .rowBuilder().addTextCells("First").buildRow()
                .buildPart()
        val wrappedTable = tB.wrap()

        TestCase.assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                              wrappedTable.dim.height)

        TestCase.assertEquals(120f, wrappedTable.dim.width)

        val dimAndPages: DimAndPages = wrappedTable.render(lp, lp.bodyTopLeft)

        Dim.assertEquals(wrappedTable.dim, dimAndPages.dim, 0.00003f)

        lp.commit()
//        val os = FileOutputStream("test3.pdf")
//        pageMgr.save(os)
    }

    companion object {
        val boxStyle = BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK))
        private val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5f, RGB_BLACK)
    }
}