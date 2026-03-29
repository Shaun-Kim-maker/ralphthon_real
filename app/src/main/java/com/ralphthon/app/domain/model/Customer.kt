package com.ralphthon.app.domain.model

data class Customer(
    val id: Long,
    val companyName: String,
    val contactName: String?,
    val industry: String,
    val lastInteractionDate: String,
    val totalConversations: Int,
    val summary: String?
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            name: String = "",
            company: String = "",
            contactName: String? = null,
            industry: String = "",
            lastInteractionDate: String = "",
            totalConversations: Int = 0,
            summary: String? = null
        ): Customer {
            val resolvedName = when {
                name.isBlank() -> "이름 없음"
                name.length > 50 -> name.take(50)
                else -> name
            }
            val resolvedCompany = if (company.isBlank()) "회사 미등록" else company
            return Customer(
                id = id,
                companyName = resolvedCompany,
                contactName = if (resolvedName == "이름 없음") null else resolvedName,
                industry = industry,
                lastInteractionDate = lastInteractionDate,
                totalConversations = totalConversations,
                summary = summary
            )
        }
    }
}
