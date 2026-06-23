package com.cinepass.utils

/**
 * API base URL for the fanverse backend (`/v1/...` routes).
 * Override per platform for local development (simulator/emulator).
 */
expect fun getApiBaseUrl(): String
