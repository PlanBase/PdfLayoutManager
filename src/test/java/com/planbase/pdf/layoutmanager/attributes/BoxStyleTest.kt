package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.CMYK_WHITE
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.junit.Assert.*
import org.junit.Test

class BoxStyleTest {
    @Test fun testBasics() {
        val bs1 = BoxStyle(Padding(1.0, 3.0, 5.0, 7.0),
                           CMYK_BLACK,
                           BorderStyle(LineStyle(CMYK_WHITE, 11.0),
                                       LineStyle(CMYK_WHITE, 13.0),
                                       LineStyle(CMYK_WHITE, 17.0),
                                       LineStyle(CMYK_WHITE, 19.0)))

        assertEquals(6.5, bs1.interiorSpaceTop(), 0.0)
        assertEquals(9.5, bs1.interiorSpaceRight(), 0.0)
        assertEquals(13.5, bs1.interiorSpaceBottom(), 0.0)
        assertEquals(16.5, bs1.interiorSpaceLeft(), 0.0)

        assertEquals(20.0, bs1.topBottomInteriorSp(), 0.0)
        assertEquals(26.0, bs1.leftRightInteriorSp(), 0.0)

        assertEquals(Coord(116.5, 193.5), bs1.applyTopLeft(Coord(100.0, 200.0)))

        assertEquals(Dim(74.0, 180.0), bs1.subtractFrom(Dim(100.0, 200.0)))

        val bs2 = BoxStyle(Padding.NO_PADDING,
                           CMYK_BLACK,
                           BorderStyle(LineStyle(CMYK_WHITE, 11.0),
                                       LineStyle(CMYK_WHITE, 13.0),
                                       LineStyle(CMYK_WHITE, 17.0),
                                       LineStyle(CMYK_WHITE, 19.0)))

        assertEquals(14.0, bs2.topBottomInteriorSp(), 0.0)
        assertEquals(16.0, bs2.leftRightInteriorSp(), 0.0)

        val bs3 = BoxStyle(Padding(1.0, 3.0, 5.0, 7.0),
                           CMYK_BLACK,
                           BorderStyle.NO_BORDERS)

        assertEquals(6.0, bs3.topBottomInteriorSp(), 0.0)
        assertEquals(10.0, bs3.leftRightInteriorSp(), 0.0)
    }
}