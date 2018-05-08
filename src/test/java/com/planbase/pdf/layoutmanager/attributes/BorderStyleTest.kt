package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.attributes.BorderStyle.Companion.NO_BORDERS
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import org.junit.Assert.*
import org.junit.Test

class BorderStyleTest {
    @Test
    fun testToString() {
        assertEquals("NO_BORDERS", NO_BORDERS.toString())
        assertEquals("BorderStyle(LineStyle(CMYK_BLACK, 7.25))",
                     BorderStyle(LineStyle(CMYK_BLACK, 7.25)).toString())
        assertEquals("BorderStyle(LineStyle(CMYK_BLACK, 3.0)," +
                     " LineStyle(CMYK_BLACK, 5.25)," +
                     " LineStyle(CMYK_BLACK, 7.5)," +
                     " LineStyle(CMYK_BLACK, 11.75))",
                     BorderStyle(LineStyle(CMYK_BLACK, 3.0),
                                 LineStyle(CMYK_BLACK, 5.25),
                                 LineStyle(CMYK_BLACK, 7.5),
                                 LineStyle(CMYK_BLACK, 11.75)).toString())
    }
}