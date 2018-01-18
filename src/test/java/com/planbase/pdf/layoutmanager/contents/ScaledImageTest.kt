package com.planbase.pdf.layoutmanager.contents

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class ScaledImageTest {
    @Test fun testConstruction() {
        val f = File("target/test-classes/graph2.png")
//    println(f.absolutePath)
        val graphPic = ImageIO.read(f)
        val so = ScaledImage(graphPic)

        // Test that our test data hasn't changed
        assertEquals(606, graphPic.width)
        assertEquals(296, graphPic.height)

        // Test that our code hasn't changed
        assertEquals(145.44f, so.dim.width)
        assertEquals(71.04f, so.dim.height)

        val wo = so.wrap()
        assertEquals(145.44f, wo.dim.width)
        assertEquals(71.04f, wo.dim.height)
    }
}