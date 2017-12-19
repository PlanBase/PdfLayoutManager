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

import com.planbase.pdf.layoutmanager.pages.RenderTarget
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord

/**
A mutable data structure to hold a wrapped line consisting of multiple items.
 @param source
 */
class MultiLineWrapped(var width: Float = 0f,
                       override var ascent: Float = 0f,
                       override var lineHeight: Float = 0f,
                       val items: MutableList<LineWrapped> = mutableListOf()) : LineWrapped {
    override val dim: Dim
            get() = Dim(width, lineHeight)

    private var descentLeading = lineHeight - ascent

    fun isEmpty() = items.isEmpty()
    fun append(fi : LineWrapped): MultiLineWrapped {
        // lineHeight has to be ascent + descentLeading because we align on the baseline
        ascent = maxOf(ascent, fi.ascent)
        descentLeading = maxOf(descentLeading, fi.lineHeight - fi.ascent)
        lineHeight = ascent + descentLeading
        width += fi.dim.width
        items.add(fi)
        return this
    }

    override fun render(lp: RenderTarget, topLeft: Coord): Dim {
        var x:Float = topLeft.x
        val y = topLeft.y
        // lineHeight has to be ascent + descentLeading because we align on the baseline
        var maxAscent = ascent
        var maxDescentLeading = lineHeight - ascent
        for (item: LineWrapped in items) {
            // Text rendering calculation spot 2/3
            // ascent is the maximum ascent for anything on this line.
            //               _____
            //          /   |     \
            //         |    |      \
            // (max)   |    |      |
            // ascent <     |_____/      ___.
            //         |    |     \     /   |
            //         |    |      \   |    |
            //          \. .|. . . .\. .\___| . . . .
            //                              |
            //                          \__/
            // Subtracting that from the top-y
            // yields the baseline, which is what we want to align on.
//            println("y=$y ascent=$ascent item=${item}")
            val ascentDiff = ascent - item.ascent
            val innerUpperLeft = Coord(x, y - ascentDiff)
//            println("ascentDiff=$ascentDiff innerUpperLeft=$innerUpperLeft")
            val adjHeight = item.render(lp, innerUpperLeft).height
            val adjustment = adjHeight - item.lineHeight
//            println("adjHeight=$adjHeight item.lineHeight=${item.lineHeight} adjustment=$adjustment")
            maxAscent = maxOf(maxAscent, item.ascent + adjustment)
            maxDescentLeading = maxOf(maxDescentLeading, item.lineHeight - item.ascent)
            x += item.dim.width
        }
        return Dim(x - topLeft.x, maxAscent + maxDescentLeading)
    }

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

private fun addLineToMultiLineWrappedsCheckBlank(MultiLineWrappeds: MutableList<MultiLineWrapped>, line: MultiLineWrapped) {
    // If this item is a blank line, take the height from the previous item (if there is one).
    if (line.isEmpty() && MultiLineWrappeds.isNotEmpty())  {
        val lastRealItem: LineWrapped = MultiLineWrappeds.last().items.last()
        line.ascent = lastRealItem.ascent
        line.lineHeight = lastRealItem.lineHeight
    }
    // Now add the line to the list.
    MultiLineWrappeds.add(line)
}

fun lineWrappablesToMultiLineWrappeds(itemsInBlock: List<LineWrappable>, maxWidth: Float) : List<MultiLineWrapped> {
    if (maxWidth < 0) {
        throw IllegalArgumentException("maxWidth must be >= 0, not " + maxWidth)
    }

    // Really should call this "List of wrapped, wrapped line lists"
    val listOfWrappedWrappedLineLists: MutableList<MultiLineWrapped> = mutableListOf()
    var line = MultiLineWrapped() // Is this right, putting no source here?

    for (item in itemsInBlock) {
//        println("About to wrap: $item")
        val rtor: LineWrapper = item.lineWrapper()
        while (rtor.hasMore()) {
            if (line.isEmpty()) {
                val something : ConTerm = rtor.getSomething(maxWidth)
//                println("🢂something=" + something)
                line.append(something.item)
                if (something is Terminal) {
//                    println("=============== TERMINAL")
//                    println("something:" + something)
                    addLineToMultiLineWrappedsCheckBlank(listOfWrappedWrappedLineLists, line)
                    line = MultiLineWrapped()
                }
            } else {
                val ctn: ConTermNone = rtor.getIfFits(maxWidth - line.width)
//                println("🢂ctn=" + ctn)

                when (ctn) {
                    is Continuing ->
                        line.append(ctn.item)
                    is Terminal -> {
//                        println("=============== TERMINAL 222222222")
//                        println("ctn:" + ctn)
                        line.append(ctn.item)
                        line = MultiLineWrapped()
                    }
                    None -> {
//                        MultiLineWrappeds.add(line)
                        addLineToMultiLineWrappedsCheckBlank(listOfWrappedWrappedLineLists, line)
                        line = MultiLineWrapped()
                    }}
            }
        }
    }
    // Don't forget to add last item.
    addLineToMultiLineWrappedsCheckBlank(listOfWrappedWrappedLineLists, line)

    return listOfWrappedWrappedLineLists.toList()
}