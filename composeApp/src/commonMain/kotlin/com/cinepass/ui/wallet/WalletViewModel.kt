package com.cinepass.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.Rs3CoinTransaction
import com.cinepass.data.api.models.Rs3Offer
import com.cinepass.data.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WalletUiState(
    val isLoading: Boolean = false,
    val coinBalance: Int = 0,
    val coinsEarned: Int = 0,
    val coinsSpent: Int = 0,
    val transactions: List<Rs3CoinTransaction> = emptyList(),
    val offers: List<Rs3Offer> = emptyList(),
    val error: String? = null,
    val minRedeemCoins: Int = 100
)

class WalletViewModel(
    private val userPrefs: UserPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState(isLoading = true))
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWallet()
    }

    fun loadWallet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = userPrefs.accessToken ?: return@launch
                val bearer = "Bearer $token"

                val walletResponse = ApiClient.rs3Api.getWallet(token = bearer)
                val txResponse = ApiClient.rs3Api.getTransactions(token = bearer, limit = 50)

                val walletBody = walletResponse.body()
                val transactions = txResponse.body().orEmpty()

                val coins = walletBody?.coins ?: 0
                val minRedeem = walletBody?.config?.get("min_redeem_coins")?.toIntOrNull() ?: 100
                val coinsEarned = transactions.filter { it.coins > 0 }.sumOf { it.coins }
                val coinsSpent = transactions.filter { it.coins < 0 }.sumOf { -it.coins }

                val offersResponse = ApiClient.rs3Api.getOffers(token = bearer, page = "wallet")
                val offers = offersResponse.body()?.data.orEmpty()

                userPrefs.coins = coins
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    coinBalance = coins,
                    coinsEarned = coinsEarned,
                    coinsSpent = coinsSpent,
                    transactions = transactions,
                    offers = offers,
                    minRedeemCoins = minRedeem,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load wallet"
                )
            }
        }
    }

    fun redeemCoins() {
        // To be implemented with offer redemption flow
    }
}
