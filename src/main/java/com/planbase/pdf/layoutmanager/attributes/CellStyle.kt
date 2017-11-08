package com.planbase.pdf.layoutmanager.attributes

data class CellStyle(val align: Align,
                     val boxStyle: BoxStyle) {

    fun align(a: Align) = CellStyle(a, boxStyle)

    override fun toString() = "CellStyle($align, $boxStyle)"

    companion object {
        val Default = CellStyle(Align.TOP_LEFT, BoxStyle.NONE)
    }
}
