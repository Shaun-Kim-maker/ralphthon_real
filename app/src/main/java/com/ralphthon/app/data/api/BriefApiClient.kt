package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.SearchResponseDto
import com.ralphthon.app.domain.model.DomainException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class BriefApiClient @Inject constructor(
    private val apiService: BriefApiService
) {
    suspend fun search(query: String): Result<SearchResponseDto> {
        return try {
            val response = apiService.search(query)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.success(SearchResponseDto(emptyList(), 0, query))
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
