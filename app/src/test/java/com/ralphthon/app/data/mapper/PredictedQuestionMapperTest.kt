package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.PredictedQuestionDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PredictedQuestionMapperTest {

    private fun makeDto(
        id: Long = 1L,
        question: String = "납기일이 언제인가요?",
        suggestedAnswer: String = "2주 내 납품 가능합니다.",
        relatedKnowledge: List<String> = listOf("납기", "배송"),
        confidence: Float = 0.85f
    ) = PredictedQuestionDto(id, question, suggestedAnswer, relatedKnowledge, confidence)

    @Test
    fun should_mapId_when_toDomain() {
        val dto = makeDto(id = 99L)
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals(99L, domain.id)
    }

    @Test
    fun should_mapQuestion_when_toDomain() {
        val dto = makeDto(question = "할인이 가능한가요?")
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals("할인이 가능한가요?", domain.question)
    }

    @Test
    fun should_mapSuggestedAnswer_when_toDomain() {
        val dto = makeDto(suggestedAnswer = "최대 10% 할인 가능합니다.")
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals("최대 10% 할인 가능합니다.", domain.suggestedAnswer)
    }

    @Test
    fun should_defaultAnswer_when_blank() {
        val dto = makeDto(suggestedAnswer = "")
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals("(답변 준비 중)", domain.suggestedAnswer)
    }

    @Test
    fun should_mapRelatedKnowledge_when_toDomain() {
        val dto = makeDto(relatedKnowledge = listOf("가격", "할인", "정책"))
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals(listOf("가격", "할인", "정책"), domain.relatedKnowledge)
    }

    @Test
    fun should_handleEmptyKnowledge_when_empty() {
        val dto = makeDto(relatedKnowledge = emptyList())
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals(emptyList<String>(), domain.relatedKnowledge)
    }

    @Test
    fun should_mapConfidence_when_toDomain() {
        val dto = makeDto(confidence = 0.75f)
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals(0.75f, domain.confidence)
    }

    @Test
    fun should_clampConfidence_when_above1() {
        val dto = makeDto(confidence = 1.5f)
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals(1.0f, domain.confidence)
    }

    @Test
    fun should_clampConfidence_when_below0() {
        val dto = makeDto(confidence = -0.5f)
        val domain = PredictedQuestionMapper.toDomain(dto)
        assertEquals(0.0f, domain.confidence)
    }

    @Test
    fun should_mapList_when_toDomainList() {
        val dtos = listOf(makeDto(id = 1L), makeDto(id = 2L))
        val domains = PredictedQuestionMapper.toDomainList(dtos)
        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
    }
}
