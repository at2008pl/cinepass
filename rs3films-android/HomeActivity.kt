package com.rs3films.app.ui.home

import android.os.Bundle
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivityHomeBinding
import com.rs3films.app.ui.auth.LoginActivity
import com.rs3films.app.ui.event.EventDetailActivity
import com.rs3films.app.ui.ticket.QRTicketActivity
import com.rs3films.app.ui.wallet.WalletActivity
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPrefs(this)

        setupUI()
        loadWallet()
    }

    override fun onResume() {
        super.onResume()
        loadWallet() // Refresh coins on return
    }

    private fun setupUI() {
        binding.apply {
            // User info
            tvWelcome.text = "Hey, ${prefs.userName?.split(" ")?.first()} 👋"
            tvCoins.text = "⬡ ${prefs.coins.withCommas()} coins"

            // RS3 Movie card
            tvMovieTitle.text = Constants.MOVIE_TITLE
            tvMovieTagline.text = Constants.MOVIE_TAGLINE
            tvMovieInfo.text = "🎬 Dr V Ravichandran • Shreya Ghoshal • Sonu Nigam"
            tvRelease.text = "🎟 Feb 2026 in Cinemas"

            // Referral code card
            tvReferralCode.text = prefs.referralCode ?: "Loading..."
            tvReferralLink.text = "fanverse.app/join/${prefs.referralCode}"

            btnCopyReferral.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Referral Code", prefs.referralCode)
                clipboard.setPrimaryClip(clip)
                toast("Referral code copied! 📋")
            }

            // Navigation
            btnViewEvent.setOnClickListener {
                startActivity<EventDetailActivity> {
                    putExtra("event_id", Constants.MOVIE_EVENT_ID)
                }
            }

            btnWallet.setOnClickListener {
                startActivity<WalletActivity>()
            }

            btnMyTickets.setOnClickListener {
                startActivity<QRTicketActivity> {
                    putExtra("show_list", true)
                }
            }

            btnLogout.setOnClickListener {
                prefs.clear()
                startActivityAndClear<LoginActivity>()
            }

            // Referral share hint
            tvShareHint.text = "Share your code — earn ⬡ 50 coins per friend who joins!"
        }
    }

    private fun loadWallet() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getWallet(prefs.bearerToken)
                if (response.isSuccessful && response.body()?.success == true) {
                    val wallet = response.body()!!.data!!
                    prefs.coins = wallet.coins
                    binding.tvCoins.text = "⬡ ${wallet.coins.withCommas()} coins"
                    binding.tvCoinValue.text = "≈ ₹${wallet.rupeeValue} value"
                    binding.tvTotalReferrals.text = "${wallet.totalReferrals} friends referred"
                }
            } catch (e: Exception) {
                // Silently fail — show cached value
            }
        }
    }
}
