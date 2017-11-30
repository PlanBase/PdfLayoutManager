package com.planbase.pdf.layoutmanager.utils

import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.junit.Assert.*
import org.junit.Test
import org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode

class DimensionsTest {
    @Test fun testBasics() {
        val xyd1 = Dimensions(Float.MAX_VALUE, Float.MIN_VALUE)
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd1.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd1.height.toDouble(), 0.00000001)
        val xyd2 = Dimensions(PDRectangle(Float.MAX_VALUE, Float.MIN_VALUE))
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd2.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd2.height.toDouble(), 0.00000001)
        val xyd3 = Dimensions.ZERO.width(java.lang.Float.MAX_VALUE).height(java.lang.Float.MIN_VALUE)
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd3.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd3.height.toDouble(), 0.00000001)

        equalsDistinctHashCode<Any, Dimensions, Dimensions, Dimensions, Dimensions>(xyd1, xyd2, xyd3, Dimensions.ZERO)

        equalsDistinctHashCode<Any, Dimensions, Dimensions, Dimensions, Dimensions>(Dimensions(5f, 3f).swapWh(),
                                                                                    Dimensions(4f, 6f).minus(Dimensions(1f, 1f)),
                                                                                    Dimensions(2f, 4f).plus(Dimensions(1f, 1f)),
                                                                                    Dimensions(3.1f, 4.9f))

        assertEquals(Dimensions(7f, 11f), Dimensions(Dimensions(7f, 11f).toRect()))

        assertTrue(Dimensions(5f, 11f).lte(Dimensions(5f, 11f)))
        assertTrue(Dimensions(5f, 11f).lte(Dimensions(5.000001f, 11.000001f)))
        assertFalse(Dimensions(5f, 11f).lte(Dimensions(4.999999f, 11f)))
        assertFalse(Dimensions(5f, 11f).lte(Dimensions(5f, 10.999999f)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx1() {
        Dimensions(3.5f, -1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx2() {
        Dimensions(-3.5f, 1f)
    }

    @Test fun testSum() {
        // I'm picking decimal parts that are binary divisions: 1/2, 3/4, 1/8, 1/16
        // To avoid rounding errors.
        assertEquals(Dimensions(12.625f, 18.8125f),
                     Dimensions.sum(listOf(Dimensions(3.5f, 5.75f), Dimensions(9.125f, 13.0625f))))
    }
}