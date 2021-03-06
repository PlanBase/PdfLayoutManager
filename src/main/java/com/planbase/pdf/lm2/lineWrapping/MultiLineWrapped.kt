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

package com.planbase.pdf.lm2.lineWrapping

import com.planbase.pdf.lm2.attributes.DimAndPageNums
import com.planbase.pdf.lm2.contents.WrappedText
import com.planbase.pdf.lm2.pages.RenderTarget
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import org.organicdesign.indented.IndentedStringable
import org.organicdesign.indented.StringUtils.listToStr
import kotlin.math.nextUp

// TODO: Rename to MultiItemWrappedLine?
/** A mutable data structure to hold a single wrapped line consisting of multiple items. */
class MultiLineWrapped(tempItems: Iterable<LineWrapped>?) : LineWrapped, IndentedStringable {
    constructor() : this(null)

    var width: Double = 0.0
    override var ascent: Double = 0.0
    internal var lineHeight: Double = 0.0
    internal val items: MutableList<LineWrapped> = mutableListOf()

    init {
        tempItems?.forEach { append(it) }
    }

    override val dim: Dim
        get() = Dim(width, lineHeight)

    override fun items(): List<LineWrapped> = items

    private var descentLeading = lineHeight - ascent

    fun isEmpty() = items.isEmpty()
    fun append(fi: LineWrapped): MultiLineWrapped {
        // lineHeight has to be ascent + descentLeading because we align on the baseline
        ascent = maxOf(ascent, fi.ascent)
        descentLeading = maxOf(descentLeading, fi.dim.height - fi.ascent)
        lineHeight = ascent + descentLeading
        width += fi.dim.width
        items.add(fi)
        return this
    }

