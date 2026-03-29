package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.UploadResponseDto
import com.ralphthon.app.domain.model.DomainException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class UploadApiClient @Inject constructor(
    private val apiService: UploadApiService
) {
    suspend fun uploadRecording(
        customerId: Long,
        conversationType: String,
        title: String,
        file: File
    ): Result<UploadResponseDto> {
        return try {
            val customerIdBody = customerId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val typeBody = conversationType.toRequestBody("text/plain".toMediaTypeOrNull())
            val titleBody = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val fileBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, fileBody)

            val response = apiService.uploadRecording(customerIdBody, typeBody, titleBody, filePart)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(DomainException.UnknownException("응답 본문이 비어있습니다"))
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
        413 -> DomainException.NetworkException("파일 크기가 너무 큽니다")
        in 500..599 -> DomainException.ServerException(code)
        else -> DomainException.UnknownException()
    }
}
