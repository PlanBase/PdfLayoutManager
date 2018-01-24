package com.planbase.pdf.layoutmanager.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManual2.Companion.CMYK_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_DARK_GRAY
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align.TOP_LEFT
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle.Companion.NO_PAD_NO_BORDER
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.PDType1Font.TIMES_BOLD_ITALIC
import org.apache.pdfbox.pdmodel.font.PDType1Font.TIMES_ROMAN
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream
import kotlin.test.assertEquals

const val twoHundred:Float = 200f
val cellStyle = CellStyle(
        TOP_LEFT, BoxStyle(Padding(2f),
                           RGB_LIGHT_GREEN,
                           BorderStyle(RGB_DARK_GRAY)))
val textStyle = TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK)
val hello = Text(textStyle, "Hello")
//val helloSpace = Text(textStyle, "Hello ")
val helloHello = Text(textStyle, "Hello Hello")
val helloHelloWidth:Float = helloHello.maxWidth() * 0.7f

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
        val lp = pageMgr.startPageGrouping()

        val cellStyle = CellStyle(
                TOP_LEFT, BoxStyle(Padding(2f),
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
        assertEquals(theText.textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height)


        val table: Table = trb.buildRow().buildPart()

//        println("lp=$lp")
//        println("lp.yBodyTop()=${lp.yBodyTop()}")

        val dim: Dim = table.wrap().render(lp, Coord(40f, lp.yBodyTop()))

//        println("lp.yBodyTop()=${lp.yBodyTop()}")
//        println("xya=$xya")

        assertEquals(squareDim, dim.height)
        lp.commit()

//        val os = FileOutputStream("testCell1.pdf")
//        pageMgr.save(os)
    }

    // Note: very similar to TableTest.testNestedTablesAcrossPageBreak()
    @Test fun testNestedCellsAcrossPageBreak() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))

        val lp = pageMgr.startPageGrouping(PdfLayoutMgr.Orientation.PORTRAIT)
        val testBorderStyle = BorderStyle(LineStyle(CMYK_BLACK, 0.1f))

        val bulletCell = Cell(CellStyle(TOP_LEFT, BoxStyle(Padding.NO_PADDING, null, testBorderStyle)), 230f,
                              listOf(Text(BULLET_TEXT_STYLE,
                                          "Some text with a bullet. " +
                                          "Some text with a bullet. " +
                                          "Some text with a bullet. " +
                                          "Some text with a bullet. "),
                                     Cell(CellStyle(TOP_LEFT, BoxStyle(Padding.NO_PADDING, CMYK_LIGHT_GREEN, BorderStyle.NO_BORDERS)),
                                          203f,
                                          listOf(Text(BULLET_TEXT_STYLE,
                                                      "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                                      "Subtext is an under and often distinct theme in a piece of writing or convers. " +
                                                      "Subtext is an under and often distinct theme in a piece of writing or convers. ")),
                                          25f)
                              ))

        val wrappedCell: WrappedCell = bulletCell.wrap()
        TestCase.assertEquals(Dim(230.0f, 124.948f), wrappedCell.dim)

        val startCoord = Coord(40f, 140f)

        val after:Dim = wrappedCell.render(lp, startCoord)
        TestCase.assertEquals(Dim(230.0f, 186.23203f), after)

        lp.commit()
        // We're just going to write to a file.
        val os = FileOutputStream("testNestedCellsAcrossPageBreak.pdf")
        // Commit it to the output stream!
        pageMgr.save(os)
    }

    @Test fun testCellAcrossPageBreak() {
        // The bold-italic text showed on the wrong page because the last line wasn't being dealt with as a unit.
        // A total line height is now calculated for the entire MultiLineWrapped when later inline text has a surprising
        // default lineHeight.  This test maybe belongs in MultiLineWrapped, but better here than nowhere.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(PdfLayoutMgr.Orientation.PORTRAIT)
        val symbola7p5 = TextStyle(TIMES_ROMAN, 7.5f, CMYK_BLACK)
        val contentCell=Cell(CellStyle(TOP_LEFT, NO_PAD_NO_BORDER), 170f,
                             listOf(Text(symbola7p5, "Men often hate each other because they fear each other; they" +
                                                     " fear each other because they don't know each other; they" +
                                                     " don't know each other because they can not communicate;" +
                                                     " they can not communicate "),
                                    Text(TextStyle(TIMES_BOLD_ITALIC, 7f, CMYK_BLACK, 12f), "because they are separated.")))

        val wrappedCell: WrappedCell = contentCell.wrap()
        assertEquals(Dim(170f, 49.4515f), wrappedCell.dim)

        // Rendered away from the page break, the dimensions are unchanged.
        val ret1:Dim = wrappedCell.render(lp, Coord(40f, 200f))
        assertEquals(Dim(170f, 49.451508f), ret1)

        // Rendered across the page break, it's bigger.
        val ret2:Dim = wrappedCell.render(lp, Coord(40f, 72f))
        assertEquals(Dim(170f, 58.65849f), ret2)

        lp.commit()
//        val os = FileOutputStream("testCellAcrossPageBreak.pdf")
//        pageMgr.save(os)
    }

}