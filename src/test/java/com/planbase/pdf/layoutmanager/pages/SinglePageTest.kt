package com.planbase.pdf.layoutmanager.pages

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.RGB_BLACK
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.Point2
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
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val page = pageMgr.page(0)
        val f = File("target/test-classes/melon.jpg")
        val melonPic = ImageIO.read(f)
        val melonHeight = 100f
        val melonWidth = 170f
        val bigMelon = ScaledImage(melonPic, XyDim(melonWidth, melonHeight)).wrap()
        val text = Text(TextStyle(PDType1Font.HELVETICA_BOLD, 11f, RGB_BLACK), "Hello")

        val squareSide = 70f
        val squareDim = XyDim(squareSide, squareSide)

        // TODO: The topLeft parameters on RenderTarget are actually LOWER-left.

        val melonX = lp.bodyTopLeft().x
        val textX = melonX + melonWidth + 10
        val squareX = textX + text.maxWidth() + 10
        val lineX1 = squareX + squareSide + 10
        val lineX2 = lineX1 + 100
        var y = lp.yBodyTop() - melonHeight

        while(y >= lp.yBodyBottom()) {
            val imgY = page.drawImage(Point2(melonX, y), bigMelon)
            assertEquals(melonHeight, imgY)

            val txtY = page.drawStyledText(Point2(textX, y), text.text, text.textStyle)
            assertEquals(text.textStyle.lineHeight(), txtY)

            val rectY = page.fillRect(Point2(squareX, y), squareDim, RGB_BLACK)
            assertEquals(squareSide, rectY)

            page.drawLine(Point2(lineX1, y), Point2(lineX2, y), LineStyle(RGB_BLACK, 1f))

            y -= melonHeight
        }

        lp.commit()
        val os = FileOutputStream("singlePage.pdf")
        pageMgr.save(os)
    }
}