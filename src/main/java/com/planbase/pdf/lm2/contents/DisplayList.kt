package com.planbase.pdf.lm2.contents

import com.planbase.pdf.lm2.attributes.Align
import com.planbase.pdf.lm2.attributes.CellStyle
import com.planbase.pdf.lm2.attributes.Padding
import com.planbase.pdf.lm2.attributes.TextStyle
import com.planbase.pdf.lm2.lineWrapping.ConTerm
import com.planbase.pdf.lm2.lineWrapping.ConTermNone
import com.planbase.pdf.lm2.lineWrapping.Continuing
import com.planbase.pdf.lm2.lineWrapping.LineWrappable
import com.planbase.pdf.lm2.lineWrapping.LineWrapper
import com.planbase.pdf.lm2.lineWrapping.None

/**
 * Represents a bulleted or numbered list (for display).  Not the List data structure (although it contains one).
 */
interface DisplayList: LineWrappable {
    val width: Double
    val initialWidth: Double

    val defaultCellStyle: CellStyle
    val initialVAlign: Align
    val initialPadTopRight: Padding
    val initialTextStyle: TextStyle
    val items: List<Cell>

    fun addItem(contents: List<LineWrappable>)

    fun wrap(): WrappedList

    fun getInitial(idx: Int):String

    @JvmDefault
    override fun lineWrapper() = object: LineWrapper {
        private var hasMore = true
        override fun hasMore(): Boolean = hasMore

        override fun getSomething(maxWidth: Double): ConTerm {
            hasMore = false
            return Continuing(wrap(), false)
        }

        override fun getIfFits(remainingWidth: Double): ConTermNone =
                if (hasMore && (width <= remainingWidth)) {
                    hasMore = false
                    Continuing(wrap(), false)
                } else {
                    None
                }
    }
}