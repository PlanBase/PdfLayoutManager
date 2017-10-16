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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.util.Matrix
import org.organicdesign.fp.oneOf.Option
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.util.Collections
import java.util.HashMap

/**
 *
 * The main class in this package; it handles page and line breaks.
 *
 * <h3>Usage (the unit test is a much better example):</h3>
 * <pre>`// Create a new manager
 * PdfLayoutMgr pageMgr = PdfLayoutMgr.newRgbPageMgr();
 *
 * PageGrouping lp = pageMgr.logicalPageStart();
 * // defaults to Landscape orientation
 * // call various lp.tableBuilder() or lp.put...() methods here.
 * // They will page-break and create extra physical pages as needed.
 * // ...
 * lp.commit();
 *
 * lp = pageMgr.logicalPageStart(PORTRAIT);
 * // These pages will be in Portrait orientation
 * // call various lp methods to put things on the next page grouping
 * // ...
 * lp.commit();
 *
 * // The file to write to
 * OutputStream os = new FileOutputStream("test.pdf");
 *
 * // Commit all pages to output stream.
 * pageMgr.save(os);`</pre>
 * <br></br>
 * <h3>Note:</h3>
 *
 * Because this class buffers and writes to an underlying stream, it is mutable, has side effects,
 * and is NOT thread-safe!
 */
