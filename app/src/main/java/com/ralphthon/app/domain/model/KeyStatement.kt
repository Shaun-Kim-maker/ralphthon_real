package com.ralphthon.app.domain.model

data class KeyStatement(
    val id: Long,
    val speaker: String,
    val text: String,
    val timestamp: String,
    val sentiment: Sentiment,
    val isImportant: Boolean
)
