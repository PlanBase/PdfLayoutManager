package com.planbase.pdf.layoutmanager.lineWrapping

import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim

/**
 * A "spacer" line wrapped item that doesn't render anything - just leaves space on the page.
 */
class BlankLineWrapped(override val dim: Dim, override val ascent: Double): LineWrapped {
    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean,
                        justifyWidth:Double) =
            DimAndPageNums(dim, lp.pageNumFor(topLeft.y) .. lp.pageNumFor(topLeft.y - dim.height))
}