package com.ralphthon.app.domain.model

data class Contact(
    val id: Long,
    val customerId: Long,
    val name: String,
    val role: String?,
    val email: String?,
    val phone: String?
)
