import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.utils.Utils
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream

class TestMultiPageLines {
    @Test fun testMultiPageLines() {
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
        val lp = pageMgr.logicalPageStart()

        // Set up some useful constants for later.
        val tableWidth = lp.pageWidth() - 2 * pMargin
        val pageRMargin = pMargin + tableWidth

        val lineStyle = LineStyle(Utils.RGB_BLACK, 1f)

        // Make a big 3-page X in a box.  Notice that we code it as though it's on one page, and the
        // API adds two more pages as needed.  This is a great test for how geometric shapes break
        // across pages.

        val topLeft = XyOffset(pMargin, lp.yBodyTop())
        val topRight = XyOffset(pageRMargin, lp.yBodyTop())
        val bottomRight = XyOffset(pageRMargin, -lp.yBodyTop())
        val bottomLeft = XyOffset(pMargin, -lp.yBodyTop())

        // top lne
        lp.drawLine(topLeft, topRight, lineStyle)
        // right line
        lp.drawLine(topRight, bottomRight, lineStyle)
        // bottom line
        lp.drawLine(bottomRight, bottomLeft, lineStyle)
        // left line
        lp.drawLine(bottomLeft, topLeft, lineStyle)

        // 3-page-long X
        lp.drawLine(topLeft, bottomRight, lineStyle)
        // Note reversed params
        lp.drawLine(bottomLeft, topRight, lineStyle)

        // middle line
        lp.drawLine(XyOffset(pMargin, 0f), XyOffset(pageRMargin, 0f), lineStyle)
        lp.commit()

        val os = FileOutputStream("multiPageLines.pdf")
        pageMgr.save(os)
    }

}