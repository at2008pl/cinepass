package com.cinepass.ui.profile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ReferralScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    ReferralScreen_New(
        onNavigateToTree = {},
        onBack = onNavigateToHome,
    )
}
