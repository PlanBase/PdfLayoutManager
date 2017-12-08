package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.cmykBlack
import com.planbase.pdf.layoutmanager.utils.cmykWhite
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.junit.Assert.*
import org.junit.Test

class BoxStyleTest {
    @Test fun testBasics() {
        val bs1 = BoxStyle(Padding(1f, 3f, 5f, 7f),
                           cmykBlack,
                           BorderStyle(LineStyle(cmykWhite, 11f),
                                       LineStyle(cmykWhite, 13f),
                                       LineStyle(cmykWhite, 17f),
                                       LineStyle(cmykWhite, 19f)))

        assertEquals(6.5f, bs1.interiorSpaceTop())
        assertEquals(9.5f, bs1.interiorSpaceRight())
        assertEquals(13.5f, bs1.interiorSpaceBottom())
        assertEquals(16.5f, bs1.interiorSpaceLeft())

        assertEquals(20f, bs1.topBottomInteriorSp())
        assertEquals(26f, bs1.leftRightInteriorSp())

        assertEquals(Coord(116.5f, 193.5f), bs1.applyTopLeft(Coord(100f, 200f)))

        assertEquals(Dim(74f, 180f), bs1.subtractFrom(Dim(100f, 200f)))

        val bs2 = BoxStyle(Padding.NO_PADDING,
                           cmykBlack,
                           BorderStyle(LineStyle(cmykWhite, 11f),
                                       LineStyle(cmykWhite, 13f),
                                       LineStyle(cmykWhite, 17f),
                                       LineStyle(cmykWhite, 19f)))

        assertEquals(14f, bs2.topBottomInteriorSp())
        assertEquals(16f, bs2.leftRightInteriorSp())

        val bs3 = BoxStyle(Padding(1f, 3f, 5f, 7f),
                           cmykBlack,
                           BorderStyle.NO_BORDERS)

        assertEquals(6f, bs3.topBottomInteriorSp())
        assertEquals(10f, bs3.leftRightInteriorSp())
    }
}