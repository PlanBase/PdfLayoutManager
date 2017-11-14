package com.planbase.pdf.layoutmanager.lineWrapping

import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset

interface PageBroken {
    val page: SinglePage
    val outerTopLeft: XyOffset
    val xyDim: XyDim

    /**
    Sends the underlying object to PDFBox to be drawn.

    @param lp RenderTarget is the SinglePage or PageGrouping to draw to.  This will contain the paper size,
    orientation, and body area which are necessary in order to calculate page breaking
    @param outerTopLeft is the offset where this item starts.
    @return the XyOffset of the outer bottom-right of the rendered item which may include extra (vertical) spacing
    required to nudge some items onto the next page so they don't end up in the margin or off the page.
     */
    fun render(): XyOffset
}