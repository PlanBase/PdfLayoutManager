package com.planbase.pdf.layoutmanager.utils

import org.junit.Assert.*
import org.junit.Test

class CoordTest {
    @Test fun testDimensionTo() {
        assertEquals(Dimensions(11f, 13f), Coord(0f, 0f).dimensionTo(Coord(11f, 13f)))
        assertEquals(Dimensions(11f, 13f), Coord(0f, 0f).dimensionTo(Coord(-11f, -13f)))
        assertEquals(Dimensions(11f, 13f), Coord(11f, 13f).dimensionTo(Coord(0f, 0f)))
        assertEquals(Dimensions(11f, 13f), Coord(-11f, -13f).dimensionTo(Coord(0f, 0f)))
    }
}