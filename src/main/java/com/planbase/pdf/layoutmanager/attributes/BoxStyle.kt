package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dimensions
import com.planbase.pdf.layoutmanager.utils.colorToString
import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 Represents the border, padding, background color, and maybe someday margin.  Every aspect of this is immutable
 because it can be specified when various items are created and doesn't need to change, even after line-wrapping.
 */
data class BoxStyle(val padding: Padding = Padding.NO_PADDING,
                    val bgColor: PDColor?,
                    val border: BorderStyle = BorderStyle.NO_BORDERS) {

    override fun toString() = "BoxStyle($padding, ${colorToString(bgColor)}, $border)"
    companion object {
        val NONE = BoxStyle(Padding.NO_PADDING, null, BorderStyle.NO_BORDERS)
    }

    fun interiorSpaceTop():Float = padding.top + (border.top.thickness / 2)

    fun interiorSpaceRight():Float = padding.right + (border.right.thickness / 2)

    fun interiorSpaceBottom():Float = padding.bottom + (border.bottom.thickness / 2)

    fun interiorSpaceLeft():Float = padding.left + (border.left.thickness / 2)

    fun applyTopLeft(xy: Coord) = Coord(xy.x + interiorSpaceLeft(), xy.y - interiorSpaceTop())

    /**
     The top and bottom padding plus half of the top and bottom border thickness.
     */
    fun topBottomInteriorSp():Float = interiorSpaceTop() + interiorSpaceBottom()

    /**
    The left and right padding plus half of the left and right border thickness.
     */
    fun leftRightInteriorSp():Float = interiorSpaceLeft() + interiorSpaceRight()

    fun subtractFrom(dim: Dimensions) = Dimensions(dim.width - leftRightInteriorSp(), dim.height - topBottomInteriorSp())
}

