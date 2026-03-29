package com.ralphthon.app.domain.model

data class Customer(
    val id: Long,
    val companyName: String,
    val contactName: String?,
    val industry: String,
    val lastInteractionDate: String,
    val totalConversations: Int,
    val summary: String?
)
