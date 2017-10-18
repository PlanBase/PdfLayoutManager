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
    val items: MutableList<FixedItem> = mutableListOf()

    fun isEmpty() = items.isEmpty()
    fun append(fi : FixedItem): TextLine {
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
        for (item: FixedItem in items) {
            item.render(lp, XyOffset(x, y - item.ascent))
            x += item.xyDim.width
        }
        return XyOffset(x, height())
    }

    override fun toString(): String {
        return "TextLine(\n" +
                "               width=$width\n" +
                "           maxAscent=$maxAscent\n" +
                "maxDescentAndLeading=$maxDescentAndLeading\n" +
                "              height=${height()}\n" +
                "               items=\n" +
                "$items)\n"
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
fun renderablesToTextLines(itemsInBlock: List<Layoutable>, maxWidth: Float) : List<TextLine> {
    if (maxWidth < 0) {
        throw IllegalArgumentException("maxWidth must be >= 0, not " + maxWidth)
    }
    val textLines: MutableList<TextLine> = mutableListOf()
    var line = TextLine()

    for (item in itemsInBlock) {
        val rtor: Layouter = item.layouter()
        while (rtor.hasMore()) {
            if (line.isEmpty()) {
                val something : ContTerm = rtor.getSomething(maxWidth)
                line.append(something.item)
                if (something.foundCr) {
                    line = TextLine()
                }
            } else {
                val ctn: ContTermNone = rtor.getIfFits(maxWidth - line.width)

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
    return textLines.toList()
}