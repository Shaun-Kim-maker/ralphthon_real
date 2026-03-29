package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.KnowledgeRepository
import javax.inject.Inject

data class CardDetailResult(
    val card: ContextCard,
    val additionalKnowledge: List<KnowledgeArticle>
)

class GetCardDetailUseCase @Inject constructor(
    private val cardRepository: CardRepository,
    private val knowledgeRepository: KnowledgeRepository
) {
    suspend operator fun invoke(cardId: Long): Result<CardDetailResult> {
        val cardResult = cardRepository.getCardById(cardId)
        return cardResult.mapCatching { card ->
            val knowledge = knowledgeRepository.getKnowledgeArticles(cardId)
                .getOrDefault(emptyList())
            CardDetailResult(card, knowledge)
        }
    }

    suspend fun getCardOnly(cardId: Long): Result<ContextCard> {
        return cardRepository.getCardById(cardId)
    }
}
