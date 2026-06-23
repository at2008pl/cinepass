package com.cinepass.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cinepass.ui.auth.LoginScreen
import com.cinepass.ui.auth.OnboardingScreen
import com.cinepass.ui.auth.RegisterScreen
import com.cinepass.ui.auth.SplashScreen
import com.cinepass.ui.home.HomeScreen_New
import com.cinepass.ui.offers.OfferDetailScreen
import com.cinepass.ui.profile.ProfileScreen_New
import com.cinepass.ui.profile.ReferralScreen_New
import com.cinepass.ui.profile.ReferralTreeScreen
import com.cinepass.ui.settings.SettingsScreen
import com.cinepass.ui.wallet.WalletScreen_New
import androidx.compose.runtime.remember
import com.cinepass.data.prefs.UserPrefs

/* ═══════════════════════════════════════════════════════════════════════════
   AppNavigation — Complete App Navigation with Bottom Navigation
   Routes: Splash → Auth → Home/Wallet/Referral/Profile
═══════════════════════════════════════════════════════════════════════════ */

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val WALLET = "wallet"
    const val REFERRAL = "referral"
    const val REFERRAL_TREE = "referral_tree"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val OFFER_DETAIL = "offer_detail/{offerId}"
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem(Routes.HOME, Icons.Default.Home, "Home")
    object Wallet : BottomNavItem(Routes.WALLET, Icons.Default.Wallet, "Wallet")
    object Referral : BottomNavItem(Routes.REFERRAL, Icons.Default.Share, "Referral")
    object Profile : BottomNavItem(Routes.PROFILE, Icons.Default.Person, "Profile")

    companion object {
        fun items() = listOf(Home, Wallet, Referral, Profile)
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    Scaffold(
        bottomBar = {
            // Show bottom nav only for main app screens (not for auth screens)
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute in listOf(Routes.HOME, Routes.WALLET, Routes.REFERRAL, Routes.PROFILE, Routes.REFERRAL_TREE)) {
                AppBottomNavigation(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // Auth Screens
            composable(Routes.SPLASH) {
                val userPrefs = remember { UserPrefs() }
                SplashScreen(
                    onGetStarted = {
                        // If credentials exist but onboarding was never seen, this is a
                        // backup-restored state on a fresh install — clear stale auth data.
                        if (userPrefs.isLoggedIn && !userPrefs.hasSeenOnboarding) {
                            userPrefs.clear()
                        }
                        val destination = when {
                            userPrefs.isLoggedIn         -> Routes.HOME        // already logged in → go home
                            userPrefs.hasSeenOnboarding  -> Routes.LOGIN       // onboarding done → go to login
                            else                         -> Routes.ONBOARDING  // first install → show onboarding
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.ONBOARDING) {
                val userPrefs = remember { UserPrefs() }
                OnboardingScreen(
                    onFinish = {
                        userPrefs.hasSeenOnboarding = true
                        navController.navigate(Routes.REGISTER) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // Main App Screens (with bottom nav)
            composable(Routes.HOME) {
                HomeScreen_New(
                    onWalletClick = { navController.navigate(Routes.WALLET) },
                    onProfileClick = { navController.navigate(Routes.PROFILE) },
                    onReferralClick = { navController.navigate(Routes.REFERRAL) }
                )
            }

            composable(Routes.WALLET) {
                WalletScreen_New(
                    onBack = { navController.popBackStack() },
                    onReferralClick = { navController.navigate(Routes.REFERRAL) },
                    onOfferClick = { offerId -> navController.navigate("offer_detail/$offerId") }
                )
            }

            composable(Routes.REFERRAL) {
                ReferralScreen_New(
                    onNavigateToTree = { navController.navigate(Routes.REFERRAL_TREE) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.REFERRAL_TREE) {
                ReferralTreeScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PROFILE) {
                val userPrefs = remember { UserPrefs() }
                ProfileScreen_New(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        // Clear persisted auth so next app start goes to Login
                        userPrefs.clear()
                        // Pop the ENTIRE back stack then go to Login — back button cannot go back
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToTree = { navController.navigate(Routes.REFERRAL_TREE) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }

            composable(
                route = Routes.OFFER_DETAIL,
                arguments = listOf(navArgument("offerId") { type = NavType.IntType })
            ) { backStackEntry ->
                val offerId = backStackEntry.destination.route
                    ?.removePrefix("offer_detail/")
                    ?.toIntOrNull()
                    ?: return@composable
                OfferDetailScreen(
                    offerId = offerId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavigation(
    navController: NavHostController,
    currentRoute: String?
) {
    val items = BottomNavItem.items()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    NavigationBar {
        items.forEach { item ->
            val isSelected = navBackStackEntry?.destination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Routes.HOME) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

