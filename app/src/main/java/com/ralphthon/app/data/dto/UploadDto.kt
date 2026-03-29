package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class UploadRequestDto(
    @SerializedName("customer_id") val customerId: Long,
    @SerializedName("conversation_type") val conversationType: String,
    @SerializedName("title") val title: String
)

data class UploadResponseDto(
    @SerializedName("conversation_id") val conversationId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)
