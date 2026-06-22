package com.cinepass.ui.profile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onLogout: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    ProfileScreen_New(
        onBack = onNavigateToHome,
        onLogout = { onLogout?.invoke() ?: onNavigateToHome() },
        viewModel = viewModel,
    )
}
