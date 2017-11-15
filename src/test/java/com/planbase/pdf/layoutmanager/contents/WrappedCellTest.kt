package com.planbase.pdf.layoutmanager.contents

import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.RGB_BLACK
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.IOException

class WrappedCellTest {
    @Test
    @Throws(IOException::class)
    fun testBasics() {
        // Nothing happens without a PdfLayoutMgr.
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))

        // A PageGrouping is a group of pages with the same settings.  When your contents scroll off
        // the bottom of a page, a new page is automatically created for you with the settings taken
        // from the LogicPage grouping. If you don't want a new page, be sure to stay within the
        // bounds of the current one!
        val lp = pageMgr.logicalPageStart()

        // Let's draw a single cell.

        val boxStyle = BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK))
        val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5f, RGB_BLACK)
        val cellWidth = 200f
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
        println(cell)
        println()
        val wrappedCell: WrappedCell = cell.wrap()
        println(wrappedCell)

        kotlin.test.assertEquals(textStyle.lineHeight() + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val upperLeft = XyOffset(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val xyOff : XyOffset = wrappedCell.render(lp, upperLeft)
        println("upperLeft=" + upperLeft)
        println("xyOff=" + xyOff)

        // TODO: This is not right.  Cell should report it's lower-righ-hand corner, no?
//        val xyOff2 : XyOffset = wrappedCell.render(lp, upperLeft.plusXMinusY(xyOff))
//        println("xyOff2=" + xyOff2)

//        assertEquals(upperLeft.plusXMinusY(XyDim(wrappedCell.xyDim.width, wrappedCell.lineHeight)), xyOff)


        lp.commit()

        // We're just going to write to a file.
        // Commit it to the output stream!
//        val os = FileOutputStream("test3.pdf")
//        pageMgr.save(os)
    }

    @Test
    @Throws(IOException::class)
    fun testMultiLine() {
        // Nothing happens without a PdfLayoutMgr.
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))

        // A PageGrouping is a group of pages with the same settings.  When your contents scroll off
        // the bottom of a page, a new page is automatically created for you with the settings taken
        // from the LogicPage grouping. If you don't want a new page, be sure to stay within the
        // bounds of the current one!
        val lp = pageMgr.logicalPageStart()

        // Let's draw a single cell.

        val boxStyle = BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK))
        val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5f, RGB_BLACK)
        val cellWidth = 300f
        val hello = Text(textStyle, "Hello\nThere\nWorld!")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
        println(cell)
        println()
        val wrappedCell: WrappedCell = cell.wrap()
        println(wrappedCell)

        kotlin.test.assertEquals((textStyle.lineHeight() * 3) + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val upperLeft = XyOffset(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val xyOff : XyOffset = wrappedCell.render(lp, upperLeft)


        lp.commit()

        // We're just going to write to a file.
//        val os = FileOutputStream("test4.pdf")
//
//        // Commit it to the output stream!
//        pageMgr.save(os)
    }
}