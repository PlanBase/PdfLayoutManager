package com.planbase.pdf.layoutmanager.attributes

/**
 * Holds the style information for a cell.
 * @param align the horizontal and vertical alignment
 * @param boxStyle the padding, background-color, and border
 * @param name this is an optional field that only affects the toString() representation, giving the style
 * a name and suppressing the more detailed information (for briefer debugging)
 */
data class CellStyle
@JvmOverloads constructor(val align: Align,
                          val boxStyle: BoxStyle,
                          val name: String? = null
) {

    fun align(a: Align) = CellStyle(a, boxStyle)

    override fun toString() = name ?: "CellStyle($align, $boxStyle)"

    companion object {
        @JvmField
        val TOP_LEFT_BORDERLESS = CellStyle(Align.TOP_LEFT, BoxStyle.NO_PAD_NO_BORDER, "TOP_LEFT_BORDERLESS")
    }
}
