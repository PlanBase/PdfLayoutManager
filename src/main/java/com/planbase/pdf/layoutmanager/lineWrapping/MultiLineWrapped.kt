// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr2
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager.lineWrapping

import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.contents.WrappedText
import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import kotlin.math.nextUp

// TODO: Rename to MultiItemWrappedLine?
/** A mutable data structure to hold a single wrapped line consisting of multiple items. */
class MultiLineWrapped(tempItems: Iterable<LineWrapped>?) : LineWrapped {
    constructor() : this(listOf())
    var width: Double = 0.0
    override var ascent: Double = 0.0
    internal var lineHeight: Double = 0.0
    internal val items: MutableList<LineWrapped> = mutableListOf()

    init {
        tempItems?.forEach { append(it) }
    }

    override val dim: Dim
            get() = Dim(width, lineHeight)

    private var descentLeading = lineHeight - ascent

    fun isEmpty() = items.isEmpty()
    fun append(fi : LineWrapped): MultiLineWrapped {
        // lineHeight has to be ascent + descentLeading because we align on the baseline
        ascent = maxOf(ascent, fi.ascent)
        descentLeading = maxOf(descentLeading, fi.dim.height - fi.ascent)
        lineHeight = ascent + descentLeading
        width += fi.dim.width
        items.add(fi)
        return this
    }

    fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean, justifyWidth: Double): DimAndPageNums {
//        println("  MultiLineWrapped.render(${lp.javaClass.simpleName}, $topLeft, reallyRender=$reallyRender, $justifyWidth=justifyWidth)")
        var x: Double = topLeft.x

        // Note that I'm using height.nextUp() so that if there's a rounding error, our whole line gets thrown to
        // the next page, not just the line-fragment with the highest ascent.
        val adj = lp.pageBreakingTopMargin(topLeft.y - dim.height, dim.height.nextUp(), 0.0)
//        println("  adj=$adj")
        val y = if (adj == 0.0) { topLeft.y } else { topLeft.y - adj }

        var pageNums:IntRange = DimAndPageNums.INVALID_PAGE_RANGE

        if (reallyRender) {
            var tempItems = items

            // Do we need to justify text?
            if (justifyWidth > 0.0) {
                val contentWidth: Double = x - topLeft.x
                // Justified text only looks good if the line is long enough.
                if (contentWidth > justifyWidth * 0.75) {
                    val numSpaces: Int = items
                            .filter{ it is WrappedText }
                            .sumBy{ (it as WrappedText).numSpaces() }
                    val wordSpacing = (justifyWidth - contentWidth) / numSpaces
                    tempItems = items.map {
                        if (it is WrappedText) {
                            it.withWordSpacing(wordSpacing)
                        } else {
                            it
                        }
                    }.toMutableList()
                }
            }

            // Reset x to left-hand side of line.
            x = topLeft.x
            // Now that we've accounted for anything on the line that could cause a page-break,
            // really render each wrapped item in this line
            //
            // Text rendering calculation spot 2/3
            // ascent is the maximum ascent for anything on this line.
            //               _____
            //          /   |     \        \
            //         |    |      \        |
            // (max)   |    |      |         > ascentDiff
            // ascent <     |_____/         |
            //         |    |     \     _  /
            //         |    |      \   |_)
            //          \. .|. . . .\. | \. . . .
            //                          ^item
            //
            // Subtracting that from the top-y
            // yields the baseline, which is what we want to align on.
            for (item: LineWrapped in tempItems) {
                val ascentDiff = ascent - item.ascent
//                println("  item=$item, ascentDiff=$ascentDiff")
                val innerUpperLeft = Coord(x, y - ascentDiff)
                val dimAndPageNums: DimAndPageNums = item.render(lp, innerUpperLeft, true)
                x += item.dim.width
                pageNums = dimAndPageNums.maxExtents(pageNums)
            }
        } else {
            pageNums = lp.pageNumFor(y) .. lp.pageNumFor(y - dim.height)
            x = topLeft.x + dim.width
        }

        return DimAndPageNums(Dim(x - topLeft.x, (topLeft.y - y) + dim.height), pageNums)
    } // end fun render()

    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean): DimAndPageNums
            = render(lp, topLeft, reallyRender, 0.0)

    override fun toString(): String {
        return "MultiLineWrapped(width=$width, ascent=$ascent, lineHeight=$lineHeight," +
               " items=\n" +
               items.fold(StringBuilder("["),
                          {acc, item ->
                              if (acc.length > 1) acc.append(",\n ")
                              acc.append(item)})
                       .append("])\n")
                       .toString()
    }
}

