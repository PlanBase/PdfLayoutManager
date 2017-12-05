package com.planbase.pdf.layoutmanager.contents

import TestManualllyPdfLayoutMgr.Companion.RGB_BLUE
import TestManualllyPdfLayoutMgr.Companion.RGB_BLUE_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_DARK_GRAY
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_YELLOW_BRIGHT
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import com.planbase.pdf.layoutmanager.utils.RGB_WHITE
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test

class TableRowBuilderTest {
    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()

        val upperLeft = Point2d(100f, 500f)

        // The third table uses the x and y offsets from the previous tables to position it to the
        // right of the first and below the second.  Negative Y is down.  This third table showcases
        // the way cells extend vertically (but not horizontally) to fit the text you put in them.
        val tB = TableBuilder()
        tB.addCellWidths(listOf(100f, 100f, 100f))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f,
                                     RGB_YELLOW_BRIGHT))
                .partBuilder().cellStyle(CellStyle(Align.BOTTOM_CENTER,
                                                   BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .rowBuilder().addTextCells("First", "Second", "Third").buildRow()
                .buildPart()
                .partBuilder().cellStyle(CellStyle(Align.MIDDLE_CENTER,
                                                   BoxStyle(Padding(2f), RGB_LIGHT_GREEN,
                                                            BorderStyle(RGB_DARK_GRAY))))
                .textStyle(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK))
                .rowBuilder()
                .align(Align.BOTTOM_RIGHT).addTextCells("Line 1")
                .align(Align.BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                         "Line two")
                .align(Align.BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                       "Line two\n" +
                                                       "[Line three is long enough to wrap]")
                .buildRow()
//                .rowBuilder().cellBuilder().align(Align.MIDDLE_RIGHT).addStrs("Line 1\n" +
//        "Line two").buildCell()
//                .cellBuilder().align(Align.MIDDLE_CENTER).addStrs("").buildCell()
//                .cellBuilder().align(Align.MIDDLE_LEFT).addStrs("Line 1").buildCell().buildRow()
//                .rowBuilder().cellBuilder().align(Align.TOP_RIGHT).addStrs("L1").buildCell()
//                .cellBuilder().align(Align.TOP_CENTER).addStrs("Line 1\n" +
//    "Line two").buildCell()
//                .cellBuilder().align(Align.TOP_LEFT).addStrs("Line 1").buildCell().buildRow()
                .buildPart()
                .buildTable()
                .wrap().render(lp, upperLeft, true)

        lp.commit()
        // We're just going to write to a file.
        // Commit it to the output stream!
//        val os = FileOutputStream("rowHeight.pdf")
//        pageMgr.save(os)

    }

    @Test fun testJustHeadings() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))

        val pMargin = PdfLayoutMgr.DOC_UNITS_PER_INCH / 2
        var lp = pageMgr.logicalPageStart()

        // Set up some useful constants for later.
        val tableWidth = lp.pageWidth() - 2 * pMargin
        val colWidth = tableWidth / 4f
        val colWidths = floatArrayOf(colWidth + 10, colWidth + 10, colWidth + 10, colWidth - 30)
        val textCellPadding = Padding(2f)

        // Set up some useful styles for later
        val heading = TextStyle(PDType1Font.HELVETICA_BOLD, 9.5f, RGB_WHITE)
        val headingCell = CellStyle(Align.BOTTOM_CENTER,
                                    BoxStyle(textCellPadding, RGB_BLUE,
                                             BorderStyle(LineStyle.NO_LINE, LineStyle(RGB_WHITE),
                                                         LineStyle.NO_LINE, LineStyle(RGB_BLUE))))
        val headingCellR = CellStyle(Align.BOTTOM_CENTER,
                                     BoxStyle(textCellPadding, RGB_BLACK,
                                              BorderStyle(LineStyle.NO_LINE, LineStyle(RGB_BLACK),
                                                          LineStyle.NO_LINE, LineStyle(RGB_WHITE))))

        // Let's do a portrait page now.  I just copied this from the previous page.
        lp = pageMgr.logicalPageStart(PdfLayoutMgr.Orientation.PORTRAIT)

        val tB = TableBuilder(colWidths.toMutableList(), headingCell, heading)
        tB.partBuilder()
                .rowBuilder()
                .cell(headingCell,
                      listOf(Text(heading, "Transliterated Russian (with un-transliterated Chinese below)")))
                .cell(headingCellR, listOf(Text(heading, "US English")))
                .cell(headingCellR, listOf(Text(heading, "Finnish")))
                .cell(headingCellR, listOf(Text(heading, "German")))
                .buildRow()
                .buildPart()
        tB.buildTable().wrap().render(lp, lp.bodyTopLeft(), true)
        lp.commit()

//        val os = FileOutputStream("rowHeight2.pdf")
//        pageMgr.save(os)
    }
}