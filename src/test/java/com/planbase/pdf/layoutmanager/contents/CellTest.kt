package com.planbase.pdf.layoutmanager.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManual2.Companion.CMYK_LIGHT_GREEN
import TestManual2.Companion.a6PortraitBody
import TestManualllyPdfLayoutMgr.Companion.RGB_DARK_GRAY
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.Align.TOP_LEFT
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream

const val twoHundred:Double = 200.0
val cellStyle = CellStyle(
        TOP_LEFT, BoxStyle(Padding(2.0),
                           RGB_LIGHT_GREEN,
                           BorderStyle(RGB_DARK_GRAY)))
val textStyle = TextStyle(PDType1Font.COURIER, 12.0, RGB_BLACK)
val hello = Text(textStyle, "Hello")
//val helloSpace = Text(textStyle, "Hello ")
val helloHello = Text(textStyle, "Hello Hello")
val helloHelloWidth:Double = helloHello.maxWidth() * 0.7

class CellTest {
    @Test fun testBasics() {
        val cell = Cell(cellStyle, twoHundred, listOf(hello))
        val wrappedCell:WrappedCell = cell.wrap()
        assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height)
    }

    @Test fun testOneWrappedLine() {
        val cell = Cell(cellStyle, helloHelloWidth, listOf(helloHello))
        val wrappedCell:WrappedCell = cell.wrap()
        assertEquals((textStyle.lineHeight * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height)
    }

    @Test fun testWrapTable() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        val cellStyle = CellStyle(
                TOP_LEFT, BoxStyle(Padding(2.0),
                                   RGB_LIGHT_GREEN,
                                   BorderStyle(RGB_DARK_GRAY)))
        val theText = Text(TextStyle(PDType1Font.COURIER, 12.0, RGB_BLACK), "Line 1")

        val squareDim = 120.0

        val tB = Table()
        val trb: TableRow = tB.addCellWidths(listOf(squareDim))
                .partBuilder()
                .minRowHeight(squareDim)
                .rowBuilder()
        val cell = Cell(cellStyle, squareDim, listOf(theText))
        trb.cell(cell.cellStyle, listOf(theText))
        assertEquals(squareDim, trb.minRowHeight)
//        println("trb=" + trb)

        val wrappedCell:WrappedCell = cell.wrap()
//        println("wrappedCell=$wrappedCell")
        assertEquals(theText.textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height)


        val table: Table = trb.buildRow().buildPart()

//        println("lp=$lp")
//        println("lp.yBodyTop()=${lp.yBodyTop()}")

        val dim: DimAndPageNums = table.wrap().render(lp, Coord(40.0, lp.yBodyTop()))

//        println("lp.yBodyTop()=${lp.yBodyTop()}")
//        println("xya=$xya")

        assertEquals(squareDim, dim.dim.height)
        pageMgr.commit()

//        val os = FileOutputStream("testCell1.pdf")
//        pageMgr.save(os)
    }

    // Note: very similar to TableTest.testNestedTablesAcrossPageBreak() but this is more specific and failed
    // in a useful way 2018-11-15.
    @Test fun testNestedCellsAcrossPageBreak() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))

        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)
        val testBorderStyle = BorderStyle(LineStyle(CMYK_BLACK, 0.1))

        val bulletCell = Cell(CellStyle(TOP_LEFT, BoxStyle(Padding.NO_PADDING, null, testBorderStyle)), 230.0,
                              listOf(Text(BULLET_TEXT_STYLE,
                                          "Some text with a bullet. " +
                                          "Some text with a bullet. " +
                                          "Some text with a bullet. " +
                                          "Some text with a bullet. "),
                                     Cell(CellStyle(TOP_LEFT, BoxStyle(Padding.NO_PADDING, CMYK_LIGHT_GREEN, BorderStyle.NO_BORDERS)),
                                          203.0,
                                          listOf(Text(BULLET_TEXT_STYLE,
                                                      "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                                      "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                                      "Subtext is an under and often distinct theme in a piece of writing or convers. ")),
                                          25.0)
                              ))

        val wrappedCell: WrappedCell = bulletCell.wrap()
        // TODO: Replace magic numbers with calculations like this:
//        val bullTxtLineHeight = BULLET_TEXT_STYLE.lineHeight
//        println("bullTxtLineHeight=$bullTxtLineHeight")
//        println("bullTxtLineHeight * 3.0 =${bullTxtLineHeight * 3.0}")
//        println("bullTxtLineHeight * 7.0 =${bullTxtLineHeight * 7.0}")
//        println("wrappedCell=\n$wrappedCell")
        Dim.assertEquals(Dim(230.0, 124.948), wrappedCell.dim, 0.0000000001)

        val startCoord = Coord(0.0, 140.0)

        val after:DimAndPageNums = wrappedCell.renderCustom(lp, startCoord, 0.0, reallyRender = true,
                                                            preventWidows = false)
        Dim.assertEquals(Dim(230.0, 186.23203), after.dim, 0.0001)

        pageMgr.commit()
        // We're just going to write to a file.
        val os = FileOutputStream("testNestedCellsAcrossPageBreak.pdf")
        // Commit it to the output stream!
        pageMgr.save(os)
    }
}