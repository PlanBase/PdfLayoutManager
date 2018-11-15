// Copyright 2017-07-21 PlanBase Inc. & Glen Peterson
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Companion.DEFAULT_MARGIN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.*
import com.planbase.pdf.layoutmanager.attributes.Align.*
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.Table
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.BULLET_CHAR
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Another how-to-use example file
 */
class TestManual2 {

    @Test
    fun testBody() {
        // Nothing happens without a PdfLayoutMgr.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))

        val bodyWidth = PDRectangle.A6.width - 80.0

        val f = File("target/test-classes/graph2.png")
//    println(f.absolutePath)
        val graphPic = ImageIO.read(f)

        val lp = pageMgr.startPageGrouping(
                PORTRAIT,
                a6PortraitBody,
                { pageNum:Int, pb: SinglePage ->
                    val isLeft = pageNum % 2 == 1
                    val leftMargin:Double = if (isLeft) 37.0 else 45.0
                    //            System.out.println("pageNum " + pageNum);
                    pb.drawLine(Coord(leftMargin, 30.0), Coord(leftMargin + bodyWidth, 30.0),
                                LineStyle(CMYK_THISTLE), true)
                    pb.drawStyledText(Coord(leftMargin, 20.0), "Page # " + pageNum,
                                      TextStyle(PDType1Font.HELVETICA, 9.0, CMYK_BLACK), true)
                    leftMargin })

        val bulletTextCellStyle = CellStyle(
                Align.TOP_LEFT, BoxStyle(
                Padding.NO_PADDING, CMYK_PALE_PEACH,
                BorderStyle(
                        LineStyle.NO_LINE, LineStyle(CMYK_QUEEN_PINK),
                        LineStyle.NO_LINE, LineStyle.NO_LINE
                )))

        // Don't make bullets like this.  See WrappedListTest for the right way to do it.
        val bulletTable: Table = Table().addCellWidths(30.0, bodyWidth - 30.0)
                .partBuilder()
                .rowBuilder()
                .cell(BULLET_CELL_STYLE, listOf(Text(BULLET_TEXT_STYLE, BULLET_CHAR)))
                .cell(bulletTextCellStyle, listOf(Text(BULLET_TEXT_STYLE, "This is some text that has a bullet")))
                .buildRow()
                .rowBuilder()
                .cell(BULLET_CELL_STYLE, listOf(Text(BULLET_TEXT_STYLE, "2.")))
                .cell(bulletTextCellStyle, listOf(Text(BULLET_TEXT_STYLE, "Text that has a number")))
                .buildRow()
                .buildPart()

        val bodyCellStyle = CellStyle(TOP_LEFT_JUSTIFY, BoxStyle(Padding(2.0), CMYK_PALE_PEACH, BorderStyle(CMYK_QUEEN_PINK)))
        val bodyCellContinuation = CellStyle(TOP_LEFT_JUSTIFY, BoxStyle(Padding(2.0, 2.0, 8.0, 2.0), CMYK_PALE_PEACH,
                                                                        BorderStyle(
                                                                                LineStyle.NO_LINE,
                                                                                LineStyle(CMYK_QUEEN_PINK),
                                                                                LineStyle.NO_LINE,
                                                                                LineStyle(CMYK_QUEEN_PINK))))

        val imageCell = CellStyle(TOP_CENTER, BoxStyle(Padding(0.0, 0.0, 8.0, 0.0), CMYK_PALE_PEACH,
                                                       BorderStyle(
                                                               LineStyle.NO_LINE,
                                                               LineStyle(CMYK_QUEEN_PINK),
                                                               LineStyle.NO_LINE,
                                                               LineStyle(CMYK_QUEEN_PINK))))

        val heading = Cell(CellStyle(BOTTOM_LEFT,
                                     BoxStyle(Padding(10.0, 2.0, 0.0, 2.0), CMYK_PALE_PEACH,
                                              BorderStyle(LineStyle(CMYK_VIOLET, 1.0)))),
                           bodyWidth,
                           listOf(Text(HEADING_TEXT_STYLE, "Some Heading")))

        var coord = Coord(0.0, pageMgr.pageDim.height - 40.0)
        var dap: DimAndPageNums =
                lp.add(coord,
                       Cell(bodyCellStyle,
                            bodyWidth,
                            listOf(Text(BULLET_TEXT_STYLE,
                                        "The long "),
                                   Text(TextStyle(PDType1Font.HELVETICA_BOLD, 18.0, CMYK_BLACK),
                                        "families"),
                                   Text(BULLET_TEXT_STYLE,
                                        " needed the national words and women said new."))).wrap())
        assertEquals(IntRange(1, 1), dap.pageNums)

        coord = coord.minusY(dap.dim.height)
        dap = lp.add(coord,  heading.wrap())
        assertEquals(IntRange(1, 1), dap.pageNums)

        coord = coord.minusY(dap.dim.height + 0.5)
        dap = lp.add(coord, Cell(bodyCellContinuation, bodyWidth,
                                 listOf(Text(BULLET_TEXT_STYLE,
                                             "The new companies told the possible hands that the books" +
                                             " were low."))).wrap())
        assertEquals(IntRange(1, 1), dap.pageNums)

        coord = coord.minusY(dap.dim.height)
        dap = lp.add(coord,  bulletTable.wrap())
        assertEquals(IntRange(1, 1), dap.pageNums)

        coord = coord.minusY(dap.dim.height)
        dap = lp.add(coord, Cell(bodyCellContinuation, bodyWidth,
                                 listOf(Text(BULLET_TEXT_STYLE,
                                             "The new companies told the possible hands  and books was low. " +
                                             "The other questions got the recent children and lots felt" +
                                             " important."))).wrap())
        assertEquals(IntRange(1, 1), dap.pageNums)

        coord = coord.minusY(dap.dim.height)
        dap = lp.add(coord, Cell(imageCell, bodyWidth, listOf(ScaledImage(graphPic))).wrap())
        assertEquals(IntRange(1, 1), dap.pageNums)

        coord = coord.minusY(dap.dim.height)
        dap = lp.add(coord, Cell(bodyCellContinuation, bodyWidth,
                                 listOf(Text(BULLET_TEXT_STYLE,
                                             "The hard eyes seemed the clear mothers and systems came economic. " +
                                             "The high months showed the possible money and eyes heard certain." +
                                             "People played the different facts and areas showed large. "))).wrap())
        assertEquals(IntRange(1, 2), dap.pageNums)

        coord = coord.minusY(dap.dim.height)
        dap = lp.add(coord,  heading.wrap())
        assertEquals(IntRange(2, 2), dap.pageNums)

        coord = coord.minusY(dap.dim.height + 0.5)
        dap = lp.add(coord, Cell(bodyCellContinuation, bodyWidth,
                                 listOf(Text(BULLET_TEXT_STYLE,
                                             "The good ways lived the different countries and stories found good." +
                                             " The certain places found the political months and facts told easy." +
                                             " The long homes ran the good governments and cases lived social."),
                                        ScaledImage(graphPic),
                                        Text(BULLET_TEXT_STYLE,
                                             ("The social people ran the local cases and men left local. The " +
                                              "easy areas saw the whole times and systems. The major rights " +
                                              "was the important children and mothers turned unimaginatively.")),
                                        ScaledImage(graphPic),
                                        Text(BULLET_TEXT_STYLE,
                                             ("The best points got the economic waters " +
                                              "and problems gave great. The whole " +
                                              "countries went the best children and " +
                                              "eyes became able to see clearly.")))).wrap())
        assertEquals(IntRange(2, 3), dap.pageNums)

        coord = coord.minusY(dap.dim.height)
        lp.drawLine(coord, coord.plusX(bodyWidth), LineStyle(CMYK_QUEEN_PINK))

        pageMgr.commit()
        // We're just going to write to a file.
        val os = FileOutputStream("test2.pdf")

        // Commit it to the output stream!
        pageMgr.save(os)
    }

