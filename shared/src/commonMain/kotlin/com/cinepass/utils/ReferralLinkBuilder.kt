package com.cinepass.utils

object ReferralLinkBuilder {

    fun buildShareLink(referralCode: String): String {
        val normalizedCode = referralCode.trim().uppercase()
        return buildString {
            append(Constants.PUBLIC_WEB_BASE_URL)
            append("/register?ref=")
            append(normalizedCode)
            append("&utm_source=cinepass&utm_medium=referral")
        }
    }
}
