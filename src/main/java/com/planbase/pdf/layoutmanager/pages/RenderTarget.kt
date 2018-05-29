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
import com.planbase.pdf.layoutmanager.attributes.PageArea
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.contents.ScaledImage.WrappedImage
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 * Represents something to be drawn to.  For page-breaking, use the [PageGrouping]
 * implementation.  For a fixed, single page use the [SinglePage] implementation.
 */
interface RenderTarget {
    /** the offset and size of the body area. */
    val body: PageArea

    /**
     * Draws a line from (x1, y1) to (x2, y2).  Direction is important if using mitering.
     *
     * @param start the Coord of the starting point
     * @param end the Coord of the ending point
     * @param lineStyle the style to draw the line with
     * @param reallyRender Only render this if true.  Otherwise, just measure and return.
     * @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    fun drawLine(start: Coord, end: Coord, lineStyle: LineStyle, reallyRender: Boolean = true): IntRange =
        drawLineStrip(listOf(start, end), lineStyle, reallyRender)

    /**
     * Draws a line from (x1, y1) to (x2, y2).  Direction is important if using mitering.
     *
     * @param start the Coord of the starting point
     * @param end the Coord of the ending point
     * @param lineStyle the style to draw the line with
     * @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    fun drawLine(start: Coord, end: Coord, lineStyle: LineStyle): IntRange
            = drawLine(start, end, lineStyle, true)

    /**
     * Draws lines from the first point to the last using the given line style.  PDF has only one line style for
     * a line strip.
     *
     * @param points the list of Coord to draw lines between.  This does *not* connect the last point to the first.
     * If you want that, add the first point again at the end of the list.
     * @param lineStyle the style to draw the line with
     * @param reallyRender Only render this if true.  Otherwise, just measure and return.
     * @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    fun drawLineStrip(points: List<Coord>, lineStyle: LineStyle, reallyRender: Boolean = true): IntRange

    /**
     * Draws lines from the first point to the last.  Convenience function for [drawLineStrip(points, lineStyle, true)]
     */
    fun drawLineStrip(points: List<Coord>, lineStyle: LineStyle): IntRange =
            drawLineStrip(points, lineStyle, true)

    /**
     * Draws a closed path.
     *
     * @param points the list of Coord to draw lines between.  The last point is assumed to connect back to the first.
     * @param lineStyle the style to draw all lines with.  PDF only allows one line width and color per path.
     * @param reallyRender Only render this if true.  Otherwise, just measure and return.
     * @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    fun drawLineLoop(points: List<Coord>, lineStyle: LineStyle, reallyRender: Boolean): IntRange

    /** Draws a closed path.  Convenience function for [drawLineLoop(points, lineStyles, true)]. */
    fun drawLineLoop(points: List<Coord>, lineStyle: LineStyle): IntRange =
            drawLineLoop(points, lineStyle, true)

    /**
     * Puts styled text on this RenderTarget
     * @param baselineLeft the Coord of the left-hand baseline point.  Ascent goes above, descent and leading below.
     * @param text the text
     * @param textStyle the style
     * @param reallyRender Only render this if true.  Otherwise, just measure and return.
     * @return the effective height after page breaking
     * (may include some extra space above to push items onto the next page).
     */
    fun drawStyledText(baselineLeft: Coord,
                       text: String,
                       textStyle: TextStyle,
                       reallyRender: Boolean = true): HeightAndPage

    /**
     * Puts styled text on this RenderTarget
     * @param baselineLeft the Coord of the left-hand baseline point.  Ascent goes above, descent and leading below.
     * @param text the text
     * @param textStyle the style
     * @return the effective height after page breaking
     * (may include some extra space above to push items onto the next page).
     */
    fun drawStyledText(baselineLeft: Coord, text: String, textStyle: TextStyle): HeightAndPage =
            drawStyledText(baselineLeft, text, textStyle, true)

    /**
     * Puts an image on this RenderTarget
     * @param bottomLeft the Coord of the lower-left-hand corner
     * @param wi the scaled, "wrapped" jpeg/png image
     * @param reallyRender Only render this if true.  Otherwise, just measure and return.
     * @return the effective height after page breaking
     * (may include some extra space above to push items onto the next page).
     */
    fun drawImage(bottomLeft: Coord, wi: WrappedImage, reallyRender: Boolean = true): HeightAndPage

    /**
     * Puts an image on this RenderTarget
     * @param bottomLeft the Coord of the lower-left-hand corner
     * @param wi the scaled, "wrapped" jpeg/png image
     * @return the effective height after page breaking
     * (may include some extra space above to push items onto the next page).
     */
    @Suppress("unused")
    fun drawImage(bottomLeft: Coord, wi: WrappedImage): HeightAndPage = drawImage(bottomLeft, wi, true)

    /**
     * Puts a colored rectangle on this RenderTarget.  There is no outline or border (that's drawn
     * separately with textLines).
     * @param bottomLeft the Coord of the lower-left-hand corner
     * @param dim width and height (dim) of rectangle
     * @param c color
     * @param reallyRender Only render this if true.  Otherwise, just measure and return.
     * @return the effective height after page breaking
     * (may include some extra space above to push items onto the next page).
     */
    fun fillRect(bottomLeft: Coord, dim: Dim, c: PDColor, reallyRender: Boolean = true): Double

    /**
     * Puts a colored rectangle on this RenderTarget.  There is no outline or border (that's drawn
     * separately with textLines).
     * @param bottomLeft the Coord of the lower-left-hand corner
     * @param dim width and height (dim) of rectangle
     * @param c color
     * @return the effective height after page breaking
     * (may include some extra space above to push items onto the next page).
     */
    @Suppress("unused")
    fun fillRect(bottomLeft: Coord, dim: Dim, c: PDColor): Double = fillRect(bottomLeft, dim, c, true)

    /**
     * Returns the top margin necessary to push this item onto a new page if it won't fit on this one.
     * If it will fit, simply returns 0.
     * @param bottomY the un-adjusted (bottom) y value.
     * @param height the height
     * @param requiredSpaceBelow if there isn't this much space left at the bottom of the page, move chunk to the top
     * of the next page.
     */
    // TODO: I keep passing y, 0.0, 0.0 instead of y - height, height, 0.0, so maybe I should make it work that way instead!
    fun pageBreakingTopMargin(bottomY: Double, height: Double = 0.0, requiredSpaceBelow: Double = 0.0): Double

    /**
     * Returns the correct page for the given Y value but MAY ACTUALLY ADD THAT PAGE, so only call if really rendering
     */
    fun pageNumFor(y:Double):Int
}