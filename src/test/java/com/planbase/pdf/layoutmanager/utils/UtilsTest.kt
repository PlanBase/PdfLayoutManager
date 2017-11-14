package com.planbase.pdf.layoutmanager.utils

import com.planbase.pdf.layoutmanager.utils.Utils.Companion.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.CMYK_WHITE
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.RGB_BLACK
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.RGB_WHITE
import com.planbase.pdf.layoutmanager.utils.Utils.Companion.colorToString
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.*
import org.junit.Test

class UtilsTest {
    @Test fun testColorToString() {
        assertEquals("null", colorToString(null))
        assertEquals("CMYK_BLACK", colorToString(CMYK_BLACK))
        assertEquals("CMYK_WHITE", colorToString(CMYK_WHITE))
        assertEquals("CMYK_WHITE", colorToString(PDColor(floatArrayOf(0f, 0f, 0f, 0f), PDDeviceCMYK.INSTANCE)))
        assertEquals("RGB_BLACK", colorToString(RGB_BLACK))
        assertEquals("RGB_WHITE", colorToString(RGB_WHITE))
        assertEquals("RGB_WHITE", colorToString(PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)))

        assertEquals("DeviceCMYK[0.2, 0.3, 0.4, 0.5]",
                     colorToString(PDColor(floatArrayOf(0.2f, 0.3f, 0.4f, 0.5f), PDDeviceCMYK.INSTANCE)))

        assertEquals("DeviceRGB[0.2, 0.3, 0.4]",
                     colorToString(PDColor(floatArrayOf(0.2f, 0.3f, 0.4f), PDDeviceRGB.INSTANCE)))

        assertEquals(PDColor(COSName.PATTERN, PDDeviceCMYK.INSTANCE).toString(),
                     colorToString(PDColor(COSName.PATTERN, PDDeviceCMYK.INSTANCE)))

        assertEquals("DeviceRGB[]",
                     colorToString(PDColor(floatArrayOf(), PDDeviceRGB.INSTANCE)))

    }
}