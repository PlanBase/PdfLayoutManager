package com.planbase.pdf.layoutmanager.contents

import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.RGB_BLACK
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import junit.framework.TestCase.assertTrue
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import kotlin.test.assertEquals

class WrappedCellTest {

    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val cellWidth = 200f
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals(textStyle.lineHeight() + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val upperLeft = XyOffset(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        wrappedCell.render(lp, upperLeft)
//        val xyOff : XyOffset = wrappedCell.render(lp, upperLeft)
//        println("upperLeft=" + upperLeft)
//        println("xyOff=" + xyOff)

        // TODO: This is not right.  Cell should report it's lower-right-hand corner, no?
//        val xyOff2 : XyOffset = wrappedCell.render(lp, upperLeft.plusXMinusY(xyOff))
//        println("xyOff2=" + xyOff2)

//        assertEquals(upperLeft.plusXMinusY(XyDim(wrappedCell.xyDim.width, wrappedCell.lineHeight)), xyOff)

        lp.commit()
//        val os = FileOutputStream("test3.pdf")
//        pageMgr.save(os)
    }

    @Test fun testMultiLine() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val cellWidth = 300f
        val hello = Text(textStyle, "Hello\nThere\nWorld!")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals((textStyle.lineHeight() * 3) + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val upperLeft = XyOffset(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        wrappedCell.render(lp, upperLeft)
        lp.commit()

//        val os = FileOutputStream("test4.pdf")
//        pageMgr.save(os)
    }

    @Test fun testRightAlign() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()

        val cellWidth = 200f
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.TOP_RIGHT, boxStyle),
                        cellWidth, listOf(hello), null)

        val wrappedCell =
                WrappedCell(XyDim(cellWidth,
                                  textStyle.lineHeight() + boxStyle.topBottomInteriorSp()),
                            CellStyle(Align.TOP_RIGHT, boxStyle),
                            listOf(MultiLineWrapped(hello.maxWidth(),
                                                    textStyle.ascent(),
                                                    textStyle.descent() + textStyle.leading(),
                                                    mutableListOf(Text.WrappedText(textStyle,
                                                                                   hello.text,
                                                                                   XyDim(hello.maxWidth(),
                                                                                         textStyle.lineHeight()),
                                                                                   hello)))))
//        val wrappedCell = cell.wrap()
//        println("cell.wrap()=${cell.wrap()}")

        kotlin.test.assertEquals(textStyle.lineHeight() + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val upperLeft = XyOffset(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.xyDim.width)

        val xyDim : XyDim = wrappedCell.render(lp, upperLeft)
//        println("upperLeft=" + upperLeft)
//        println("xyOff=" + xyOff)

        // TODO: Enable!
        assertEquals(cellWidth, xyDim.width)

        // TODO: This is not right.  Cell should report it's lower-righ-hand corner, no?
//        val xyOff2 : XyOffset = wrappedCell.render(lp, upperLeft.plusXMinusY(xyOff))
//        println("xyOff2=" + xyOff2)

        assertTrue(XyDim.within(0.00002f, wrappedCell.xyDim, xyDim))

        lp.commit()

//        // We're just going to write to a file.
//        // Commit it to the output stream!
//        val os = FileOutputStream("wrappedCellRight.pdf")
//        pageMgr.save(os)
    }

    companion object {
        val boxStyle = BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK))
        private val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5f, RGB_BLACK)
    }
}