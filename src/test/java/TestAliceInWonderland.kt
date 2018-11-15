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

import TestManual2.Companion.a6PortraitBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.Align.BOTTOM_LEFT
import com.planbase.pdf.layoutmanager.attributes.Align.TOP_LEFT_JUSTIFY
import com.planbase.pdf.layoutmanager.attributes.BorderStyle.Companion.NO_BORDERS
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Table
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.util.Charsets
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.TreeMap
import kotlin.test.assertEquals

/**
 * Another how-to-use example file
 */
class TestAliceInWonderland {

    @Test
    fun testBook() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))

        val titleFontFile = File("target/test-classes/EmilysCandy-Regular.ttf")
        val titleFont: PDType0Font = pageMgr.loadTrueTypeFont(titleFontFile)

        val handwritingFontFile = File("target/test-classes/MrDeHaviland-Regular.ttf")
        val handwritingFont: PDType0Font = pageMgr.loadTrueTypeFont(handwritingFontFile)

        val incipit = TextStyle(titleFont, 36.0, CMYK_BLACK, "incipit", 30.0)
        val chapTitleCellStyle = CellStyle(BOTTOM_LEFT, BoxStyle(Padding(50.0, 0.0, 10.0, 0.0), null, NO_BORDERS))
        val bodyCellStyle = CellStyle(TOP_LEFT_JUSTIFY, BoxStyle(Padding(10.0, 0.0, 0.0, 0.0), null, NO_BORDERS))
        val heading = TextStyle(titleFont, 16.0, CMYK_BLACK, "heading", 16.0, 0.0, 0.2, 0.0)
        val bodyText = TextStyle(PDType1Font.TIMES_ROMAN, 12.0, CMYK_BLACK)
        val handwriting = TextStyle(handwritingFont, 17.0, CMYK_BLACK, "handwriting", 20.0, wordSpacing = 4.0)
        val thought = TextStyle(PDType1Font.TIMES_ITALIC, 12.0, CMYK_BLACK)

        val lp = pageMgr.startPageGrouping(
                PORTRAIT,
                a6PortraitBody,
                { pageNum:Int, pb: SinglePage ->
                    val isLeft = pageNum % 2 == 1
                    val leftMargin:Double = if (isLeft) 37.0 else 45.0
                    pb.drawStyledText(Coord(leftMargin + (a6PortraitBody.dim.width / 2), 20.0), "$pageNum.",
                                      TextStyle(PDType1Font.TIMES_ROMAN, 8.0, CMYK_BLACK), true)
                    leftMargin })

