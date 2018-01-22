package com.planbase.pdf.layoutmanager.pages

import TestManual2.Companion.BULLET_TEXT_STYLE
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDRectangle.*
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.imageio.ImageIO

class PageGroupingTest {
    @Test
    @Throws(IOException::class)
    fun testBasics() {
        var pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        var lp = pageMgr.startPageGrouping()

        // Just testing some default values before potentially merging changes that could make
        // these variable.
        assertEquals((LETTER.width - PdfLayoutMgr.DEFAULT_MARGIN).toDouble(), lp.yBodyTop().toDouble(), 0.000000001)
        assertEquals(PdfLayoutMgr.DEFAULT_MARGIN.toDouble(), lp.lowerLeftBody.y.toDouble(), 0.000000001)
        assertEquals(LETTER.height.toDouble(), lp.pageWidth().toDouble(), 0.000000001)
        assertEquals((LETTER.width - PdfLayoutMgr.DEFAULT_MARGIN * 2).toDouble(), lp.bodyDim.height.toDouble(), 0.000000001)

        lp = pageMgr.startPageGrouping(PORTRAIT)

        assertEquals((LETTER.height - PdfLayoutMgr.DEFAULT_MARGIN).toDouble(), lp.yBodyTop().toDouble(), 0.000000001)
        assertEquals(PdfLayoutMgr.DEFAULT_MARGIN.toDouble(), lp.lowerLeftBody.y.toDouble(), 0.000000001)
        assertEquals(LETTER.width.toDouble(), lp.pageWidth().toDouble(), 0.000000001)
        assertEquals((LETTER.height - PdfLayoutMgr.DEFAULT_MARGIN * 2).toDouble(), lp.bodyDim.height.toDouble(), 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        lp.commit()
        pageMgr.save(ByteArrayOutputStream())

        // Make a new manager for a new test.
        pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(A1))
        lp = pageMgr.startPageGrouping(PORTRAIT)

        assertEquals((A1.height - PdfLayoutMgr.DEFAULT_MARGIN).toDouble(), lp.yBodyTop().toDouble(), 0.000000001)
        assertEquals(PdfLayoutMgr.DEFAULT_MARGIN.toDouble(), lp.lowerLeftBody.y.toDouble(), 0.000000001)
        assertEquals(A1.width.toDouble(), lp.pageWidth().toDouble(), 0.000000001)
        assertEquals((A1.height - PdfLayoutMgr.DEFAULT_MARGIN * 2).toDouble(), lp.bodyDim.height.toDouble(), 0.000000001)

        lp = pageMgr.startPageGrouping()

        assertEquals((A1.width - PdfLayoutMgr.DEFAULT_MARGIN).toDouble(), lp.yBodyTop().toDouble(), 0.000000001)
        assertEquals(PdfLayoutMgr.DEFAULT_MARGIN.toDouble(), lp.lowerLeftBody.y.toDouble(), 0.000000001)
        assertEquals(A1.height.toDouble(), lp.pageWidth().toDouble(), 0.000000001)
        assertEquals((A1.width - PdfLayoutMgr.DEFAULT_MARGIN * 2).toDouble(), lp.bodyDim.height.toDouble(), 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        lp.commit()
        pageMgr.save(ByteArrayOutputStream())

        val topM = 20f
        val bottomM = 60f
        // Make a new manager for a new test.
        pageMgr = PdfLayoutMgr(PDDeviceGray.INSTANCE, Dim(A6))
        lp = PageGrouping(pageMgr, PORTRAIT, Coord(PdfLayoutMgr.DEFAULT_MARGIN, bottomM),
                          pageMgr.pageDim.minus(Dim(PdfLayoutMgr.DEFAULT_MARGIN * 2, topM + bottomM)))

        assertEquals((A6.height - topM).toDouble(), lp.yBodyTop().toDouble(), 0.000000001)
        assertEquals(bottomM.toDouble(), lp.lowerLeftBody.y.toDouble(), 0.000000001)
        assertEquals(A6.width.toDouble(), lp.pageWidth().toDouble(), 0.000000001)
        assertEquals((A6.height - (topM + bottomM)).toDouble(), lp.bodyDim.height.toDouble(), 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        lp.commit()
        pageMgr.save(ByteArrayOutputStream())

        // Make a new manager for a new test.
        pageMgr = PdfLayoutMgr(PDDeviceGray.INSTANCE, Dim(A6))
        lp = PageGrouping(pageMgr, LANDSCAPE, Coord(PdfLayoutMgr.DEFAULT_MARGIN, bottomM),
                          pageMgr.pageDim.swapWh()
                                                                       .minus(Dim(PdfLayoutMgr.DEFAULT_MARGIN * 2, topM + bottomM)))

        assertEquals((A6.width - topM).toDouble(), lp.yBodyTop().toDouble(), 0.000000001)
        assertEquals(bottomM.toDouble(), lp.lowerLeftBody.y.toDouble(), 0.000000001)
        assertEquals(A6.height.toDouble(), lp.pageWidth().toDouble(), 0.000000001)
        assertEquals((A6.width - (topM + bottomM)).toDouble(), lp.bodyDim.height.toDouble(), 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        lp.commit()
        pageMgr.save(ByteArrayOutputStream())
    }

    @Test fun testBasics2() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()
        val f = File("target/test-classes/melon.jpg")
        val melonPic = ImageIO.read(f)
        val melonHeight = 98f
        val melonWidth = 170f
        val bigMelon = ScaledImage(melonPic, Dim(melonWidth, melonHeight)).wrap()
        val bigText = Text(TextStyle(PDType1Font.TIMES_ROMAN, 93.75f, RGB_BLACK), "gN")

        val squareDim = Dim(squareSide, squareSide)

        val melonX = lp.bodyTopLeft().x
        val textX = melonX + melonWidth + 10
        val squareX = textX + bigText.maxWidth() + 10
        val lineX1 = squareX + squareSide + 10
        val cellX1 = lineX1 + squareSide + 10
        val tableX1 = cellX1 + squareSide + 10
        var y = lp.yBodyTop() - melonHeight

        while(y >= lp.lowerLeftBody.y) {
            val imgY = lp.drawImage(Coord(melonX, y), bigMelon, true)
            assertEquals(melonHeight, imgY)

            val txtY = lp.drawStyledText(Coord(textX, y), bigText.text, bigText.textStyle, true)
            assertEquals(bigText.textStyle.lineHeight, txtY)

            val rectY = lp.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
            assertEquals(squareSide, rectY)

            diamondRect(lp, Coord(lineX1, y), squareSide)

            val cellDim = qbfCell.render(lp, Coord(cellX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfCell.dim, cellDim, 0.00004f)

            val tableDim = qbfTable.render(lp, Coord(tableX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfTable.dim, tableDim, 0.00002f)

            y -= melonHeight
        }

        // This is the page-break.
        // Images must vertically fit entirely on one page,
        // So they are pushed down as necessary to fit.
        val imgY2: Float = lp.drawImage(Coord(melonX, y), bigMelon, true)
        assertTrue(melonHeight < imgY2) // When the picture breaks across the page, extra height is added.

        // Words must vertically fit entirely on one page,
        // So they are pushed down as necessary to fit.
        val txtY2: Float = lp.drawStyledText(Coord(textX, y), bigText.text, bigText.textStyle, true)
        assertTrue(bigText.textStyle.lineHeight < txtY2)

        // Rectangles span multiple pages, so their height should be unchanged.
        val rectY2: Float = lp.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
        assertEquals(squareSide, rectY2)

        // Lines span multiple pages, so their height should be unchanged.
        // Also, lines don't have a height.
        diamondRect(lp, Coord(lineX1, y), squareSide)
//            lp.drawLine(Coord(lineX1, y), Coord(lineX2, y), LineStyle(RGB_BLACK, 1f))

        val cellDim2 = qbfCell.render(lp, Coord(cellX1, y + qbfCell.dim.height))
//        println("qbfCell.dim=${qbfCell.dim} tableDim2=${cellDim2}")
        assertTrue(qbfCell.dim.height < cellDim2.height)

//        val tableDim2 = qbfTable.render(lp, Coord(tableX1, y))
        val tableDim2 = qbfTable.render(lp, Coord(tableX1, y + qbfCell.dim.height))
//        println("qbfTable.dim=${qbfTable.dim} tableDim2=${tableDim2}")

        assertTrue(qbfTable.dim.height < tableDim2.height)
        assertEquals(qbfCell.dim.height, qbfTable.dim.height)
        assertEquals(cellDim2.height, tableDim2.height)

        y -= listOf(imgY2, txtY2, rectY2).max() as Float

        while(y >= lp.lowerLeftBody.y - 400) {
            val imgY:Float = lp.drawImage(Coord(melonX, y), bigMelon, true)
            assertEquals(melonHeight, imgY)

            val txtY:Float = lp.drawStyledText(Coord(textX, y), bigText.text, bigText.textStyle, true)
            assertEquals(bigText.textStyle.lineHeight, txtY)

            val rectY:Float = lp.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
            assertEquals(squareSide, rectY)

            diamondRect(lp, Coord(lineX1, y), squareSide)
//            lp.drawLine(Coord(lineX1, y), Coord(lineX2, y), LineStyle(RGB_BLACK, 1f))

            val cellDim = qbfCell.render(lp, Coord(cellX1, y + qbfCell.dim.height))
            assertEquals(qbfCell.dim, cellDim)

//            val tableDim = qbfTable.render(lp, Coord(tableX1, y))
            val tableDim = qbfTable.render(lp, Coord(tableX1, y + qbfCell.dim.height))
            assertEquals(qbfTable.dim, tableDim)

            y -= listOf(imgY, txtY, rectY).max() as Float
        }

        lp.commit()
        val os = FileOutputStream("pageGrouping.pdf")
        pageMgr.save(os)
    }

    @Test fun testPageBreakingTopMargin() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val bodyWidth = PDRectangle.A6.width - 80f

        val f = File("target/test-classes/graph2.png")
        val graphPic = ImageIO.read(f)

        val lp = pageMgr.startPageGrouping(PORTRAIT, null)

        val cell = Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2f), null, BorderStyle(LineStyle(CMYK_BLACK, 0.1f)))),
                        bodyWidth,
                        listOf(Text(BULLET_TEXT_STYLE,
                                    ("The " +
                                     "best points got the economic waters " +
                                     "and problems gave great. The whole " +
                                     "countries went the best children and " +
                                     "eyes came able.")),
                               Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2f), null, BorderStyle.NO_BORDERS)),
                                    bodyWidth - 6f,
                                    listOf(Text(BULLET_TEXT_STYLE,
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  " +
                                                "This paragraph is too long to fit on a single page.  ")), 30f),
                               ScaledImage(graphPic)))
        val wrappedCell = cell.wrap()
        assertEquals(Dim(217.63782f, 509.17203f), wrappedCell.dim)

        // This is not a great test because I'm not sure this feature is really meant to work blocks that cross
        // multiple pages.  In fact, it looks pretty bad for those blocks.
        val finalDim = wrappedCell.render(lp, Coord(40f, PDRectangle.A6.height - 40f))
        assertEquals(Dim(217.63782f, 800.7112f), finalDim)

        lp.commit()

