package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.lineWrapping.PageBroken
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.XyDim
import com.planbase.pdf.layoutmanager.utils.XyOffset

class PageBrokenCell(override val page: SinglePage,
                     override val outerTopLeft: XyOffset,
                     override val xyDim: XyDim,
                     val cellStyle: CellStyle,
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