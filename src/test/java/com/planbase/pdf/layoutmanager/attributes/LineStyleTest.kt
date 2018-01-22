package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.attributes.LineStyle.Companion.NO_LINE
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import org.junit.Assert.*
import kotlin.test.Test

class LineStyleTest {
    @Test fun testToString() {
        assertEquals("NO_LINE", NO_LINE.toString())
        assertEquals("LineStyle(CMYK_BLACK, 7.25f)", LineStyle(CMYK_BLACK, 7.25f).toString())
    }
}