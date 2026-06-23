package com.cinepass.utils

/** Production fanverse API (`/v1/auth`, `/v1/app`, etc.) */
const val HOSTED_API_BASE_URL = "http://117.198.99.60:6055/v1/"

const val HOSTED_WEB_BASE_URL = "http://117.198.99.60:6055"

/**
 * API base URL for the fanverse backend (`/v1/...` routes).
 */
expect fun getApiBaseUrl(): String
