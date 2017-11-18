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

import com.planbase.pdf.layoutmanager.*
import com.planbase.pdf.layoutmanager.attributes.Align.TOP_LEFT
import com.planbase.pdf.layoutmanager.attributes.Align.TOP_RIGHT
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.TableBuilder
import com.planbase.pdf.layoutmanager.contents.TableRowBuilder
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.Utils
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.imageio.ImageIO

/**
 * Created by gpeterso on 6/6/17.
 */
class TestManual2 {

@Test
fun testBodyMargins() {
    // Nothing happens without a PdfLayoutMgr.
    val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, XyDim(PDRectangle.A6))

    val bodyWidth = PDRectangle.A6.width - 80f

    val f = File("target/test-classes/graph2.png")
//    println(f.absolutePath)
    val graphPic = ImageIO.read(f)

    val lp = pageMgr.logicalPageStart(
            PORTRAIT,
            { pageNum:Int, pb: SinglePage ->
                val isLeft = pageNum % 2 == 1
                val leftMargin:Float = if (isLeft) 37f else 45f
                //            System.out.println("pageNum " + pageNum);
                pb.drawLine(leftMargin, 30f, leftMargin + bodyWidth, 30f,
                            LineStyle(Utils.CMYK_BLACK))
                pb.drawStyledText(leftMargin, 20f, "Page # " + pageNum,
                                  TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK))
                leftMargin })

    //        TableBuilder tB = new TableBuilder();
    //        Table table = tB.addCellWidths(20f, 80f)
    //                        .partBuilder()
    //                        .rowBuilder()
    //                        .cellBuilder().cellStyle(BULLET_CELL_STYLE)
    //                        .add(BULLET_TEXT_STYLE, vec(BULLET_CHAR)).buildCell()
    //                        .cellBuilder().add(new TextStyle(PDType1Font.HELVETICA, 12f, Utils.Companion.getCMYK_BLACK()), vec("This is some text that has a bullet")).buildCell()
    //                        .buildRow()
    //                        .rowBuilder()
    //                        .cellBuilder().cellStyle(BULLET_CELL_STYLE)
    //                        .add(BULLET_TEXT_STYLE, vec("2.")).buildCell()
    //                        .cellBuilder().add(new TextStyle(PDType1Font.HELVETICA, 12f, Utils.Companion.getCMYK_BLACK()), vec("text that has a number")).buildCell()
    //                        .buildRow()
    //                        .buildPart()
    //                        .buildTable();


    val tpb = TableBuilder()
            .addCellWidths(20f, 80f).partBuilder()

    // This could be in a loop that prints out list items.
    tpb.rowBuilder().cell(BULLET_CELL_STYLE, listOf(Text(BULLET_TEXT_STYLE, Utils.BULLET_CHAR)))
            .cell(contents = listOf(Text(BULLET_TEXT_STYLE, "This is some text that has a bullet")))
            .buildRow()
    // Next iteration in the loop
            .rowBuilder()
            .cell(BULLET_CELL_STYLE, listOf(Text(BULLET_TEXT_STYLE, "2.")))
            .cell(contents = listOf(Text(BULLET_TEXT_STYLE, "text that has a number")))
            .buildRow()

    // TODO: actually display these bullets!
    // After the loop, build the table.
