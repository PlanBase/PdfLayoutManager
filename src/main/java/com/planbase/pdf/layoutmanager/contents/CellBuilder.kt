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

package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.LineWrappable

/**
 * Something that can be built into a table cell, OR just something rendered within a box model
 * (like HTML) where the table-free cell is the box.
 */
interface CellBuilder {
    val width:Float
    
    /** Creates a new CellBuilder with the given BoxStyle  */
    fun boxStyle(cs: BoxStyle): CellBuilder

    /** Creates a new CellBuilder with the given alignment  */
    fun align(a: Align): CellBuilder

    /** Creates a new CellBuilder with the given TextStyle  */
    fun textStyle(x: TextStyle): CellBuilder

    /**
     * Adds the given [LineWrappable] content to this cell.
     * To add multiple Renderables at once, use [.addAll] instead.
     */
    fun add(rs: LineWrappable): CellBuilder

    /**
     * Adds the given list of [LineWrappable] content to this cell.
     */
    fun addAll(js: Collection<LineWrappable>): CellBuilder

    /**
     * Adds text, but you must have textStyle set properly (or inherited) before calling this.
     */
    fun addStrs(vararg ss: String): CellBuilder

    /** Adds a list of text with the given textStyle  */
    fun add(ts: TextStyle, ls: Iterable<String>): CellBuilder
}