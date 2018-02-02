// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr2
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager

import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.contents.ScaledImage.WrappedImage
import com.planbase.pdf.layoutmanager.pages.PageGrouping
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.util.Matrix
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.HashMap

/**
 * Manages a PDF document.  You need one of these to do almost anything useful.
 *
 * ## Usage (the unit test is a much better example)
 * ```kotlin
 * // Create a new manager
 * PdfLayoutMgr pageMgr = PdfLayoutMgr.newRgbPageMgr();
 *
 * PageGrouping lp = pageMgr.startPageGrouping();
 * // defaults to Landscape orientation
 * // call various lp.tableBuilder() or lp.put...() methods here.
 * // They will page-break and create extra physical pages as needed.
 * // ...
 * lp.commit();
 *
 * lp = pageMgr.startPageGrouping(PORTRAIT);
 * // These pages will be in Portrait orientation
 * // call various lp methods to put things on the next page grouping
 * // ...
 * lp.commit();
 *
 * // The file to write to
 * OutputStream os = new FileOutputStream("test.pdf");
 *
 * // Commit all pages to output stream.
 * pageMgr.save(os);
 * ```
 *
 * # Note:
 *
 * Because this class buffers and writes to an underlying stream, it is mutable, has side effects,
 * and is NOT thread-safe!
 *
 * @param colorSpace the color-space for the document.  Often PDDeviceCMYK.INSTANCE or PDDeviceRGB.INSTANCE
 * @param pageDim Returns the width and height of the paper-size where THE HEIGHT IS ALWAYS THE LONGER DIMENSION.
 * You may need to swap these for landscape: `pageDim().swapWh()`.  For this reason, it's not a
 * good idea to use this directly.  Use the corrected values through a [PageGrouping] instead.
 * @param pageReactor Takes a page number and returns an x-offset for that page.  Use this for effects like page
 * numbering or different offsets or headers/footers for even and odd pages or the start of a chapter.
 */
