package com.planbase.pdf.layoutmanager.contents

import TestManualllyPdfLayoutMgr.Companion.RGB_BLUE_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_DARK_GRAY
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_YELLOW_BRIGHT
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.utils.Utils
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.*
import org.junit.Test
import java.io.FileOutputStream

class TableRowBuilderTest {
    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()

        val upperLeft = XyOffset(100f, 500f)

        // The third table uses the x and y offsets from the previous tables to position it to the
        // right of the first and below the second.  Negative Y is down.  This third table showcases
        // the way cells extend vertically (but not horizontally) to fit the text you put in them.
        val tB = TableBuilder()
        tB.addCellWidths(listOf(100f, 100f, 100f))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f,
                                     RGB_YELLOW_BRIGHT))
                .partBuilder().cellStyle(CellStyle(Align.BOTTOM_CENTER,
                                                   BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(Utils.RGB_BLACK))))
                .rowBuilder().addTextCells("First", "Second", "Third").buildRow()
                .buildPart()
                .partBuilder().cellStyle(CellStyle(Align.MIDDLE_CENTER,
                                                   BoxStyle(Padding(2f), RGB_LIGHT_GREEN,
                                                            BorderStyle(RGB_DARK_GRAY))))
                .textStyle(TextStyle(PDType1Font.COURIER, 12f, Utils.RGB_BLACK))
                .rowBuilder().cellBuilder().align(Align.BOTTOM_RIGHT).addStrs("Line 1").buildCell()
                .cellBuilder().align(Align.BOTTOM_CENTER).addStrs("Line 1\n", "Line two").buildCell()
                .cellBuilder().align(Align.BOTTOM_LEFT)
                .addStrs("Line 1\n", "Line two\n", "[Line three is long enough to wrap]").buildCell()
                .buildRow()
//                .rowBuilder().cellBuilder().align(Align.MIDDLE_RIGHT).addStrs("Line 1\n", "Line two").buildCell()
//                .cellBuilder().align(Align.MIDDLE_CENTER).addStrs("").buildCell()
//                .cellBuilder().align(Align.MIDDLE_LEFT).addStrs("Line 1").buildCell().buildRow()
//                .rowBuilder().cellBuilder().align(Align.TOP_RIGHT).addStrs("L1").buildCell()
//                .cellBuilder().align(Align.TOP_CENTER).addStrs("Line 1\n", "Line two").buildCell()
//                .cellBuilder().align(Align.TOP_LEFT).addStrs("Line 1").buildCell().buildRow()
                .buildPart()
                .buildTable()
                .render(lp, upperLeft)

        lp.commit()
        // We're just going to write to a file.
        // Commit it to the output stream!
//        val os = FileOutputStream("rowHeight.pdf")
//        pageMgr.save(os)

    }
}