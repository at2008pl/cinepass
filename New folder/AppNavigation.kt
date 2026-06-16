package com.cinepass.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cinepass.ui.auth.LoginScreen
import com.cinepass.ui.auth.RegisterScreen
import com.cinepass.ui.events.EventDetailScreen
import com.cinepass.ui.events.EventListScreen
import com.cinepass.ui.home.HomeScreen
import com.cinepass.ui.profile.ProfileScreen
import com.cinepass.ui.profile.ReferralScreen
import com.cinepass.ui.tickets.QRCodeScreen
import com.cinepass.ui.tickets.TicketScreen
import com.cinepass.ui.wallet.WalletScreen

// All app routes defined here
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val EVENT_LIST = "event_list"
    const val EVENT_DETAIL = "event_detail/{eventId}"
    const val WALLET = "wallet"
    const val TICKETS = "tickets"
    const val QR_CODE = "qr_code/{ticketId}"
    const val PROFILE = "profile"
    const val REFERRAL = "referral"

    // Helper functions to build routes with params
    fun eventDetail(eventId: String) = "event_detail/$eventId"
    fun qrCode(ticketId: String) = "qr_code/$ticketId"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.HOME) },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate(Routes.HOME) },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Main Screens
        composable(Routes.HOME) {
            HomeScreen(
                onEventClick = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                },
                onWalletClick = { navController.navigate(Routes.WALLET) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.EVENT_LIST) {
            EventListScreen(
                onEventClick = { eventId ->
                    navController.navigate(Routes.eventDetail(eventId))
                }
            )
        }

        composable(Routes.EVENT_DETAIL) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                eventId = eventId,
                onTicketBooked = { navController.navigate(Routes.TICKETS) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.WALLET) {
            WalletScreen(
                onBack = { navController.popBackStack() },
                onReferralClick = { navController.navigate(Routes.REFERRAL) }
            )
        }

        composable(Routes.TICKETS) {
            TicketScreen(
                onTicketClick = { ticketId ->
                    navController.navigate(Routes.qrCode(ticketId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.QR_CODE) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            QRCodeScreen(
                ticketId = ticketId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onReferralClick = { navController.navigate(Routes.REFERRAL) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REFERRAL) {
            ReferralScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
