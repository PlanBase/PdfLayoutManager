package com.planbase.pdf.layoutmanager

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 Represents the border, padding, background color, and maybe someday margin.  Every aspect of this is immutable
 because it can be specified when various items are created and doesn't need to change, even after line-wrapping.
 */
data class BoxStyle(val padding: Padding?,
                    val bgColor: PDColor?,
                    val border: BorderStyle?) {
    companion object {
        val NONE = BoxStyle(null,
                            null,
                            null)
    }
}

