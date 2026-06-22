package com.cinepass.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.Rs3FeedPost
import com.cinepass.data.api.models.Rs3Offer
import com.cinepass.data.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val refreshing: Boolean = false,
    val feedPosts: List<Rs3FeedPost> = emptyList(),
    val offers: List<Rs3Offer> = emptyList(),
    val error: String? = null,
    val userCoins: Int = 0
)

class HomeViewModel(
    private val userPrefs: UserPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = userPrefs.accessToken ?: return@launch
                
                // Load feed
                val feedResponse = ApiClient.rs3Api.getFeed(page = 1, limit = 20)
                val feedPosts = feedResponse.body()?.data ?: emptyList()
                
                // Load offers for home page
                val offersResponse = ApiClient.rs3Api.getOffers(token = "Bearer $token", page = "home")
                val offers = offersResponse.body()?.data ?: emptyList()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    feedPosts = feedPosts,
                    offers = offers,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load feed"
                )
            }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(refreshing = true)
            try {
                val feedResponse = ApiClient.rs3Api.getFeed(page = 1, limit = 20)
                val feedPosts = feedResponse.body()?.data ?: emptyList()
                
                _uiState.value = _uiState.value.copy(
                    refreshing = false,
                    feedPosts = feedPosts,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    refreshing = false,
                    error = e.message
                )
            }
        }
    }

    fun claimOffer(offerId: Int) {
        viewModelScope.launch {
            try {
                val token = userPrefs.accessToken ?: return@launch
                ApiClient.rs3Api.claimOffer(
                    token = "Bearer $token",
                    offerId = offerId
                )
                loadHomeData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to claim offer: ${e.message}"
                )
            }
        }
    }
}
