package com.cinepass.data.repository

import com.cinepass.data.api.ApiService
import com.cinepass.data.models.Wallet
import com.cinepass.data.models.WalletTransaction
import com.cinepass.data.prefs.UserPrefs

class WalletRepository(
    private val apiService: ApiService,
    private val userPrefs: UserPrefs,
) {

    suspend fun getMyWallet(): Wallet {
        val walletResponse = apiService.getWallet()
        val txResponse = apiService.getTransactions(page = 1, limit = 100)

        val walletData = walletResponse.body()
        val transactions = txResponse.body()?.data.orEmpty()

        val totalCoins = walletData?.totalCoins ?: 0
        userPrefs.coins = totalCoins

        return Wallet(
            userId = walletData?.userId ?: 0,
            totalCoins = totalCoins,
            transactions = transactions
        )
    }
}

