package com.planbase.pdf.layoutmanager.utils

import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.junit.Assert.*
import org.junit.Test
import org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode

class XyDimTest {
    @Test fun testBasics() {
        val xyd1 = XyDim(Float.MAX_VALUE, Float.MIN_VALUE)
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd1.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd1.height.toDouble(), 0.00000001)
        val xyd2 = XyDim(PDRectangle(Float.MAX_VALUE, Float.MIN_VALUE))
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd2.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd2.height.toDouble(), 0.00000001)
        val xyd3 = XyDim.ZERO.width(java.lang.Float.MAX_VALUE).height(java.lang.Float.MIN_VALUE)
        assertEquals(java.lang.Float.MAX_VALUE.toDouble(), xyd3.width.toDouble(), 0.00000001)
        assertEquals(java.lang.Float.MIN_VALUE.toDouble(), xyd3.height.toDouble(), 0.00000001)

        equalsDistinctHashCode<Any, XyDim, XyDim, XyDim, XyDim>(xyd1, xyd2, xyd3, XyDim.ZERO)

        equalsDistinctHashCode<Any, XyDim, XyDim, XyDim, XyDim>(XyDim(5f, 3f).swapWh(),
                                                                XyDim(4f, 6f).minus(XyDim(1f, 1f)),
                                                                XyDim(2f, 4f).plus(XyDim(1f, 1f)),
                                                                XyDim(3.1f, 4.9f))

        assertEquals(XyDim(7f, 11f), XyDim(XyDim(7f, 11f).toRect()))

        assertTrue(XyDim(5f, 11f).lte(XyDim(5f, 11f)))
        assertTrue(XyDim(5f, 11f).lte(XyDim(5.000001f, 11.000001f)))
        assertFalse(XyDim(5f, 11f).lte(XyDim(4.999999f, 11f)))
        assertFalse(XyDim(5f, 11f).lte(XyDim(5f, 10.999999f)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx1() {
        XyDim(3.5f, -1f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEx2() {
        XyDim(-3.5f, 1f)
    }
}