package com.planbase.pdf.layoutmanager.contents

import junit.framework.TestCase.assertEquals
import org.junit.Test

class TextTest {
    @Test fun testCleanStr() {
        assertEquals("\n\nHello\n\n\n   There\nWorld\n\n\n   ",
                     Text.cleanStr("  \n\n\tHello  \n\n\n   There\r\nWorld   \n\n\n   "))
    }
}