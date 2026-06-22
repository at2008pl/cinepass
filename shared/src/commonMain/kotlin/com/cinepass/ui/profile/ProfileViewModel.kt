package com.cinepass.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.Rs3Profile
import com.cinepass.data.api.models.Rs3Redemption
import com.cinepass.data.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: Rs3Profile? = null,
    val error: String? = null,
    val isEditing: Boolean = false,
    val updateMessage: String? = null,
    val redemptions: List<Rs3Redemption> = emptyList(),
    /** Live CMS content from the "legal" section: key → value (HTML string) */
    val cmsContent: Map<String, String> = emptyMap()
)

class ProfileViewModel(
    private val userPrefs: UserPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
        loadLegalContent()
    }

    fun loadLegalContent() {
        viewModelScope.launch {
            try {
                val response = ApiClient.rs3Api.getCmsSection("legal")
                val items = response.body()?.data ?: return@launch
                val map = items.associate { it.contentKey to it.value }
                _uiState.value = _uiState.value.copy(cmsContent = map)
            } catch (_: Exception) { }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = userPrefs.accessToken ?: return@launch
                val response = ApiClient.rs3Api.getProfile(
                    token = "Bearer $token"
                )
                val profile = response.body()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    profile = profile,
                    error = if (profile == null) "Failed to load profile" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load profile"
                )
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            try {
                val token = userPrefs.accessToken ?: return@launch
                val response = ApiClient.rs3Api.updateProfile(
                    token = "Bearer $token",
                    request = com.cinepass.data.api.models.UpdateProfileRequest(
                        name = name,
                        email = email
                    )
                )
                val updatedProfile = response.body()
                if (updatedProfile != null) {
                    _uiState.value = _uiState.value.copy(
                        profile = updatedProfile,
                        isEditing = false,
                        updateMessage = "Profile updated successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update profile"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unable to update profile"
                )
            }
        }
    }

    fun toggleEditing() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun loadRedemptions() {
        viewModelScope.launch {
            try {
                val token = userPrefs.accessToken ?: return@launch
                val response = ApiClient.rs3Api.getMyRedemptions("Bearer $token")
                val redemptions = response.body().orEmpty()
                _uiState.value = _uiState.value.copy(redemptions = redemptions)
            } catch (_: Exception) { }
        }
    }
}
