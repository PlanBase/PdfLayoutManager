package com.planbase.pdf.layoutmanager.attributes

data class CellStyle(val boxStyle: BoxStyle,
                     val align: Align) {

    fun align(a: Align) = CellStyle(boxStyle, a)

    companion object {
        val Default = CellStyle(BoxStyle.NONE, Align.TOP_LEFT)
    }
}
