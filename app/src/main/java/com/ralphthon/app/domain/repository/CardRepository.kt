package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.ContextCard

interface CardRepository {
    suspend fun getCardsByCustomerId(customerId: Long, page: Int = 0, size: Int = 10): Result<List<ContextCard>>
    suspend fun getCardById(id: Long): Result<ContextCard>
}
