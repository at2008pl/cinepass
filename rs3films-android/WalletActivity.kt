package com.rs3films.app.ui.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rs3films.app.data.api.ApiClient
import com.rs3films.app.data.api.models.RedeemRequest
import com.rs3films.app.data.api.models.TransactionData
import com.rs3films.app.data.prefs.UserPrefs
import com.rs3films.app.databinding.ActivityWalletBinding
import com.rs3films.app.databinding.ItemTransactionBinding
import com.rs3films.app.utils.*
import kotlinx.coroutines.launch

class WalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletBinding
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs = UserPrefs(this)

        supportActionBar?.title = "My Wallet"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadWallet()
        loadTransactions()
        setupRedemption()
    }

    private fun loadWallet() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getWallet(prefs.bearerToken)
                if (response.isSuccessful && response.body()?.success == true) {
                    val w = response.body()!!.data!!
                    prefs.coins = w.coins

                    binding.apply {
                        tvCoins.text = w.coins.withCommas()
                        tvRupeeValue.text = "≈ ₹${w.rupeeValue} redeemable value"
                        tvTotalEarned.text = "${w.totalEarned.withCommas()} earned"
                        tvTotalSpent.text = "${w.totalSpent.withCommas()} spent"
                        tvReferrals.text = "${w.totalReferrals} friends referred"

                        // Referral earnings breakdown
                        tvEarnRate.text = "⬡ 50 per signup · ⬡ 100 per attendance · ⬡ 25 (L2 bonus)"
                    }
                }
            } catch (e: Exception) {
                // Show cached
                binding.tvCoins.text = prefs.coins.withCommas()
            }
        }
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTransactions(prefs.bearerToken)
                if (response.isSuccessful) {
                    val transactions = response.body()?.data ?: emptyList()
                    binding.rvTransactions.apply {
                        layoutManager = LinearLayoutManager(this@WalletActivity)
                        adapter = TransactionAdapter(transactions)
                    }

                    if (transactions.isEmpty()) {
                        binding.tvNoTransactions.show()
                    }
                }
            } catch (e: Exception) { /* silent */ }
        }
    }

    private fun setupRedemption() {
        binding.btnRedeem.setOnClickListener {
            val upiId = binding.etUpiId.text.toString().trim()
            val coinsStr = binding.etRedeemCoins.text.toString().trim()

            if (upiId.isEmpty()) { toast("Enter UPI ID"); return@setOnClickListener }
            if (coinsStr.isEmpty()) { toast("Enter coins to redeem"); return@setOnClickListener }

            val coins = coinsStr.toIntOrNull() ?: 0
            if (coins < 500) { toast("Minimum 500 coins required"); return@setOnClickListener }
            if (coins > prefs.coins) { toast("Insufficient coins"); return@setOnClickListener }

            redeemCoins(coins, upiId)
        }
    }

    private fun redeemCoins(coins: Int, upiId: String) {
        binding.btnRedeem.isEnabled = false
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.redeemCoins(
                    prefs.bearerToken,
                    RedeemRequest(coins, upiId)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    toastLong("₹${(coins * 0.1).toInt()} will be credited to $upiId in 1-2 days")
                    loadWallet()
                    loadTransactions()
                } else {
                    toast(response.body()?.message ?: "Redemption failed")
                }
            } catch (e: Exception) {
                toast("Connection error")
            } finally {
                binding.btnRedeem.isEnabled = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

// ─── Transaction Adapter ──────────────────────────────────────────
class TransactionAdapter(
    private val transactions: List<TransactionData>
) : RecyclerView.Adapter<TransactionAdapter.VH>() {

    inner class VH(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = transactions[position]
        holder.binding.apply {
            tvIcon.text = t.type.toTransactionIcon()
            tvDescription.text = t.type.toTransactionLabel()
            tvDate.text = t.createdAt.toDisplayDate()
            tvAmount.text = if (t.coins > 0) "+${t.coins} ⬡" else "${t.coins} ⬡"
            tvAmount.setTextColor(
                if (t.coins > 0) 0xFF06D6A0.toInt() else 0xFFFF3C5F.toInt()
            )
        }
    }
}
