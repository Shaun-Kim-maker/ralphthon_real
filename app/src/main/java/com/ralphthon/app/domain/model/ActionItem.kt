package com.ralphthon.app.domain.model

data class ActionItem(
    val id: Long,
    val description: String,
    val assignee: String,
    val dueDate: String?,
    val status: ActionItemStatus
)
