package com.planbase.pdf.lm2.lineWrapping

import com.planbase.pdf.lm2.attributes.DimAndPageNums
import com.planbase.pdf.lm2.pages.RenderTarget
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim

/**
 * A "spacer" line wrapped item that doesn't render anything - just leaves space on the page.
 */
data class BlankLineWrapped(override val dim: Dim, override val ascent: Double): LineWrapped {
    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean,
                        justifyWidth:Double) =
            DimAndPageNums(dim, lp.pageNumFor(topLeft.y) .. lp.pageNumFor(topLeft.y - dim.height))
}