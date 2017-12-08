package com.planbase.pdf.layoutmanager.utils

import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.junit.Assert.*
import org.junit.Test
import org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode

class DimTest {
    @Test fun testBasics() {
        val xyd1 = Dim(Float.MAX_VALUE, Float.MIN_VALUE)
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd1.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd1.height.toDouble(), 0.00000001)
        val xyd2 = Dim(PDRectangle(Float.MAX_VALUE, Float.MIN_VALUE))
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd2.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd2.height.toDouble(), 0.00000001)
        val xyd3 = Dim.ZERO.width(java.lang.Float.MAX_VALUE).height(java.lang.Float.MIN_VALUE)
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd3.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd3.height.toDouble(), 0.00000001)

        equalsDistinctHashCode<Any, Dim, Dim, Dim, Dim>(xyd1, xyd2, xyd3, Dim.ZERO)

        equalsDistinctHashCode<Any, Dim, Dim, Dim, Dim>(Dim(5f, 3f).swapWh(),
                                                        Dim(4f, 6f).minus(Dim(1f, 1f)),
                                                        Dim(2f, 4f).plus(Dim(1f, 1f)),
                                                        Dim(3.1f, 4.9f))

        assertEquals(Dim(7f, 11f), Dim(Dim(7f, 11f).toRect()))

        assertTrue(Dim(5f, 11f).lte(Dim(5f, 11f)))
        assertTrue(Dim(5f, 11f).lte(Dim(5.000001f, 11.000001f)))
        assertFalse(Dim(5f, 11f).lte(Dim(4.999999f, 11f)))
        assertFalse(Dim(5f, 11f).lte(Dim(5f, 10.999999f)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx1() {
        Dim(3.5f, -1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx2() {
        Dim(-3.5f, 1f)
    }

    @Test fun testSum() {
        // I'm picking decimal parts that are binary divisions: 1/2, 3/4, 1/8, 1/16
        // To avoid rounding errors.
        assertEquals(Dim(12.625f, 18.8125f),
                     Dim.sum(listOf(Dim(3.5f, 5.75f), Dim(9.125f, 13.0625f))))
    }
}