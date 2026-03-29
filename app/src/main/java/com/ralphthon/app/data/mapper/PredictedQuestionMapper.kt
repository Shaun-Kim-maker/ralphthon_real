package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.PredictedQuestionDto
import com.ralphthon.app.domain.model.PredictedQuestion

object PredictedQuestionMapper {
    fun toDomain(dto: PredictedQuestionDto): PredictedQuestion {
        return PredictedQuestion(
            id = dto.id,
            question = dto.question,
            suggestedAnswer = if (dto.suggestedAnswer.isBlank()) "(답변 준비 중)" else dto.suggestedAnswer,
            relatedKnowledge = dto.relatedKnowledge,
            confidence = dto.confidence.coerceIn(0f, 1f)
        )
    }

    fun toDomainList(dtos: List<PredictedQuestionDto>): List<PredictedQuestion> = dtos.map { toDomain(it) }

    fun toDto(domain: PredictedQuestion): PredictedQuestionDto {
        return PredictedQuestionDto(
            id = domain.id,
            question = domain.question,
            suggestedAnswer = domain.suggestedAnswer,
            relatedKnowledge = domain.relatedKnowledge,
            confidence = domain.confidence
        )
    }
}
