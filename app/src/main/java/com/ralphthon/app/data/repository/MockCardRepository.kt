package com.ralphthon.app.data.repository

import com.ralphthon.app.data.mock.MockDataGenerator
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.repository.CardRepository
import javax.inject.Inject

class MockCardRepository @Inject constructor() : CardRepository {

    override suspend fun getCardsByCustomerId(customerId: Long, page: Int, size: Int): Result<List<ContextCard>> {
        val allCards = MockDataGenerator.getCardsByCustomerId(customerId)
        val fromIndex = page * size
        if (fromIndex >= allCards.size) return Result.success(emptyList())
        val toIndex = minOf(fromIndex + size, allCards.size)
        return Result.success(allCards.subList(fromIndex, toIndex))
    }

    override suspend fun getCardById(id: Long): Result<ContextCard> {
        val card = MockDataGenerator.getCardById(id)
        return if (card != null) {
            Result.success(card)
        } else {
            Result.failure(DomainException.NotFoundException())
        }
    }
}
