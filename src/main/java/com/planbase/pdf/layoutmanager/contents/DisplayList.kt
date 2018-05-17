package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.ConTerm
import com.planbase.pdf.layoutmanager.lineWrapping.ConTermNone
import com.planbase.pdf.layoutmanager.lineWrapping.Continuing
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrapper
import com.planbase.pdf.layoutmanager.lineWrapping.None

interface DisplayList: LineWrappable {
    val initialTextStyle: TextStyle
    val padTopRight: Padding
    val vAlign: Align
    val defaultCellStyle: CellStyle
    val width: Double
    val initialWidth: Double
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
            return Continuing(wrap())
        }

        override fun getIfFits(remainingWidth: Double): ConTermNone =
                if (hasMore && (width <= remainingWidth)) {
                    hasMore = false
                    Continuing(wrap())
                } else {
                    None
                }
    }
}