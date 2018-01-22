package com.planbase.pdf.layoutmanager.utils

import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.asserter

class UtilsTest {
    @Test fun testColorToString() {
        assertEquals("null", colorToString(null))
        assertEquals("CMYK_BLACK", colorToString(CMYK_BLACK))
        assertEquals("CMYK_WHITE", colorToString(CMYK_WHITE))
        assertEquals("CMYK_WHITE", colorToString(PDColor(floatArrayOf(0f, 0f, 0f, 0f), PDDeviceCMYK.INSTANCE)))
        assertEquals("RGB_BLACK", colorToString(RGB_BLACK))
        assertEquals("RGB_WHITE", colorToString(RGB_WHITE))
        assertEquals("RGB_WHITE", colorToString(PDColor(floatArrayOf(1f, 1f, 1f), PDDeviceRGB.INSTANCE)))

        assertEquals("PDColor(floatArrayOf(0.2f, 0.3f, 0.4f, 0.5f), PDDeviceCMYK.INSTANCE)",
                     colorToString(PDColor(floatArrayOf(0.2f, 0.3f, 0.4f, 0.5f), PDDeviceCMYK.INSTANCE)))

        assertEquals("PDColor(floatArrayOf(0.2f, 0.3f, 0.4f), PDDeviceRGB.INSTANCE)",
                     colorToString(PDColor(floatArrayOf(0.2f, 0.3f, 0.4f), PDDeviceRGB.INSTANCE)))

        assertEquals(PDColor(COSName.PATTERN, PDDeviceCMYK.INSTANCE).toString(),
                     colorToString(PDColor(COSName.PATTERN, PDDeviceCMYK.INSTANCE)))
    }

    @Test fun testColorModelToString() {
        val graphPic = ImageIO.read(File("target/test-classes/graph2.png"))
        val colorModel = graphPic.colorModel
        assertEquals("ColorModel([8, 8, 8], TYPE_RGB, OPAQUE)", colorModelToStr(colorModel))
        assertEquals("BufferedImage(ColorModel([8, 8, 8], TYPE_RGB, OPAQUE), 606x296)", buffImgToStr(graphPic))
    }

    @Test fun testCollectionToString() {
        assertEquals("listOf(1, 2, 3, 4, 5)",
                     listToStr(listOf(1, 2, 3, 4, 5)))

        assertEquals("mutableListOf(\"first\", \"second\", \"third\")",
                     mutableListToStr(mutableListOf("first", "second", "third")))

        assertEquals("listOf('a', 'b', 'c', 'd')",
                     listToStr(listOf('a', 'b', 'c', 'd')))

        // rad 2, e, pi
        assertEquals("listOf(1.41421f, 2.71828f, 3.14159f)",
                     listToStr(listOf(1.41421f, 2.71828f, 3.14159f)))
    }
}