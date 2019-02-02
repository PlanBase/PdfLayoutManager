package com.planbase.pdf.lm2.attributes

import com.planbase.pdf.lm2.attributes.Padding.Companion.DEFAULT_TEXT_PADDING
import com.planbase.pdf.lm2.attributes.Padding.Companion.NO_PADDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode
import kotlin.test.Test

class PaddingTest {
    @Test
    fun staticFactoryTest() {
        assertTrue(NO_PADDING == Padding(0.0))
        assertTrue(NO_PADDING == Padding(0.0, 0.0, 0.0, 0.0))
        assertTrue(DEFAULT_TEXT_PADDING == Padding(1.5, 1.5, 2.0, 1.5))

        val (top, right, bottom, left) = Padding(2.0)
        assertEquals(2.0, top, 0.0)
        assertEquals(2.0, right, 0.0)
        assertEquals(2.0, bottom, 0.0)
        assertEquals(2.0, left, 0.0)

        val (top1, right1, bottom1, left1) = Padding(3.0, 5.0, 7.0, 11.0)
        assertEquals(3.0, top1, 0.0)
        assertEquals(5.0, right1, 0.0)
        assertEquals(7.0, bottom1, 0.0)
        assertEquals(11.0, left1, 0.0)
    }

    @Test
    fun equalHashTest() {
        // Test first item different
        equalsDistinctHashCode(Padding(1.0), Padding(1.0, 1.0), Padding(1.0),
                               Padding(2.0, 1.0, 1.0, 1.0))

        // Test transposed middle items are different (but have same hashcode)
        equalsDistinctHashCode(Padding(3.0, 5.0, 7.0, 1.1), Padding(3.0, 5.0, 7.0, 1.1),
                               Padding(3.0, 5.0, 7.0, 1.1),
                               Padding(3.0, 7.0, 5.0, 1.1))

        // Padding values that differ by less than 0.1 have the same hashcode
        // but are not equal.  Prove it (also tests last item is different):
        equalsDistinctHashCode(Padding(1.0), Padding(1.0, 1.0, 1.0, 1.0), Padding(1.0),
                               Padding(1.0, 1.0, 1.0, 1.0001))
    }

    @Test fun withModifiersTest() {
        val pad = NO_PADDING
        assertEquals(Padding(0.0, 0.0, 0.0, 0.0), pad)
        assertEquals(Padding(1.0, 0.0, 0.0, 0.0), pad.withTop(1.0))
        assertEquals(Padding(0.0, 3.0, 0.0, 0.0), pad.withRight(3.0))
        assertEquals(Padding(0.0, 0.0, 5.0, 0.0), pad.withBottom(5.0))
        assertEquals(Padding(0.0, 0.0, 0.0, 7.0), pad.withLeft(7.0))

        assertEquals(Padding(7.0, 5.0, 3.0, 1.0),
                     pad.withTop(7.0)
                             .withRight(5.0)
                             .withBottom(3.0)
                             .withLeft(1.0))
    }
}