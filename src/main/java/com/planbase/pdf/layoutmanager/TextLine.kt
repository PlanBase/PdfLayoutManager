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

/** A mutable data structure to hold a line. */
class TextLine {
    var width: Float = 0f
    var maxAscent: Float = 0f
    var maxDescentAndLeading: Float = 0f
    val items: MutableList<LineWrapped> = mutableListOf()

    fun isEmpty() = items.isEmpty()
    fun append(fi : LineWrapped): TextLine {
        maxAscent = maxOf(maxAscent, fi.ascent)
        maxDescentAndLeading = maxOf(maxDescentAndLeading, fi.descentAndLeading)
        width += fi.xyDim.width
        items.add(fi)
        return this
    }
    fun height(): Float = maxAscent + maxDescentAndLeading
//    fun xyDim(): XyDim = XyDim.of(width, height())
    fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset {
        var x:Float = outerTopLeft.x
        val y = outerTopLeft.y
        for (item: LineWrapped in items) {
            item.render(lp, XyOffset(x, y - item.ascent))
            x += item.xyDim.width
        }
        return XyOffset(x, height())
    }

    override fun toString(): String {
        return "TextLine(width=$width maxAscent=$maxAscent maxDescentAndLeading=$maxDescentAndLeading" +
               " height=${height()} items=\n" +
               items.fold(StringBuilder("["),
                          {acc, item ->
                              if (acc.length > 1) acc.append(",\n ")
                              acc.append(item)})
                       .append("])\n")
                       .toString()
    }
}

/**
 Given a maximum width, turns a list of renderables into a list of fixed-item textLines.
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
fun renderablesToTextLines(itemsInBlock: List<LineWrappable>, maxWidth: Float) : List<TextLine> {
    if (maxWidth < 0) {
        throw IllegalArgumentException("maxWidth must be >= 0, not " + maxWidth)
    }
    val textLines: MutableList<TextLine> = mutableListOf()
    var line = TextLine()

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
                    textLines.add(line)
                    line = TextLine()
                }
            } else {
                val ctn: ConTermNone = rtor.getIfFits(maxWidth - line.width)
//                println("🢂ctn=" + ctn)

                when (ctn) {
                    is Continuing ->
                        line.append(ctn.item)
                    is Terminal -> {
                        line.append(ctn.item)
                        line = TextLine()
                    }
                    None -> {
                        textLines.add(line)
                        line = TextLine()
                    }}
            }
        }
    }
    // The last item could be a blank line.  If so, take the height from the previous line.
    // TODO: We could have internal blank lines too - think about and test that!
    if (line.isEmpty() && textLines.isNotEmpty())  {
        val lastRealItem:LineWrapped = textLines.last().items.last()
        line.maxAscent = lastRealItem.ascent
        line.maxDescentAndLeading = lastRealItem.descentAndLeading
    }

    // Don't forget to add last item.
    textLines.add(line)

    return textLines.toList()
}