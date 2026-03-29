package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.KnowledgeDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KnowledgeMapperTest {

    private fun makeDto(
        id: Long = 1L,
        title: String = "테스트 제목",
        content: String = "테스트 내용",
        category: String = "기술",
        relevanceScore: Float = 0.9f
    ) = KnowledgeDto(id, title, content, category, relevanceScore)

    @Test
    fun should_mapId_when_toDomain() {
        val dto = makeDto(id = 42L)
        val domain = KnowledgeMapper.toDomain(dto)
        assertEquals(42L, domain.id)
    }

    @Test
    fun should_mapTitle_when_toDomain() {
        val dto = makeDto(title = "영업 전략")
        val domain = KnowledgeMapper.toDomain(dto)
        assertEquals("영업 전략", domain.title)
    }

    @Test
    fun should_mapContent_when_toDomain() {
        val dto = makeDto(content = "상세 내용입니다")
        val domain = KnowledgeMapper.toDomain(dto)
        assertEquals("상세 내용입니다", domain.content)
    }

    @Test
    fun should_defaultContent_when_blank() {
        val dto = makeDto(content = "")
        val domain = KnowledgeMapper.toDomain(dto)
        assertEquals("(내용 없음)", domain.content)
    }

    @Test
    fun should_mapCategory_when_toDomain() {
        val dto = makeDto(category = "영업")
        val domain = KnowledgeMapper.toDomain(dto)
        assertEquals("영업", domain.category)
    }

    @Test
    fun should_mapRelevanceScore_when_toDomain() {
        val dto = makeDto(relevanceScore = 0.75f)
        val domain = KnowledgeMapper.toDomain(dto)
        assertEquals(0.75f, domain.relevanceScore)
    }

    @Test
    fun should_mapList_when_toDomainList() {
        val dtos = listOf(makeDto(id = 1L), makeDto(id = 2L))
        val domains = KnowledgeMapper.toDomainList(dtos)
        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
    }

    @Test
    fun should_preserveFields_when_roundTrip() {
        val original = makeDto(
            id = 7L,
            title = "라운드트립 제목",
            content = "라운드트립 내용",
            category = "기획",
            relevanceScore = 0.88f
        )
        val domain = KnowledgeMapper.toDomain(original)
        val result = KnowledgeMapper.toDto(domain)
        assertEquals(original.id, result.id)
        assertEquals(original.title, result.title)
        assertEquals(original.content, result.content)
        assertEquals(original.category, result.category)
        assertEquals(original.relevanceScore, result.relevanceScore)
    }
}
