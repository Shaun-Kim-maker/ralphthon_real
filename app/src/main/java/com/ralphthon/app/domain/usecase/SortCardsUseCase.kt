package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ContextCard
import javax.inject.Inject

enum class SortOrder { DATE_DESC, DATE_ASC, SENTIMENT_HIGH, SENTIMENT_LOW }

class SortCardsUseCase @Inject constructor() {
    operator fun invoke(cards: List<ContextCard>, sortOrder: SortOrder): List<ContextCard> {
        return when (sortOrder) {
            SortOrder.DATE_DESC -> cards.sortedByDescending { it.date }
            SortOrder.DATE_ASC -> cards.sortedBy { it.date }
            SortOrder.SENTIMENT_HIGH -> cards.sortedByDescending { it.sentimentScore }
            SortOrder.SENTIMENT_LOW -> cards.sortedBy { it.sentimentScore }
        }
    }
}
