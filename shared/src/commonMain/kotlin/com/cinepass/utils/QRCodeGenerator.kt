package com.cinepass.utils

import androidx.compose.ui.graphics.ImageBitmap

expect object QRCodeGenerator {
    fun generate(data: String, size: Int = 512): ImageBitmap?
}