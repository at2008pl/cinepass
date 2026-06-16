package com.cinepass.ui.auth

import androidx.compose.runtime.Composable

object RegisterScreenProvider {
    var content: @Composable (onRegisterSuccess: () -> Unit, onNavigateToLogin: () -> Unit) -> Unit = { _, _ -> }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    RegisterScreenProvider.content(onRegisterSuccess, onNavigateToLogin)
}
