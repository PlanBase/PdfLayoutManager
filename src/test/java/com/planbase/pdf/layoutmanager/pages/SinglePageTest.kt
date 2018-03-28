package com.planbase.pdf.layoutmanager.pages

import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_BLUE
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.*
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.Table
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import org.apache.pdfbox.pdmodel.common.PDRectangle.*
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

class SinglePageTest {
    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val page:SinglePage = pageMgr.page(0)
        val f = File("target/test-classes/melon.jpg")
        val melonPic = ImageIO.read(f)
        val melonHeight = 100f
        val melonWidth = 170f
        val bigMelon = ScaledImage(melonPic, Dim(melonWidth, melonHeight)).wrap()
        val bigText = Text(TextStyle(PDType1Font.TIMES_ROMAN, 90f, RGB_BLACK), "gN")

        val squareDim = Dim(squareSide, squareSide)

        val melonX = lp.body.topLeft.x
        val textX = melonX + melonWidth + 10
        val squareX = textX + bigText.maxWidth() + 10
        val lineX1 = squareX + squareSide + 10
        val cellX1 = lineX1 + squareSide + 10
        val tableX1 = cellX1 + squareSide + 10
        var y = lp.yBodyTop() - melonHeight

        while (y >= lp.yBodyBottom) {
            val imgHaP:HeightAndPage = page.drawImage(Coord(melonX, y), bigMelon, true)
            assertEquals(melonHeight, imgHaP.height)

            val txtHaP:HeightAndPage = page.drawStyledText(Coord(textX, y), bigText.text, bigText.textStyle, true)
            assertEquals(bigText.textStyle.lineHeight, txtHaP.height)

            val rectY = page.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
            assertEquals(squareSide, rectY)

            diamondRect(page, Coord(lineX1, y), squareSide)

            val cellDaP: DimAndPageNums = qbfCell.render(page, Coord(cellX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfCell.dim, cellDaP.dim, 0.00004f)

            val tableDaP: DimAndPageNums = qbfTable.render(page, Coord(tableX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfTable.dim, tableDaP.dim, 0.00002f)

            y -= melonHeight
        }

        pageMgr.commit()
        val os = FileOutputStream("singlePage.pdf")
        pageMgr.save(os)
    }
}
fun diamondRect(page:RenderTarget, lowerLeft: Coord, size:Float) {
    val ls = LineStyle(RGB_BLACK, 1f)
    val (xLeft, yBot) = lowerLeft
    val xRight = xLeft + size
    val yTop = yBot + size
    val halfSize = size / 2f
    val xMid = xLeft + halfSize
    val yMid = yBot + halfSize

    // Square drawn counter-clockwise (widdershins) from lowerLeft
    page.drawLineStrip(listOf(lowerLeft,
                              Coord(xRight, yBot),
                              Coord(xRight, yTop),
                              Coord(xLeft, yTop),
                              lowerLeft),
                       ls, true)

    val midTop = Coord(xMid, yTop)
    // Diamond drawn clockwise (deosil) from top
    page.drawLineStrip(listOf(midTop,
                              Coord(xRight, yMid),
                              Coord(xMid, yBot),
                              Coord(xLeft, yMid),
                              midTop),
                       ls, true)
}

const val squareSide = 70f
val times15 = TextStyle(PDType1Font.TIMES_ROMAN, 15f, RGB_BLACK)
val paleGreenLeft = CellStyle(Align.TOP_LEFT,
                              BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK)))
val paleBlueLeft = CellStyle(Align.TOP_LEFT,
                             BoxStyle(Padding(2f), RGB_LIGHT_BLUE, BorderStyle(RGB_BLACK)))
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
