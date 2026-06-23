package com.cinepass

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.cinepass.data.preferences.ReferralPreferences
import com.cinepass.navigation.AppNavigation
import com.cinepass.ui.theme.CinepassTheme
import com.google.firebase.messaging.FirebaseMessaging
import com.cinepass.utils.ClipboardHelper
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val tag = "MainActivity"
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            Log.d(tag, "Notification permission granted: $granted")
        }

        requestNotificationPermissionIfNeeded()
        initFcm()

        lifecycleScope.launch {
            checkReferralSources(intent)
        }

        setContent {
            CinepassTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        lifecycleScope.launch {
            checkReferralSources(intent)
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initFcm() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(tag, "FCM token obtained: ${token?.take(20)}...")
            } else {
                Log.e(tag, "Failed to get FCM token", task.exception)
            }
        }

        val targetRoute = intent.getStringExtra("target_route")
        if (!targetRoute.isNullOrEmpty()) {
            Log.d(tag, "Opened from notification, target route: $targetRoute")
        }
    }

    private suspend fun checkReferralSources(intent: Intent) {
        val referralPrefs = ReferralPreferences(this)

        val deepLinkCode = getCodeFromDeepLink(intent)
        if (!deepLinkCode.isNullOrBlank()) {
            Log.d(tag, "Found referral code from deep link: $deepLinkCode")
            referralPrefs.savePendingReferralCode(deepLinkCode)
            return
        }

        val clipboardCode = ClipboardHelper.getReferralCodeFromClipboard()
        if (!clipboardCode.isNullOrBlank()) {
            Log.d(tag, "Found referral code from clipboard: $clipboardCode")
            referralPrefs.savePendingReferralCode(clipboardCode)
            return
        }

        Log.d(tag, "No referral code found from deep link or clipboard")
    }

    private fun getCodeFromDeepLink(intent: Intent): String? {
        val data: Uri? = intent.data

        if (data != null) {
            Log.d(tag, "Deep link received: $data")
            return when {
                // cinepass://register?ref=CODE  or  cinepass://dl?ref=CODE
                data.scheme == "cinepass" -> data.getQueryParameter("ref")
                // https://rs3films.com/dl?ref=CODE  or  https://cinepass.app/register?ref=CODE
                data.scheme == "https" -> data.getQueryParameter("ref")
                else -> null
            }
        }

        return null
    }
}
