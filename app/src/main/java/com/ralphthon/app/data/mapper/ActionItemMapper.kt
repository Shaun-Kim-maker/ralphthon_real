package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.ActionItemDto
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus

object ActionItemMapper {
    fun toDomain(dto: ActionItemDto): ActionItem {
        return ActionItem(
            id = dto.id,
            description = dto.description,
            assignee = if (dto.assignee.isBlank()) "(미지정)" else dto.assignee,
            dueDate = dto.dueDate,
            status = ActionItemStatus.valueOf(dto.status)
        )
    }

    fun toDomainList(dtos: List<ActionItemDto>): List<ActionItem> = dtos.map { toDomain(it) }

    fun toDto(domain: ActionItem): ActionItemDto {
        return ActionItemDto(
            id = domain.id,
            description = domain.description,
            assignee = domain.assignee,
            dueDate = domain.dueDate,
            status = domain.status.name
        )
    }
}
