package com.ralphthon.app.domain.model

data class PriceCommitment(
    val id: Long,
    val amount: Double,
    val currency: String,
    val condition: String,
    val mentionedAt: String
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            amount: Double = 0.0,
            currency: String = "",
            condition: String = "",
            mentionedAt: String = ""
        ): PriceCommitment {
            if (amount < 0.0) throw IllegalArgumentException("Amount must not be negative")
            val resolvedCurrency = if (currency.isBlank()) "KRW" else currency.trim().uppercase()
            val resolvedCondition = if (condition.isBlank()) "(조건 없음)" else condition
            return PriceCommitment(
                id = id,
                amount = amount,
                currency = resolvedCurrency,
                condition = resolvedCondition,
                mentionedAt = mentionedAt
            )
        }
    }
}