//        val os = FileOutputStream("testPageBreakingTopMargin.pdf")
//        pageMgr.save(os)
    }

    @Test fun testPageBreakWithInlineNearBottom() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val bodyWidth = PDRectangle.A6.width - 80f
        val lp = pageMgr.startPageGrouping(PORTRAIT, null)

        val cell = Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2f), null, BorderStyle(LineStyle(CMYK_BLACK, 0.1f)))),
                        bodyWidth,
                        listOf(Text(BULLET_TEXT_STYLE,
                                    ("Some stuff.")),
                               Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2f), null, BorderStyle.NO_BORDERS)),
                                    bodyWidth - 6f,
                                    listOf(Text(BULLET_TEXT_STYLE,
                                                "This paragraph is too long to fit on a single page.  " +
                                                        "This paragraph is too long to fit on a single page.  "),
                                           Text(TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 12f, CMYK_BLACK),
                                                "This is supposed to span the page break.")))))
        val wrappedCell = cell.wrap()
        assertEquals(Dim(217.63782f, 78.276f), wrappedCell.dim)

        // This is not a great test because I'm not sure this feature is really meant to work blocks that cross
        // multiple pages.  In fact, it looks pretty bad for those blocks.
        val finalDim = wrappedCell.render(lp, Coord(40f, 110f))
        assertEquals(Dim(217.63782f, 87.27999f), finalDim)

        lp.commit()

//        pageMgr.save(FileOutputStream("testPageBreakWithInlineNearBottom.pdf"))
    }
}