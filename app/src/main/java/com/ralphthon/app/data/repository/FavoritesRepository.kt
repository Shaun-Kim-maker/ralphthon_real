package com.ralphthon.app.data.repository

import javax.inject.Inject
import javax.inject.Singleton

interface FavoritesRepository {
    suspend fun toggleFavorite(customerId: Long): Boolean
    suspend fun isFavorite(customerId: Long): Boolean
    suspend fun getFavoriteIds(): Set<Long>
}

@Singleton
class FavoritesRepositoryImpl @Inject constructor() : FavoritesRepository {
    private val favorites = mutableSetOf<Long>()

    override suspend fun toggleFavorite(customerId: Long): Boolean {
        return if (favorites.contains(customerId)) {
            favorites.remove(customerId)
            false
        } else {
            favorites.add(customerId)
            true
        }
    }

    override suspend fun isFavorite(customerId: Long): Boolean = favorites.contains(customerId)
    override suspend fun getFavoriteIds(): Set<Long> = favorites.toSet()
}
