package com.planbase.pdf.layoutmanager

import org.junit.Test

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SingleItemRenderatorTest {
//    @Test fun testSingleItemRenderator() {
//        val r = object : Arrangeable {
//            override fun calcDimensions(maxWidth: Float): XyDim {
//                return XyDim(5f, 7f)
//            }
//
//            override fun render(lp: RenderTarget, outerTopLeft: XyOffset, outerDimensions: XyDim): XyOffset? {
//                return null
//            }
//
//            override fun arranger(): Arranger {
//                return Arranger.SingleItemRenderator(this)
//            }
//        }
//
//        var tor = r.arranger()
//        assertTrue(tor.hasMore())
//        assertEquals(r.calcDimensions(0f), tor.getSomething(9f).item.xyDim())
//        assertFalse(tor.hasMore())
//
//        tor = r.arranger()
//        assertTrue(tor.hasMore())
//        assertTrue(tor.getIfFits(3f) is None)
//        assertTrue(tor.hasMore())
//        assertEquals(r.calcDimensions(0f), tor.getIfFits(9f).match({ c -> c }, { t -> t }, null).xyDim())
//        assertFalse(tor.hasMore())
//    }
}