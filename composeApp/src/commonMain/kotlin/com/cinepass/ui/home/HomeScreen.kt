package com.cinepass.ui.home

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    onEventClick: (String) -> Unit,
    onWalletClick: () -> Unit,
    onProfileClick: () -> Unit,
    onReferralClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    HomeScreen_New(
        onEventClick = onEventClick,
        onWalletClick = onWalletClick,
        onProfileClick = onProfileClick,
        onReferralClick = onReferralClick,
        viewModel = viewModel,
    )
}
