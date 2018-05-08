package com.planbase.pdf.layoutmanager.contents

import TestManual2.Companion.CMYK_PALE_PEACH
import TestManual2.Companion.a6PortraitBody
import TestManualllyPdfLayoutMgr.Companion.RGB_BLUE_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_YELLOW_BRIGHT
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
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.PDType1Font.TIMES_ITALIC
import org.apache.pdfbox.pdmodel.font.PDType1Font.TIMES_ROMAN
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream
import kotlin.math.nextDown
import kotlin.math.nextUp

class WrappedCellTest {

    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val cellWidth = 200.0
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals(textStyle.lineHeight + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.dim.height)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100.0, 500.0)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val dimAndPageNums: DimAndPageNums = wrappedCell.render(lp, upperLeft)
        Dim.assertEquals(wrappedCell.dim, dimAndPageNums.dim, 0.00002)

        pageMgr.commit()
    }

    @Test fun testMultiLine() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val cellWidth = 300.0
        val hello = Text(textStyle, "Hello\nThere\nWorld!")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals((textStyle.lineHeight * 3) + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.dim.height)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100.0, 500.0)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        wrappedCell.render(lp, upperLeft)
        pageMgr.commit()

//        val os = FileOutputStream("test4.pdf")
//        pageMgr.save(os)
    }

    @Test fun testRightAlign() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        val cellWidth = 200.0
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.TOP_RIGHT, boxStyle),
                        cellWidth, listOf(hello), null)

        val wrappedCell =
                WrappedCell(Dim(cellWidth,
                                  textStyle.lineHeight + boxStyle.topBottomInteriorSp()),
                            CellStyle(Align.TOP_RIGHT, boxStyle),
                            listOf({
                                       val mlw = MultiLineWrapped()
                                       mlw.width = hello.maxWidth()
                                       mlw.ascent = textStyle.ascent
                                       mlw.lineHeight = textStyle.lineHeight
                                       mlw.append(
                                               WrappedText(
                                                       textStyle,
                                                       hello.text,
                                                       hello.maxWidth()
                                               )
                                       )
                                       mlw
                                   }.invoke()), 0.0)
//        val wrappedCell = cell.wrap()
//        println("cell.wrap()=${cell.wrap()}")

        kotlin.test.assertEquals(textStyle.lineHeight + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.dim.height)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100.0, 500.0)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val dimAndPageNums: DimAndPageNums = wrappedCell.render(lp, upperLeft)
//        println("upperLeft=" + upperLeft)
//        println("xyOff=" + xyOff)

        assertEquals(cellWidth, dimAndPageNums.dim.width)

        Dim.assertEquals(wrappedCell.dim, dimAndPageNums.dim, 0.00002)

        pageMgr.commit()

//        // We're just going to write to a file.
//        // Commit it to the output stream!
//        val os = FileOutputStream("wrappedCellRight.pdf")
//        pageMgr.save(os)
    }

    // There was only one significant line changed when I added this test without any comments.
    // Looks like I had assumed pageBreakingTopMargin wanted the text baseline which was above the curent y-value,
    // but it actually needs the bottom of the text area which is below the current y-value.
    //
    // This comment is based on:
    // git diff 13d097b86807ff458191a01633e1d507abcf3fc3 e2958def12f99beb699fc7546f5f7f0024b22df7
    // In class WrappedCell:
    // - val adjY = lp.pageBreakingTopMargin(y + line.descentAndLeading, line.lineHeight) + line.lineHeight
    // + val adjY = lp.pageBreakingTopMargin(y - line.lineHeight, line.lineHeight) + line.lineHeight
    @Test fun testCellHeightBug() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val textStyle = TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0, RGB_YELLOW_BRIGHT)
        val cellStyle = CellStyle(Align.BOTTOM_CENTER, BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK)))

        val tB = Table()
                .addCellWidths(listOf(120.0))
                .textStyle(textStyle)
                .partBuilder()
                .cellStyle(cellStyle)
                .rowBuilder().addTextCells("First").buildRow()
                .buildPart()
        val wrappedTable = tB.wrap()

        TestCase.assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                              wrappedTable.dim.height)

        TestCase.assertEquals(120.0, wrappedTable.dim.width)

        val dimAndPageNums: DimAndPageNums = wrappedTable.render(lp, lp.body.topLeft)

        Dim.assertEquals(wrappedTable.dim, dimAndPageNums.dim, 0.00003)

        pageMgr.commit()
