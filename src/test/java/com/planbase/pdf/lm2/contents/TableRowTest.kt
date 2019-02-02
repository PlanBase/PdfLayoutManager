package com.planbase.pdf.lm2.contents

import TestManuallyPdfLayoutMgr.Companion.RGB_BLUE
import TestManuallyPdfLayoutMgr.Companion.RGB_BLUE_GREEN
import TestManuallyPdfLayoutMgr.Companion.RGB_DARK_GRAY
import TestManuallyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManuallyPdfLayoutMgr.Companion.RGB_YELLOW_BRIGHT
import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import TestManuallyPdfLayoutMgr.Companion.letterPortraitBody
import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.attributes.*
import com.planbase.pdf.lm2.attributes.Orientation.*
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import com.planbase.pdf.lm2.utils.RGB_BLACK
import com.planbase.pdf.lm2.utils.RGB_WHITE
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test

class TableRowTest {
    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        val upperLeft = Coord(100.0, 500.0)

        // The third table uses the x and y offsets from the previous tables to position it to the
        // right of the first and below the second.  Negative Y is down.  This third table showcases
        // the way cells extend vertically (but not horizontally) to fit the text you put in them.
        val tB = Table()
        tB.addCellWidths(listOf(100.0, 100.0, 100.0))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0,
                                     RGB_YELLOW_BRIGHT))
                .startPart().cellStyle(CellStyle(Align.BOTTOM_CENTER,
                                                 BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .startRow().addTextCells("First", "Second", "Third").endRow()
                .endPart()
                .startPart().cellStyle(CellStyle(Align.MIDDLE_CENTER,
                                                 BoxStyle(Padding(2.0), RGB_LIGHT_GREEN,
                                                            BorderStyle(RGB_DARK_GRAY))))
                .textStyle(TextStyle(PDType1Font.COURIER, 12.0, RGB_BLACK))
                .startRow()
                .align(Align.BOTTOM_RIGHT).addTextCells("Line 1")
                .align(Align.BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                         "Line two")
                .align(Align.BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                       "Line two\n" +
                                                       "[Line three is long enough to wrap]")
                .endRow()
//                .startRow().cellBuilder().align(Align.MIDDLE_RIGHT).addStrs("Line 1\n" +
//        "Line two").buildCell()
//                .cellBuilder().align(Align.MIDDLE_CENTER).addStrs("").buildCell()
//                .cellBuilder().align(Align.MIDDLE_LEFT).addStrs("Line 1").buildCell().endRow()
//                .startRow().cellBuilder().align(Align.TOP_RIGHT).addStrs("L1").buildCell()
//                .cellBuilder().align(Align.TOP_CENTER).addStrs("Line 1\n" +
//    "Line two").buildCell()
//                .cellBuilder().align(Align.TOP_LEFT).addStrs("Line 1").buildCell().endRow()
                .endPart()
                .wrap().render(lp, upperLeft)

        pageMgr.commit()
        // We're just going to write to a file.
        // Commit it to the output stream!
//        val os = FileOutputStream("rowHeight.pdf")
//        pageMgr.save(os)

    }

    @Test fun testJustHeadings() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))

        val pMargin = PdfLayoutMgr.DOC_UNITS_PER_INCH / 2
        var lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        // Set up some useful constants for later.
        val tableWidth = lp.pageWidth() - 2 * pMargin
        val colWidth = tableWidth / 4.0
        val colWidths = doubleArrayOf(colWidth + 10.0, colWidth + 10.0, colWidth + 10.0, colWidth - 30.0)
        val textCellPadding = Padding(2.0)

        // Set up some useful styles for later
        val heading = TextStyle(PDType1Font.HELVETICA_BOLD, 9.5, RGB_WHITE)
        val headingCell = CellStyle(Align.BOTTOM_CENTER,
                                    BoxStyle(textCellPadding, RGB_BLUE,
                                             BorderStyle(LineStyle.NO_LINE, LineStyle(RGB_WHITE),
                                                         LineStyle.NO_LINE, LineStyle(RGB_BLUE))))
        val headingCellR = CellStyle(Align.BOTTOM_CENTER,
                                     BoxStyle(textCellPadding, RGB_BLACK,
                                              BorderStyle(LineStyle.NO_LINE, LineStyle(RGB_BLACK),
                                                          LineStyle.NO_LINE, LineStyle(RGB_WHITE))))

        // Let's do a portrait page now.  I just copied this from the previous page.
        lp = pageMgr.startPageGrouping(PORTRAIT, letterPortraitBody)

        val tB = Table(colWidths.toMutableList(), headingCell, heading)
        tB.startPart()
                .startRow()
                .cell(headingCell,
                      listOf(Text(heading, "Transliterated Russian (with un-transliterated Chinese below)")))
                .cell(headingCellR, listOf(Text(heading, "US English")))
                .cell(headingCellR, listOf(Text(heading, "Finnish")))
                .cell(headingCellR, listOf(Text(heading, "German")))
                .endRow()
                .endPart()
        tB.wrap().render(lp, lp.body.topLeft)
        pageMgr.commit()

//        val os = FileOutputStream("rowHeight2.pdf")
//        pageMgr.save(os)
    }
}