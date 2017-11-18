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
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

class TestManualllyPdfLayoutMgr {

    @Test fun testPdf() {
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
        tB.addCellWidths(listOf(120f, 120f, 120f))
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
                .align(TOP_LEFT).addTextCells("Line 1\n" +
                                              "Line two\n" +
                                              "Line three")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two\n" +
                                                "Line three")
                .align(TOP_RIGHT).addTextCells("Line 1\n" +
                                               "Line two\n" +
                                               "Line three")
                .buildRow()
                .rowBuilder()
                .align(MIDDLE_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(MIDDLE_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .buildRow()
                .rowBuilder()
                .align(BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(BOTTOM_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .buildRow()
                .buildPart()
        val xya:XyDim = tB.buildTable().wrap()
                .render(lp, lp.bodyTopLeft())

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
                .align(BOTTOM_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .buildRow()
                .rowBuilder()
                .align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .align(MIDDLE_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(MIDDLE_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .buildRow()
                .rowBuilder()
                .align(TOP_RIGHT).addTextCells("Line 1\n" +
                                               "Line two\n" +
                                               "Line three")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two\n" +
                                                "Line three")
                .align(TOP_LEFT).addTextCells("Line 1\n" +
                                              "Line two\n" +
                                              "Line three")
                .buildRow()
                .buildPart()
        val xyb:XyDim = tB.buildTable().wrap()
                .render(lp, XyOffset(lp.bodyTopLeft().x + xya.width + 10, lp.yBodyTop()))

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
                .rowBuilder().align(BOTTOM_RIGHT).addTextCells("Line 1")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two")
                .align(BOTTOM_LEFT)
                .addTextCells("Line 1\n" +
                              "Line two\n" +
                              "[Line three is long enough to wrap]")
                .buildRow()
                .rowBuilder().align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                               "Line two")
                .align(MIDDLE_CENTER).addTextCells("")
                .align(MIDDLE_LEFT).addTextCells("Line 1").buildRow()
                .rowBuilder().align(TOP_RIGHT).addTextCells("L1")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two")
                .align(TOP_LEFT).addTextCells("Line 1").buildRow()
                .buildPart()
                .buildTable()
                .wrap()
                .render(lp, XyOffset(lp.bodyTopLeft().x + xya.width + 10, lp.yBodyTop() - xyb.height - 10))

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
                .align(TOP_LEFT).addTextCells("Line 1\n" +
                                              "Line two\n" +
                                              "Line three")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two\n" +
                                                "Line three")
                .align(TOP_RIGHT).addTextCells("Line 1\n" +
                                               "Line two\n" +
                                               "Line three")
                .buildRow()
                .rowBuilder()
                .align(MIDDLE_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(MIDDLE_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .buildRow()
                .rowBuilder()
                .align(BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(BOTTOM_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .buildRow()
                .buildPart()
        val xyc:XyDim = tB.buildTable()
                .wrap()
                .render(lp, lp.bodyTopLeft())

        // This was very hastily added to this test to prove that font loading works (it does).
        val fontFile = File("target/test-classes/LiberationMono-Bold.ttf")
        val liberationFont: PDType0Font = pageMgr.loadTrueTypeFont(fontFile)
        Cell(CellStyle(MIDDLE_CENTER,
                       BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(RGB_DARK_GRAY))),
             200f,
             TextStyle(liberationFont, 12f, RGB_BLACK),
             listOf("Hello Liberation Mono Bold Font!"))
                .wrap()
                .render(lp, lp.bodyTopLeft().plusXMinusY(xyc))

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
                .wrap()
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

            cell.wrap().render(pb, XyOffset(pMargin, LETTER.width - 27))
            0f // Don't offset whole page.
        }

        // We're going to reset and reuse this y variable.
//        var y = lp.yBodyTop()

        val f = File("target/test-classes/melon.jpg")
        val melonPic = ImageIO.read(f)

        tB = TableBuilder(colWidths.toMutableList(), headingCell, heading)
        tB.partBuilder()
                .rowBuilder()
                .cell(headingCell, listOf(Text(heading, "Transliterated Russian (with un-transliterated Chinese below)")))
                .cell(headingCellR, listOf(Text(heading, "US English")))
                .cell(headingCellR, listOf(Text(heading, "Finnish")))
                .cell(headingCellR, listOf(Text(heading, "German")))
                .buildRow()
                .buildPart()
                .partBuilder()
                .rowBuilder()
                .cell(regularCell,
                      listOf(Text(regular,
                                  "This used to have Russian and Chinese text.\n" +
                                  "The Russian was transliterated and the\n" +
                                  "Chinese was turned into bullets.\n" +
                                  "PDFBox 2.x, now handles many characters better,\n" +
                                  "but throws exceptions for\n" +
                                  "characters it doesn't understand.\n" +
                                  "Truth be told, I don't understand so well how\n" +
                                  "it works, but I think if you get an exception,\n" +
                                  "you need to load a font like:\n" +
                                  "PDFont font = PDTrueTypeFont.loadTTF(document, \"Arial.ttf\");\n" +
                                  "See:\n" +
                                  "https://pdfbox.apache.org/1.8/cookbook/\n" +
                                  "workingwithfonts.html\n" +
                                  "\n\n" +
                                  "here\n" +
                                  "are\n" +
                                  "more lines\n" +
                                  //                                   "Россия – священная наша держава,\n" +
                                  //                                   "Россия – любимая наша страна.\n" +
                                  //                                   "Могучая воля, великая слава –\n" +
                                  //                                   "Твоё достоянье на все времена!\n" +
                                  //                                   null,
                                  //                                   "Chorus:\n" +
                                  //                                   null,
                                  //                                   "Славься, Отечество наше свободное, Братских народов союз\n" +
                                  //                                   " вековой, Предками данная мудрость народная! Славься, страна!\n" +
                                  //                                   " Мы гордимся тобой!\n" +
                                  //                                   null,
                                  //                                   "От южных морей до полярного края Раскинулись наши леса и\n" +
                                  //                                   " поля. Одна ты на свете! Одна ты такая – Хранимая Богом \n" +
                                  //                                   "родная земля!\n" +
                                  //                                   null,
                                  //                                   "Chorus:\n" +
                                  //                                   null,
                                  //                                   "Широкий простор для мечты и для жизни\n" +
                                  //                                   "Грядущие нам открывают года.\n" +
                                  //                                   "Нам силу даёт наша верность Отчизне.\n" +
                                  //                                   "Так было, так есть и так будет всегда!\n" +
                                  //                                   null,
                                  //                                   "Chorus\n" +
                                  //                                   null,
                                  //                                   null,
                                  //                                   null,
                                  //                                   "Chinese will not print.  The substitution character is a\n" +
                                  //                                   " bullet, so below should be lots of bullets.\n" +
                                  //                                   null,
                                  //                                   "起來！不願做奴隸的人們！ \n" +
                                  //                                   "把我們的血肉，築成我們新的長城！ \n" +
                                  //                                   "中華民族到了最危險的時候， \n" +
                                  //                                   "每個人被迫著發出最後的吼聲。 \n" +
                                  //                                   "起來！起來！起來！ \n" +
                                  //                                   "我們萬眾一心， \n" +
                                  //                                   "冒著敵人的炮火，前進！ \n" +
                                  //                                   "冒著敵人的炮火，前進！ \n" +
                                  //                                   "前進！前進！進！\n" +
                                  "\n\n" +
                                  "Here is a picture with the default and other sizes.  Though\n" +
                                  " it shows up several times, the image data is only attached\n" +
                                  " to the file once and reused."),
                             ScaledImage(melonPic),
                             ScaledImage(melonPic, XyDim(50f, 50f)),
                             Text(regular, " Melon "),
                             ScaledImage(melonPic, XyDim(50f, 50f)),
                             Text(regular, " Yum!"),
                             ScaledImage(melonPic, XyDim(170f, 100f)),
                             Text(regular, "Watermelon!")))
                .cell(regularCell,
                      listOf(Text(textStyle = regular,
                                  initialText = "O say can you see by the dawn's early light,\n" +
                                                "What so proudly we hailed at the twilight's last gleaming,\n" +
                                                "Whose broad stripes and bright stars\n" +
                                                "through the perilous fight,\n" +
                                                "O'er the ramparts we watched, were so gallantly streaming?\n" +
                                                "And the rockets' red glare, the bombs bursting in air,\n" +
                                                "Gave proof through the night that our flag was still there;\n" +
                                                "O say does that star-spangled banner yet wave,\n" +
                                                "O'er the land of the free and the home of the brave?\n" +
                                                "\n" +
                                                "On the shore dimly seen through the mists of the deep,\n" +
                                                "Where the foe's haughty host in dread silence reposes,\n" +
                                                "What is that which the breeze, o'er the towering steep,\n" +
                                                "As it fitfully blows, half conceals, half discloses?\n" +
                                                "Now it catches the gleam of the morning's first beam,\n" +
                                                "In full glory reflected now shines in the stream:\n" +
                                                "'Tis the star-spangled banner, O! long may it wave\n" +
                                                "O'er the land of the free and the home of the brave.\n" +
                                                "\n" +
                                                "\n" +
                                                "\n" +
                                                "\n" +
                                                "\n" +
                                                "And where is that band who so vauntingly swore\n" +
                                                "That the havoc of war and the battle's confusion,\n" +
                                                "A home and a country, should leave us no more?\n" +
                                                "Their blood has washed out their foul footsteps' pollution.\n" +
                                                "No refuge could save the hireling and slave\n" +
                                                "From the terror of flight, or the gloom of the grave:\n" +
                                                "And the star-spangled banner in triumph doth wave,\n" +
                                                "O'er the land of the free and the home of the brave.\n" +
                                                "\n" +
                                                "O thus be it ever, when freemen shall stand \n" +
                                                "Between their loved home and the war's desolation. \n" +
                                                "Blest with vict'ry and peace, may the Heav'n rescued land \n" +
                                                "Praise the Power that hath made and preserved us a nation! \n" +
                                                "Then conquer we must, when our cause it is just, \n" +
                                                "And this be our motto: \"In God is our trust.\" \n" +
                                                "And the star-spangled banner in triumph shall wave \n" +
                                                "O'er the land of the free and the home of the brave!\n" +
                                                "\n" +
                                                "more \n" +
                                                "lines \n" +
                                                "to \n" +
                                                "test")))
                .cell(regularCell,
                      listOf(Text(regular,
                                  "Maamme\n" +
                                  "\n" +
                                  "Monument to the Vårt Land poem in Helsinki. \n" +
                                  "Oi maamme, Suomi, synnyinmaa, \n" +
                                  "soi, sana kultainen! \n" +
                                  "Ei laaksoa, ei kukkulaa, \n" +
                                  "ei vettä, rantaa rakkaampaa \n" +
                                  "kuin kotimaa tää pohjoinen, \n" +
                                  "maa kallis isien. \n" +
                                  "Sun kukoistukses kuorestaan \n" +
                                  "kerrankin puhkeaa; \n" +
                                  "viel' lempemme saa nousemaan \n" +
                                  "sun toivos, riemus loistossaan, \n" +
                                  "ja kerran laulus, synnyinmaa \n" +
                                  "korkeemman kaiun saa.\n" +
                                  "\n" +
                                  "Vårt land\n" +
                                  "\n" +
                                  "(the original, by Johan Ludvig Runeberg) \n" +
                                  "Vårt land, vårt land, vårt fosterland, \n" +
                                  "ljud högt, o dyra ord! \n" +
                                  "Ej lyfts en höjd mot himlens rand, \n" +
                                  "ej sänks en dal, ej sköljs en strand, \n" +
                                  "mer älskad än vår bygd i nord, \n" +
                                  "än våra fäders jord! \n" +
                                  "Din blomning, sluten än i knopp, \n" +
                                  "Skall mogna ur sitt tvång; \n" +
                                  "Se, ur vår kärlek skall gå opp \n" +
                                  "Ditt ljus, din glans, din fröjd, ditt hopp. \n" +
                                  "Och högre klinga skall en gång \n" +
                                  "Vår fosterländska sång.\n\n")))
                .cell(regularCell,
                      listOf(Text(regular,
                                  "Einigkeit und Recht und Freiheit \n" +
                                  "Für das deutsche Vaterland! \n" +
                                  "Danach lasst uns alle streben \n" +
                                  "Brüderlich mit Herz und Hand! \n" +
                                  "Einigkeit und Recht und Freiheit \n" +
                                  "Sind des Glückes Unterpfand;\n" +
                                  "Blüh' im Glanze dieses Glückes, \n" +
                                  "  Blühe, deutsches Vaterland!")))
                .buildRow()
                .rowBuilder()
                .cell(regularCell, listOf(Text(regular, "Another row of cells")))
                .cell(regularCell, listOf(Text(regular, "On the second page")))
                .cell(regularCell, listOf(Text(regular, "Just like any other page")))
                .cell(regularCell, listOf(Text(regular, "That's it!")))
                .buildRow()
                .buildPart()
        tB.buildTable()
                .wrap()
                .render(lp, lp.bodyTopLeft())
        lp.commit()

        val lineStyle = LineStyle(RGB_BLACK, 1f)

        lp = pageMgr.logicalPageStart(PdfLayoutMgr.Orientation.LANDSCAPE
        ) { pageNum, pb->
            val cell = Cell(pageHeadCellStyle, tableWidth,
                            pageHeadTextStyle,
                            listOf(("Test Logical Page Four " +
                                    " (physical page " + pageNum + ")")))
            cell.wrap().render(pb, XyOffset(pMargin, LETTER.width - 27))
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
