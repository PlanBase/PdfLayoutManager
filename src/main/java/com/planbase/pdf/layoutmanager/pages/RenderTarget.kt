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
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.Coord
import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 * Represents something to be drawn to.  For page-breaking, use the [PageGrouping]
 * implementation.  For a fixed, single page use the [SinglePage] implementation.
 */
interface RenderTarget {
    /**
     Draws a line from (x1, y1) to (x2, y2).  Direction is important if using mitering.

     @param start the Coord of the starting point
     @param end the Coord of the ending point
     @param lineStyle the style to draw the line with
     @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    fun drawLine(start: Coord, end: Coord, lineStyle: LineStyle, reallyRender: Boolean): RenderTarget {
        drawLineStrip(listOf(start, end), lineStyle, reallyRender)
        return this
    }

    /**
    Draws lines from the first point to the last.  Direction is important if using mitering.

    @param points the list of Coord to draw lines between.  This does *not* connect the last point to the first.
    If you want that, add the first point again at the end of the list.
    @param lineStyle the style to draw the line with
    @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    fun drawLineStrip(points: List<Coord>, lineStyle: LineStyle, reallyRender: Boolean): RenderTarget

    /**
     Puts styled text on this RenderTarget
     @param baselineLeft the Coord of the left-hand baseline point.  Ascent goes above, descent and leading below.
     @param text the text
     @param textStyle the style
     @return the effective height after page breaking
     (may include some extra space above to push items onto the next page).
     */
    fun drawStyledText(baselineLeft: Coord, text: String, textStyle: TextStyle, reallyRender: Boolean): Float

    /**
     Puts an image on this RenderTarget
     @param bottomLeft the Coord of the lower-left-hand corner
     @param wi the scaled, "wrapped" jpeg/png image
     @return the effective height after page breaking
     (may include some extra space above to push items onto the next page).
     */
    fun drawImage(bottomLeft: Coord, wi: WrappedImage, reallyRender: Boolean): Float

    /**
     Puts a colored rectangle on this RenderTarget.  There is no outline or border (that's drawn
     separately with textLines).
     @param bottomLeft the Coord of the lower-left-hand corner
     @param dimensions width and height (dimensions) of rectangle
     @param c color
     @return the effective height after page breaking
     (may include some extra space above to push items onto the next page).
     */
    fun fillRect(bottomLeft: Coord, dimensions: Dimensions, c: PDColor, reallyRender: Boolean): Float

    /**
    Returns the top margin necessary to push this item onto a new page if it won't fit on this one.
    If it will fit, simply returns 0.
     */
    fun pageBreakingTopMargin(bottomY:Float, height:Float):Float
}