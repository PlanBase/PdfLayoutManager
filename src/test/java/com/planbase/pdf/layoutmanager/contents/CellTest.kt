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
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test

class CellTest {

    @Test
    fun testWrap() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()

        val cellStyle = CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2f),
                                                           RGB_LIGHT_GREEN,
                                                           BorderStyle(RGB_DARK_GRAY)))
        val theText = Text(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK), "Line 1")

        val squareDim = 120f

        val tB = TableBuilder()
        val trb:TableRowBuilder = tB.addCellWidths(listOf(squareDim))
                .partBuilder()
                .minRowHeight(squareDim)
                .rowBuilder()
        val cell = Cell(cellStyle, squareDim, listOf(theText), trb)
        trb.cell(cell.cellStyle, listOf(theText))
        kotlin.test.assertEquals(squareDim, trb.minRowHeight)
//        println("trb=" + trb)

        val wrappedCell:WrappedCell = cell.wrap()
//        println("wrappedCell=$wrappedCell")
        kotlin.test.assertEquals(squareDim, wrappedCell.dimensions.height)

        trb.buildRow()
                .buildPart()
        val table: Table = tB.buildTable()

        val xya: Dimensions = table.wrap().render(lp, Point2d(40f, lp.yBodyTop()), true)

//        println("lp.yBodyTop()=${lp.yBodyTop()}")
//        println("xya=$xya")

        kotlin.test.assertEquals(squareDim, xya.height)
        lp.commit()

//        val os = FileOutputStream("testCell1.pdf")
//        pageMgr.save(os)
    }

}