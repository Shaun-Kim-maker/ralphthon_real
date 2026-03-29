package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.Conversation
import com.ralphthon.app.domain.model.ConversationType

interface UploadRepository {
    suspend fun uploadRecording(
        customerId: Long,
        conversationType: ConversationType,
        title: String,
        filePath: String
    ): Result<Conversation>
}
