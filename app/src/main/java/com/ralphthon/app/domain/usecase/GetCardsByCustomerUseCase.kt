package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.CardRepository
import javax.inject.Inject

class GetCardsByCustomerUseCase @Inject constructor(
    private val repository: CardRepository
) {
    suspend operator fun invoke(customerId: Long, page: Int = 0, size: Int = 10): Result<List<ContextCard>> {
        return repository.getCardsByCustomerId(customerId, page, size)
    }

    suspend fun getFiltered(
        customerId: Long,
        conversationType: ConversationType? = null,
        sentiment: Sentiment? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): Result<List<ContextCard>> {
        return repository.getCardsByCustomerId(customerId).map { cards ->
            cards.filter { card ->
                (conversationType == null || card.conversationType == conversationType) &&
                (sentiment == null || card.sentiment == sentiment) &&
                (dateFrom == null || card.date >= dateFrom) &&
                (dateTo == null || card.date <= dateTo)
            }
        }
    }

    suspend fun getSorted(customerId: Long, sortBy: SortBy = SortBy.DATE_DESC): Result<List<ContextCard>> {
        return repository.getCardsByCustomerId(customerId).map { cards ->
            when (sortBy) {
                SortBy.DATE_DESC -> cards.sortedByDescending { it.date }
                SortBy.DATE_ASC -> cards.sortedBy { it.date }
                SortBy.SENTIMENT_DESC -> cards.sortedByDescending { it.sentimentScore }
                SortBy.TITLE -> cards.sortedBy { it.title }
            }
        }
    }

    enum class SortBy { DATE_DESC, DATE_ASC, SENTIMENT_DESC, TITLE }
}
