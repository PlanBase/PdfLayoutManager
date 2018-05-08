package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.junit.Assert.*
import org.junit.Test

class AlignTest {
    val startCoord = Coord(100.0, 200.0)
    val outerDim = Dim(17.0, 15.0)
    val innerDim = Dim(11.0, 13.0)
    @Test fun testInnerTopLeft() {
        assertEquals(Coord(100.0, 200.0), Align.TOP_LEFT.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(103.0, 200.0), Align.TOP_CENTER.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(106.0, 200.0), Align.TOP_RIGHT.innerTopLeft(outerDim, innerDim, startCoord))

        assertEquals(Coord(100.0, 199.0), Align.MIDDLE_LEFT.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(103.0, 199.0), Align.MIDDLE_CENTER.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(106.0, 199.0), Align.MIDDLE_RIGHT.innerTopLeft(outerDim, innerDim, startCoord))

        assertEquals(Coord(100.0, 198.0), Align.BOTTOM_LEFT.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(103.0, 198.0), Align.BOTTOM_CENTER.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(106.0, 198.0), Align.BOTTOM_RIGHT.innerTopLeft(outerDim, innerDim, startCoord))
    }
}