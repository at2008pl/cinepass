package com.cinepass.ui.wallet

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun WalletScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToReferral: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel(),
) {
    WalletScreen_New(
        onBack = onNavigateToHome,
        onReferralClick = onNavigateToReferral,
        viewModel = viewModel,
    )
}
