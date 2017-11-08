import TestManualllyPdfLayoutMgr.Companion.RGB_BLACK
import TestManualllyPdfLayoutMgr.Companion.RGB_BLUE_GREEN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.Cell
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.contents.WrappedCell
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream
import java.io.IOException
import kotlin.test.assertEquals

class TestManualCell {
    @Test
    @Throws(IOException::class)
    fun testPdf() {
        // Nothing happens without a PdfLayoutMgr.
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, XyDim(PDRectangle.LETTER))

        // A PageGrouping is a group of pages with the same settings.  When your contents scroll off
        // the bottom of a page, a new page is automatically created for you with the settings taken
        // from the LogicPage grouping. If you don't want a new page, be sure to stay within the
        // bounds of the current one!
        val lp = pageMgr.logicalPageStart()

        // Let's draw a single cell.

        val boxStyle = BoxStyle(Padding(2f), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))
        val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5f, RGB_BLACK)
        val cellWidth = 300f
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(Text(textStyle, "Hello")))
        println(cell)
        println()
        val wrappedCell: WrappedCell = cell.fix()
        println(wrappedCell)

// TODO: Re-enable
        assertEquals(textStyle.lineHeight() + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.lineHeight)

        assertEquals(cellWidth,
                     wrappedCell.xyDim.width)

        val xyOff : XyOffset = wrappedCell.render(lp, XyOffset(40f, 200f))

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
        val os = FileOutputStream("test3.pdf")

        // Commit it to the output stream!
        pageMgr.save(os)
    }
}