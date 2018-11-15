package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.lineWrapping.ConTerm
import com.planbase.pdf.layoutmanager.lineWrapping.ConTermNone
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.lineWrapping.None

// TODO: Try moving Text.tryGettingText to this class
/**
 * A line-wrapper for wrapping text.
 */
internal class TextLineWrapper(private val txt: Text) : LineWrapper {
    private var idx = 0

    override fun hasMore(): Boolean = idx < txt.text.length

    override fun getSomething(maxWidth: Double): ConTerm {
//            println("      TextLineWrapper.getSomething($maxWidth)")
        if (maxWidth < 0) {
            throw IllegalArgumentException("Illegal negative width: $maxWidth")
        }
        val rowIdx = Text.tryGettingText(maxWidth, idx, txt)
//            println("rowIdx=$rowIdx")
        idx = rowIdx.idx
        return rowIdx.toContTerm()
    }

    override fun getIfFits(remainingWidth: Double): ConTermNone {
//            println("      TextLineWrapper.getIfFits($remainingWidth)")
        if (remainingWidth <= 0) {
//                return None
            throw IllegalArgumentException("remainingWidth must be > 0")
        }
        val ctri = Text.tryGettingText(remainingWidth, idx, txt)
        val row = ctri.row
        return if (row.dim.width <= remainingWidth) {
            idx = ctri.idx
            ctri.toContTerm() as ConTermNone
        } else {
            None
        }
    }
}