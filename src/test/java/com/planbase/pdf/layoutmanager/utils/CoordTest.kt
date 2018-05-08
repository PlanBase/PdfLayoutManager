package com.planbase.pdf.layoutmanager.utils

import org.junit.Assert.*
import kotlin.test.Test

class CoordTest {
    @Test fun testDimensionTo() {
        assertEquals(Dim(11.0, 13.0), Coord(0.0, 0.0).dimensionTo(Coord(11.0, 13.0)))
        assertEquals(Dim(11.0, 13.0), Coord(0.0, 0.0).dimensionTo(Coord(-11.0, -13.0)))
        assertEquals(Dim(11.0, 13.0), Coord(11.0, 13.0).dimensionTo(Coord(0.0, 0.0)))
        assertEquals(Dim(11.0, 13.0), Coord(-11.0, -13.0).dimensionTo(Coord(0.0, 0.0)))
    }

    @Test fun testToString() {
        assertEquals("Coord(1.0, 3.5)", Coord(1.0, 3.5).toString())
    }
}