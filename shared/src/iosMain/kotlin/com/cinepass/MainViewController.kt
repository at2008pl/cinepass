package com.cinepass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.cinepass.data.preferences.ReferralPreferences
import com.cinepass.di.initKoin
import com.cinepass.navigation.AppNavigation
import com.cinepass.ui.theme.CinepassTheme
import com.cinepass.utils.ClipboardHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

fun MainViewController(): UIViewController {
    initKoin()
    appScope.launch {
        val referralPrefs = ReferralPreferences()
        val clipboardCode = ClipboardHelper.getReferralCodeFromClipboard()
        if (!clipboardCode.isNullOrBlank()) {
            referralPrefs.savePendingReferralCode(clipboardCode)
        }
    }
    return ComposeUIViewController {
        CinepassTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                AppNavigation()
            }
        }
    }
}
