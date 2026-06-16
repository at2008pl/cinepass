package com.cinepass.utils

expect object ClipboardHelper {
    fun getReferralCodeFromClipboard(): String?
    fun clearClipboard()
}
