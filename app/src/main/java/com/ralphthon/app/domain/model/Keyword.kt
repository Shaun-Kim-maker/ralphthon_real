package com.ralphthon.app.domain.model

data class Keyword(
    val text: String,
    val category: KeywordCategory,
    val frequency: Int
)
