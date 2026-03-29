package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.repository.CardRepository
import javax.inject.Inject

class GetPredictedQuestionsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(customerId: Long, limit: Int = 10): Result<List<PredictedQuestion>> {
        return cardRepository.getCardsByCustomerId(customerId, 0, 100).map { cards ->
            cards.flatMap { it.predictedQuestions }
                .distinctBy { it.question }
                .sortedByDescending { it.confidence }
                .take(limit)
        }
    }

    suspend fun getHighConfidence(customerId: Long, minConfidence: Float = 0.7f): Result<List<PredictedQuestion>> {
        return invoke(customerId, Int.MAX_VALUE).map { questions ->
            questions.filter { it.confidence >= minConfidence }
        }
    }

    suspend fun getWithKnowledge(customerId: Long): Result<List<PredictedQuestion>> {
        return invoke(customerId, Int.MAX_VALUE).map { questions ->
            questions.filter { it.relatedKnowledge.isNotEmpty() }
        }
    }
}
