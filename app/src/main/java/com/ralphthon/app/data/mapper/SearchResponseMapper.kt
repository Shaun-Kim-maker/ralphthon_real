package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.SearchResponseDto
import com.ralphthon.app.data.dto.SearchResultDto
import com.ralphthon.app.domain.model.SearchResult

object SearchResponseMapper {
    fun toDomainList(dto: SearchResponseDto): List<SearchResult> = dto.results.map { toDomain(it) }

    fun toDomain(dto: SearchResultDto): SearchResult = SearchResult(
        id = dto.id,
        type = dto.type,
        title = dto.title,
        snippet = dto.snippet,
        highlightRanges = dto.highlightRanges.map { IntRange(it[0], it[1]) },
        sourceId = dto.sourceId,
        relevanceScore = dto.relevanceScore
    )
}
