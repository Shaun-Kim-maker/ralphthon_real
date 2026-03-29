package com.ralphthon.app.data.dto

import com.google.gson.annotations.SerializedName

data class ActionItemDto(
    @SerializedName("id") val id: Long,
    @SerializedName("description") val description: String,
    @SerializedName("assignee") val assignee: String,
    @SerializedName("due_date") val dueDate: String?,
    @SerializedName("status") val status: String
)
