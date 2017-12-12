package com.planbase.pdf.layoutmanager.contents

import TestManualllyPdfLayoutMgr.Companion.RGB_DARK_GRAY
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream
import kotlin.test.assertEquals

val twoHundred:Float = 200f
val cellStyle = CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2f),
                                                   RGB_LIGHT_GREEN,
                                                   BorderStyle(RGB_DARK_GRAY)))
val textStyle = TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK)
val hello = Text(textStyle, "Hello")
val helloSpace = Text(textStyle, "Hello ")
val helloHello = Text(textStyle, "Hello Hello")
val helloHelloWidth:Float = helloHello.maxWidth() * 0.7f

class CellTest {
    @Test fun testBasics() {
        val cell = Cell(cellStyle, twoHundred, listOf(hello))
        val wrappedCell:WrappedCell = cell.wrap()
        assertEquals(textStyle.lineHeight() + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height)
    }

    @Test fun testOneWrappedLine() {
        val cell = Cell(cellStyle, helloHelloWidth, listOf(helloHello))
        val wrappedCell:WrappedCell = cell.wrap()
        assertEquals((textStyle.lineHeight() * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height)
    }

    @Test fun testWrapTable() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()

        val cellStyle = CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2f),
                                                           RGB_LIGHT_GREEN,
                                                           BorderStyle(RGB_DARK_GRAY)))
        val theText = Text(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK), "Line 1")

        val squareDim = 120f

        val tB = Table()
        val trb: TableRow = tB.addCellWidths(listOf(squareDim))
                .partBuilder()
                .minRowHeight(squareDim)
                .rowBuilder()
        val cell = Cell(cellStyle, squareDim, listOf(theText), trb)
        trb.cell(cell.cellStyle, listOf(theText))
        assertEquals(squareDim, trb.minRowHeight)
//        println("trb=" + trb)

        val wrappedCell:WrappedCell = cell.wrap()
//        println("wrappedCell=$wrappedCell")
        assertEquals(theText.textStyle.lineHeight() + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height)


        val table: Table = trb.buildRow().buildPart()

//        println("lp=$lp")
//        println("lp.yBodyTop()=${lp.yBodyTop()}")

        val dim: Dim = table.wrap().render(lp, Coord(40f, lp.yBodyTop()))

//        println("lp.yBodyTop()=${lp.yBodyTop()}")
//        println("xya=$xya")

        assertEquals(squareDim, dim.height)
        lp.commit()

        val os = FileOutputStream("testCell1.pdf")
        pageMgr.save(os)
    }

}