package com.ralphthon.app.data.mapper

import com.ralphthon.app.data.dto.ActionItemDto
import com.ralphthon.app.domain.model.ActionItemStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ActionItemMapperTest {

    private fun makeDto(
        id: Long = 1L,
        description: String = "계약서 검토",
        assignee: String = "홍길동",
        dueDate: String? = "2026-04-01",
        status: String = "OPEN"
    ) = ActionItemDto(id, description, assignee, dueDate, status)

    @Test
    fun should_mapId_when_toDomain() {
        val dto = makeDto(id = 55L)
        val domain = ActionItemMapper.toDomain(dto)
        assertEquals(55L, domain.id)
    }

    @Test
    fun should_mapDescription_when_toDomain() {
        val dto = makeDto(description = "견적서 발송")
        val domain = ActionItemMapper.toDomain(dto)
        assertEquals("견적서 발송", domain.description)
    }

    @Test
    fun should_mapAssignee_when_toDomain() {
        val dto = makeDto(assignee = "김영희")
        val domain = ActionItemMapper.toDomain(dto)
        assertEquals("김영희", domain.assignee)
    }

    @Test
    fun should_defaultAssignee_when_blank() {
        val dto = makeDto(assignee = "")
        val domain = ActionItemMapper.toDomain(dto)
        assertEquals("(미지정)", domain.assignee)
    }

    @Test
    fun should_mapDueDate_when_present() {
        val dto = makeDto(dueDate = "2026-05-15")
        val domain = ActionItemMapper.toDomain(dto)
        assertEquals("2026-05-15", domain.dueDate)
    }

    @Test
    fun should_handleNullDueDate_when_null() {
        val dto = makeDto(dueDate = null)
        val domain = ActionItemMapper.toDomain(dto)
        assertNull(domain.dueDate)
    }

    @Test
    fun should_mapStatusOpen_when_toDomain() {
        val dto = makeDto(status = "OPEN")
        val domain = ActionItemMapper.toDomain(dto)
        assertEquals(ActionItemStatus.OPEN, domain.status)
    }

    @Test
    fun should_mapStatusDone_when_toDomain() {
        val dto = makeDto(status = "DONE")
        val domain = ActionItemMapper.toDomain(dto)
        assertEquals(ActionItemStatus.DONE, domain.status)
    }

    @Test
    fun should_mapList_when_toDomainList() {
        val dtos = listOf(makeDto(id = 1L), makeDto(id = 2L))
        val domains = ActionItemMapper.toDomainList(dtos)
        assertEquals(2, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(2L, domains[1].id)
    }

    @Test
    fun should_preserveFields_when_roundTrip() {
        val original = makeDto(
            id = 3L,
            description = "미팅 일정 잡기",
            assignee = "이순신",
            dueDate = "2026-04-10",
            status = "OPEN"
        )
        val domain = ActionItemMapper.toDomain(original)
        val result = ActionItemMapper.toDto(domain)
        assertEquals(original.id, result.id)
        assertEquals(original.description, result.description)
        assertEquals(original.assignee, result.assignee)
        assertEquals(original.dueDate, result.dueDate)
        assertEquals(original.status, result.status)
    }
}
