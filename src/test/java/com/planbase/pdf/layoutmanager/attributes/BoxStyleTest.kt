package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.CMYK_WHITE
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
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

        assertEquals(6.5f, bs1.interiorSpaceTop())
        assertEquals(9.5f, bs1.interiorSpaceRight())
        assertEquals(13.5f, bs1.interiorSpaceBottom())
        assertEquals(16.5f, bs1.interiorSpaceLeft())

        assertEquals(20f, bs1.topBottomInteriorSp())
        assertEquals(26f, bs1.leftRightInteriorSp())

        assertEquals(Coord(116.5f, 193.5f), bs1.applyTopLeft(Coord(100f, 200f)))

        assertEquals(Dim(74f, 180f), bs1.subtractFrom(Dim(100f, 200f)))

        val bs2 = BoxStyle(Padding.NO_PADDING,
                           CMYK_BLACK,
                           BorderStyle(LineStyle(CMYK_WHITE, 11f),
                                       LineStyle(CMYK_WHITE, 13f),
                                       LineStyle(CMYK_WHITE, 17f),
                                       LineStyle(CMYK_WHITE, 19f)))

        assertEquals(14f, bs2.topBottomInteriorSp())
        assertEquals(16f, bs2.leftRightInteriorSp())

        val bs3 = BoxStyle(Padding(1f, 3f, 5f, 7f),
                           CMYK_BLACK,
                           BorderStyle.NO_BORDERS)

        assertEquals(6f, bs3.topBottomInteriorSp())
        assertEquals(10f, bs3.leftRightInteriorSp())
    }
}