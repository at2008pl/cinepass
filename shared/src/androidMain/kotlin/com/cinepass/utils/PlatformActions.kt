package com.cinepass.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import org.koin.core.context.GlobalContext

actual object PlatformActions {
    actual fun copyToClipboard(label: String, text: String) {
        val context = GlobalContext.get().get<Context>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    actual fun shareText(text: String, chooserTitle: String) {
        val context = GlobalContext.get().get<Context>()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(
            Intent.createChooser(intent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
