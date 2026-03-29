package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class CustomerDto(
    @SerializedName("id") val id: Long,
    @SerializedName("company_name") val companyName: String,
    @SerializedName("contact_name") val contactName: String?,
    @SerializedName("industry") val industry: String,
    @SerializedName("last_interaction_date") val lastInteractionDate: String,
    @SerializedName("total_conversations") val totalConversations: Int,
    @SerializedName("summary") val summary: String?
)
