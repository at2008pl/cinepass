package com.cinepass.utils

actual object PlatformActions {
    actual fun copyToClipboard(label: String, text: String) = Unit

    actual fun shareText(text: String, chooserTitle: String) = Unit
}
