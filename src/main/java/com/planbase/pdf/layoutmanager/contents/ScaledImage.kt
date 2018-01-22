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
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import java.awt.image.BufferedImage

/**
 * Represents a Jpeg image and the document units it should be scaled to.  When a ScaledImage is added
 * to a PdfLayoutMgr, its underlying bufferedImage is compared to the images already embedded in that
 * PDF file.  If an equivalent bufferedImage object is found, the underlying image is not added to
 * the document twice.  Only the additional position and scaling of that image is added.  This
 * significantly decreases the file size of the resulting PDF when images are reused within that
 * document.
 */
data class ScaledImage(private val bufferedImage: BufferedImage,
                       val dim: Dim) : LineWrappable {

    /**
     * Returns a new buffered image with width and height calculated from the source BufferedImage
     * assuming that it will print at 300 DPI.  There are 72 document units per inch, so the actual
     * formula is: bi.width / 300 * 72
     * @param bufferedImage the source BufferedImage
     * @return a ScaledImage holding the width and height for that image.
     */
    constructor(bufferedImage:BufferedImage) :
            this(bufferedImage, Dim(bufferedImage.width * IMAGE_SCALE,
                                      bufferedImage.height * IMAGE_SCALE))

    fun wrap():WrappedImage = WrappedImage(bufferedImage, dim)

    override fun lineWrapper(): LineWrapper = LineWrapper.preWrappedLineWrapper(WrappedImage(bufferedImage, dim))

    override fun toString(): String =
//            "ScaledImage(${buffImgToStr(bufferedImage)}, $dim)"
            "ScaledImage(img, $dim)"

    data class WrappedImage(val bufferedImage: BufferedImage,
                            override val dim: Dim) : LineWrapped {

        override val ascent: Float = dim.height

        override val lineHeight: Float = dim.height

        override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): Dim =
                dim.height(lp.drawImage(topLeft.minusY(dim.height), this, reallyRender))
    }

    companion object {
        private const val ASSUMED_IMAGE_DPI: Float = 300f

        /**
         * The default scaling factor for images.  Assumes image is normally seen at 300dpi and your output document
         * uses 72 units per inch.
         */
        const val IMAGE_SCALE: Float = 1f / ASSUMED_IMAGE_DPI * PdfLayoutMgr.DOC_UNITS_PER_INCH
    }
}
