package com.planbase.pdf.lm2.contents

import com.planbase.pdf.lm2.lineWrapping.ConTerm
import com.planbase.pdf.lm2.lineWrapping.Continuing
import com.planbase.pdf.lm2.lineWrapping.Terminal

/**
 * This is just an internal class for tracking how far we've gone in wrapping text and converting that into
 * a ConTerm.
 */
internal data class RowIdx(val row: WrappedText,
                           val idx: Int,
                           val foundCr: Boolean,
                           val hasMore: Boolean) {

    fun toContTerm() : ConTerm =
            if (foundCr) {
                Terminal(row)
            } else {
                Continuing(row, hasMore)
            }
}