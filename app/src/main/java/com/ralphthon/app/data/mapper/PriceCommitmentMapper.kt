package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.PriceCommitmentDto
import com.ralphthon.app.domain.model.PriceCommitment

object PriceCommitmentMapper {
    fun toDomain(dto: PriceCommitmentDto): PriceCommitment {
        return PriceCommitment(
            id = dto.id,
            amount = dto.amount,
            currency = dto.currency.trim().uppercase(),
            condition = if (dto.condition.isBlank()) "(조건 없음)" else dto.condition,
            mentionedAt = dto.mentionedAt
        )
    }

    fun toDomainList(dtos: List<PriceCommitmentDto>): List<PriceCommitment> = dtos.map { toDomain(it) }

    fun toDto(domain: PriceCommitment): PriceCommitmentDto {
        return PriceCommitmentDto(
            id = domain.id,
            amount = domain.amount,
            currency = domain.currency,
            condition = domain.condition,
            mentionedAt = domain.mentionedAt
        )
    }
}
