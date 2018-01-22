package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.attributes.BorderStyle.Companion.NO_BORDERS
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import org.junit.Assert.*
import org.junit.Test

class BorderStyleTest {
    @Test
    fun testToString() {
        assertEquals("NO_BORDERS", NO_BORDERS.toString())
        assertEquals("BorderStyle(LineStyle(CMYK_BLACK, 7.25f))",
                     BorderStyle(LineStyle(CMYK_BLACK, 7.25f)).toString())
        assertEquals("BorderStyle(LineStyle(CMYK_BLACK, 3f)," +
                     " LineStyle(CMYK_BLACK, 5.25f)," +
                     " LineStyle(CMYK_BLACK, 7.5f)," +
                     " LineStyle(CMYK_BLACK, 11.75f))",
                     BorderStyle(LineStyle(CMYK_BLACK, 3f),
                                 LineStyle(CMYK_BLACK, 5.25f),
                                 LineStyle(CMYK_BLACK, 7.5f),
                                 LineStyle(CMYK_BLACK, 11.75f)).toString())
    }
}