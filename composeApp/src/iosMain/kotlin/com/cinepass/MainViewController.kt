package com.cinepass

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import com.cinepass.navigation.AppNavigation
import com.cinepass.ui.theme.CinepassTheme
import com.cinepass.ui.auth.RegisterScreenProvider
import com.cinepass.ui.auth.IosRegisterScreen

fun MainViewController(): UIViewController {
    RegisterScreenProvider.content = { onSuccess, onLogin ->
        IosRegisterScreen(onSuccess, onLogin)
    }
    return ComposeUIViewController {
        CinepassTheme {
            AppNavigation()
        }
    }
}
