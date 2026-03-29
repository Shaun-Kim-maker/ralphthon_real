package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class ErrorDto(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: String?
)