//    val bullets = tpb.buildPart().buildTable()

    Cell(CellStyle(TOP_LEFT, BoxStyle(Padding(2f), CMYK_LIGHT_GREEN, BorderStyle(CMYK_DARK_GRAY))),
         bodyWidth,
         listOf(Text(TextStyle(PDType1Font.HELVETICA, 12f, Utils.CMYK_BLACK),
                     "The long "),
                Text(TextStyle(PDType1Font.HELVETICA_BOLD, 18f, Utils.CMYK_BLACK),
                     "families"),
                Text(TextStyle(PDType1Font.HELVETICA, 12f, Utils.CMYK_BLACK),
                     " needed the national " +
                     "words and women said new. The new " +
                     "companies told the possible hands " +
                     "and books was low. The other " +
                     "questions got the recent children and " +
                     "lots felt important. The sure hands " +
                     "moved the major stories and countries " +
                     "showed possible. The major students " +
                     "began the international rights and " +
                     "places got free. The able homes said " +
                     "the better work and cases went free."),
                ScaledImage(graphPic),
                Text(TextStyle(PDType1Font.HELVETICA, 12f, Utils.CMYK_BLACK),
                     ("The hard eyes seemed the clear " +
                      "mothers and systems came economic. " +
                      "The high months showed the possible " +
                      "money and eyes heard certain. The " +
                      "true men played the different facts and " +
                      "areas showed large. The good ways " +
                      "lived the different countries and " +
                      "stories found good. The certain " +
                      "places found the political months and " +
                      "facts told easy. The long homes ran " +
                      "the good governments and cases " +
                      "lived social.")),
                ScaledImage(graphPic),
                Text(TextStyle(PDType1Font.HELVETICA, 12f, Utils.CMYK_BLACK),
                     ("The social people ran the " +
                      "local cases and men left local. The " +
                      "easy areas saw the whole times and " +
                      "systems became national. The whole " +
                      "Page # 1questions lived the white points and " +
                      "governments had national. The real " +
                      "families saw the hard stories and Mrs " +
                      "looked late. The young studies had " +
                      "the other times and families started " +
                      "late. The public years saw the hard " +
                      "stories and waters used sure. The " +
                      "clear lives showed the white work and " +
                      "people used long. The major rights " +
                      "was the important children and " +
                      "mothers turned able. The " +
                      "international men kept the real " +
                      "questions and nights made big.")),
                ScaledImage(graphPic),
                Text(TextStyle(PDType1Font.HELVETICA, 12f, Utils.CMYK_BLACK),
                     ("The " +
                      "best points got the economic waters " +
                      "and problems gave great. The whole " +
                      "countries went the best children and " +
                      "eyes came able."))), null).wrap()
            .render(lp, XyOffset(0f, PDRectangle.A6.height - 40f))
    //        table
    //        );
    lp.commit()
    // We're just going to write to a file.
    val os = FileOutputStream("test2.pdf")

    // Commit it to the output stream!
    pageMgr.save(os)
}

    companion object {

        val CMYK_DARK_GRAY = PDColor(floatArrayOf(0f, 0f, 0f, 0.2f), PDDeviceCMYK.INSTANCE)
        val CMYK_LIGHT_GREEN = PDColor(floatArrayOf(0.05f, 0f, 0.1f, 0.01f), PDDeviceCMYK.INSTANCE)

        internal val BULLET_CELL_STYLE = CellStyle(TOP_RIGHT, BoxStyle(Padding(0f, 4f, 0f, 0f), null, BorderStyle.NO_BORDERS))
        internal val BULLET_TEXT_STYLE = TextStyle(PDType1Font.HELVETICA, 12f, Utils.CMYK_BLACK)

        // adj plNoun verb adj descriptiveNoun
        // and
        // subject verb pronoun matching descriptive noun

        internal val adjs:List<String> = listOf("able", "bad", "best", "better", "big", "black", "certain", "clear", "different",
                                                "early", "easy", "economic", "federal", "free", "full", "good", "great", "hard",
                                                "high", "human", "important", "international", "large", "late", "little", "local",
                                                "long", "low", "major", "military", "national", "new", "old", "only", "other",
                                                "political", "possible", "public", "real", "recent", "right", "small", "social",
                                                "special", "strong", "sure", "true", "white", "whole", "young")

        internal val verbs:List<String> = listOf("asked", "was", "became", "began", "called", "could", "came", "did", "felt",
                                                 "found", "got", "gave", "went", "had", "heard", "helped", "kept", "knew", "left",
                                                 "let", "liked", "lived", "looked", "made", "meant", "moved", "needed",
                                                 "played", "put", "ran", "said", "saw", "seemed", "showed", "started", "took",
                                                 "talked", "told", "thought", "tried", "turned", "used", "wanted", "willed",
                                                 "worked")

        internal val nouns:List<String> = listOf("areas", "books", "businesses", "cases", "children", "companies", "countries",
                                                 "days", "eyes", "facts", "families", "governments", "groups", "hands", "homes",
                                                 "jobs", "lives", "lots", "men", "money", "months", "mothers", "Mrs", "nights",
                                                 "numbers", "parts", "people", "places", "points", "problems", "programs",
                                                 "questions", "rights", "rooms", "schools", "states", "stories", "students",
                                                 "studies", "systems", "things", "times", "waters", "ways",
                                                 "weeks", "women", "words", "work", "worlds", "years")

        internal fun mumble(times:Int):String {
            val rand = SecureRandom()
            val sB = StringBuilder()
            for (i in 0 until times)
            {
                sB.append("The ").append(adjs[rand.nextInt(adjs.size)]).append(" ")
                        .append(nouns[rand.nextInt(nouns.size)]).append(" ")
                        .append(verbs[rand.nextInt(verbs.size)]).append(" the ")
                        .append(adjs[rand.nextInt(adjs.size)]).append(" ")
                        .append(nouns[rand.nextInt(nouns.size)]).append(" and ")
                        .append(nouns[rand.nextInt(nouns.size)]).append(" ")
                        .append(verbs[rand.nextInt(verbs.size)]).append(" ")
                        .append(adjs[rand.nextInt(adjs.size)]).append(".  ")
            }
            return sB.toString()
        }
    }
}