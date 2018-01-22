package com.planbase.pdf.layoutmanager.utils

import org.junit.Assert.*
import kotlin.test.Test

class CoordTest {
    @Test fun testDimensionTo() {
        assertEquals(Dim(11f, 13f), Coord(0f, 0f).dimensionTo(Coord(11f, 13f)))
        assertEquals(Dim(11f, 13f), Coord(0f, 0f).dimensionTo(Coord(-11f, -13f)))
        assertEquals(Dim(11f, 13f), Coord(11f, 13f).dimensionTo(Coord(0f, 0f)))
        assertEquals(Dim(11f, 13f), Coord(-11f, -13f).dimensionTo(Coord(0f, 0f)))
    }

    @Test fun testToString() {
        assertEquals("Coord(1f, 3.5f)", Coord(1f, 3.5f).toString())
    }
}