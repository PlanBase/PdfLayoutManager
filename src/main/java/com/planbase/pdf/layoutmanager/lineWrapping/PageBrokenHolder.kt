package com.planbase.pdf.layoutmanager.lineWrapping

import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset

class PageBrokenHolder(override val page: SinglePage,
                       override val outerTopLeft: XyOffset,
                       override val xyDim: XyDim,
                       val items: List<LineWrapped>) : PageBroken {
    override fun render(): XyOffset {
        var x:Float = outerTopLeft.x
        val y = outerTopLeft.y
        for (item: LineWrapped in items) {
            item.renderToPage(page, XyOffset(x, y - item.ascent))
            x += item.xyDim.width
        }
        return outerTopLeft.plusXMinusY(xyDim)
    }
}