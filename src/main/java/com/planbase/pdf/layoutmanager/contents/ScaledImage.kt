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

package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import java.awt.image.BufferedImage

/**
 * Represents a Jpeg image and the document units it should be scaled to.  When a ScaledImage is added
 * to a PdfLayoutMgr, its underlying bufferedImage is compared to the images already embedded in that
 * PDF file.  If an equivalent bufferedImage object is found, the underlying image is not added to
 * the document twice.  Only the additional position and scaling of that image is added.  This
 * significantly decreases the file size of the resulting PDF when images are reused within that
 * document.
 */
class ScaledImage(val bufferedImage: BufferedImage,
                  override val xyDim: XyDim) : LineWrapped, LineWrappable {

    override val boxStyle = BoxStyle.NONE

    /**
     * Returns a new buffered image with width and height calculated from the source BufferedImage
     * assuming that it will print at 300 DPI.  There are 72 document units per inch, so the actual
     * formula is: bi.width / 300 * 72
     * @param bufferedImage the source BufferedImage
     * @return a ScaledImage holding the width and height for that image.
     */
    constructor(bufferedImage:BufferedImage) :
            this(bufferedImage, XyDim(bufferedImage.width * IMAGE_SCALE,
                                      bufferedImage.height * IMAGE_SCALE))

    override val ascent: Float = xyDim.height

    override val descentAndLeading: Float = 0f

    override val lineHeight: Float = xyDim.height

    /** {@inheritDoc}  */
    override fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        // use bottom of image for page-breaking calculation.
        val y = lp.drawImage(outerTopLeft.x, outerTopLeft.y, this)
        return XyOffset(outerTopLeft.x + xyDim.width, y)
    }

    override fun lineWrapper(): LineWrapper = LineWrapped.preWrappedLineWrapper(this)

    companion object {
        private val ASSUMED_IMAGE_DPI = 300f
        val IMAGE_SCALE = 1f / ASSUMED_IMAGE_DPI * PdfLayoutMgr.DOC_UNITS_PER_INCH
    }
}
