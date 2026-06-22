package com.cinepass.utils

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard

actual object PlatformActions {
    actual fun copyToClipboard(label: String, text: String) {
        UIPasteboard.generalPasteboard.string = text
    }

    actual fun shareText(text: String, chooserTitle: String) {
        val controller = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val activity = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )
        controller.presentViewController(activity, animated = true, completion = null)
    }
}
