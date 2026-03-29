package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.PriceCommitmentDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PriceCommitmentMapperTest {

    private fun makeDto(
        id: Long = 1L,
        amount: Double = 1000000.0,
        currency: String = "KRW",
        condition: String = "계약 조건",
        mentionedAt: String = "2026-03-01T10:00:00"
    ) = PriceCommitmentDto(id, amount, currency, condition, mentionedAt)

    @Test
    fun should_mapId_when_toDomain() {
        val dto = makeDto(id = 42L)
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals(42L, domain.id)
    }

    @Test
    fun should_mapAmount_when_toDomain() {
        val dto = makeDto(amount = 5000000.0)
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals(5000000.0, domain.amount)
    }

    @Test
    fun should_mapCurrency_when_toDomain() {
        val dto = makeDto(currency = "USD")
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals("USD", domain.currency)
    }

    @Test
    fun should_trimCurrency_when_hasSpaces() {
        val dto = makeDto(currency = " USD ")
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals("USD", domain.currency)
    }

    @Test
    fun should_upperCaseCurrency_when_lowercase() {
        val dto = makeDto(currency = "usd")
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals("USD", domain.currency)
    }

    @Test
    fun should_mapCondition_when_toDomain() {
        val dto = makeDto(condition = "3개월 할부")
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals("3개월 할부", domain.condition)
    }

    @Test
    fun should_defaultCondition_when_blank() {
        val dto = makeDto(condition = "")
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals("(조건 없음)", domain.condition)
    }

    @Test
    fun should_mapMentionedAt_when_toDomain() {
        val dto = makeDto(mentionedAt = "2026-03-15T14:30:00")
        val domain = PriceCommitmentMapper.toDomain(dto)
        assertEquals("2026-03-15T14:30:00", domain.mentionedAt)
    }

    @Test
    fun should_mapList_when_toDomainList() {
        val dtos = listOf(makeDto(id = 1L), makeDto(id = 2L))
        val domains = PriceCommitmentMapper.toDomainList(dtos)
        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
    }

    @Test
    fun should_preserveFields_when_roundTrip() {
        val original = makeDto(
            id = 7L,
            amount = 2500000.0,
            currency = "KRW",
            condition = "연간 계약",
            mentionedAt = "2026-03-20T09:00:00"
        )
        val domain = PriceCommitmentMapper.toDomain(original)
        val result = PriceCommitmentMapper.toDto(domain)
        assertEquals(original.id, result.id)
        assertEquals(original.amount, result.amount)
        assertEquals(original.currency, result.currency)
        assertEquals(original.condition, result.condition)
        assertEquals(original.mentionedAt, result.mentionedAt)
    }
}
