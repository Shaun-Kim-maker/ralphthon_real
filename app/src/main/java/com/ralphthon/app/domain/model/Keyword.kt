package com.ralphthon.app.domain.model

data class Keyword(
    val text: String,
    val category: KeywordCategory,
    val frequency: Int
) {
    companion object {
        fun withDefaults(
            term: String,
            category: KeywordCategory = KeywordCategory.GENERAL,
            frequency: Int = 1
        ): Keyword {
            if (term.isBlank()) throw IllegalArgumentException("Keyword term must not be blank")
            return Keyword(text = term, category = category, frequency = frequency)
        }
    }
}
