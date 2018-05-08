package com.planbase.pdf.layoutmanager.contents

import TestManual2.Companion.BULLET_TEXT_STYLE
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.Continuing
import junit.framework.TestCase.assertEquals
import org.junit.Test

class WrappedTextTest {
    @Test
    fun testExactLineWrapping() {
        val text = Text(BULLET_TEXT_STYLE, "months showed the possible money and")
        assertEquals(214.104, text.maxWidth(), 0.0)
        assertEquals(makeContinuing(BULLET_TEXT_STYLE, text.text),
                     text.lineWrapper().getSomething(214.104))

        assertEquals(makeContinuing(BULLET_TEXT_STYLE, "months showed the possible money"),
                     text.lineWrapper().getSomething(214.103))
    }
}

fun makeContinuing(textStyle: TextStyle, str:String): Continuing =
        Continuing(WrappedText(textStyle, str, textStyle.stringWidthInDocUnits(str)))