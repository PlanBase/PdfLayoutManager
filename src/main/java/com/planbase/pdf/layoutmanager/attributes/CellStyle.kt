package com.planbase.pdf.layoutmanager.attributes

data class CellStyle(val align: Align,
                     val boxStyle: BoxStyle) {

    fun align(a: Align) = CellStyle(a, boxStyle)

    override fun toString() = "CellStyle($align, $boxStyle)"

    companion object {
        @JvmField
        val TOP_LEFT_BORDERLESS = CellStyle(Align.TOP_LEFT, BoxStyle.NO_PAD_NO_BORDER)
    }
}
