package com.planbase.pdf.lm2.utils

import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.junit.Assert.*
import org.junit.Test
import org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode

class DimTest {
    @Test fun testBasics() {
        val max = Float.MAX_VALUE.toDouble()
        val min = Float.MIN_VALUE.toDouble()
        val xyd1 = Dim(max, min)
        assertEquals(max, xyd1.width, 0.00000001)
        assertEquals(min, xyd1.height, 0.00000001)
        val xyd2 = Dim(PDRectangle(Float.MAX_VALUE, Float.MIN_VALUE))
        assertEquals(Float.MAX_VALUE, xyd2.width.toFloat(), 0.000001f)
        assertEquals(Float.MIN_VALUE, xyd2.height.toFloat(), 0.000001f)
        val xyd3 = Dim.ZERO.withWidth(max).withHeight(min)
        assertEquals(max, xyd3.width, 0.00000001)
        assertEquals(min, xyd3.height, 0.00000001)

        equalsDistinctHashCode<Any, Dim, Dim, Dim, Dim>(xyd1, xyd2, xyd3, Dim.ZERO)

        equalsDistinctHashCode<Any, Dim, Dim, Dim, Dim>(Dim(5.0, 3.0).swapWh(),
                                                        Dim(4.0, 6.0).minus(Dim(1.0, 1.0)),
                                                        Dim(2.0, 4.0).plus(Dim(1.0, 1.0)),
                                                        Dim(3.1, 4.9))

        assertEquals(Dim(7.0, 11.0), Dim(Dim(7.0, 11.0).toRect()))

        assertTrue(Dim(5.0, 11.0).lte(Dim(5.0, 11.0)))
        assertTrue(Dim(5.0, 11.0).lte(Dim(5.000001, 11.000001)))
        assertFalse(Dim(5.0, 11.0).lte(Dim(4.999999, 11.0)))
        assertFalse(Dim(5.0, 11.0).lte(Dim(5.0, 10.999999)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx1() {
        Dim(3.5, -1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx2() {
        Dim(-3.5, 1.0)
    }

    @Test fun testSum() {
        // I'm picking decimal parts that are binary divisions: 1/2, 3/4, 1/8, 1/16
        // To avoid rounding errors.
        assertEquals(Dim(12.625, 18.8125),
                     Dim.sum(listOf(Dim(3.5, 5.75), Dim(9.125, 13.0625))))
    }
}