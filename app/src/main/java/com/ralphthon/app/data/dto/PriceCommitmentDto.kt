package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class PriceCommitmentDto(
    @SerializedName("id") val id: Long,
    @SerializedName("amount") val amount: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("condition") val condition: String,
    @SerializedName("mentioned_at") val mentionedAt: String
)
