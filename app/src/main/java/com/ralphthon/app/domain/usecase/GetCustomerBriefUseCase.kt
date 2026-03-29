package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.CardRepository
import com.ralphthon.app.domain.repository.CustomerRepository
import javax.inject.Inject

data class CustomerBrief(
    val customer: Customer,
    val lastConversationSummary: String,
    val lastCustomerMeetingSummary: String?,
    val lastInternalMeetingSummary: String?,
    val predictedQuestions: List<PredictedQuestion>,
    val priceHistory: List<PriceCommitment>,
    val openActionItemsCount: Int,
    val recentActionItems: List<ActionItem>,
    val overallSentiment: Sentiment,
    val totalCards: Int
)

class GetCustomerBriefUseCase @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val cardRepository: CardRepository
) {
    suspend operator fun invoke(customerId: Long): Result<CustomerBrief> {
        val customerResult = customerRepository.getCustomerById(customerId)
        val customer = customerResult.getOrElse { return Result.failure(it) }

        val cardsResult = cardRepository.getCardsByCustomerId(customerId, 0, 100)
        val cards = cardsResult.getOrElse { return Result.failure(it) }

        val sortedCards = cards.sortedByDescending { it.date }

        val lastCustomerMeeting = sortedCards.firstOrNull { it.conversationType == ConversationType.CUSTOMER_MEETING }
        val lastInternalMeeting = sortedCards.firstOrNull { it.conversationType == ConversationType.INTERNAL_MEETING }

        val lastConversationSummary = buildIntegratedSummary(lastCustomerMeeting, lastInternalMeeting)

        val allPredictedQuestions = sortedCards
            .flatMap { it.predictedQuestions }
            .distinctBy { it.question }
            .sortedByDescending { it.confidence }
            .take(5)

        val allPriceCommitments = sortedCards
            .flatMap { it.priceCommitments }
            .sortedByDescending { it.mentionedAt }

        val allActionItems = sortedCards.flatMap { it.actionItems }
        val openCount = allActionItems.count { it.status == ActionItemStatus.OPEN }
        val recentActions = allActionItems
            .filter { it.status == ActionItemStatus.OPEN }
            .take(5)

        val overallSentiment = calculateOverallSentiment(sortedCards)

        return Result.success(
            CustomerBrief(
                customer = customer,
                lastConversationSummary = lastConversationSummary,
                lastCustomerMeetingSummary = lastCustomerMeeting?.summary,
                lastInternalMeetingSummary = lastInternalMeeting?.summary,
                predictedQuestions = allPredictedQuestions,
                priceHistory = allPriceCommitments,
                openActionItemsCount = openCount,
                recentActionItems = recentActions,
                overallSentiment = overallSentiment,
                totalCards = cards.size
            )
        )
    }

    private fun buildIntegratedSummary(
        customerMeeting: ContextCard?,
        internalMeeting: ContextCard?
    ): String {
        val parts = mutableListOf<String>()
        customerMeeting?.let { parts.add("[고객 미팅] ${it.summary}") }
        internalMeeting?.let { parts.add("[사내 회의] ${it.summary}") }
        return if (parts.isEmpty()) "대화 기록이 없습니다" else parts.joinToString("\n")
    }

    private fun calculateOverallSentiment(cards: List<ContextCard>): Sentiment {
        if (cards.isEmpty()) return Sentiment.NEUTRAL
        val avgScore = cards.map { it.sentimentScore }.average().toFloat()
        return when {
            avgScore >= 0.6f -> Sentiment.POSITIVE
            avgScore <= 0.4f -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
    }
}
