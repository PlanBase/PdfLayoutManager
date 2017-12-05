package com.planbase.pdf.layoutmanager.utils

import org.junit.Assert.*
import org.junit.Test

class Point2dTest {
    @Test fun testDimensionTo() {
        assertEquals(Dimensions(11f, 13f), Point2d(0f,0f).dimensionTo(Point2d(11f, 13f)))
        assertEquals(Dimensions(11f, 13f), Point2d(0f,0f).dimensionTo(Point2d(-11f, -13f)))
        assertEquals(Dimensions(11f, 13f), Point2d(11f, 13f).dimensionTo(Point2d(0f,0f)))
        assertEquals(Dimensions(11f, 13f), Point2d(-11f, -13f).dimensionTo(Point2d(0f,0f)))
    }
}