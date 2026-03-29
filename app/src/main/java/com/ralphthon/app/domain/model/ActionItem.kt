package com.ralphthon.app.domain.model

data class ActionItem(
    val id: Long,
    val description: String,
    val assignee: String,
    val dueDate: String?,
    val status: ActionItemStatus
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            description: String = "",
            assignee: String = "",
            dueDate: String? = null,
            status: ActionItemStatus = ActionItemStatus.OPEN
        ): ActionItem {
            val trimmedDescription = description.trim()
            if (trimmedDescription.isBlank()) throw IllegalArgumentException("Description must not be blank")
            val resolvedDescription = if (trimmedDescription.length > 200) trimmedDescription.take(200) else trimmedDescription
            val resolvedAssignee = if (assignee.isBlank()) "(미지정)" else assignee
            return ActionItem(
                id = id,
                description = resolvedDescription,
                assignee = resolvedAssignee,
                dueDate = dueDate,
                status = status
            )
        }
    }
}