//        pageMgr.logicalPageEnd(lp)
//        pageMgr.ensurePageIdx(pageMgr.unCommittedPageIdx())

        data class TitlePage(val title:String, val p:Int)
        val tableOfContents:MutableList<TitlePage> = mutableListOf()
        val index:TreeMap<String,MutableList<IntRange>> = TreeMap()

        var dap: DimAndPageNums =
                lp.appendCell(0.0, chapTitleCellStyle,
                              listOf(Text(incipit, "1. "),
                                     Text(heading, "Down the Rabbit Hole")))
        assertEquals(IntRange(1, 1), dap.pageNums)
        tableOfContents.add(TitlePage("1. Down the Rabbit Hole", dap.pageNums.start))

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "Alice was beginning to get very tired of sitting by her sister on" +
                                        " the bank, and of having nothing to do: once or twice she had" +
                                        " peeped into the book her sister was reading, but it had no" +
                                        " pictures or conversations in it, "),
                                   Text(thought, "and what is the use of a book,"),
                                   Text(bodyText, " thought Alice, "),
                                   Text(thought, "without pictures or conversation?")))
        assertEquals(IntRange(1, 1), dap.pageNums)
        addToIndex("Bank", dap, index)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "So she was considering, in her own mind (as well as she could, for" +
                                        " the hot day made her feel very sleepy and stupid), whether the" +
                                        " pleasure of making a daisy-chain would be worth the trouble of" +
                                        " getting up and picking the daisies, when suddenly a White Rabbit" +
                                        " with pink eyes ran close by her.")))
        assertEquals(IntRange(1, 1), dap.pageNums)
        addToIndex("White Rabbit", dap, index)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "There was nothing so very remarkable in that; nor did Alice think" +
                                        " it so very much out of the way to hear the Rabbit say to itself," +
                                        " \"Oh dear! Oh dear! I shall be too late!\" (when she thought it" +
                                        " over afterwards, it occurred to her that she ought to have wondered" +
                                        " at this, but at the time it all seemed quite natural); but when the" +
                                        " Rabbit actually took a watch out of its waistcoat-pocket, and" +
                                        " looked at it, and then hurried on, Alice started to her feet, for" +
                                        " it flashed across her mind that she had never before seen a rabbit" +
                                        " with either a waistcoat-pocket, or a watch to take out of it, and," +
                                        " burning with curiosity, she ran across the field after it, and was" +
                                        " just in time to see it pop down a large rabbit-hole under the hedge.")))
        assertEquals(IntRange(1, 2), dap.pageNums)
        addToIndex("Feet", dap, index)
        addToIndex("Waistcoat-pocket", dap, index)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "In another moment down went Alice after it, never once considering how" +
                                        " in the world she was to get out again....")))
        assertEquals(IntRange(2, 2), dap.pageNums)

        // Forces a new page.
        lp.cursorToNewPage()

        dap = lp.appendCell(0.0, chapTitleCellStyle,
                            listOf(Text(incipit, "2. "),
                                   Text(heading, "The Pool of Tears")))
        assertEquals(IntRange(3, 3), dap.pageNums)
        tableOfContents.add(TitlePage("2. The Pool of Tears", dap.pageNums.start))

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "\"Curiouser and curiouser!\" cried Alice (she was so much surprised, that" +
                                        " for the moment she quite forgot how to speak good English). \"Now I'm" +
                                        " opening out like the largest telescope that ever was! Good-bye, feet!\"" +
                                        " (for when she looked down at her feet, they seemed to be almost out of" +
                                        " sight, they were getting so far off). \"Oh, my poor little feet, I wonder" +
                                        " who will put on your shoes and stockings for you now, dears? I’m sure I" +
                                        " shan't be able! I shall be a great deal too far off to trouble myself" +
                                        " about you: you must manage the best way you can—but I must be kind to" +
                                        " them,\" thought Alice, \"or perhaps they won't walk the way I want to go!" +
                                        " Let me see. I’ll give them a new pair of boots every Christmas.\"")))
        assertEquals(IntRange(3, 3), dap.pageNums)
        addToIndex("Feet", dap, index)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "And she went on planning to herself how she would manage it. "),
                                   Text(thought, "They must go by the carrier, "),
                                   Text(bodyText, "she thought; "),
                                   Text(thought,
                                        "and how funny it’ll seem, sending presents to one’s own feet!" +
                                        " And how odd the directions will look!")))
        assertEquals(IntRange(4, 4), dap.pageNums)
        addToIndex("Feet", dap, index)

        dap = lp.appendCell(40.0, bodyCellStyle,
                            listOf(Text(handwriting,
                                        "Alice’s Right Foot, Esq.\n" +
                                        "Hearthrug\n" +
                                        "Near the Fender\n" +
                                        "(with Alice’s love)")))
        assertEquals(IntRange(4, 4), dap.pageNums)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(thought,
                                        "Oh dear, what nonsense I’m talking!")))
        assertEquals(IntRange(4, 4), dap.pageNums)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "Just then her head struck against the roof of the hall: in fact she was" +
                                        " now more than nine feet high, and she at once took up the little golden" +
                                        " key and hurried off to the garden door....")))
        assertEquals(IntRange(4, 4), dap.pageNums)

        lp.cursorToNewPage()

        // Check the output that there's no space between "a" and "Long"!
        // Other tests should fail if that's the case, so fix them first.
        dap = lp.appendCell(0.0, chapTitleCellStyle,
                            listOf(Text(incipit, "3. "),
                                   Text(heading, "A Caucus-Race and a Long Tale")))
        assertEquals(IntRange(5, 5), dap.pageNums)
        tableOfContents.add(TitlePage("3. A Caucus-Race and a Long Tale", dap.pageNums.start))

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "They were indeed a queer-looking party that assembled on the bank—the" +
                                        " birds with draggled feathers, the animals with their fur clinging close" +
                                        " to them, and all dripping wet, cross, and uncomfortable.")))
        assertEquals(IntRange(5, 5), dap.pageNums)
        addToIndex("Bank", dap, index)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "The first question of course was, how to get dry again: they had a" +
                                        " consultation about this, and after a few minutes it seemed quite" +
                                        " natural to Alice to find herself talking familiarly with them, as if" +
                                        " she had known them all her life. Indeed, she had quite a long argument" +
                                        " with the Lory, who at last turned sulky, and would only say, \"I’m older" +
                                        " than you, and must know better.\" And this Alice would not allow, without" +
                                        " knowing how old it was, and, as the Lory positively refused to tell its" +
                                        " age, there was no more to be said.")))
        assertEquals(IntRange(5, 6), dap.pageNums)
        addToIndex("Lory, the", dap, index)

        dap = lp.appendCell(0.0, bodyCellStyle,
                            listOf(Text(bodyText,
                                        "At last the Mouse, who seemed to be a person of authority among them," +
                                        " called out, \"Sit down, all of you, and listen to me! I’ll soon make" +
                                        " you dry enough!\" They all sat down at once, in a large ring, with the" +
                                        " Mouse in the middle. Alice kept her eyes anxiously fixed on it, for she" +
                                        " felt sure she would catch a bad cold if she did not get dry very soon....")))
        assertEquals(IntRange(6, 6), dap.pageNums)

        lp.cursorToNewPage()

        dap = lp.appendCell(0.0, chapTitleCellStyle,
                            listOf(Text(heading, "Index")))
        tableOfContents.add(TitlePage("Index", dap.pageNums.start))

        for (item in index) {
            val ret = mutableListOf(Text(bodyText, "${item.key}: "))
            var first = true
            for (it in item.value) {
                if (first) {
                    first = false
                } else {
                    ret.add(Text(bodyText, ","))
                }
                ret.add(Text(bodyText, if (it.start == it.endInclusive) {
                    " ${it.start}"
                } else {
                    " ${it.start}-${it.endInclusive}"
                }))
            }

            lp.appendCell(0.0, bodyCellStyle, ret)
        }

        var sp = SinglePage(-2, pageMgr, null, lp.body)
        sp.cursorY =  sp.cursorY - 100.0
        dap = sp.appendCell(bodyCellStyle, listOf(Text(incipit, "Alice in Wonderland")))
        assertEquals(IntRange(-2, -2), dap.pageNums)
        dap = sp.appendCell(bodyCellStyle, listOf(Text(heading, "By Lewis Carroll")))
        assertEquals(IntRange(-2, -2), dap.pageNums)
        pageMgr.insertPageAt(sp, 0)

        sp = SinglePage(-1, pageMgr, null, lp.body)

        dap = sp.appendCell(chapTitleCellStyle,
                             listOf(Text(heading, "Table of Contents")))
        assertEquals(IntRange(-1, -1), dap.pageNums)

        var table = Table(mutableListOf(200.0, 25.0))
        var rowBuilder = table.partBuilder()
                .rowBuilder()

        tableOfContents.forEach {
            item -> rowBuilder = rowBuilder.cell(bodyCellStyle, listOf(Text(bodyText, item.title)))
                .cell(bodyCellStyle, listOf(Text(bodyText, "" + item.p))).buildRow().rowBuilder()
        }
        table = rowBuilder.buildRow().buildPart()

        dap = sp.add(Coord(0.0, sp.body.topLeft.y - dap.dim.height), table.wrap())
        assertEquals(IntRange(-1, -1), dap.pageNums)
        pageMgr.insertPageAt(sp, 1)

        // If we don't set the file identifiers, PDFBox will set them for us.  We only care because we're checking
        // this into git so that when something breaks, we can easily compare with a known-good file to see how big
        // a change it is.
        val docId = COSString("Alice In Wonderland Test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.commit()

        // We're just going to write to a file.
        pageMgr.save(FileOutputStream("alice.pdf"))
    }

    private fun addToIndex(
            key: String,
            dap: DimAndPageNums,
            index: TreeMap<String, MutableList<IntRange>>) {
        val pp: MutableList<IntRange> = index.getOrDefault(key, mutableListOf())
        pp.add(dap.pageNums)
        index[key] = pp
    }
}