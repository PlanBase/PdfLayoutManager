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
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Companion.DOC_UNITS_PER_INCH
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.*
import com.planbase.pdf.layoutmanager.contents.Text
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER
import org.apache.pdfbox.pdmodel.font.PDType1Font.*
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.junit.Test
import java.io.FileOutputStream

/**
 * Very simple how-to-use example file
 */
class TestBasics {

    /**
     * For a tour of more features, check out [TestAliceInWonderland].
     * For a somewhat outdated tour of tables and lines see [TestManuallyPdfLayoutMgr].
     */
    @Test
    fun testSimple() {
        // Make a manager with the given color model and starting page size.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(LETTER))

        // Declare a text style to use.
        val bodyText = TextStyle(TIMES_ITALIC, 36.0, CMYK_BLACK)

        // Start a bunch of pages (add more text to use more than one)
        val lp = pageMgr.startPageGrouping(PORTRAIT, letterPortraitBody)

        // Stick some text on the page(s)
        lp.appendCell(0.0, CellStyle(Align.TOP_LEFT_JUSTIFY, BoxStyle.NO_PAD_NO_BORDER),
                      listOf(Text(bodyText, "\"Darkness within darkness: the gateway to all" +
                                            " understanding.\" — Lao Tzu")))

        // Commit all your work and write it to a file
        pageMgr.commit()
        pageMgr.save(FileOutputStream("helloWorld.pdf"))
    }

    companion object {
        private const val oneInch = DOC_UNITS_PER_INCH

        val letterPortraitBody = PageArea(Coord(oneInch, // from left-hand-side
                                                LETTER.height - oneInch), // from bottom of page

                                          // Body area is an inch from each side of the page, so 2" smaller over all.
                                          Dim(LETTER).minus(Dim(oneInch * 2, oneInch * 2)))

    }
}