package com.cinepass.utils

expect object PlatformActions {
    fun copyToClipboard(label: String, text: String)
    fun shareText(text: String, chooserTitle: String = "Share")
}
