package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable

/**
 * Represents a bulleted list (for display).  Compare with [NumberList].
 */
class BulletList(
        override val width: Double,
        override val initialWidth: Double,
        override val defaultCellStyle: CellStyle,
        override val initialVAlign: Align,
        override val initialPadTopRight: Padding,
        override val initialTextStyle: TextStyle,
        private val bullet: String): DisplayList {

    private val innerWidth: Double = width - initialWidth
    private val secretItems: MutableList<Cell> = mutableListOf()
    override val items
        get() = secretItems.toList()
    
    override fun addItem(contents: List<LineWrappable>) {
        secretItems.add(Cell(defaultCellStyle, innerWidth, contents))
    }

    override fun wrap() = WrappedList(this)

    override fun getInitial(idx: Int):String = bullet
}