import com.planbase.pdf.layoutmanager.*
import com.planbase.pdf.layoutmanager.attributes.Align.*
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle.Companion.NO_LINE
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.ScaledImage
import com.planbase.pdf.layoutmanager.contents.TableBuilder
import com.planbase.pdf.layoutmanager.contents.TablePart
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.RGB_BLACK
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.RGB_WHITE
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.imageio.ImageIO

class TestManualllyPdfLayoutMgr {

    @Test
    @Throws(IOException::class)
    fun testPdf() {
        // Nothing happens without a PdfLayoutMgr.
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))

        // One inch is 72 document units.  36 is about a half-inch - enough margin to satisfy most
        // printers. A typical monitor has 72 dots per inch, so you can think of these as pixels
        // even though they aren't.  Things can be aligned right, center, top, or anywhere within
        // a "pixel".
        val pMargin = PdfLayoutMgr.DOC_UNITS_PER_INCH / 2

        // A PageGrouping is a group of pages with the same settings.  When your contents scroll off
        // the bottom of a page, a new page is automatically created for you with the settings taken
        // from the LogicPage grouping. If you don't want a new page, be sure to stay within the
        // bounds of the current one!
        var lp = pageMgr.logicalPageStart()

        // Set up some useful constants for later.
        val tableWidth = lp.pageWidth() - 2 * pMargin
        val pageRMargin = pMargin + tableWidth
        val colWidth = tableWidth / 4f
        val colWidths = floatArrayOf(colWidth + 10, colWidth + 10, colWidth + 10, colWidth - 30)
        val textCellPadding = Padding(2f)

        // Set up some useful styles for later
        val heading = TextStyle(PDType1Font.HELVETICA_BOLD, 9.5f, RGB_WHITE)
        val headingCell = CellStyle(BOTTOM_CENTER,
                                    BoxStyle(textCellPadding, RGB_BLUE,
                                             BorderStyle(NO_LINE, LineStyle(RGB_WHITE),
                                                         NO_LINE, LineStyle(RGB_BLUE))))
        val headingCellR = CellStyle(BOTTOM_CENTER,
                                     BoxStyle(textCellPadding, RGB_BLACK,
                                              BorderStyle(NO_LINE, LineStyle(RGB_BLACK),
                                                          NO_LINE, LineStyle(RGB_WHITE))))

        val regular = TextStyle(PDType1Font.HELVETICA, 9.5f, RGB_BLACK)
        val regularCell = CellStyle(TOP_LEFT,
                                    BoxStyle(textCellPadding, null,
                                             BorderStyle(NO_LINE, LineStyle(RGB_BLACK),
                                                         LineStyle(RGB_BLACK), LineStyle(RGB_BLACK))))

        // Let's draw three tables on our first landscape-style page grouping.

        // Draw the first table with lots of extra room to show off the vertical and horizontal
        // alignment.
        var tB = TableBuilder()
        val tp1 : TablePart = tB.addCellWidths(listOf(120f, 120f, 120f))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, RGB_YELLOW_BRIGHT))
                .partBuilder()
                .cellStyle(CellStyle(BOTTOM_CENTER, BoxStyle(Padding(2f),
                                                             RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .rowBuilder().addTextCells("First", "Second", "Third").buildRow()
                .buildPart()
                .partBuilder()
                .cellStyle(CellStyle(MIDDLE_CENTER, BoxStyle(Padding(2f),
                                                             RGB_LIGHT_GREEN,
                                                             BorderStyle(RGB_DARK_GRAY))))
                .minRowHeight(120f)
                .textStyle(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK))
                .rowBuilder()
                .cellBuilder().align(TOP_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .cellBuilder().align(TOP_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .cellBuilder().align(TOP_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .buildRow()

        println("tp1=" + tp1)

        tp1.rowBuilder()
                .cellBuilder().align(MIDDLE_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .cellBuilder().align(MIDDLE_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .cellBuilder().align(MIDDLE_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .buildRow()
                .rowBuilder()
                .cellBuilder().align(BOTTOM_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .cellBuilder().align(BOTTOM_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .cellBuilder().align(BOTTOM_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .buildRow()
                .buildPart()
                val xya:XyOffset = tB.buildTable()
                                 .render(lp, XyOffset(40f, lp.yBodyTop()))

                // The second table uses the x and y offsets from the previous table to position it to the
                // right of the first.
                tB = TableBuilder()
                tB.addCellWidths(listOf(100f, 100f, 100f))
                  .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, RGB_YELLOW_BRIGHT))
                  .partBuilder().cellStyle(CellStyle(BOTTOM_CENTER,
                                                     BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                  .rowBuilder().addTextCells("First", "Second", "Third").buildRow()
                  .buildPart()
                  .partBuilder().cellStyle(CellStyle(MIDDLE_CENTER,
                                                     BoxStyle(Padding(2f), RGB_LIGHT_GREEN,
                                                              BorderStyle(RGB_DARK_GRAY))))
                  .minRowHeight(100f)
                  .textStyle(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK))
                  .rowBuilder()
                  .cellBuilder().align(BOTTOM_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(BOTTOM_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(BOTTOM_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .buildRow()
                  .rowBuilder()
                  .cellBuilder().align(MIDDLE_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(MIDDLE_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(MIDDLE_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .buildRow()
                  .rowBuilder()
                  .cellBuilder().align(TOP_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(TOP_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(TOP_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .buildRow()
                  .buildPart()
                val xyb:XyOffset = tB.buildTable()
                                 .render(lp, XyOffset(xya.x + 10, lp.yBodyTop()))

                // The third table uses the x and y offsets from the previous tables to position it to the
                // right of the first and below the second.  Negative Y is down.  This third table showcases
                // the way cells extend vertically (but not horizontally) to fit the text you put in them.
                tB = TableBuilder()
                tB.addCellWidths(listOf(100f, 100f, 100f))
                  .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f,
                                          RGB_YELLOW_BRIGHT))
                  .partBuilder().cellStyle(CellStyle(BOTTOM_CENTER,
                                                     BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                  .rowBuilder().addTextCells("First", "Second", "Third").buildRow()
                  .buildPart()
                  .partBuilder().cellStyle(CellStyle(MIDDLE_CENTER,
                                                     BoxStyle(Padding(2f), RGB_LIGHT_GREEN,
                                                              BorderStyle(RGB_DARK_GRAY))))
                  .textStyle(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK))
                  .rowBuilder().cellBuilder().align(BOTTOM_RIGHT).addStrs("Line 1\n").buildCell()
                  .cellBuilder().align(BOTTOM_CENTER).addStrs("Line 1\n", "Line two\n").buildCell()
                  .cellBuilder().align(BOTTOM_LEFT)
                  .addStrs("Line 1\n", "Line two\n", "[Line three is long enough to wrap]").buildCell()
                  .buildRow()
                  .rowBuilder().cellBuilder().align(MIDDLE_RIGHT).addStrs("Line 1\n", "Line two\n").buildCell()
                  .cellBuilder().align(MIDDLE_CENTER).addStrs("").buildCell()
                  .cellBuilder().align(MIDDLE_LEFT).addStrs("Line 1\n").buildCell().buildRow()
                  .rowBuilder().cellBuilder().align(TOP_RIGHT).addStrs("L1").buildCell()
                  .cellBuilder().align(TOP_CENTER).addStrs("Line 1\n", "Line two\n").buildCell()
                  .cellBuilder().align(TOP_LEFT).addStrs("Line 1\n").buildCell().buildRow()
                  .buildPart()
                  .buildTable()
                  .render(lp, XyOffset(xya.x + 10, xyb.y - 10))

                lp.commit()

                // Let's do a portrait page now.  I just copied this from the previous page.
                lp = pageMgr.logicalPageStart(PdfLayoutMgr.Orientation.PORTRAIT)
                tB = TableBuilder()
                tB.addCellWidths(listOf(120f, 120f, 120f))
                  .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, RGB_YELLOW_BRIGHT))
                  .partBuilder().cellStyle(CellStyle(BOTTOM_CENTER,
                                                     BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                  .rowBuilder().addTextCells("First", "Second", "Third").buildRow()
                  .buildPart()
                  .partBuilder().cellStyle(CellStyle(MIDDLE_CENTER,
                                                     BoxStyle(Padding(2f), RGB_LIGHT_GREEN,
                                                              BorderStyle(RGB_DARK_GRAY))))
                        .minRowHeight(120f)
                  .textStyle(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK))
                  .rowBuilder()
                  .cellBuilder().align(TOP_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(TOP_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(TOP_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .buildRow()
                  .rowBuilder()
                  .cellBuilder().align(MIDDLE_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(MIDDLE_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(MIDDLE_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .buildRow()
                  .rowBuilder()
                  .cellBuilder().align(BOTTOM_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(BOTTOM_CENTER).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .cellBuilder().align(BOTTOM_RIGHT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                  .buildRow()
                  .buildPart()
                val xyOff = tB.buildTable()
                                   .render(lp, XyOffset(40f, lp.yBodyTop()))

                // This was very hastily added to this test to prove that font loading works (it does).
                val fontFile = File("target/test-classes/LiberationMono-Bold.ttf")
                val liberationFont: PDType0Font = pageMgr.loadTrueTypeFont(fontFile)
                lp.drawCell(xyOff.x, xyOff.y,
                            Cell(CellStyle(MIDDLE_CENTER,
                                           BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_DARK_GRAY))),
                                 200f,
                                 TextStyle(liberationFont, 12f, RGB_BLACK),
                                 listOf("Hello Liberation Mono Bold Font!")).fix())

                tB = TableBuilder()
                tB.addCellWidths(listOf(100f))
                  .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f,
                                           RGB_YELLOW_BRIGHT))
                  .partBuilder().cellStyle(CellStyle(MIDDLE_CENTER,
                                                     BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                  .rowBuilder().addTextCells("Lower-Right").buildRow()
                  .buildPart()
                // Where's the lower-right-hand corner?  Put a cell there.
                tB.buildTable()
                  .render(lp, XyOffset(lp.pageWidth() - (100 + pMargin),
                                          lp.yBodyBottom() + 15 + pMargin))

                lp.commit()

        // More landscape pages
        val pageHeadTextStyle = TextStyle(PDType1Font.HELVETICA, 7f, RGB_BLACK)
        val pageHeadCellStyle = CellStyle(TOP_CENTER, BoxStyle.NONE)
        lp = pageMgr.logicalPageStart(PdfLayoutMgr.Orientation.LANDSCAPE
        ) { pageNum, pb->
            val cell = Cell(pageHeadCellStyle, tableWidth, pageHeadTextStyle,
                            listOf(("Test Logical Page Three" +
                                    " (physical page " + pageNum + ")")))

            cell.fix().render(pb, XyOffset(pMargin, LETTER.width - 27))
            0f // Don't offset whole page.
        }

        // We're going to reset and reuse this y variable.
        var y = lp.yBodyTop()

//        y = lp.putRow(pMargin, y,
//                      Cell(headingCell, colWidths[0], heading,
//                           listOf("Transliterated Russian (with un-transliterated Chinese below)")),
//                      Cell(headingCellR, colWidths[1], heading, listOf("US English")),
//                      Cell(headingCellR, colWidths[2], heading, listOf("Finnish")),
//                      Cell(headingCellR, colWidths[3], heading, listOf("German")))
//
//        val f = File("target/test-classes/melon.jpg")
//        println(f.absolutePath)
//        val melonPic = ImageIO.read(f)
//
//        y = lp.putRow(pMargin, y,
//                      Cell(regularCell, colWidths[0], regular,
//                           listOf("This used to have Russian and Chinese text.",
//                                  "The Russian was transliterated and the",
//                                  "Chinese was turned into bullets.",
//                                  "PDFBox 2.x, now handles many characters better,",
//                                  "but throws exceptions for",
//                                  "characters it doesn't understand.",
//                                  "Truth be told, I don't understand so well how",
//                                  "it works, but I think if you get an exception,",
//                                  "you need to load a font like:",
//                                  "PDFont font = PDTrueTypeFont.loadTTF(document, \"Arial.ttf\");",
//                                  "See:",
//                                  "https://pdfbox.apache.org/1.8/cookbook/",
//                                  "workingwithfonts.html",
//                                  "\n",
//                                  "here",
//                                  "are",
//                                  "more lines",
//                                   //                                   "Россия – священная наша держава,",
//                                   //                                   "Россия – любимая наша страна.",
//                                   //                                   "Могучая воля, великая слава –",
//                                   //                                   "Твоё достоянье на все времена!",
//                                   //                                   null,
//                                   //                                   "Chorus:",
//                                   //                                   null,
//                                   //                                   "Славься, Отечество наше свободное, Братских народов союз" +
//                                   //                                   " вековой, Предками данная мудрость народная! Славься, страна!" +
//                                   //                                   " Мы гордимся тобой!",
//                                   //                                   null,
//                                   //                                   "От южных морей до полярного края Раскинулись наши леса и" +
//                                   //                                   " поля. Одна ты на свете! Одна ты такая – Хранимая Богом " +
//                                   //                                   "родная земля!",
//                                   //                                   null,
//                                   //                                   "Chorus:",
//                                   //                                   null,
//                                   //                                   "Широкий простор для мечты и для жизни",
//                                   //                                   "Грядущие нам открывают года.",
//                                   //                                   "Нам силу даёт наша верность Отчизне.",
//                                   //                                   "Так было, так есть и так будет всегда!",
//                                   //                                   null,
//                                   //                                   "Chorus",
//                                   //                                   null,
//                                   //                                   null,
//                                   //                                   null,
//                                   //                                   "Chinese will not print.  The substitution character is a" +
//                                   //                                   " bullet, so below should be lots of bullets.",
//                                   //                                   null,
//                                   //                                   "起來！不願做奴隸的人們！ " +
//                                   //                                   "把我們的血肉，築成我們新的長城！ " +
//                                   //                                   "中華民族到了最危險的時候， " +
//                                   //                                   "每個人被迫著發出最後的吼聲。 " +
//                                   //                                   "起來！起來！起來！ " +
//                                   //                                   "我們萬眾一心， " +
//                                   //                                   "冒著敵人的炮火，前進！ " +
//                                   //                                   "冒著敵人的炮火，前進！ " +
//                                   //                                   "前進！前進！進！",
//                                  "\n",
//                                  "Here is a picture with the default and other sizes.  Though" +
//                                  " it shows up several times, the image data is only attached" +
//                                  " to the file once and reused."))
//                              .addAll(listOf(ScaledImage(melonPic),
//                                             ScaledImage(melonPic, XyDim(50f, 50f)),
//                                             Text(regular, " Melon "),
//                                             ScaledImage(melonPic, XyDim(50f, 50f)),
//                                             Text(regular, " Yum!"),
//                                             ScaledImage(melonPic, XyDim(170f, 100f)),
//                                             Text(regular, "Watermelon!"))),
//                      Cell(regularCell, colWidths[1],
//                           regular,
//                           listOf(("O say can you see by the dawn's early light, " +
//                                   "What so proudly we hailed at the twilight's last gleaming, " +
//                                   "Whose broad stripes and bright stars " +
//                                   "through the perilous fight, " +
//                                   "O'er the ramparts we watched, were so gallantly streaming? " +
//                                   "And the rockets' red glare, the bombs bursting in air, " +
//                                   "Gave proof through the night that our flag was still there; " +
//                                   "O say does that star-spangled banner yet wave, " +
//                                   "O'er the land of the free and the home of the brave? "),
//                                  "\n\n",
//                                  "On the shore dimly seen through the mists of the deep, ",
//                                  "Where the foe's haughty host in dread silence reposes, ",
//                                  "What is that which the breeze, o'er the towering steep, ",
//                                  "As it fitfully blows, half conceals, half discloses? ",
//                                  "Now it catches the gleam of the morning's first beam, ",
//                                  "In full glory reflected now shines in the stream: ",
//                                  "'Tis the star-spangled banner, O! long may it wave ",
//                                  "O'er the land of the free and the home of the brave. ",
//                                  "\n",
//                                  "\n",
//                                  "\n",
//                                  "\n",
//                                  "\n",
//                                  ("And where is that band who so vauntingly swore " +
//                                   "That the havoc of war and the battle's confusion, " +
//                                   "A home and a country, should leave us no more? " +
//                                   "Their blood has washed out their foul footsteps' pollution. " +
//                                   "No refuge could save the hireling and slave " +
//                                   "From the terror of flight, or the gloom of the grave: " +
//                                   "And the star-spangled banner in triumph doth wave, " +
//                                   "O'er the land of the free and the home of the brave. " +
//                                   "\n\n"),
//                                  ("O thus be it ever, when freemen shall stand " +
//                                   "Between their loved home and the war's desolation. " +
//                                   "Blest with vict'ry and peace, may the Heav'n rescued land " +
//                                   "Praise the Power that hath made and preserved us a nation! " +
//                                   "Then conquer we must, when our cause it is just, " +
//                                   "And this be our motto: \"In God is our trust.\" " +
//                                   "And the star-spangled banner in triumph shall wave " +
//                                   "O'er the land of the free and the home of the brave!"),
//                                  "\n\n",
//                                  "more ",
//                                  "lines ",
//                                  "to ",
//                                  "test")),
//                      Cell(regularCell, colWidths[2], regular,
//                           listOf("Maamme",
//                                  "\n",
//                                  ("Monument to the Vårt Land poem in Helsinki. " +
//                                   "Oi maamme, Suomi, synnyinmaa, " +
//                                   "soi, sana kultainen! " +
//                                   "Ei laaksoa, ei kukkulaa, " +
//                                   "ei vettä, rantaa rakkaampaa " +
//                                   "kuin kotimaa tää pohjoinen, " +
//                                   "maa kallis isien. " +
//                                   "Sun kukoistukses kuorestaan " +
//                                   "kerrankin puhkeaa; " +
//                                   "viel' lempemme saa nousemaan " +
//                                   "sun toivos, riemus loistossaan, " +
//                                   "ja kerran laulus, synnyinmaa " +
//                                   "korkeemman kaiun saa. "),
//                                  "\n\n",
//                                  "Vårt land ",
//                                  "\n\n",
//                                  ("(the original, by Johan Ludvig Runeberg) " +
//                                   "Vårt land, vårt land, vårt fosterland, " +
//                                   "ljud högt, o dyra ord! " +
//                                   "Ej lyfts en höjd mot himlens rand, " +
//                                   "ej sänks en dal, ej sköljs en strand, " +
//                                   "mer älskad än vår bygd i nord, " +
//                                   "än våra fäders jord! " +
//                                   "Din blomning, sluten än i knopp, " +
//                                   "Skall mogna ur sitt tvång; " +
//                                   "Se, ur vår kärlek skall gå opp " +
//                                   "Ditt ljus, din glans, din fröjd, ditt hopp. " +
//                                   "Och högre klinga skall en gång " +
//                                   "Vår fosterländska sång.\n\n"))),
//                      Cell(regularCell, colWidths[3], regular,
//                           listOf(// Older first 2 verses obsolete.
//                                   ("Einigkeit und Recht und Freiheit " +
//                                    "Für das deutsche Vaterland! " +
//                                    "Danach lasst uns alle streben " +
//                                    "Brüderlich mit Herz und Hand! " +
//                                    "Einigkeit und Recht und Freiheit " +
//                                    "Sind des Glückes Unterpfand;" +
//                                    "Blüh' im Glanze dieses Glückes, " +
//                                    "  Blühe, deutsches Vaterland!"))))
//
//        lp.putRow(pMargin, y,
//                  Cell(regularCell, colWidths[0], regular, listOf("Another row of cells")),
//                  Cell(regularCell, colWidths[1], regular, listOf("On the second page")),
//                  Cell(regularCell, colWidths[2], regular, listOf("Just like any other page")),
//                  Cell(regularCell, colWidths[3], regular, listOf("That's it!")))
        lp.commit()

        val lineStyle = LineStyle(RGB_BLACK, 1f)

        lp = pageMgr.logicalPageStart(PdfLayoutMgr.Orientation.LANDSCAPE
        ) { pageNum, pb->
            val cell = Cell(pageHeadCellStyle, tableWidth,
                            pageHeadTextStyle,
                            listOf(("Test Logical Page Four " +
                                    " (physical page " + pageNum + ")")))
            cell.fix().render(pb, XyOffset(pMargin, LETTER.width - 27))
            0f // Don't offset whole page.
        }

        // Make a big 3-page X in a box.  Notice that we code it as though it's on one page, and the
        // API adds two more pages as needed.  This is a great test for how geometric shapes break
        // across pages.

        // top lne
        lp.drawLine(pMargin, lp.yBodyTop(), pageRMargin, lp.yBodyTop(), lineStyle)
        // left line
        lp.drawLine(pMargin, lp.yBodyTop(), pMargin, -lp.yBodyTop(), lineStyle)
        // 3-page-long X
        lp.drawLine(pMargin, lp.yBodyTop(), pageRMargin, -lp.yBodyTop(), lineStyle)
        // middle line
        lp.drawLine(pMargin, 0f, pageRMargin, 0f, lineStyle)
        lp.drawLine(pageRMargin, lp.yBodyTop(), pMargin, -lp.yBodyTop(), lineStyle)
        // right line
        lp.drawLine(pageRMargin, lp.yBodyTop(), pageRMargin, -lp.yBodyTop(), lineStyle)
        // bottom line
        lp.drawLine(pMargin, -lp.yBodyTop(), pageRMargin, -lp.yBodyTop(), lineStyle)
        lp.commit()

        // All done - write it out!

        // In a web application, this could be:
        //
        // httpServletResponse.setContentType("application/pdf") // your server may do this for you.
        // os = httpServletResponse.getOutputStream()            // you probably have to do this
        //
        // Also, in a web app, you probably want name your action something.pdf and put
        // target="_blank" on your link to the PDF download action.

        // We're just going to write to a file.
        val os = FileOutputStream("test.pdf")

        // Commit it to the output stream!
        pageMgr.save(os)
    }

    companion object {
        internal val RGB_BLUE = PDColor(floatArrayOf(0.2f, 0.2f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_BLUE_GREEN = PDColor(floatArrayOf(0.2f, 0.4f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_DARK_GRAY = PDColor(floatArrayOf(0.2f, 0.2f, 0.2f), PDDeviceRGB.INSTANCE)
        internal val RGB_LIGHT_GREEN = PDColor(floatArrayOf(0.8f, 1f, 0.8f), PDDeviceRGB.INSTANCE)
        internal val RGB_YELLOW_BRIGHT = PDColor(floatArrayOf(1f, 1f, 0f), PDDeviceRGB.INSTANCE)
    }
}
