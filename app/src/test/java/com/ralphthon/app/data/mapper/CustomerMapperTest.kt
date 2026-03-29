package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.CustomerDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CustomerMapperTest {

    private fun makeDto(
        id: Long = 1L,
        companyName: String = "삼성전자",
        contactName: String? = "홍길동",
        industry: String = "전자",
        lastInteractionDate: String = "2026-03-01",
        totalConversations: Int = 5,
        summary: String? = "요약"
    ) = CustomerDto(id, companyName, contactName, industry, lastInteractionDate, totalConversations, summary)

    @Test
    fun should_mapId_when_toDomain() {
        val dto = makeDto(id = 42L)
        val domain = CustomerMapper.toDomain(dto)
        assertEquals(42L, domain.id)
    }

    @Test
    fun should_mapCompanyName_when_toDomain() {
        val dto = makeDto(companyName = "LG전자")
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("LG전자", domain.companyName)
    }

    @Test
    fun should_mapContactName_when_toDomain() {
        val dto = makeDto(contactName = "김철수")
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("김철수", domain.contactName)
    }

    @Test
    fun should_mapNullContactName_when_null() {
        val dto = makeDto(contactName = null)
        val domain = CustomerMapper.toDomain(dto)
        assertNull(domain.contactName)
    }

    @Test
    fun should_mapIndustry_when_toDomain() {
        val dto = makeDto(industry = "IT서비스")
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("IT서비스", domain.industry)
    }

    @Test
    fun should_mapLastInteractionDate_when_toDomain() {
        val dto = makeDto(lastInteractionDate = "2026-01-15")
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("2026-01-15", domain.lastInteractionDate)
    }

    @Test
    fun should_mapTotalConversations_when_toDomain() {
        val dto = makeDto(totalConversations = 10)
        val domain = CustomerMapper.toDomain(dto)
        assertEquals(10, domain.totalConversations)
    }

    @Test
    fun should_mapSummary_when_toDomain() {
        val dto = makeDto(summary = "고객 요약 내용")
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("고객 요약 내용", domain.summary)
    }

    @Test
    fun should_mapNullSummary_when_null() {
        val dto = makeDto(summary = null)
        val domain = CustomerMapper.toDomain(dto)
        assertNull(domain.summary)
    }

    @Test
    fun should_defaultCompanyName_when_blank() {
        val dto = makeDto(companyName = "")
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("회사 미등록", domain.companyName)
    }

    @Test
    fun should_mapList_when_toDomainList() {
        val dtos = listOf(makeDto(id = 1L), makeDto(id = 2L))
        val domains = CustomerMapper.toDomainList(dtos)
        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
    }

    @Test
    fun should_returnEmptyList_when_emptyInput() {
        val domains = CustomerMapper.toDomainList(emptyList())
        assertEquals(0, domains.size)
    }

    @Test
    fun should_mapToDto_when_fromDomain() {
        val dto = makeDto(id = 99L, companyName = "현대자동차")
        val domain = CustomerMapper.toDomain(dto)
        val resultDto = CustomerMapper.toDto(domain)
        assertEquals(99L, resultDto.id)
        assertEquals("현대자동차", resultDto.companyName)
    }

    @Test
    fun should_handleSpecialChars_when_companyHasSpecial() {
        val dto = makeDto(companyName = "삼성전자(주)")
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("삼성전자(주)", domain.companyName)
    }

    @Test
    fun should_preserveAllFields_when_roundTrip() {
        val original = makeDto(
            id = 7L,
            companyName = "SK하이닉스",
            contactName = "박지성",
            industry = "반도체",
            lastInteractionDate = "2026-02-28",
            totalConversations = 3,
            summary = "반도체 구매 논의"
        )
        val domain = CustomerMapper.toDomain(original)
        val result = CustomerMapper.toDto(domain)
        assertEquals(original.id, result.id)
        assertEquals(original.companyName, result.companyName)
        assertEquals(original.contactName, result.contactName)
        assertEquals(original.industry, result.industry)
        assertEquals(original.lastInteractionDate, result.lastInteractionDate)
        assertEquals(original.totalConversations, result.totalConversations)
        assertEquals(original.summary, result.summary)
    }
}
