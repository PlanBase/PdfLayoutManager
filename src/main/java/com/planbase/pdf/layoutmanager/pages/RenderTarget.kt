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

package com.planbase.pdf.layoutmanager.pages

import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.ScaledImage.WrappedImage
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset
import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 * Represents something to be drawn to.  For page-breaking, use the [PageGrouping]
 * implementation.  For a fixed, single page use the [SinglePage] implementation.
 */
interface RenderTarget {
    /**
     Must draw from higher to lower.  Thus y1 must be &gt;= y2 (remember, higher y values
     are up).
     @param topLeft the XyOffset of the topmost y and leftmost x
     @param bottomRight the XyOffset of the bottommost y and rightmost x
     @param lineStyle the style to draw the line with
     @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    // TODO: This should go back to being x1, y1, x2, y2 because lines can be drawn in any direction.
    fun drawLine(topLeft:XyOffset, bottomRight:XyOffset, lineStyle: LineStyle): RenderTarget

    /**
     Puts styled text on this RenderTarget
     @param bottomLeft the XyOffset of the lower-left-hand corner
     @param text the text
     @param textStyle the style
     @return the effective height after page breaking
     (may include some extra space above to push items onto the next page).
     */
    fun drawStyledText(bottomLeft:XyOffset, text: String, textStyle: TextStyle): Float

    /**
     Puts an image on this RenderTarget
     @param bottomLeft the XyOffset of the lower-left-hand corner
     @param wi the scaled, "wrapped" jpeg/png image
     @return the effective height after page breaking
     (may include some extra space above to push items onto the next page).
     */
    fun drawImage(bottomLeft:XyOffset, wi: WrappedImage): Float

    /**
     Puts a colored rectangle on this RenderTarget.  There is no outline or border (that's drawn
     separately with textLines).
     @param bottomLeft the XyOffset of the lower-left-hand corner
     @param xyDim width and height (dimensions) of rectangle
     @param c color
     @return the effective height after page breaking
     (may include some extra space above to push items onto the next page).
     */
    fun fillRect(bottomLeft: XyOffset, xyDim: XyDim, c: PDColor): Float
}