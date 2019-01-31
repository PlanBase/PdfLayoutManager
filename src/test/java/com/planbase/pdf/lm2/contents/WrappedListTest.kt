package com.planbase.pdf.lm2.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import TestManual2.Companion.a6PortraitBody
import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.attributes.*
import com.planbase.pdf.lm2.attributes.Orientation.*
import com.planbase.pdf.lm2.utils.BULLET_CHAR
import com.planbase.pdf.lm2.utils.CMYK_BLACK
import com.planbase.pdf.lm2.utils.Dim
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.util.Charsets
import org.junit.Test
import kotlin.math.nextUp
import kotlin.test.assertEquals

class WrappedListTest {
    val padding = Padding(2.0)
    val cellStyle = CellStyle(Align.TOP_LEFT,
                              BoxStyle(padding, null,
                                       BorderStyle(LineStyle(CMYK_BLACK, 0f.nextUp().toDouble())))) //BorderStyle.NO_BORDERS))

    @Test fun sixthBullet2LinesBeforePgBreak() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(
                PORTRAIT,
                a6PortraitBody
        )

        val bl = BulletList(200.0, 20.0, cellStyle, Align.TOP_RIGHT, padding, BULLET_TEXT_STYLE, BULLET_CHAR)
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "First Bullet")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Second Bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "This is a third bullet which has lots of text - enough to wrap at least once"
                        )
                )
        )
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Fourth bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text. "
                        )
                )
        )
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Sixth bullet which has two lines of text on this page and several gorgeous lines on" +
                                " the next page so that there's no chance of a widow."
                        )
                )
        )

        val wrappedList = bl.wrap()
        lp.append(wrappedList)
        assertEquals(-4.616000000000042, lp.cursorY)

        val docId = COSString("Bullet Test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testBullets1.pdf"))
    }

    // Orphan prevention test!
    @Test fun sixthBullet1LineBeforePgBreak() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(
                PORTRAIT,
                a6PortraitBody
        )

        val bl = BulletList(200.0, 20.0, cellStyle, Align.TOP_RIGHT, padding, BULLET_TEXT_STYLE, BULLET_CHAR)
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "First Bullet")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Second Bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "This is a third bullet which has lots of text - enough to wrap at least once"
                        )
                )
        )
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Fourth bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text.  Oh yeah. "
                        )
                )
        )
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Sixth bullet which gets moved to the next page instead of leaving a single orphan" +
                                " line on the first page.  This should look dandy!"
                        )
                )
        )

        val wrappedList = bl.wrap()
        lp.append(wrappedList)
        assertEquals(-22.488000000000056, lp.cursorY)

        val docId = COSString("Bullet Test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testBullets1.pdf"))
    }

    @Test fun sixthBulletNoPgBreak() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(
                PORTRAIT,
                a6PortraitBody
        )

        val bl = BulletList(200.0, 20.0, cellStyle, Align.TOP_RIGHT, padding, BULLET_TEXT_STYLE, BULLET_CHAR)
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "First Bullet")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Second Bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "This is a third bullet which has lots of text - enough to wrap at least once"
                        )
                )
        )
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Fourth bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text. "
                        )
                )
        )
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Sixth bullet which has 2 lines of text that both fit on this page."
                        )
                )
        )

        val wrappedList = bl.wrap()
        lp.append(wrappedList)
        assertEquals(39.47155737304672, lp.cursorY)

        val docId = COSString("Bullet Test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testBullets2.pdf"))
    }

    @Test fun sixthBullet2LinesAfterPgBreak() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(
                PORTRAIT,
                a6PortraitBody
        )

        val bl = NumberList(200.0, 20.0, cellStyle, Align.TOP_RIGHT, padding, BULLET_TEXT_STYLE, 1, ".")
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "First Bullet")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Second Bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "This is a third bullet which has lots of text - enough to wrap at least once"
                        )
                )
        )
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Fourth bullet")))
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once. " +
                                "Fifth bullet which has lots of text - enough to wrap at least once."
                        )
                )
        )
        bl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "Sixth bullet, 2 lines of text - enough to end on next page."
                        )
                )
        )

        val wrappedList = bl.wrap()
        lp.append(wrappedList)
        assertEquals(5.255999999999972, lp.cursorY)

        val docId = COSString("Bullet Test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testBullets3.pdf"))
    }

    @Test fun nestedLists() {
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(
                PORTRAIT,
                a6PortraitBody
        )

        val nl = NumberList(200.0, 20.0, cellStyle, Align.TOP_RIGHT, padding, BULLET_TEXT_STYLE, 1, ".")
        nl.addItem(listOf(Text(BULLET_TEXT_STYLE, "First Bullet")))
        nl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Second Bullet second second second second second second second second second second second second second second")))
        nl.addItem(
                listOf(
                        Text(
                                BULLET_TEXT_STYLE,
                                "This is a third bullet which has lots of text - enough to wrap at least once" +
                                " third third third third third third third third third third third third third third third third third third"
                        )
                )
        )
        nl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Fourth bullet fourth fourth fourth fourth fourth fourth fourth fourth fourth fourth fourth fourth fourth fourth fourth fourth")))

        val bl = BulletList(170.0, 20.0, cellStyle, Align.TOP_RIGHT, padding, BULLET_TEXT_STYLE, "-")
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "One")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Two")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Three")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Four all fits on page one and line-wraps two full times there.")))
        bl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Five on page two.")))

        nl.addItem(listOf(Text(BULLET_TEXT_STYLE, "Fifth primary bullet has sub-list:"),
                          bl))

        val wrappedList = nl.wrap()
        lp.append(wrappedList)
        assertEquals(23.12799999999993, lp.cursorY)

        val docId = COSString("Bullet Test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testBullets4.pdf"))
    }

