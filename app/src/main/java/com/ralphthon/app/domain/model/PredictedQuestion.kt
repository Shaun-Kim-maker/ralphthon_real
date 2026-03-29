package com.ralphthon.app.domain.model

data class PredictedQuestion(
    val id: Long,
    val question: String,
    val suggestedAnswer: String,
    val relatedKnowledge: List<String>,
    val confidence: Float
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            question: String = "",
            suggestedAnswer: String = "",
            relatedKnowledge: List<String> = emptyList(),
            confidence: Float = 0f
        ): PredictedQuestion {
            if (question.isBlank()) throw IllegalArgumentException("Question must not be blank")
            val resolvedAnswer = if (suggestedAnswer.isBlank()) "(답변 준비 중)" else suggestedAnswer
            val resolvedConfidence = confidence.coerceIn(0f, 1f)
            return PredictedQuestion(
                id = id,
                question = question,
                suggestedAnswer = resolvedAnswer,
                relatedKnowledge = relatedKnowledge,
                confidence = resolvedConfidence
            )
        }
    }
}
