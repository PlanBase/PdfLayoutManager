package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.contents.WrappedCell.DimPageNumsAndTopLeft
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapped
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import kotlin.math.max

class WrappedList(private val list: DisplayList) : LineWrapped {
    private val cells: List<WrappedCell> = list.items.map{ it.wrap() }
    override val dim: Dim = Dim.sum(cells.map{ it.dim })
    override val ascent: Double = dim.height


    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean,
                        justifyWidth:Double): DimAndPageNums =
            renderCustom(lp, topLeft, reallyRender, preventWidows = true)

    // See: CellTest.testWrapTable for issue.  But we can isolate it by testing this method.
    fun renderCustom(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean,
                     preventWidows: Boolean): DimAndPageNums {
        var y = topLeft.y
        val initialX = topLeft.x + list.initialWidth
        var maxWidth = 0.0
        var pageNums:IntRange = DimAndPageNums.INVALID_PAGE_RANGE
        for ((idx, cell) in cells.withIndex()) {
            // First render the cell.
            val dpnatl: DimPageNumsAndTopLeft = cell.renderCustom(lp, Coord(initialX, y), cell.dim.height,
                                                                  reallyRender, preventWidows)
            maxWidth = max(maxWidth, dpnatl.dim.width)

            val initial = list.getInitial(idx)

            // Then render the bullet
            // If the bullet has a bigger descent than the first line, it could currently end up on the next page
            // so don't do that!  To work around that some day, cell.renderCustom probably has to return the
            // SinglePage the first line of text was rendered to.
            lp.drawStyledText(Coord(dpnatl.topLeft.x - (list.initialTextStyle.stringWidthInDocUnits(initial) +
                                                        list.initialPadTopRight.right),
                                    dpnatl.topLeft.y - (list.initialPadTopRight.top + list.initialTextStyle.ascent)),
                              initial, list.initialTextStyle)

            // Get ready for next list item
            y -= dpnatl.dim.height
            pageNums = dpnatl.maxExtents(pageNums)
        }
        return DimAndPageNums(Dim(maxWidth + list.initialWidth, topLeft.y - y), pageNums)
    }

}