//        val baos = ByteArrayOutputStream()
//
//        // We're just going to write to a file.
//        pageMgr.save(baos)
//
//        val byteArray = baos.toByteArray()
//
//        val pdfVerStr = "%PDF-1.4\n" +
//                        "%"
//        val startStr = "1 0 obj\n" +
//                       "<<\n" +
//                       "/Type /Catalog\n" +
//                       "/Version /1.4\n" +
//                       "/Pages 2 0 R\n" +
//                       ">>\n" +
//                       "endobj\n" +
//                       "3 0 obj\n" +
//                       "<<\n" +
//                       "/Producer (PlanBase PdfLayoutMgr2)\n" +
//                       ">>\n" +
//                       "endobj\n" +
//                       "2 0 obj\n" +
//                       "<<\n" +
//                       "/Type /Pages\n" +
//                       "/Kids [4 0 R]\n" +
//                       "/Count 1\n" +
//                       ">>\n" +
//                       "endobj\n" +
//                       "4 0 obj\n" +
//                       "<<\n" +
//                       "/Type /Page\n" +
//                       "/MediaBox [0.0 0.0 297.63782 419.52756]\n" +
//                       "/Contents 5 0 R\n" +
//                       "/Resources 6 0 R\n" +
//                       "/Parent 2 0 R\n" +
//                       ">>\n" +
//                       "endobj\n" +
//                       "5 0 obj\n" +
//                       "<<\n" +
//                       "/Length 154\n" +
//                       "/Filter /FlateDecode\n" +
//                       ">>\n" +
//                       "stream"
//
//        val endStr = "endstream\n" +
//                     "endobj\n" +
//                     "5 0 obj\n" +
//                     "<<\n" +
//                     "/Font 6 0 R\n" +
//                     ">>\n" +
//                     "endobj\n" +
//                     "6 0 obj\n" +
//                     "<<\n" +
//                     "/F1 7 0 R\n" +
//                     ">>\n" +
//                     "endobj\n" +
//                     "7 0 obj\n" +
//                     "<<\n" +
//                     "/Type /Font\n" +
//                     "/Subtype /Type1\n" +
//                     "/BaseFont /Helvetica\n" +
//                     "/Encoding /WinAnsiEncoding\n" +
//                     ">>\n" +
//                     "endobj\n" +
//                     "xref\n" +
//                     "0 8\n" +
//                     "0000000000 65535 f\n" +
//                     "0000000015 00000 n\n" +
//                     "0000000078 00000 n\n" +
//                     "0000000135 00000 n\n" +
//                     "0000000255 00000 n\n" +
//                     "0000000483 00000 n\n" +
//                     "0000000516 00000 n\n" +
//                     "0000000547 00000 n\n" +
//                     "trailer\n" +
//                     "<<\n" +
//                     "/Root 1 0 R\n" +
//                     "/ID [(Bullet Test PDF) (Bullet Test PDF)]\n" +
//                     "/Size 8\n" +
//                     ">>\n" +
//                     "startxref\n" +
//                     "644\n" +
//                     "%%EOF"
//
//        val actPdfVerStr = String(byteArray.slice(0 until pdfVerStr.length).toByteArray())
////        println("actPdfVerStr=$actPdfVerStr")
//        assertEquals(pdfVerStr, actPdfVerStr)
//
//        val verJunkLen = pdfVerStr.length + 4
//        val actStartBa = byteArray.slice(verJunkLen + 1 .. startStr.length + verJunkLen).toByteArray()
//        val actStartStr = String(actStartBa)
//
////        println("actStartStr=$actStartStr")
//        // Q: Why is this failing, but saying, "Contents are identical?
//        // Compare with byte arrays to find different characters that don't show up well in strings.
////        println("startStr.length=${startStr.length}")
//        assertEquals(startStr, actStartStr)
//
//        assertStringEquals(endStr, String(byteArray.slice((byteArray.size - endStr.length - 9) until byteArray.size - 1).toByteArray()))
////        assertArrayEquals(endStr.toByteArray(), byteArray.slice((byteArray.size - endStr.length - 9) until byteArray.size - 9).toByteArray())
////        assertEquals(endStr, String(byteArray.slice((byteArray.size - endStr.length - 9) until byteArray.size - 1).toByteArray()))
//
//        println("baos.toByteArray()=${String(baos.toByteArray())}")
//    }

//    fun assertStringEquals(a:String, b:String) {
//        val baa = a.toByteArray()
//        val bab = b.toByteArray()
//        var i = 0
//        while (i < baa.size) {
//            if (baa[i] != bab[i]) {
//                val startIdx:Int = max(0, i - 10)
//                val endIdx:Int = min(i + 10, baa.size - 1)
//                fail("Strings differ at element $i [${baa[i]}] vs. [${bab[i]}] near [${a.substring(startIdx .. endIdx)}]\n" +
//                     "Full Str a: $a\n" +
//                     "Full Str b: $b\n")
//            }
//
//            i++
//        }
//        assertEquals(a, b)
//    }
}