/**
 Given a maximum width, turns a list of renderables into a list of fixed-item WrappedMultiLineWrappeds.
 This allows each line to contain multiple Renderables.  They are baseline-aligned.
 If any renderables are not text, their bottom is aligned to the text baseline.
 
Start a new line.
For each renderable
  While renderable is not empty
    If our current line is empty
      add items.getSomething(blockWidth) to our current line.
    Else
      If getIfFits(blockWidth - line.widthSoFar)
        add it to our current line.
      Else
        complete our line
        Add it to finishedLines
        start a new line.
 */
fun wrapLines(wrappables: List<LineWrappable>, maxWidth: Double) : List<MultiLineWrapped> {
    if (maxWidth < 0) {
        throw IllegalArgumentException("maxWidth must be >= 0, not $maxWidth")
    }

    // These are lines consisting of multiple (line-wrapped) items.
    val wrappedLines: MutableList<MultiLineWrapped> = mutableListOf()

    // This is the current line we're working on.
    var currLine = MultiLineWrapped() // Is this right, putting no source here?

//    var prevItem:LineWrappable = LineWrappable.ZeroLineWrappable
    for (item in wrappables) {
//        println("About to wrap: $item")
        val lineWrapper: LineWrapper = item.lineWrapper()
        while (lineWrapper.hasMore()) {
            if (currLine.isEmpty()) {
                val something : ConTerm = lineWrapper.getSomething(maxWidth)
//                println("🢂something=" + something)
                currLine.append(something.item)
                if (something is Terminal) {
//                    println("=============== TERMINAL")
//                    println("something:" + something)
                    addLineCheckBlank(currLine, wrappedLines)
                    currLine = MultiLineWrapped()
                } else if (lineWrapper.hasMore()) {
                    // We have a line of text which is too long and must be broken up.  But if it’s too long by less
                    // than the width of a single space, it truncates the final space from the text fragment, then
                    // looks again and sees that the next word now fits, so adds it to the same line *without the space*
                    // which is wrong.  To fix this, we check if the lineWrapper already gave us all it has for this
                    // line and if so, we store it and start the next line.
                    addLineCheckBlank(currLine, wrappedLines)
                    currLine = MultiLineWrapped()
                }
            } else {
                val ctn: ConTermNone = lineWrapper.getIfFits(maxWidth - currLine.width)
//                println("🢂ctn=" + ctn)

                when (ctn) {
                    is Continuing ->
                        currLine.append(ctn.item)
                    is Terminal -> {
//                        println("=============== TERMINAL 222222222")
//                        println("ctn:" + ctn)
                        currLine.append(ctn.item)
                        currLine = MultiLineWrapped()
                    }
                    None -> {
//                        MultiLineWrappeds.add(currLine)
                        addLineCheckBlank(currLine, wrappedLines)
                        currLine = MultiLineWrapped()
                    }}
            }
        } // end while lineWrapper.hasMore()
    } // end for item in wrappables
//    println("Line before last item: $currLine")

    // Don't forget to add last item.
    addLineCheckBlank(currLine, wrappedLines)

    return wrappedLines.toList()
}

private fun addLineCheckBlank(currLine: MultiLineWrapped,
                              wrappedLines: MutableList<MultiLineWrapped>) {
    // If this item is a blank line, take the height from the previous item (if there is one).
    if (currLine.isEmpty() && wrappedLines.isNotEmpty())  {
        val lastRealItem: LineWrapped = wrappedLines.last().items.last()
        currLine.ascent = lastRealItem.ascent
        currLine.lineHeight = lastRealItem.dim.height
    }
    wrappedLines.add(currLine)
}