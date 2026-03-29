package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.KnowledgeDto
import com.ralphthon.app.domain.model.DomainException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class KnowledgeApiClient @Inject constructor(
    private val apiService: KnowledgeApiService
) {
    suspend fun getKnowledgeArticles(cardId: Long): Result<List<KnowledgeDto>> {
        return try {
            val response = apiService.getKnowledgeArticles(cardId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(mapHttpError(response.code()))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(DomainException.TimeoutException(cause = e))
        } catch (e: IOException) {
            Result.failure(DomainException.NetworkException(cause = e))
        } catch (e: Exception) {
            Result.failure(DomainException.UnknownException(cause = e))
        }
    }

    suspend fun searchKnowledge(query: String): Result<List<KnowledgeDto>> {
        return try {
            val response = apiService.searchKnowledge(query)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(mapHttpError(response.code()))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(DomainException.TimeoutException(cause = e))
        } catch (e: IOException) {
            Result.failure(DomainException.NetworkException(cause = e))
        } catch (e: Exception) {
            Result.failure(DomainException.UnknownException(cause = e))
        }
    }

    private fun mapHttpError(code: Int): DomainException = when (code) {
        400 -> DomainException.NetworkException("잘못된 요청입니다")
        401 -> DomainException.UnauthorizedException()
        403 -> DomainException.UnauthorizedException("접근 권한이 없습니다")
        404 -> DomainException.NotFoundException()
        in 500..599 -> DomainException.ServerException(code)
        else -> DomainException.UnknownException()
    }
}