class PdfLayoutMgr(private val colorSpace: PDColorSpace,
                   val pageDim: Dim,
                   private var pageReactor:((Int, SinglePage) -> Float)? = null) {
    private val doc = PDDocument()

    /**
     * This returns the wrapped PDDocument instance.  This is a highly mutable data structure.
     * Accessing it directly while also using PdfLayoutMgr is inherently unsafe and untested.
     * This method is provided so you can do things like add encryption or use other features of PDFBox not yet
     * directly supported by PdfLayoutMgr.
     */
    fun getPDDocButBeCareful(): PDDocument = doc

    // You can have many DrawJpegs backed by only a few images - it is a flyweight, and this
    // hash map keeps track of the few underlying images, even as instances of DrawJpeg
    // represent all the places where these images are used.
    // CRITICAL: This means that the the set of jpgs must be thrown out and created anew for each
    // document!  Thus, a private final field on the PdfLayoutMgr instead of DrawJpeg, and DrawJpeg
    // must be an inner class (or this would have to be package scoped).
    private val imageCache = HashMap<BufferedImage, PDImageXObject>()

    private val pages:MutableList<SinglePage> = mutableListOf()

    // pages.size() counts the first page as 1, so 0 is the appropriate sentinel value
    private var unCommittedPageIdx:Int = 0

    fun unCommittedPageIdx():Int = unCommittedPageIdx

    enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    internal fun ensureCached(sj: WrappedImage): PDImageXObject {
        val bufferedImage = sj.bufferedImage
        var temp: PDImageXObject? = imageCache[bufferedImage]
        if (temp == null) {
            val problems:MutableList<Throwable> = mutableListOf()

            temp = try {
                LosslessFactory.createFromImage(doc, bufferedImage)
            } catch (t: Throwable) {
                problems.plus(t)
                try {
                    JPEGFactory.createFromImage(doc, bufferedImage)
                } catch (u: Throwable) {
                    if (problems.isEmpty()) {
                        throw Exception("Caught exception creating a JPEG from a bufferedImage", u)
                    } else {
                        problems.plus(u)
                        throw Exception("Caught exceptions creating Lossless/JPEG PDImageXObjects from" +
                                        " a bufferedImage: $problems")
                    }
                }
            }

            imageCache.put(bufferedImage, temp!!)
        }
        return temp
    }

    fun hasAnyPages():Boolean = pages.size > 0

    fun page(idx:Int):SinglePage = pages[idx]

    fun ensurePageIdx(idx:Int) {
        while (pages.size <= idx) {
            pages.add(SinglePage(pages.size + 1, this, pageReactor))
        }
    }

    /**
     * Call this to commit the PDF information to the underlying stream after it is completely built.
     */
    @Throws(IOException::class)
    fun save(os: OutputStream) {
        doc.save(os)
        doc.close()
    }

    /**
     * Tells this PdfLayoutMgr that you want to start a new logical page (which may be broken across
     * two or more physical pages) in the requested page orientation.
     */
    // Part of end-user public interface
    fun startPageGrouping(o: Orientation,
                          pr: ((Int, SinglePage) -> Float)?): PageGrouping {
        pageReactor = pr
        val pb = SinglePage(pages.size + 1, this, pageReactor)
        pages.add(pb)
        val bodyDim: Dim = if (o == Orientation.PORTRAIT)
            this.pageDim.minus(DEFAULT_DOUBLE_MARGIN_DIM)
        else
            this.pageDim.swapWh().minus(DEFAULT_DOUBLE_MARGIN_DIM)
        return PageGrouping(this, o, Coord(DEFAULT_MARGIN, DEFAULT_MARGIN + bodyDim.height), bodyDim)
    }

    /**
     * Tells this PdfLayoutMgr that you want to start a new logical page (which may be broken across
     * two or more physical pages) in the requested page orientation.
     */
    fun startPageGrouping(o: Orientation): PageGrouping = startPageGrouping(o, null)

    /**
     * Get a new logical page (which may be broken across two or more physical pages) in Landscape orientation.
     */
    fun startPageGrouping(): PageGrouping = startPageGrouping(LANDSCAPE, null)

    /**
     * Loads a TrueType font (and embeds it into the document?) from the given file into a
     * PDType0Font object.
     */
    @Throws(IOException::class)
    fun loadTrueTypeFont(fontFile: File): PDType0Font = PDType0Font.load(doc, fontFile)

    /**
     * Call this when you are through with your current set of pages to commit all pending text and
     * drawing operations.  This is the only method that throws an IOException because the purpose of
     * PdfLayoutMgr is to buffer all operations until a page is complete so that it can safely be
     * written to the underlying stream.  This method turns the potential pages into real output.
     * Call when you need a page break, or your document is done and you need to write it out.
     *
     * @throws IOException if there is a failure writing to the underlying stream.
     */
    @Throws(IOException::class)
    fun logicalPageEnd(lp: PageGrouping) {

        // Write out all uncommitted pages.
        while (unCommittedPageIdx < pages.size) {
            val pdPage = PDPage(pageDim.toRect())
            if (lp.orientation === LANDSCAPE) {
                pdPage.rotation = 90
            }
            var stream: PDPageContentStream? = null
            try {
                stream = PDPageContentStream(doc, pdPage)
                doc.addPage(pdPage)

                if (lp.orientation === LANDSCAPE) {
                    stream.transform(Matrix(0f, 1f, -1f, 0f, pageDim.width, 0f))
                }
                stream.setStrokingColor(colorSpace.initialColor)
                stream.setNonStrokingColor(colorSpace.initialColor)

                val pb = pages[unCommittedPageIdx]
                pb.commit(stream)
                lp.commitBorderItems(stream)

                stream.close()
                // Set to null to show that no exception was thrown and no need to close again.
                stream = null
            } finally {
                // Let it throw an exception if the closing doesn't work.
                if (stream != null) {
                    stream.close()
                }
            }
            unCommittedPageIdx++
        }
    }

    override fun equals(other: Any?): Boolean {
        // First, the obvious...
        if (this === other) {
            return true
        }
        if (other == null) {
            return false
        }
        if (other !is PdfLayoutMgr) {
            return false
        }
        // Details...
        return this.doc == other.doc && this.pages == other.pages
    }

    override fun hashCode(): Int = doc.hashCode() + pages.hashCode()

    companion object {
        /**
         * If you use no scaling when printing the output PDF, PDFBox shows approximately 72
         * Document-Units Per Inch.  This makes one pixel on an average desktop monitor correspond to
         * roughly one document unit.  This is a useful constant for page layout math.
         */
        const val DOC_UNITS_PER_INCH: Float = 72f

        /**
         * Some printers need at least 1/2" of margin (36 "pixels") in order to accept a print job.
         * This amount seems to accommodate all printers.
         */
        const val DEFAULT_MARGIN: Float = 37f

        private val DEFAULT_DOUBLE_MARGIN_DIM = Dim(DEFAULT_MARGIN * 2, DEFAULT_MARGIN * 2)
    }
}
