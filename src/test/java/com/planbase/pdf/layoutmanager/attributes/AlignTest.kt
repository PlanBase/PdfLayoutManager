package com.planbase.pdf.layoutmanager.attributes

import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import org.junit.Assert.*
import org.junit.Test

class AlignTest {
    val startCoord = Coord(100f, 200f)
    val outerDim = Dim(17f, 15f)
    val innerDim = Dim(11f, 13f)
    @Test fun testInnerTopLeft() {
        assertEquals(Coord(100f, 200f), Align.TOP_LEFT.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(103f, 200f), Align.TOP_CENTER.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(106f, 200f), Align.TOP_RIGHT.innerTopLeft(outerDim, innerDim, startCoord))

        assertEquals(Coord(100f, 199f), Align.MIDDLE_LEFT.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(103f, 199f), Align.MIDDLE_CENTER.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(106f, 199f), Align.MIDDLE_RIGHT.innerTopLeft(outerDim, innerDim, startCoord))

        assertEquals(Coord(100f, 198f), Align.BOTTOM_LEFT.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(103f, 198f), Align.BOTTOM_CENTER.innerTopLeft(outerDim, innerDim, startCoord))
        assertEquals(Coord(106f, 198f), Align.BOTTOM_RIGHT.innerTopLeft(outerDim, innerDim, startCoord))
    }
}