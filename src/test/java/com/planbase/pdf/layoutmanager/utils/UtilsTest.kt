package com.planbase.pdf.layoutmanager.utils

import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.*
import org.junit.Test

class UtilsTest {
    @Test fun testColorToString() {
        assertEquals("null", colorToString(null))
        assertEquals("cmykBlack", colorToString(cmykBlack))
        assertEquals("cmykWhite", colorToString(cmykWhite))
        assertEquals("cmykWhite", colorToString(PDColor(floatArrayOf(0f, 0f, 0f, 0f), PDDeviceCMYK.INSTANCE)))
        assertEquals("rgbBlack", colorToString(rgbBlack))
        assertEquals("rgbWhite", colorToString(rgbWhite))
        assertEquals("rgbWhite", colorToString(PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)))

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