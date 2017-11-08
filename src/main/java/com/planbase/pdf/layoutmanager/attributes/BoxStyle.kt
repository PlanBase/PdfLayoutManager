package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.Utils.Companion.colorToString
import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 Represents the border, padding, background color, and maybe someday margin.  Every aspect of this is immutable
 because it can be specified when various items are created and doesn't need to change, even after line-wrapping.
 */
data class BoxStyle(val padding: Padding?,
                    val bgColor: PDColor?,
                    val border: BorderStyle?) {

    override fun toString() = "BoxStyle($padding, ${colorToString(bgColor)}, $border)"
    companion object {
        val NONE = BoxStyle(null, null, null)
    }
}

