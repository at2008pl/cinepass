package com.cinepass.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinepass.data.api.ApiClient
import com.cinepass.data.api.models.Rs3ReferralEntry
import com.cinepass.data.prefs.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TreeNode(
    val id: Int,
    val name: String,
    val level: Int,
    val coinsAwarded: Int,
    val joinDate: String,
    val children: List<TreeNode> = emptyList()
)

data class ReferralTreeUiState(
    val isLoading: Boolean = true,
    val rootNode: TreeNode? = null,
    val totalNodes: Int = 0,
    val totalEarnings: Int = 0,
    val maxLevel: Int = 0,
    val levelStats: Map<Int, Int> = emptyMap(), // level -> count
    val error: String? = null
)

class ReferralTreeViewModel(
    private val userPrefs: UserPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReferralTreeUiState())
    val uiState: StateFlow<ReferralTreeUiState> = _uiState.asStateFlow()

    init {
        loadReferralTree()
    }

    fun loadReferralTree() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val token = userPrefs.accessToken ?: throw Exception("Not logged in")
                val bearer = "Bearer $token"

                // Use locally stored user info for the root node — no extra API call needed
                val userId = userPrefs.userId?.toIntOrNull() ?: throw Exception("User ID not found")
                val userName = userPrefs.userName ?: "You"

                val treeResponse = ApiClient.rs3Api.getReferralTree(bearer)
                val treeData = treeResponse.body()

                if (treeData != null) {
                    val referrals = treeData.referrals
                    val levelStats = referrals.groupBy { it.level }.mapValues { it.value.size }
                    val totalEarnings = referrals.sumOf { it.coinsAwarded }

                    val root = TreeNode(
                        id = userId,
                        name = userName,
                        level = 0,
                        coinsAwarded = 0,
                        joinDate = "",
                        children = buildTree(referrals, userId)
                    )

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        rootNode = root,
                        totalNodes = treeData.totalChainMembers + 1,
                        totalEarnings = totalEarnings,
                        maxLevel = treeData.chainDepth,
                        levelStats = levelStats,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Server error (${treeResponse.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load tree"
                )
            }
        }
    }

    private fun buildTree(referrals: List<Rs3ReferralEntry>, parentId: Int): List<TreeNode> {
        return referrals.filter { it.parentId == parentId }.map { entry ->
            TreeNode(
                id = entry.id,
                name = entry.name ?: "Member",
                level = entry.level,
                coinsAwarded = entry.coinsAwarded,
                joinDate = entry.joinDate ?: "",
                children = buildTree(referrals, entry.id)
            )
        }
    }
}
