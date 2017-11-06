import com.planbase.pdf.layoutmanager.*
import com.planbase.pdf.layoutmanager.attributes.Align.*
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.TableBuilder
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream
import java.io.IOException

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
        val headingCell = BoxStyle(textCellPadding, RGB_BLUE,
                                                                             BorderStyle(null, LineStyle(RGB_WHITE), null, LineStyle(RGB_BLUE)))
        val headingCellR = BoxStyle(textCellPadding, RGB_BLACK,
                                                                              BorderStyle(null, LineStyle(RGB_BLACK), null, LineStyle(RGB_WHITE)))

        val regular = TextStyle(PDType1Font.HELVETICA, 9.5f, RGB_BLACK)
        val regularCell = BoxStyle(textCellPadding, null,
                                                                             BorderStyle(null, LineStyle(RGB_BLACK),
                                                                                         LineStyle(RGB_BLACK), LineStyle(RGB_BLACK)))

        // Let's draw three tables on our first landscape-style page grouping.

        // Draw the first table with lots of extra room to show off the vertical and horizontal
        // alignment.
        var tB = TableBuilder()
        tB.addCellWidths(listOf(120f))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, RGB_YELLOW_BRIGHT))
                .partBuilder()
                .boxStyle(BoxStyle(Padding(2f),
                                   RGB_BLUE_GREEN, BorderStyle(RGB_BLACK)))
                .align(BOTTOM_CENTER)
                .rowBuilder().addTextCells("First").buildRow()
                .buildPart()
                .partBuilder()
                .boxStyle(BoxStyle(Padding(2f),
                                   RGB_LIGHT_GREEN,
                                   BorderStyle(RGB_DARK_GRAY)))
                .align(MIDDLE_CENTER)
                .minRowHeight(120f)
                .textStyle(TextStyle(PDType1Font.COURIER, 12f, RGB_BLACK))
                .rowBuilder()
                .cellBuilder().align(TOP_LEFT).addStrs("Line 1\n", "Line two\n", "Line three").buildCell()
                .buildRow()
                .buildPart()
        tB.buildTable()
                .render(lp, XyOffset(40f, lp.yBodyTop()))
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
        internal val RGB_BLACK = PDColor(floatArrayOf(0f, 0f, 0f), PDDeviceRGB.INSTANCE)
        internal val RGB_BLUE = PDColor(floatArrayOf(0.2f, 0.2f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_BLUE_GREEN = PDColor(floatArrayOf(0.2f, 0.4f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_DARK_GRAY = PDColor(floatArrayOf(0.2f, 0.2f, 0.2f), PDDeviceRGB.INSTANCE)
        internal val RGB_LIGHT_GREEN = PDColor(floatArrayOf(0.8f, 1f, 0.8f), PDDeviceRGB.INSTANCE)
        internal val RGB_WHITE = PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_YELLOW_BRIGHT = PDColor(floatArrayOf(1f, 1f, 0f), PDDeviceRGB.INSTANCE)
    }
}
