package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.colorToString
import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 * Represents the border, padding, background color, and maybe someday margin.  Every aspect of this is immutable
 * because it can be specified when various items are created and doesn't need to change, even after line-wrapping.
 */
data class BoxStyle(val padding: Padding = Padding.NO_PADDING,
                    val bgColor: PDColor?,
                    val border: BorderStyle = BorderStyle.NO_BORDERS) {

    fun interiorSpaceTop(): Double = padding.top + (border.top.thickness / 2.0)

    fun interiorSpaceRight(): Double = padding.right + (border.right.thickness / 2.0)

    fun interiorSpaceBottom(): Double = padding.bottom + (border.bottom.thickness / 2.0)

    fun interiorSpaceLeft(): Double = padding.left + (border.left.thickness / 2.0)

    fun applyTopLeft(xy: Coord) = Coord(xy.x + interiorSpaceLeft(), xy.y - interiorSpaceTop())

    /**
     The top and bottom padding plus half of the top and bottom border thickness.
     */
    fun topBottomInteriorSp(): Double = interiorSpaceTop() + interiorSpaceBottom()

    /**
    The left and right padding plus half of the left and right border thickness.
     */
    fun leftRightInteriorSp(): Double = interiorSpaceLeft() + interiorSpaceRight()

    fun subtractFrom(dim: Dim) = Dim(dim.width - leftRightInteriorSp(), dim.height - topBottomInteriorSp())

    override fun toString() =
            if (this == NO_PAD_NO_BORDER) {
                "NO_PAD_NO_BORDER"
            } else {
                "BoxStyle($padding, ${colorToString(bgColor)}, $border)"
            }

    companion object {
        /**
         * A BoxStyle with [Padding.NO_PADDING], no background color and [BorderStyle.NO_BORDERS]
         */
        @JvmField
        val NO_PAD_NO_BORDER = BoxStyle(Padding.NO_PADDING, null, BorderStyle.NO_BORDERS)
    }
}

