package com.cinepass.data.api

private val EMAIL_REGEX = Regex("""^[A-Za-z0-9+_.'-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

object AuthIdentifiers {
    fun normalizeLoginIdentifier(raw: String): String {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return trimmed
        if (EMAIL_REGEX.matches(trimmed)) return trimmed.lowercase()
        return normalizePhone(trimmed)
    }

    fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.length == 10 -> "+91$digits"
            digits.length == 12 && digits.startsWith("91") -> "+$digits"
            raw.trim().startsWith("+") -> raw.trim()
            else -> raw.trim()
        }
    }

    fun isValidLoginIdentifier(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return false
        if (EMAIL_REGEX.matches(trimmed)) return true
        val digits = trimmed.filter { it.isDigit() }
        return digits.length == 10 || (digits.length == 12 && digits.startsWith("91"))
    }

    fun isValidPhone(raw: String): Boolean {
        val digits = raw.filter { it.isDigit() }
        return digits.length == 10 || (digits.length == 12 && digits.startsWith("91"))
    }
}
