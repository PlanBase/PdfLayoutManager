package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.Utils.Companion.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.CMYK_WHITE
import org.junit.Assert.*
import org.junit.Test

class BoxStyleTest {
    @Test fun testBasics() {
        val bs1 = BoxStyle(Padding(1f, 3f, 5f, 7f),
                          CMYK_BLACK,
                          BorderStyle(LineStyle(CMYK_WHITE, 11f),
                                      LineStyle(CMYK_WHITE, 13f),
                                      LineStyle(CMYK_WHITE, 17f),
                                      LineStyle(CMYK_WHITE, 19f)))

        assertEquals(42f, bs1.leftRightThickness())
        assertEquals(34f, bs1.topBottomThickness())

        val bs2 = BoxStyle(Padding.NO_PADDING,
                           CMYK_BLACK,
                           BorderStyle(LineStyle(CMYK_WHITE, 11f),
                                       LineStyle(CMYK_WHITE, 13f),
                                       LineStyle(CMYK_WHITE, 17f),
                                       LineStyle(CMYK_WHITE, 19f)))

        assertEquals(32f, bs2.leftRightThickness())
        assertEquals(28f, bs2.topBottomThickness())

        val bs3 = BoxStyle(Padding(1f, 3f, 5f, 7f),
                           CMYK_BLACK,
                           BorderStyle.NO_BORDERS)

        assertEquals(10f, bs3.leftRightThickness())
        assertEquals(6f, bs3.topBottomThickness())
    }
}