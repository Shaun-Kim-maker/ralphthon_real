package com.ralphthon.app.domain.model

data class PriceCommitment(
    val id: Long,
    val amount: Double,
    val currency: String,
    val condition: String,
    val mentionedAt: String
)
