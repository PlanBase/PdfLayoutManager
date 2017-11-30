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
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Point2d
import java.awt.image.BufferedImage

/**
 * Represents a Jpeg image and the document units it should be scaled to.  When a ScaledImage is added
 * to a PdfLayoutMgr, its underlying bufferedImage is compared to the images already embedded in that
 * PDF file.  If an equivalent bufferedImage object is found, the underlying image is not added to
 * the document twice.  Only the additional position and scaling of that image is added.  This
 * significantly decreases the file size of the resulting PDF when images are reused within that
 * document.
 */
class ScaledImage(private val bufferedImage: BufferedImage,
                  val dimensions: Dimensions) : LineWrappable {

    /**
     * Returns a new buffered image with width and height calculated from the source BufferedImage
     * assuming that it will print at 300 DPI.  There are 72 document units per inch, so the actual
     * formula is: bi.width / 300 * 72
     * @param bufferedImage the source BufferedImage
     * @return a ScaledImage holding the width and height for that image.
     */
    constructor(bufferedImage:BufferedImage) :
            this(bufferedImage, Dimensions(bufferedImage.width * IMAGE_SCALE,
                                      bufferedImage.height * IMAGE_SCALE))

    fun wrap():WrappedImage = WrappedImage(bufferedImage, dimensions)

    override fun lineWrapper(): LineWrapper = LineWrapper.preWrappedLineWrapper(WrappedImage(bufferedImage, dimensions))

    data class WrappedImage(val bufferedImage: BufferedImage,
                            override val dimensions: Dimensions) : LineWrapped {

        override val ascent: Float = dimensions.height

        override val descentAndLeading: Float = 0f

        override val lineHeight: Float = dimensions.height

        /** {@inheritDoc}  */
        override fun render(lp: RenderTarget, topLeft: Point2d): Dimensions =
                dimensions.height(lp.drawImage(topLeft, this))
    }

    companion object {
        private val ASSUMED_IMAGE_DPI = 300f
        val IMAGE_SCALE = 1f / ASSUMED_IMAGE_DPI * PdfLayoutMgr.DOC_UNITS_PER_INCH
    }
}
