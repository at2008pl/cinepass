package com.cinepass.data.preferences

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ReferralPreferences(private val settings: Settings = Settings()) {
    
    constructor(context: Any?) : this(Settings())
    
    companion object {
        private const val PENDING_REFERRAL_CODE = "pending_referral_code"
    }
    
    suspend fun savePendingReferralCode(code: String) {
        settings.putString(PENDING_REFERRAL_CODE, code)
    }
    
    fun getPendingReferralCode(): Flow<String?> = flow {
        emit(settings.getStringOrNull(PENDING_REFERRAL_CODE))
    }
    
    suspend fun clearPendingReferralCode() {
        settings.remove(PENDING_REFERRAL_CODE)
    }
}
