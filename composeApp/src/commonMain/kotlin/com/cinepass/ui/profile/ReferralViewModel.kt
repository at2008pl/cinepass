package com.cinepass.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.Rs3ReferralChainItem
import com.cinepass.data.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReferralUiState(
    val isLoading: Boolean = true,
    val referralCode: String = "",
    val totalReferrals: Int = 0,
    val coinsEarned: Int = 0,
    val referralChain: List<Rs3ReferralChainItem> = emptyList(),
    val error: String? = null,
)

class ReferralViewModel(
    private val userPrefs: UserPrefs
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReferralUiState())
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = userPrefs.accessToken ?: return@launch
                
                // Get profile to get referral code
                val profileResponse = ApiClient.rs3Api.getProfile(
                    token = "Bearer $token"
                )
                val profile = profileResponse.body()
                
                if (profile != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        referralCode = profile.referralCode,
                        totalReferrals = profile.referralsCount,
                        coinsEarned = profile.coins,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load referral data"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load referrals"
                )
            }
        }
    }
}
