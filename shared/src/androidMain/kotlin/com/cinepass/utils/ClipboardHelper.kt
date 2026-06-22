package com.cinepass.utils

import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import org.koin.core.context.GlobalContext

actual object ClipboardHelper {
    private const val TAG = "ClipboardHelper"
    private val REFERRAL_CODE_PATTERN = Regex("RS3_[A-Z0-9]{6}")

    actual fun getReferralCodeFromClipboard(): String? {
        try {
            val context = GlobalContext.get().get<Context>()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clipData = clipboard?.primaryClip
            
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: return null
                
                // Check if text contains a valid referral code pattern
                val match = REFERRAL_CODE_PATTERN.find(text)
                if (match != null) {
                    Log.d(TAG, "Found referral code in clipboard: ${match.value}")
                    return match.value
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading clipboard", e)
        }
        
        return null
    }

    actual fun clearClipboard() {
        try {
            val context = GlobalContext.get().get<Context>()
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.clearPrimaryClip()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing clipboard", e)
        }
    }
}
