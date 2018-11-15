package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.lineWrapping.ConTerm
import com.planbase.pdf.layoutmanager.lineWrapping.Continuing
import com.planbase.pdf.layoutmanager.lineWrapping.Terminal

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