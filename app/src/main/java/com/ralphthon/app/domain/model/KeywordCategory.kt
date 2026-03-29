package com.ralphthon.app.domain.model

enum class KeywordCategory {
    PRODUCT,
    PRICE,
    COMPETITOR,
    TECHNICAL,
    BUSINESS,
    GENERAL;

    companion object {
        fun fromString(value: String): KeywordCategory {
            return entries.firstOrNull { it.name == value.uppercase() } ?: GENERAL
        }
    }
}
