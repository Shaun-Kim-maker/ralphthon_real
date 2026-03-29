package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.Conversation
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.repository.UploadRepository
import javax.inject.Inject

class UploadConversationUseCase @Inject constructor(
    private val repository: UploadRepository
) {
    suspend operator fun invoke(
        customerId: Long,
        conversationType: ConversationType,
        title: String,
        filePath: String
    ): Result<Conversation> {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("제목을 입력해주세요"))
        }
        if (filePath.isBlank()) {
            return Result.failure(IllegalArgumentException("파일을 선택해주세요"))
        }
        if (customerId <= 0) {
            return Result.failure(IllegalArgumentException("유효하지 않은 고객입니다"))
        }
        return repository.uploadRecording(customerId, conversationType, trimmedTitle, filePath)
    }
}