class PdfLayoutMgr(private val colorSpace: PDColorSpace,
                   /**
                    * Returns the width and height of the paper-size where THE HEIGHT IS ALWAYS THE LONGER DIMENSION.
                    * You may need to swap these for landscape: `pageDim().swapWh()`.  For this reason, it's not a
                    * good idea to use this directly.  Use the corrected values through a [PageGrouping]
                    * instead.
                    */
                   val pageDim:XyDim) {
    private val doc = PDDocument()
//    val pageDim = XyDim(mb ?: PDRectangle.LETTER)


    // TODO: add Sensible defaults, such as textStyle?
    //    private TextStyle textStyle;
    //    private PDRectangle pageDimensions;
    //    private Padding pageMargins;
    //    private PDRectangle printableArea;
    //
    //    public TextStyle textStyle() { return textStyle; }
    //    public PDRectangle pageDimensions() { return pageDimensions; }
    //    public Padding pageMargins() { return pageMargins; }
    //    public PDRectangle printableArea() { return printableArea; }

    // You can have many DrawJpegs backed by only a few images - it is a flyweight, and this
    // hash map keeps track of the few underlying images, even as intances of DrawJpeg
    // represent all the places where these images are used.
    // CRITICAL: This means that the the set of jpgs must be thrown out and created anew for each
    // document!  Thus, a private final field on the PdfLayoutMgr instead of DrawJpeg, and DrawJpeg
    // must be an inner class (or this would have to be package scoped).
    private val jpegMap = HashMap<BufferedImage, PDImageXObject>()

    // You can have many DrawPngs backed by only a few images - it is a flyweight, and this
    // hash map keeps track of the few underlying images, even as intances of DrawPng
    // represent all the places where these images are used.
    // CRITICAL: This means that the the set of jpgs must be thrown out and created anew for each
    // document!  Thus, a private final field on the PdfLayoutMgr instead of DrawPng, and DrawPng
    // must be an inner class (or this would have to be package scoped).
    private val pngMap = HashMap<BufferedImage, PDImageXObject>()

    private val pages:MutableList<SinglePage> = mutableListOf()
    // Just to start, takes a page number and returns an x-offset for that page.
    var pageReactor: ((Int, SinglePage) -> Float)? = null

    // pages.size() counts the first page as 1, so 0 is the appropriate sentinel value
    private var unCommittedPageIdx = 0

    enum class Orientation {
        PORTRAIT,
        LANDSCAPE
    }

    internal fun ensureCached(sj: ScaledJpeg): PDImageXObject {
        val bufferedImage = sj.bufferedImage
        var temp: PDImageXObject? = jpegMap[bufferedImage]
        if (temp == null) {
            try {
                temp = JPEGFactory.createFromImage(doc, bufferedImage)
            } catch (ioe: IOException) {
                // can there ever be an exception here?  Doesn't it get written later?
                throw IllegalStateException("Caught exception creating a PDImageXObject from a bufferedImage", ioe)
            }

            jpegMap.put(bufferedImage, temp)
        }
        return temp!!
    }

    internal fun ensureCached(sj: ScaledPng): PDImageXObject {
        val bufferedImage = sj.bufferedImage
        var temp: PDImageXObject? = pngMap[bufferedImage]
        if (temp == null) {
            try {
                temp = LosslessFactory.createFromImage(doc, bufferedImage)
            } catch (ioe: IOException) {
                // can there ever be an exception here?  Doesn't it get written later?
                throw IllegalStateException("Caught exception creating a PDImageXObject from a bufferedImage", ioe)
            }

            pngMap.put(bufferedImage, temp)
        }
        return temp!!
    }
    //    private final PDRectangle pageSize;

    internal fun pages(): List<SinglePage> {
        return Collections.unmodifiableList(pages)
    }

    /**
     * Returns the correct page for the given value of y.  This lets the user use any Y value and
     * we continue extending their canvas downward (negative) by adding extra pages.
     * @param origY the un-adjusted y value.
     * @return the proper page and adjusted y value for that page.
     */
    internal fun appropriatePage(lp: PageGrouping, origY: Float, height: Float): PageGrouping.PageBufferAndY {
        var y = origY
        if (pages.size < 1) {
            throw IllegalStateException("Cannot work with the any pages until one has been" + " created by calling newPage().")
        }
        var idx = unCommittedPageIdx
        // Get the first possible page.  Just keep moving to the top of the next page until it's in
        // the printable area.
        while (y < lp.yBodyBottom()) {
            y += lp.bodyHeight()
            idx++
            if (pages.size <= idx) {
                pages.add(SinglePage(pages.size + 1, this, Option.someOrNullNoneOf(pageReactor)))
            }
        }
        val ps = pages[idx]
        var adj = 0f
        if (y + height > lp.yBodyTop()) {
            val oldY = y
            y = lp.yBodyTop() - height
            adj = y - oldY
        }
        return PageGrouping.PageBufferAndY(ps, y, adj)
    }

    /**
     * Call this to commit the PDF information to the underlying stream after it is completely built.
     */
    @Throws(IOException::class)
    fun save(os: OutputStream) {
        doc.save(os)
        doc.close()
    }

    // TODO: Add logicalPage() method and call pages.add() lazily for the first item actually shown on a page, and logicalPageEnd called before a save.
    /**
     * Tells this PdfLayoutMgr that you want to start a new logical page (which may be broken across
     * two or more physical pages) in the requested page orientation.
     */
    // Part of end-user public interface
    fun logicalPageStart(o: Orientation,
                         pr: ((Int, SinglePage) -> Float)?): PageGrouping {
        pageReactor = pr
        val pb = SinglePage(pages.size + 1, this, Option.someOrNullNoneOf(pageReactor))
        pages.add(pb)
        return PageGrouping(this, o)
    }

    /**
     * Tells this PdfLayoutMgr that you want to start a new logical page (which may be broken across
     * two or more physical pages) in the requested page orientation.
     */
    fun logicalPageStart(o: Orientation): PageGrouping {
        return logicalPageStart(o, null)
    }

    /**
     * Get a new logical page (which may be broken across two or more physical pages) in Landscape orientation.
     */
    fun logicalPageStart(): PageGrouping {
        return logicalPageStart(LANDSCAPE, null)
    }

    /**
     * Loads a TrueType font (and embeds it into the document?) from the given file into a
     * PDType0Font object.
     */
    @Throws(IOException::class)
    fun loadTrueTypeFont(fontFile: File): PDType0Font {
        return PDType0Font.load(doc, fontFile)
    }

    /**
     * Call this when you are through with your current set of pages to commit all pending text and
     * drawing operations.  This is the only method that throws an IOException because the purpose of
     * PdfLayoutMgr is to buffer all operations until a page is complete so that it can safely be
     * written to the underlying stream.  This method turns the potential pages into real output.
     * Call when you need a page break, or your document is done and you need to write it out.
     *
     * @throws IOException - if there is a failure writing to the underlying stream.
     */
    @Throws(IOException::class)
    internal fun logicalPageEnd(lp: PageGrouping) {

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

    override fun hashCode(): Int {
        return doc.hashCode() + pages.hashCode()
    }

    companion object {

        // private Logger logger = Logger.getLogger(PdfLayoutMgr.class);

        //        logger.info("Ascent: " + PDType1Font.HELVETICA.getFontDescriptor().getAscent());
        //        logger.info("StemH: " + PDType1Font.HELVETICA.getFontDescriptor().getStemH());
        //        logger.info("CapHeight: " + PDType1Font.HELVETICA.getFontDescriptor().getCapHeight());
        //        logger.info("XHeight: " + PDType1Font.HELVETICA.getFontDescriptor().getXHeight());
        //        logger.info("Descent: " + PDType1Font.HELVETICA.getFontDescriptor().getDescent());
        //        logger.info("Leading: " + PDType1Font.HELVETICA.getFontDescriptor().getLeading());
        //
        //        logger.info("Height: " + PDType1Font.HELVETICA.getFontDescriptor().getFontBoundingBox().getHeight());
        //
        //        Ascent:    718.0
        //        StemH:       0.0
        //        CapHeight: 718.0
        //        XHeight:   523.0
        //        Descent:  -207.0
        //        Leading:     0.0
        //        Height:   1156.0
        // CapHeight - descent = 925
        // 925 - descent = 1132 which is still less than 1156.
        // I'm going to make line-height =
        // Java FontMetrics says getHeight() = getAscent() + getDescent() + getLeading().
        // I think ascent and descent are compatible with this.  I'm going to make Leading be
        // -descent/2

        /**
         * If you use no scaling when printing the output PDF, PDFBox shows approximately 72
         * Document-Units Per Inch.  This makes one pixel on an average desktop monitor correspond to
         * roughly one document unit.  This is a useful constant for page layout math.
         */
        val DOC_UNITS_PER_INCH = 72f
        // Some printers need at least 1/2" of margin (36 "pixels") in order to accept a print job.
        // This amount seems to accommodate all printers.
        val DEFAULT_MARGIN = 37f
    }

    //    public XyOffset putRect(XyOffset outerTopLeft, XyDim outerDimensions, final PDColor c) {
    ////        System.out.println("putRect(" + outerTopLeft + " " + outerDimensions + " " +
    ////                           Utils.toString(c) + ")");
    //        putRect(outerTopLeft.x(), outerTopLeft.y(), outerDimensions.x(), outerDimensions.y(), c);
    //        return XyOffset.of(outerTopLeft.x() + outerDimensions.x(),
    //                           outerTopLeft.y() - outerDimensions.y());
    //    }

    //    /**
    //     Puts text on the page.
    //     @param x the x-value of the top-left corner.
    //     @param origY the logical-page Y-value of the top-left corner.
    //     @param cell the cell containing the styling and text to render.
    //     @return the bottom Y-value (logical-page) of the rendered cell.
    //     */
    //    public float putCell(final float x, float origY, final Cell cell) {
    //        return cell.processRows(x, origY, false, this);
    //    }

}