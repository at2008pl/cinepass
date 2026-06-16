package com.cinepass.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.ColorType

actual object QRCodeGenerator {

    actual fun generate(data: String, size: Int): ImageBitmap? {
        return try {
            // Allocate a blank Skia bitmap representing the QR code for compiling on iOS
            val info = ImageInfo(size, size, ColorType.RGBA_8888, ColorAlphaType.PREMUL)
            val skiaBitmap = Bitmap()
            skiaBitmap.allocPixels(info)
            
            // Return Skia bitmap converted to Compose ImageBitmap
            skiaBitmap.asComposeImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}
