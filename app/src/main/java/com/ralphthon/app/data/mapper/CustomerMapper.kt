package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.CustomerDto
import com.ralphthon.app.domain.model.Customer

object CustomerMapper {
    fun toDomain(dto: CustomerDto): Customer {
        return Customer(
            id = dto.id,
            companyName = dto.companyName.ifBlank { "회사 미등록" },
            contactName = dto.contactName,
            industry = dto.industry,
            lastInteractionDate = dto.lastInteractionDate,
            totalConversations = dto.totalConversations,
            summary = dto.summary
        )
    }

    fun toDomainList(dtos: List<CustomerDto>): List<Customer> = dtos.map { toDomain(it) }

    fun toDto(domain: Customer): CustomerDto {
        return CustomerDto(
            id = domain.id,
            companyName = domain.companyName,
            contactName = domain.contactName,
            industry = domain.industry,
            lastInteractionDate = domain.lastInteractionDate,
            totalConversations = domain.totalConversations,
            summary = domain.summary
        )
    }
}
