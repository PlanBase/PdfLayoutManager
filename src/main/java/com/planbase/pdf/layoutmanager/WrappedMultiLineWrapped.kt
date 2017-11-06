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

package com.planbase.pdf.layoutmanager

// TODO: Rename to WrappedLine
/**
A mutable data structure to hold a wrapped line.
 @param source
 */
class WrappedMultiLineWrapped : LineWrapped {
    var width: Float = 0f

    override val xyDim:XyDim
            get() = XyDim(width, lineHeight)

    override var ascent: Float = 0f

    override var descentAndLeading: Float = 0f

    override val lineHeight: Float
            get() = ascent + descentAndLeading

    val items: MutableList<LineWrapped> = mutableListOf()

    fun isEmpty() = items.isEmpty()
    fun append(fi : LineWrapped): WrappedMultiLineWrapped {
        ascent = maxOf(ascent, fi.ascent)
        descentAndLeading = maxOf(descentAndLeading, fi.descentAndLeading)
        width += fi.xyDim.width
        items.add(fi)
        return this
    }

    override fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset {
        var x:Float = outerTopLeft.x
        val y = outerTopLeft.y
        for (item: LineWrapped in items) {
            item.render(lp, XyOffset(x, y - item.ascent))
            x += item.xyDim.width
        }
        return XyOffset(x, lineHeight)
    }

    override fun toString(): String {
        return "WrappedMultiLineWrapped(width=$width ascent=$ascent descentAndLeading=$descentAndLeading" +
               " height=$lineHeight items=\n" +
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

private fun addLineToWrappedMultiLineWrappedsCheckBlank(WrappedMultiLineWrappeds: MutableList<WrappedMultiLineWrapped>, line: WrappedMultiLineWrapped) {
    // If this item is a blank line, take the height from the previous item (if there is one).
    if (line.isEmpty() && WrappedMultiLineWrappeds.isNotEmpty())  {
        val lastRealItem:LineWrapped = WrappedMultiLineWrappeds.last().items.last()
        line.ascent = lastRealItem.ascent
        line.descentAndLeading = lastRealItem.descentAndLeading
    }
    // Now add the line to the list.
    WrappedMultiLineWrappeds.add(line)
}

fun renderablesToWrappedMultiLineWrappeds(itemsInBlock: List<LineWrappable>, maxWidth: Float) : List<WrappedMultiLineWrapped> {
    if (maxWidth < 0) {
        throw IllegalArgumentException("maxWidth must be >= 0, not " + maxWidth)
    }

    // Really should call this "List of wrapped, wrapped line lists"
    val listOfWrappedWrappedLineLists: MutableList<WrappedMultiLineWrapped> = mutableListOf()
    var line = WrappedMultiLineWrapped() // Is this right, putting no source here?

    for (item in itemsInBlock) {
        val rtor: LineWrapper = item.lineWrapper()
        while (rtor.hasMore()) {
            if (line.isEmpty()) {
                val something : ConTerm = rtor.getSomething(maxWidth)
//                println("🢂something=" + something)
                line.append(something.item)
                if (something is Terminal) {
//                    println("=============== TERMINAL")
//                    println("something:" + something)
                    addLineToWrappedMultiLineWrappedsCheckBlank(listOfWrappedWrappedLineLists, line)
                    line = WrappedMultiLineWrapped()
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
                        line = WrappedMultiLineWrapped()
                    }
                    None -> {
//                        WrappedMultiLineWrappeds.add(line)
                        addLineToWrappedMultiLineWrappedsCheckBlank(listOfWrappedWrappedLineLists, line)
                        line = WrappedMultiLineWrapped()
                    }}
            }
        }
    }
    // Don't forget to add last item.
    addLineToWrappedMultiLineWrappedsCheckBlank(listOfWrappedWrappedLineLists, line)

    return listOfWrappedWrappedLineLists.toList()
}