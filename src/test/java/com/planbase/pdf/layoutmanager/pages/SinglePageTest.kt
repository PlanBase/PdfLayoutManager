package com.planbase.pdf.layoutmanager.pages

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d
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
        val text = Text(TextStyle(PDType1Font.TIMES_ROMAN, 90f, RGB_BLACK), "gxNh")

        val squareSide = 70f
        val squareDim = Dimensions(squareSide, squareSide)

        // TODO: The topLeft parameters on RenderTarget are actually LOWER-left.

        val melonX = lp.bodyTopLeft().x
        val textX = melonX + melonWidth + 10
        val squareX = textX + text.maxWidth() + 10
        val lineX1 = squareX + squareSide + 10
        var y = lp.yBodyTop() - melonHeight

        while(y >= lp.yBodyBottom()) {
            val imgY = page.drawImage(Point2d(melonX, y), bigMelon)
            assertEquals(melonHeight, imgY)

            val txtY = page.drawStyledText(Point2d(textX, y), text.text, text.textStyle)
            assertEquals(text.textStyle.lineHeight(), txtY)

            val rectY = page.fillRect(Point2d(squareX, y), squareDim, RGB_BLACK)
            assertEquals(squareSide, rectY)

            diamondRect(page, Point2d(lineX1, y), 70f)

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
                       ls)

    val midTop = Point2d(xMid, yTop)
    // Diamond drawn clockwise (deosil) from top
    page.drawLineStrip(listOf(midTop,
                              Point2d(xRight, yMid),
                              Point2d(xMid, yBot),
                              Point2d(xLeft, yMid),
                              midTop),
                       ls)
}
