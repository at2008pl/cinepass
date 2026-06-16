package com.cinepass.ui.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.Rs3Offer
import com.cinepass.data.api.models.ClaimOfferResponse
import com.cinepass.data.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OfferDetailUiState(
    val isLoading: Boolean = true,
    val offer: Rs3Offer? = null,
    val userCoins: Int = 0,
    val canRedeem: Boolean = false,
    val isRedeeming: Boolean = false,
    val redeemResult: ClaimOfferResponse? = null,
    val error: String? = null,
    val success: String? = null
)

class OfferDetailViewModel(
    private val userPrefs: UserPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfferDetailUiState())
    val uiState: StateFlow<OfferDetailUiState> = _uiState.asStateFlow()

    fun loadOffer(offerId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = userPrefs.accessToken ?: throw Exception("No auth token")
                val bearer = "Bearer $token"

                // Try wallet page first, then referral, then home
                var foundOffer: com.cinepass.data.api.models.Rs3Offer? = null
                for (page in listOf("wallet", "referral", "home")) {
                    val resp = ApiClient.rs3Api.getOffers(token = bearer, page = page)
                    foundOffer = resp.body()?.data?.find { it.id == offerId }
                    if (foundOffer != null) break
                }

                if (foundOffer != null) {
                    val userCoins = userPrefs.coins
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        offer = foundOffer,
                        userCoins = userCoins,
                        canRedeem = !foundOffer.claimed && userCoins >= (foundOffer.coinCost ?: 0),
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Offer not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load offer"
                )
            }
        }
    }

    fun redeemOffer(offerId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRedeeming = true, error = null)
            try {
                val token = userPrefs.accessToken ?: throw Exception("No auth token")

                val response = ApiClient.rs3Api.claimOffer(
                    token = "Bearer $token",
                    offerId = offerId
                )

                val result = response.body()
                if (result != null) {
                    _uiState.value = _uiState.value.copy(
                        isRedeeming = false,
                        redeemResult = result,
                        success = "Offer claimed successfully! Check your email for ${result.rewardValue}",
                        offer = _uiState.value.offer?.copy(claimed = true)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isRedeeming = false,
                        error = "Failed to claim offer"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRedeeming = false,
                    error = e.message ?: "Unable to redeem offer"
                )
            }
        }
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(success = null)
    }
}