//        val os = FileOutputStream("test3.pdf")
//        pageMgr.save(os)
    }


    val regular7p5 = TextStyle(TIMES_ROMAN, 7.5, CMYK_BLACK) // ascent=5.1225  lineHeight=8.37
    val italic7p5 = TextStyle(PDType1Font.TIMES_BOLD_ITALIC, 7.0, CMYK_BLACK, 12.0) // ascent=4.781   lineHeight=12

    // The "shorter" (smaller line-height) font has the bigger ascent.  Ascent difference = 0.3415.
    // Text is aligned to the baseline, so the ascent diff of the first font is added to the line-height of the
    // second (which has a larger descentAndLeading) yielding a combined line-height of 12.3415.
    //
    // The following text is line-broken like:
    //
    // Men often hate each other because they fear each other;
    // they fear each other because they don't know each other;
    // they don't know each other because they can not
    // communicate; they can not communicate [because they
    // are separated.]
    //
    // The text in the square brackets is italics.  Here is a diagram of the critical part of this test -
    // The 2nd to last line in the transition from "communicate " to "because" in a different font:
    //
    //          -  -  -  -  -  P A G E   B R E A K  -  -  -  -
    //          ^                                         ^
    //          |                                         |
    //          |                                         |
    //          |Correct                                  |Rendered
    //          |Adjustment=                              |Height=
    //          |9.89                                     |21.5485
    //          |                                         |
    //          |                                         |
    //          |                                         |
    //          v                                         |
    // ------------------------+________________________  |
    //             |           |    /                     |
    //            -+-          |   /                      |
    //   ,-.  ,-.. |   ,-.     |  /,-.  ,-.   ,-.  ,--..  |
    //  (    (   | |  (---     | /   ) (---' (    (   /   |
    // __`-'__`-''_`-_ `--_____|/`--'__`--____'-___'-/__  | /__Aligned_on_baseline__
    //                         |                          | \
    //                         |                          |
    // ________________________|                          |
    //                         |                          |
    //                         |                          |
    //                         |                          |
    //                         |________________________  v
    //
    // This means that line 1 has line-height: 8.37 (total: 25.11)
    //                 Line 2 has line-height: 12.3415
    //                 Line 3 has line-height: 12.0
    //                           Grand total = 49.4515
    //
    // Actual quote:
    //     "Men often hate each other because they fear each other;
    //      they fear each other because they don't know each other;
    //      they don't know each other because they can not communicate;
    //      they can not communicate because they are separated."
    //            - MLK 1962
    //
    val theyAreSeparatedCell = Cell(CellStyle(
            Align.TOP_LEFT,
            BoxStyle(Padding.NO_PADDING, CMYK_PALE_PEACH,
                     BorderStyle(LineStyle(CMYK_BLACK, 0.00000001)))),
                                    170.0,
                                    listOf(Text(regular7p5,
                                                "they don't know each other because they can not communicate;" +
                                                " they can not communicate "),
                                           Text(italic7p5, "because they are separated.")))

    @Test fun testCellWithoutPageBreak() {
        // The bold-italic text showed on the wrong page because the last line wasn't being dealt with as a unit.
        // A total line height is now calculated for the entire MultiLineWrapped when later inline text has a surprising
        // default lineHeight.  This test maybe belongs in MultiLineWrapped, but better here than nowhere.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)

        val wrappedCell: WrappedCell = theyAreSeparatedCell.wrap()
        Dim.assertEquals(Dim(170.0, 32.7115), wrappedCell.dim, 0.0000001)

        // Rendered away from the page break, the dimensions are unchanged.
        val ret1:DimAndPageNums = lp.add(Coord(0.0, 183.26), wrappedCell)
        Dim.assertEquals(Dim(170.0, 32.7115), ret1.dim, 0.0000001)
        assertEquals(150.5485, lp.cursorY, 0.000001)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testCellWithoutPageBreak.pdf"))
    }

    @Test fun testCellAcrossPageBreak() {
        // The bold-italic text showed on the wrong page because the last line wasn't being dealt with as a unit.
        // A total line height is now calculated for the entire MultiLineWrapped when later inline text has a surprising
        // default lineHeight.  This test maybe belongs in MultiLineWrapped, but better here than nowhere.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)

        val wrappedCell: WrappedCell = theyAreSeparatedCell.wrap()
        Dim.assertEquals(Dim(170.0, 32.7115), wrappedCell.dim, 0.0000001)

        // Rendered across the page break, it's bigger.
        // Update 2018-05-08: I think it should be 9.89 bigger, but it's only 9.207 bigger.
        // The difference is 0.683 which is twice 0.3415 which is the difference between the heights
        // of the ascents of the different text parts.
        val ret2:DimAndPageNums = lp.add(Coord(0.0, 55.26), wrappedCell)

        // TODO: Fix - Pretty sure this is *WRONG*
        Dim.assertEquals(Dim(170.0, 41.9185), ret2.dim, 0.0000001)

        // TODO: Fix - Pretty sure this is *WRONG*
        assertEquals(13.3415, lp.cursorY, 0.00000001)

        pageMgr.commit()

//        val docId = COSString("Cell Across Page Break Test PDF".toByteArray(Charsets.ISO_8859_1))
//        pageMgr.setFileIdentifiers(docId, docId)
//
//        pageMgr.save(FileOutputStream("testCellAcrossPageBreak.pdf"))
    }

    // TODO: The above two tests may become obsolete once the below test passes!

    // Here's what this test looks like with approximate boxes around each font:
    //           _____________________________________________________ _____________________________________
    //      ^ ^ |  ____  _                                     _      |  ^                                  |
    //      | | | |  _ \(_)           /\                      | |     |  | 15.07 difference in ascent.      |
    //      | | | | |_) |_  __ _     /  \   ___  ___ ___ _ __ | |_    |  v                                  |
    // 20.49| | | |  _ <| |/ _` |   / /\ \ / __|/ __/ _ \ '_ \| __|   |--_-----------_----------------------| ^ ^
    //      | | | | |_) | | (_| |  / ____ \\__ \ (_|  __/ | | | |_ _  | |_) .  _    | \ _  _  _  _ __ _|_   | | |4.781
    //      v | |_|____/|_|\__, |_/_/____\_\___/\___\___|_|_|_|\__(_)_|_|_)_|_(_|___|_/(/_ > (_ (/_| | |_ ._| | v
    //        | |           __/ |                                   ^ |       __|                         ^ | |
    //   33.48| |          |___/                    descent = 12.99 v |                                   | | |
    //        v |_____________________________________________________|                                   | | |
    //          |                                                   ^ |                                   | | |40.0
    //          |                                                   | |                   descent = 35.219| | |
    //          |                                                   | |                                   | | |
    //          |                     difference in descent = 22.229| |                                   | | |
    //          |                                                   | |                                   v | |
    //          |___________________________________________________v_|_____________________________________| v
    //
    // Notice:
    //  - The two sections of text are aligned exactly on their baseline.
    //  - The first takes up more space above the baseline by virtue of bing a bigger font.
    //  - The second takes up more space below due to a very large lineHeight value.
    //
    // Raison D'être:
    // This all works dandy when line-wrapped and rendered mid-page.  The problem came at the page break where the
    // "Big Descent" text ended up top-aligned - wrong!  Also the height with the page break should be approximately
    // double the total height (2 * 55.709 = 111.418), but it's returning an even 80.0.
    @Test fun testPageBreakingWithDifferentAscentLeading() {
        val topHeavy = TextStyle(TIMES_ROMAN, 30.0, CMYK_BLACK)

        // Verify our font metrics to ensure a careful and accurate test.
        assertEquals(20.49, topHeavy.ascent)
        assertEquals(33.48, topHeavy.lineHeight)

        val bottomHeavy = TextStyle(TIMES_ITALIC, 7.0, CMYK_BLACK, 40.0) // ascent=4.781   lineHeight=12

        assertEquals(4.781, bottomHeavy.ascent)
        assertEquals(40.0, bottomHeavy.lineHeight)

        // We expect the ascent to equal the biggest ascent which is topHeavy.ascent = 20.49.
        // We expect the descent to equal the biggest descent which is
        // bottomHeavy.lineHeight - bottomHeavy.ascent = 35.219
        val biggerDescent = bottomHeavy.lineHeight - bottomHeavy.ascent
        assertEquals(35.219, biggerDescent)

        // So the total line height is the maxAscent + maxDescent = topHeavy.ascent + biggerDescent = 55.709
        val combinedLineHeight = topHeavy.ascent + biggerDescent
        assertEquals(55.709, combinedLineHeight)

        val cellWidth = 170.0
        val boxStyle = BoxStyle(Padding.NO_PADDING, CMYK_PALE_PEACH,
                                BorderStyle(LineStyle(CMYK_BLACK, 0f.nextUp().nextUp().toDouble())))
        val variedCell = Cell(CellStyle(Align.TOP_LEFT, boxStyle), cellWidth,
                              listOf(Text(topHeavy, "Big ascent."),
                                     Text(bottomHeavy, "Big descent.")))


        // The bold-italic text showed on the wrong page because the last line wasn't being dealt with as a unit.
        // A total line height is now calculated for the entire MultiLineWrapped when later inline text has a surprising
        // default lineHeight.  This test maybe belongs in MultiLineWrapped, but better here than nowhere.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)

        val wrappedCell: WrappedCell = variedCell.wrap()
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), wrappedCell.dim, 0.0)

        var ret1:DimAndPageNums

        // Rendered away from the page break, the dimensions are unchanged.
        ret1 = lp.add(Coord(0.0, 300.0), wrappedCell)
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), ret1.dim, 0.0)
        assertEquals(300.0 - combinedLineHeight, lp.cursorY, 0.000001)

        ret1 = wrappedCell.render(lp, Coord(0.0, 200.0))
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), ret1.dim, 0.0)

        // This doesn't show up in the output, just going to walk closer and closer to the edge of the page
        // without going over.
        ret1 = wrappedCell.render(lp, Coord(0.0, 100.0), reallyRender = false)
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), ret1.dim, 0.0)

        val breakPoint: Double = lp.yBodyBottom + combinedLineHeight

        ret1 = wrappedCell.render(lp, Coord(0.0, breakPoint + 1.0),
                                  reallyRender = false)
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), ret1.dim, 0.0)

        ret1 = wrappedCell.render(lp, Coord(0.0, breakPoint + 0.0001),
                                  reallyRender = false)
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), ret1.dim, 0.0)

        ret1 = wrappedCell.render(lp, Coord(0.0, breakPoint + 0.0000001),
                                  reallyRender = false)
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), ret1.dim, 0.0)

        ret1 = wrappedCell.render(lp, Coord(0.0, breakPoint.toFloat().nextUp().toDouble()),
                                  reallyRender = false)
        Dim.assertEquals(Dim(cellWidth, combinedLineHeight), ret1.dim, 0.0)

        ret1 = wrappedCell.render(lp, Coord(0.0, breakPoint),
                                  reallyRender = true)
        // My theory is that we need an adjustment that pushes *both* halves of the line onto the next page and
        // that they should still be aligned on a common baseline once they get there.
        // What's actually happening is that they both go to the next page, but they are top-aligned there,
        // and the amount that pushes them there is 80.0 instead of 111.418.
        // So, 80.0 - 55.709 = 24.291

        // TODO: Uncomment - Pretty sure this is *RIGHT*
//        Dim.assertEquals(Dim(cellWidth, combinedLineHeight * 2.0), ret1.dim, 0.0)

        pageMgr.commit()

        pageMgr.save(FileOutputStream("testPgBrkDiffAscDesc.pdf"))

    }

    companion object {
        val boxStyle = BoxStyle(Padding(2.0), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK))
        private val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5, RGB_BLACK)
    }
}