    override fun render(lp: RenderTarget, topLeft: Coord, reallyRender: Boolean, justifyWidth: Double): DimAndPageNums {
//        println("this=$this")
//        println("  MultiLineWrapped.render(${lp.javaClass.simpleName}, $topLeft, reallyRender=$reallyRender, $justifyWidth=justifyWidth)")

        // Note that I'm using height.nextUp() so that if there's a rounding error, our whole line gets thrown to
        // the next page, not just the line-fragment with the highest ascent.
        val adj = lp.pageBreakingTopMargin(topLeft.y - dim.height, dim.height.nextUp(), 0.0)
//        println("  adj=$adj")
        val y = if (adj == 0.0) {
            topLeft.y
        } else {
            topLeft.y - adj
        }

        var pageNums: IntRange = DimAndPageNums.INVALID_PAGE_RANGE
        var x: Double = topLeft.x

        if (reallyRender) {
            var tempItems = items

            // Text justification calculation 2/2
            // Do we need to justify text?
            if (justifyWidth > 0.0) {
                val contentWidth: Double = dim.width
                // Justified text only looks good if the line is long enough.
                if (contentWidth > justifyWidth * 0.75) {
                    val numSpaces: Int = items
                            .filter { it is WrappedText }
                            .sumBy { (it as WrappedText).numSpaces }
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
            pageNums = lp.pageNumFor(y)..lp.pageNumFor(y - dim.height)
            x = topLeft.x + dim.width
        }

        return DimAndPageNums(Dim(x - topLeft.x, (topLeft.y - y) + dim.height), pageNums)
    } // end fun render()

    override fun indentedStr(indent: Int): String =
            "MultiLineWrapped(width=$width, ascent=$ascent, lineHeight=$lineHeight, items=\n" +
            "${listToStr(indent + "MultiLineWrapped(".length, items)})"

    override fun toString(): String = indentedStr(0)

    companion object {
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
        @JvmStatic
        fun wrapLines(wrappables: List<LineWrappable>, maxWidth: Double): List<LineWrapped> {
//    println("================in wrapLines...")
//    println("wrapLines($wrappables, $maxWidth)")
            if (maxWidth < 0) {
                throw IllegalArgumentException("maxWidth must be >= 0, not $maxWidth")
            }

            // These are lines consisting of multiple (line-wrapped) items.
            val wrappedLines: MutableList<LineWrapped> = mutableListOf()

            // This is the current line we're working on.
            var wrappedLine = MultiLineWrapped() // Is this right, putting no source here?

            // var prevItem:LineWrappable = LineWrappable.ZeroLineWrappable
            var unusedWidth = maxWidth
            for (item in wrappables) {
//        println("Wrappable: $item")
                val lineWrapper: LineWrapper = item.lineWrapper()
                while (lineWrapper.hasMore()) {
//            println("  lineWrapper.hasMore()")
                    if (wrappedLine.isEmpty()) {
                        unusedWidth = maxWidth
//                println("    wrappedLine.isEmpty()")
                        val something: ConTerm = lineWrapper.getSomething(maxWidth)
//                println("🢂something=$something")
                        wrappedLine.append(something.item)
                        unusedWidth -= something.item.dim.width
//                println("      unusedWidth1=$unusedWidth")
                        if (something is Terminal) {
//                    println("=============== TERMINAL")
//                    println("something:$something")
                            addLineCheckBlank(wrappedLine, wrappedLines)
                            wrappedLine = MultiLineWrapped()
                            unusedWidth = maxWidth
                        } else if ((something is Continuing) &&
                                   (something.hasMore)) {
//                    println("  Continuing and has more.  Adding line so far and starting a new one.")
                            // If we got as much as we could and there's more left, then it has to go on the next line.
                            addLineCheckBlank(wrappedLine, wrappedLines)
                            wrappedLine = MultiLineWrapped()
                            unusedWidth = maxWidth
                        } else if (lineWrapper.hasMore()) {
                            throw Exception("This shouldn't happen any more.  1")
//                    println("  LOOKAHEAD??? lineWrapper.hasMore()")
                            // We have a line of text which is too long and must be broken up.  But if it’s too long by less
                            // than the width of a single space, it truncates the final space from the text fragment, then
                            // looks again and sees that the next word now fits, so adds it to the same line *without the space*
                            // which is wrong.  To fix this, we check if the lineWrapper already gave us all it has for this
                            // line and if so, we store it and start the next line.
//                    addLineCheckBlank(wrappedLine, wrappedLines)
//                    wrappedLine = MultiLineWrapped()
//                    // This should never happen any more and should be deleted.
//                    if ( (something is Continuing) &&
//                         (something.item is WrappedText) ) {
//                        unusedWidth -= (something.item as WrappedText).textStyle.spaceWidth
//                    }
//                    println("      unusedWidth2=$unusedWidth")
                        }
                    } else {
//                println("    wrappedLine is not empty")
                        val ctn: ConTermNone = lineWrapper.getIfFits(unusedWidth) //maxWidth - wrappedLine.width)
//                println("🢂ctn=$ctn")

                        when (ctn) {
                            is Continuing -> {
//                        println("  appending result to current wrapped line")
                                wrappedLine.append(ctn.item)
                                if (ctn.hasMore) {
//                            println("      ctn.hasMore but it has to go on the next line.")
                                    addLineCheckBlank(wrappedLine, wrappedLines)
                                    wrappedLine = MultiLineWrapped()
                                    unusedWidth = maxWidth
                                } else {
//                            println("      ctn doesn't have more, so something else might fit on this line.")
                                    unusedWidth -= ctn.item.dim.width
                                }
//                        println("      unusedWidth3=$unusedWidth")
                                if (unusedWidth <= 0) {
                                    throw Exception("This shouldn't happen any more.  2")
//                            addLineCheckBlank(wrappedLine, wrappedLines)
//                            wrappedLine = MultiLineWrapped()
//                            unusedWidth = maxWidth
                                }
                            }
                            is Terminal -> {
//                        println("=============== TERMINAL 222222222")
//                        println("ctn:$ctn")
                                wrappedLine.append(ctn.item)
                                wrappedLine = MultiLineWrapped()
                                unusedWidth = maxWidth
                            }
                            None -> {
//                        println("=============== NONE 222222222")
                                // MultiLineWrappeds.add(wrappedLine)
                                addLineCheckBlank(wrappedLine, wrappedLines)
                                wrappedLine = MultiLineWrapped()
                                unusedWidth = maxWidth
                            }
                        }
                    }
                } // end while lineWrapper.hasMore()
            } // end for item in wrappables
//    println("Line before last item: $wrappedLine")

            // Don't forget to add last item.
            addLineCheckBlank(wrappedLine, wrappedLines)

            return wrappedLines.toList()
        }

        private fun addLineCheckBlank(currLine: MultiLineWrapped,
                                      wrappedLines: MutableList<LineWrapped>) {
            // If this item is a blank line, take the height from the previous item (if there is one).
            if (currLine.isEmpty() && wrappedLines.isNotEmpty()) {
                val lastRealItem: LineWrapped = wrappedLines.last().items().last()
//        println("================================ adding a spacer ===========================")
                wrappedLines.add(BlankLineWrapped(Dim(0.0, lastRealItem.dim.height), lastRealItem.ascent))
            } else if (currLine.items.size == 1) {
                // Don't add a MultilineWrapped if we can just add a single wrapped thing.
                // Trim space from final item on completed line.
                val lastItem = currLine.items[0]
                wrappedLines.add(when (lastItem) {
                                     is WrappedText -> lastItem.withoutTrailingSpace()
                                     else -> lastItem
                                 })
            } else {
                // Add multiple items.
                if (currLine.items.size > 1) {
                    // Trim space from final item on completed line.
                    val lastIdx = currLine.items.lastIndex
                    val lastItem = currLine.items[lastIdx]
                    if (lastItem is WrappedText) {
                        currLine.items.removeAt(lastIdx)
                        currLine.items.add(lastItem.withoutTrailingSpace())
                    }
                }
                wrappedLines.add(currLine)
            }
        }
    }
}