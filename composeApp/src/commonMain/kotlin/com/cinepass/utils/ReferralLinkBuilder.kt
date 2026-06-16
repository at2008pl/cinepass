package com.cinepass.utils

import android.net.Uri

object ReferralLinkBuilder {

    fun buildShareLink(referralCode: String): String {
        val normalizedCode = referralCode.trim().uppercase()

        return Uri.parse("${Constants.PUBLIC_WEB_BASE_URL}/register")
            .buildUpon()
            .appendQueryParameter("ref", normalizedCode)
            .appendQueryParameter("utm_source", "cinepass")
            .appendQueryParameter("utm_medium", "referral")
            .build()
            .toString()
    }
}
