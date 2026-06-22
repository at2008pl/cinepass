package com.cinepass.ui.auth

import androidx.compose.runtime.Composable

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    RegisterScreenContent(onRegisterSuccess, onNavigateToLogin)
}
