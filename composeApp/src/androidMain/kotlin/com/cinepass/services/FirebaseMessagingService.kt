package com.cinepass.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cinepass.MainActivity
import com.cinepass.R
import com.cinepass.data.prefs.UserPrefs
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/* ═══════════════════════════════════════════════════════════════════════════
   FirebaseMessagingService — FCM Message Handling
   Handles: New tokens, incoming messages, notification display
═══════════════════════════════════════════════════════════════════════════ */

class FirebaseMessagingService : FirebaseMessagingService(), KoinComponent {

    private val userPrefs: UserPrefs by inject()

    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "FCM Token: $token")
        
        // Save token to preferences
        scope.launch {
            userPrefs.setFcmToken(token)
        }

        // In production, you would send this token to your backend
        // so that the server can send notifications to this specific device
        sendTokenToBackend(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        // Extract data
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "RS³ Films"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: ""
        val notificationType = remoteMessage.data["type"] ?: "general"
        val targetRoute = remoteMessage.data["target"] // e.g., "wallet", "referral", "offers"

        // Display notification
        showNotification(title, body, notificationType, targetRoute)
    }

    private fun showNotification(
        title: String,
        body: String,
        type: String,
        targetRoute: String? = null
    ) {
        val notificationId = (System.currentTimeMillis().toInt())
        val channelId = "rs3_notifications"

        // Create notification channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "RS³ Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for RS³ Films offers and updates"
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add extras for routing
            targetRoute?.let { putExtra("target_route", it) }
            putExtra("notification_type", type)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        // Set color based on notification type
        val color = when (type) {
            "offer_claimed" -> 0xFFA67C2E.toInt() // Gold
            "referral_earned" -> 0xFF2E6B45.toInt() // Green
            "wallet_updated" -> 0xFF1C1408.toInt() // Ebony
            else -> 0xFFA67C2E.toInt() // Default gold
        }
        builder.setColor(color)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, builder.build())

    Log.d("FCM", "Notification displayed: $title | $body | Type: $type")
    }

    private fun sendTokenToBackend(token: String) {
        // This would typically be called to update the backend
        // with the new FCM token for this device
        // For now, we just log it
        scope.launch {
            try {
                Log.d("FCM", "FCM Token saved and ready to sync with backend: $token")
                // In production:
                // ApiClient.rs3Api.updateFcmToken(token = "Bearer ${userPrefs.getAccessToken()}", fcmToken = token)
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send token to backend", e)
            }
        }
    }
}

/* Message Format Example:
{
  "notification": {
    "title": "Offer Claimed!",
    "body": "You've successfully claimed 'Movie Ticket Discount'"
  },
  "data": {
    "type": "offer_claimed",
    "target": "wallet",
    "offer_id": "123"
  }
}

Types:
- offer_claimed: When user claims an offer
- referral_earned: When someone uses referral code
- wallet_updated: Coin balance updates
- reward_redeemed: When user redeems coins
- general: Default notifications
*/
