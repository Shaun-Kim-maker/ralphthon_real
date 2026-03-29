package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.repository.CardRepository
import javax.inject.Inject

data class PriceHistoryResult(
    val allCommitments: List<PriceCommitment>,
    val byCurrency: Map<String, List<PriceCommitment>>,
    val latestPrice: PriceCommitment?,
    val totalCount: Int
)

class GetPriceHistoryUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(customerId: Long): Result<PriceHistoryResult> {
        return cardRepository.getCardsByCustomerId(customerId, 0, 100).map { cards ->
            val allCommitments = cards
                .flatMap { it.priceCommitments }
                .sortedByDescending { it.mentionedAt }

            val byCurrency = allCommitments.groupBy { it.currency }
            val latestPrice = allCommitments.firstOrNull()

            PriceHistoryResult(allCommitments, byCurrency, latestPrice, allCommitments.size)
        }
    }

    suspend fun getByDateRange(customerId: Long, from: String, to: String): Result<List<PriceCommitment>> {
        return invoke(customerId).map { result ->
            result.allCommitments.filter { it.mentionedAt in from..to }
        }
    }

    suspend fun getByCurrency(customerId: Long, currency: String): Result<List<PriceCommitment>> {
        return invoke(customerId).map { result ->
            result.byCurrency[currency] ?: emptyList()
        }
    }
}
