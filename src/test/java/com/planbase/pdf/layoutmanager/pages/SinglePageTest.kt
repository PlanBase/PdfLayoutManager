package com.planbase.pdf.layoutmanager.pages

import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_BLUE
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.Table
import com.planbase.pdf.layoutmanager.contents.TableBuilder
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

class SinglePageTest {
    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val page:SinglePage = pageMgr.page(0)
        val f = File("target/test-classes/melon.jpg")
        val melonPic = ImageIO.read(f)
        val melonHeight = 100f
        val melonWidth = 170f
        val bigMelon = ScaledImage(melonPic, Dimensions(melonWidth, melonHeight)).wrap()
        val bigText = Text(TextStyle(PDType1Font.TIMES_ROMAN, 90f, RGB_BLACK), "gN")

        val squareDim = Dimensions(squareSide, squareSide)

        val melonX = lp.bodyTopLeft().x
        val textX = melonX + melonWidth + 10
        val squareX = textX + bigText.maxWidth() + 10
        val lineX1 = squareX + squareSide + 10
        val cellX1 = lineX1 + squareSide + 10
        val tableX1 = cellX1 + squareSide + 10
        var y = lp.yBodyTop() - melonHeight

        while(y >= lp.yBodyBottom()) {
            val imgY = page.drawImage(Point2d(melonX, y), bigMelon, true)
            assertEquals(melonHeight, imgY)

            val txtY = page.drawStyledText(Point2d(textX, y), bigText.text, bigText.textStyle, true)
            assertEquals(bigText.textStyle.lineHeight(), txtY)

            val rectY = page.fillRect(Point2d(squareX, y), squareDim, RGB_BLACK, true)
            assertEquals(squareSide, rectY)

            diamondRect(page, Point2d(lineX1, y), squareSide)

            val cellDim = qbfCell.render(page, Point2d(cellX1, y + qbfCell.dimensions.height))
            assertEquals(qbfCell.dimensions, cellDim)

            val tableDim = qbfTable.render(page, Point2d(tableX1, y))
            assertEquals(qbfTable.dimensions, tableDim)

            y -= melonHeight
        }

        lp.commit()
        val os = FileOutputStream("singlePage.pdf")
        pageMgr.save(os)
    }
}
fun diamondRect(page:RenderTarget, lowerLeft:Point2d, size:Float) {
    val ls = LineStyle(RGB_BLACK, 1f)
    val (xLeft, yBot) = lowerLeft
    val xRight = xLeft + size
    val yTop = yBot + size
    val halfSize = size / 2f
    val xMid = xLeft + halfSize
    val yMid = yBot + halfSize

    // Square drawn counter-clockwise (widdershins) from lowerLeft
    page.drawLineStrip(listOf(lowerLeft,
                              Point2d(xRight, yBot),
                              Point2d(xRight, yTop),
                              Point2d(xLeft, yTop),
                              lowerLeft),
                       ls, true)

    val midTop = Point2d(xMid, yTop)
    // Diamond drawn clockwise (deosil) from top
    page.drawLineStrip(listOf(midTop,
                              Point2d(xRight, yMid),
                              Point2d(xMid, yBot),
                              Point2d(xLeft, yMid),
                              midTop),
                       ls, true)
}

val squareSide = 70f
val times15 = TextStyle(PDType1Font.TIMES_ROMAN, 15f, RGB_BLACK)
val paleGreenLeft = CellStyle(Align.TOP_LEFT,
                              BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK)))
val paleBlueLeft = CellStyle(Align.TOP_LEFT,
                             BoxStyle(Padding(2f), RGB_LIGHT_BLUE, BorderStyle(RGB_BLACK)))
val qbfCell = Cell(paleGreenLeft, squareSide,
                   listOf(Text(times15, "The quick brown fox jumps over the lazy dog"))).wrap()

val qbfTable: Table.WrappedTable =
        TableBuilder(mutableListOf(squareSide, squareSide))
                .partBuilder()
                .rowBuilder()
                .cell(paleBlueLeft, listOf(Text(times15, "The quick brown fox jumps over the lazy dog")))
                .cell(paleBlueLeft, listOf(Text(times15, "Etaoin shrdlu cmfwyp")))
                .buildRow()
                .buildPart()
                .buildTable()
                .wrap()
