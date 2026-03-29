package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.KnowledgeDto
import com.ralphthon.app.domain.model.KnowledgeArticle

object KnowledgeMapper {
    fun toDomain(dto: KnowledgeDto): KnowledgeArticle = KnowledgeArticle(
        id = dto.id,
        title = dto.title,
        content = dto.content.ifBlank { "(내용 없음)" },
        category = dto.category,
        relevanceScore = dto.relevanceScore
    )

    fun toDomainList(dtos: List<KnowledgeDto>): List<KnowledgeArticle> = dtos.map { toDomain(it) }

    fun toDto(domain: KnowledgeArticle): KnowledgeDto = KnowledgeDto(
        id = domain.id,
        title = domain.title,
        content = domain.content,
        category = domain.category,
        relevanceScore = domain.relevanceScore
    )
}
