package com.ralphthon.app.domain.usecase

import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: com.ralphthon.app.data.repository.FavoritesRepository
) {
    suspend operator fun invoke(customerId: Long): Result<Boolean> {
        return try {
            val isFavorite = favoritesRepository.toggleFavorite(customerId)
            Result.success(isFavorite)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