    companion object {

        val CMYK_COOL_GRAY = PDColor(floatArrayOf(0.13f, 0.2f, 0f, 0.57f), PDDeviceCMYK.INSTANCE)
        val CMYK_LIGHT_GREEN = PDColor(floatArrayOf(0.05f, 0f, 0.1f, 0.01f), PDDeviceCMYK.INSTANCE)
        val CMYK_QUEEN_PINK = PDColor(floatArrayOf(0.0f, 0.11f, 0f, 0.09f), PDDeviceCMYK.INSTANCE)
        val CMYK_PALE_PEACH = PDColor(floatArrayOf(0.0f, 0.055f, 0.06f, 0f), PDDeviceCMYK.INSTANCE)
        val CMYK_THISTLE = PDColor(floatArrayOf(0.05f, 0.19f, 0f, 0.09f), PDDeviceCMYK.INSTANCE)
        val CMYK_VIOLET = PDColor(floatArrayOf(0.46f, 0.48f, 0f, 0f), PDDeviceCMYK.INSTANCE)

        val a6PortraitBody = PageArea(Coord(DEFAULT_MARGIN, PDRectangle.A6.height - DEFAULT_MARGIN),
                                      Dim(PDRectangle.A6).minus(Dim(DEFAULT_MARGIN * 2, DEFAULT_MARGIN * 2)))

        internal val BULLET_CELL_STYLE = CellStyle(TOP_RIGHT,
                                                   BoxStyle(Padding(0.0, 4.0, 15.0, 0.0), CMYK_PALE_PEACH,
                                                            BorderStyle(
                                                                    LineStyle.NO_LINE, LineStyle.NO_LINE,
                                                                    LineStyle.NO_LINE, LineStyle(CMYK_QUEEN_PINK))))
        internal val BULLET_TEXT_STYLE = TextStyle(PDType1Font.HELVETICA, 12.0, CMYK_BLACK, "BULLET_TEXT_STYLE")

        internal val HEADING_TEXT_STYLE = TextStyle(PDType1Font.TIMES_BOLD, 16.0, CMYK_COOL_GRAY, "HEADING_TEXT_STYLE")
    }
}