package com.planbase.pdf.lm2.attributes

import com.planbase.pdf.lm2.attributes.LineStyle.Companion.NO_LINE
import com.planbase.pdf.lm2.utils.CMYK_BLACK
import org.junit.Assert.*
import kotlin.test.Test

class LineStyleTest {
    @Test fun testToString() {
        assertEquals("NO_LINE", NO_LINE.toString())
        assertEquals("LineStyle(CMYK_BLACK, 7.25)", LineStyle(CMYK_BLACK, 7.25).toString())
    }
}