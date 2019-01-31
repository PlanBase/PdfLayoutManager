package com.planbase.pdf.lm2.pages

import TestManuallyPdfLayoutMgr.Companion.RGB_DARK_GRAY
import TestManuallyPdfLayoutMgr.Companion.RGB_LIGHT_BLUE
import TestManuallyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.attributes.Orientation.LANDSCAPE
import com.planbase.pdf.lm2.attributes.*
import com.planbase.pdf.lm2.contents.Cell
import com.planbase.pdf.lm2.contents.Table
import com.planbase.pdf.lm2.contents.Text
import com.planbase.pdf.lm2.pages.PageGroupingTest.Companion.bigMelon
import com.planbase.pdf.lm2.pages.PageGroupingTest.Companion.bigText
import com.planbase.pdf.lm2.pages.PageGroupingTest.Companion.melonHeight
import com.planbase.pdf.lm2.pages.PageGroupingTest.Companion.melonWidth
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import com.planbase.pdf.lm2.utils.LineJoinStyle
import com.planbase.pdf.lm2.utils.RGB_BLACK
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.apache.pdfbox.util.Charsets
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.FileOutputStream

class SinglePageTest {
    /**
     * If this is failing on the text boxes on the right, check that
     * [com.planbase.pdf.lm2.contents.TextLineWrapperTest.quickBrownFox] is working first.
     */
    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val pg:SinglePage = pageMgr.page(0)

        val squareDim = Dim(squareSide, squareSide)

        val melonX = lp.body.topLeft.x
        val textX = melonX + melonWidth + 10
        val squareX = textX + bigText.maxWidth() + 10
        val lineX1 = squareX + squareSide + 10
        val cellX1 = lineX1 + squareSide + 10
        val tableX1 = cellX1 + squareSide + 10
        var y = lp.yBodyTop() - melonHeight

        while (y >= lp.yBodyBottom) {
            val imgHaP: HeightAndPage = pg.drawImage(Coord(melonX, y), bigMelon)
            assertEquals(melonHeight, imgHaP.height, 0.0)

            val txtHaP: HeightAndPage = pg.drawStyledText(Coord(textX, y), bigText.text, bigText.textStyle, true)
            assertEquals(bigText.textStyle.lineHeight, txtHaP.height, 0.0)

            val rectY: Double = pg.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
            assertEquals(squareSide, rectY, 0.0)

            diamondRect(pg, Coord(lineX1, y), squareSide)

            val cellDaP: DimAndPageNums = qbfCell.render(pg, Coord(cellX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfCell.dim, cellDaP.dim, 0.0)

            val tableDaP: DimAndPageNums = qbfTable.render(pg, Coord(tableX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfTable.dim, tableDaP.dim, 0.0)

            y -= melonHeight
        }

        pageMgr.commit()

        val docId = COSString("SinglePage test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.save(FileOutputStream("singlePage.pdf"))
    }

//    // This works, but it's an all manual test, which is never a good Unit test.
//    @Test fun testLoop() {
//        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(A6))
//        val lp = pageMgr.startPageGrouping(PdfLayoutMgr.Orientation.PORTRAIT, a6PortraitBody)
//        val page:SinglePage = pageMgr.page(0)
//
//        val squareWidth = 10
//
//        val square1X = 0.0
//        val square2X = square1X + 20.0
//        val y = lp.yBodyTop()
//        val yBot = y - squareWidth
//
//        page.drawLineLoop(listOf(Coord(square2X, y),
//                                 Coord(square2X + squareWidth, y),
//                                 Coord(square2X + squareWidth, yBot),
//                                 Coord(square2X, yBot)),
//                          LineStyle(CMYK_VIOLET, 20.0),
//                          LineJoinStyle.ROUND,
//                          CMYK_THISTLE,
//                          true)
//        pageMgr.commit()
//
//        val docId = COSString("SinglePage test PDF".toByteArray(Charsets.ISO_8859_1))
//        pageMgr.setFileIdentifiers(docId, docId)
//
//        pageMgr.save(FileOutputStream("lineStripAndLoop.pdf"))
//    }
}

fun diamondRect(page:RenderTarget, lowerLeft: Coord, size:Double) {
    val ls = LineStyle(RGB_BLACK, 1.0)
    val (xLeft, yBot) = lowerLeft
    val xRight = xLeft + size
    val yTop = yBot + size
    val halfSize = size / 2.0
    val xMid = xLeft + halfSize
    val yMid = yBot + halfSize

    // Square drawn counter-clockwise (widdershins) from lowerLeft
    page.drawLineLoop(listOf(lowerLeft,
                              Coord(xRight, yBot),
                              Coord(xRight, yTop),
                              Coord(xLeft, yTop)),
                      ls)

    val midTop = Coord(xMid, yTop)
    // Diamond drawn clockwise (deosil) from top
    page.drawLineStrip(listOf(midTop,
                              Coord(xRight, yMid),
                              Coord(xMid, yBot),
                              Coord(xLeft, yMid),
                              midTop),
                       ls, LineJoinStyle.BEVEL)
}

const val squareSide = 70.0
val times15 = TextStyle(PDType1Font.TIMES_ROMAN, 15.0, RGB_BLACK)
val thickLine = LineStyle(RGB_DARK_GRAY, 4.0)
val paleGreenLeft = CellStyle(Align.TOP_LEFT,
                              BoxStyle(Padding(1.0), RGB_LIGHT_GREEN,
                                       BorderStyle(thickLine,thickLine,thickLine,thickLine,LineJoinStyle.ROUND)))
val paleBlueLeft = CellStyle(Align.TOP_LEFT,
                             BoxStyle(Padding(2.0), RGB_LIGHT_BLUE, BorderStyle(RGB_BLACK)))
val qbfCell = Cell(paleGreenLeft, squareSide,
                   listOf(Text(times15, "The quick brown fox jumps over the lazy dog"))).wrap()

val qbfTable: Table.WrappedTable =
        Table(mutableListOf(squareSide, squareSide))
                .partBuilder()
                .rowBuilder()
                .cell(paleBlueLeft, listOf(Text(times15, "The quick brown fox jumps over the lazy dog")))
                .cell(paleBlueLeft, listOf(Text(times15, "Etaoin shrdlu cmfwyp")))
                .buildRow()
                .buildPart()
                .wrap()
