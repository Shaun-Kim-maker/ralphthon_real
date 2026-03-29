package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.SearchResponseDto
import com.ralphthon.app.data.dto.SearchResultDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SearchResponseMapperTest {

    private fun makeResultDto(
        id: Long = 1L,
        type: String = "card",
        title: String = "검색 결과",
        snippet: String = "스니펫 내용",
        highlightRanges: List<List<Int>> = listOf(listOf(0, 3)),
        sourceId: Long = 10L,
        relevanceScore: Float = 0.85f
    ) = SearchResultDto(id, type, title, snippet, highlightRanges, sourceId, relevanceScore)

    private fun makeResponseDto(
        results: List<SearchResultDto> = listOf(makeResultDto()),
        totalCount: Int = 1,
        query: String = "검색어"
    ) = SearchResponseDto(results, totalCount, query)

    @Test
    fun should_mapResults_when_toDomainList() {
        val dto = makeResponseDto(results = listOf(makeResultDto(id = 1L), makeResultDto(id = 2L)))
        val domains = SearchResponseMapper.toDomainList(dto)
        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
    }

    @Test
    fun should_returnEmpty_when_noResults() {
        val dto = makeResponseDto(results = emptyList())
        val domains = SearchResponseMapper.toDomainList(dto)
        assertEquals(0, domains.size)
    }

    @Test
    fun should_mapType_when_toDomain() {
        val dto = makeResultDto(type = "customer")
        val domain = SearchResponseMapper.toDomain(dto)
        assertEquals("customer", domain.type)
    }

    @Test
    fun should_mapTitle_when_toDomain() {
        val dto = makeResultDto(title = "삼성전자 미팅")
        val domain = SearchResponseMapper.toDomain(dto)
        assertEquals("삼성전자 미팅", domain.title)
    }

    @Test
    fun should_mapSnippet_when_toDomain() {
        val dto = makeResultDto(snippet = "주요 내용 발췌")
        val domain = SearchResponseMapper.toDomain(dto)
        assertEquals("주요 내용 발췌", domain.snippet)
    }

    @Test
    fun should_mapHighlightRanges_when_present() {
        val dto = makeResultDto(highlightRanges = listOf(listOf(0, 5), listOf(10, 15)))
        val domain = SearchResponseMapper.toDomain(dto)
        assertEquals(2, domain.highlightRanges.size)
        assertEquals(IntRange(0, 5), domain.highlightRanges[0])
        assertEquals(IntRange(10, 15), domain.highlightRanges[1])
    }

    @Test
    fun should_mapRelevanceScore_when_toDomain() {
        val dto = makeResultDto(relevanceScore = 0.65f)
        val domain = SearchResponseMapper.toDomain(dto)
        assertEquals(0.65f, domain.relevanceScore)
    }
}
