package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.CardDto
import com.ralphthon.app.domain.model.DomainException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class CardApiClient @Inject constructor(
    private val apiService: CardApiService
) {
    suspend fun getCardsByCustomerId(customerId: Long, page: Int = 0, size: Int = 10): Result<List<CardDto>> {
        return try {
            val response = apiService.getCardsByCustomerId(customerId, page, size)
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

    suspend fun getCardById(id: Long): Result<CardDto> {
        return try {
            val response = apiService.getCardById(id)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(DomainException.NotFoundException())
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

    private fun mapHttpError(code: Int): DomainException {
        return when (code) {
            400 -> DomainException.NetworkException("잘못된 요청입니다")
            401 -> DomainException.UnauthorizedException()
            403 -> DomainException.UnauthorizedException("접근 권한이 없습니다")
            404 -> DomainException.NotFoundException()
            in 500..599 -> DomainException.ServerException(code)
            else -> DomainException.UnknownException()
        }
    }
}
