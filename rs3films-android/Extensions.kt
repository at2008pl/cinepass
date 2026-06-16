package com.rs3films.app.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

// Toast shortcuts
fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Context.toastLong(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
fun Fragment.toast(msg: String) = requireContext().toast(msg)

// Snackbar
fun View.snack(msg: String) = Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).show()
fun View.snackLong(msg: String) = Snackbar.make(this, msg, Snackbar.LENGTH_LONG).show()

// View visibility
fun View.show() { visibility = View.VISIBLE }
fun View.hide() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

// Intent navigation
inline fun <reified T : Activity> Context.startActivity(block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, T::class.java).apply(block))
}

inline fun <reified T : Activity> Activity.startActivityAndClear(block: Intent.() -> Unit = {}) {
    startActivity(Intent(this, T::class.java).apply(block).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
}

// Date formatting
fun String.toDisplayDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(this)
        outputFormat.format(date ?: return this)
    } catch (e: Exception) { this }
}

fun String.toDisplayTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(this)
        outputFormat.format(date ?: return this)
    } catch (e: Exception) { this }
}

// Coins to rupee
fun Int.coinsToRupee(): String = "₹%.0f".format(this * Constants.COIN_TO_RUPEE)
fun Double.toRupee(): String = "₹%.2f".format(this)
fun Int.withCommas(): String = String.format("%,d", this)

// Transaction type display
fun String.toTransactionLabel(): String = when (this) {
    "REFERRAL_SIGNUP"   -> "Friend joined via your referral"
    "REFERRAL_ATTENDED" -> "Friend attended event"
    "REFERRAL_L2"       -> "Network referral bonus"
    "TICKET_PURCHASE"   -> "Ticket purchase"
    "ATTENDANCE_BONUS"  -> "Event attendance bonus"
    "LEADERBOARD_BONUS" -> "Leaderboard reward"
    "CASH_REDEMPTION"   -> "Cash redeemed"
    "MERCH_PURCHASE"    -> "Merchandise purchase"
    "ADMIN_CREDIT"      -> "Coin refund"
    else -> this
}

fun String.toTransactionIcon(): String = when (this) {
    "REFERRAL_SIGNUP", "REFERRAL_ATTENDED", "REFERRAL_L2" -> "🔗"
    "TICKET_PURCHASE" -> "🎟"
    "ATTENDANCE_BONUS" -> "✅"
    "LEADERBOARD_BONUS" -> "🏆"
    "CASH_REDEMPTION" -> "💰"
    "ADMIN_CREDIT" -> "↩"
    else -> "⬡"
}
