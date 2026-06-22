package com.cinepass.utils

import platform.UIKit.UIPasteboard

actual object ClipboardHelper {
    private val REFERRAL_CODE_PATTERN = Regex("RS3_[A-Z0-9]{6}")

    actual fun getReferralCodeFromClipboard(): String? {
        try {
            val pasteboard = UIPasteboard.generalPasteboard
            val text = pasteboard.string
            if (text != null) {
                val match = REFERRAL_CODE_PATTERN.find(text)
                if (match != null) {
                    return match.value
                }
            }
        } catch (e: Exception) {
            // Log or ignore pasteboard access errors on iOS
        }
        return null
    }

    actual fun clearClipboard() {
        try {
            val pasteboard = UIPasteboard.generalPasteboard
            pasteboard.string = ""
        } catch (e: Exception) {
            // Log or ignore pasteboard write errors on iOS
        }
    }
}
