package com.planbase.pdf.layoutmanager

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode
import org.organicdesign.testUtils.EqualsContract.equalsSameHashCode

class PaddingTest {
    @Test
    fun staticFactoryTest() {
        assertTrue(Padding.NO_PADDING === Padding(0f))
        assertTrue(Padding.NO_PADDING === Padding(0f, 0f, 0f, 0f))
        assertTrue(Padding.DEFAULT_TEXT_PADDING === Padding(1.5f, 1.5f, 2f, 1.5f))

        val (top, right, bottom, left) = Padding(2f)
        assertEquals(2.0f, top, 0.0f)
        assertEquals(2.0f, right, 0.0f)
        assertEquals(2.0f, bottom, 0.0f)
        assertEquals(2.0f, left, 0.0f)

        val (top1, right1, bottom1, left1) = Padding(3f, 5f, 7f, 11f)
        assertEquals(3.0f, top1, 0.0f)
        assertEquals(5.0f, right1, 0.0f)
        assertEquals(7.0f, bottom1, 0.0f)
        assertEquals(11.0f, left1, 0.0f)
    }

    @Test
    fun equalHashTest() {
        // Test first item different
        equalsDistinctHashCode<Any, Padding, Padding, Padding, Padding>(Padding(1f), Padding(1f, 1f, 1f, 1f), Padding(1f),
                                                                        Padding(2f, 1f, 1f, 1f))

        // Test transposed middle items are different (but have same hashcode)
        equalsSameHashCode<Any, Padding, Padding, Padding, Padding>(Padding(3f, 5f, 7f, 1.1f), Padding(3f, 5f, 7f, 1.1f),
                                                                    Padding(3f, 5f, 7f, 1.1f),
                                                                    Padding(3f, 7f, 5f, 1.1f))

        // Padding values that differ by less than 0.1f have the same hashcode
        // but are not equal.  Prove it (also tests last item is different):
        equalsSameHashCode<Any, Padding, Padding, Padding, Padding>(Padding(1f), Padding(1f, 1f, 1f, 1f), Padding(1f),
                                                                    Padding(1f, 1f, 1f, 1.0001f))
    }
}