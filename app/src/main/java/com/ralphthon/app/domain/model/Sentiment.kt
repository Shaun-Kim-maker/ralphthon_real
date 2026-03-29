package com.ralphthon.app.domain.model

enum class Sentiment {
    POSITIVE,
    NEGATIVE,
    NEUTRAL;

    companion object {
        fun fromString(value: String): Sentiment {
            return entries.firstOrNull { it.name == value.uppercase() } ?: NEUTRAL
        }
    }
}
