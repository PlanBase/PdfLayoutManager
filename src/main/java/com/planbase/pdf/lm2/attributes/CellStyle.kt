package com.planbase.pdf.lm2.attributes

/**
 * Holds the immutable style information for a cell.
 * @param align the horizontal and vertical alignment
 * @param boxStyle the padding, background-color, and border
 * @param debuggingName this is an optional field that only affects the toString() representation, giving the style
 * a name and suppressing the more detailed information (for briefer debugging).
 */
data class CellStyle
@JvmOverloads constructor(
        val align: Align,
        val boxStyle: BoxStyle,
        val debuggingName: String? = null) {

    /**
     * Returns a new immutable CellStyle with the given Alignment and the current boxStyle (but not debuggingName).
     */
    fun withAlign(a: Align) = CellStyle(a, boxStyle)

    override fun toString() = debuggingName ?: "CellStyle($align, $boxStyle)"

    companion object {
        /**
         * A CellStyle with [Align.TOP_LEFT], [BoxStyle.NO_PAD_NO_BORDER], and debuggingName="TOP_LEFT_BORDERLESS".
         */
        @JvmField
        val TOP_LEFT_BORDERLESS = CellStyle(Align.TOP_LEFT, BoxStyle.NO_PAD_NO_BORDER, "TOP_LEFT_BORDERLESS")
    }
}
