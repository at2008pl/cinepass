package com.cinepass.utils

import platform.Foundation.NSProcessInfo

/**
 * iOS Simulator can reach the Mac host backend at 127.0.0.1.
 * Physical devices must use your machine's LAN IP (e.g. http://192.168.x.x:8055/v1/).
 */
actual fun getApiBaseUrl(): String {
    val isSimulator = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null
    return if (isSimulator) {
        "http://127.0.0.1:8055/v1/"
    } else {
        "http://117.198.99.60:8055/v1/"
    }
}
