package com.ralphthon.app.data.repository

import com.ralphthon.app.domain.model.Conversation
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.domain.repository.UploadRepository
import javax.inject.Inject

class MockUploadRepository @Inject constructor() : UploadRepository {

    private var nextId = 1000L

    override suspend fun uploadRecording(
        customerId: Long,
        conversationType: ConversationType,
        title: String,
        filePath: String
    ): Result<Conversation> {
        val conversation = Conversation(
            id = nextId++,
            customerId = customerId,
            title = title,
            date = "2026-03-29",
            type = conversationType,
            duration = 0,
            summary = "",
            sentiment = Sentiment.NEUTRAL,
            keywords = emptyList(),
            keyStatements = emptyList(),
            priceCommitments = emptyList(),
            actionItems = emptyList(),
            predictedQuestions = emptyList()
        )
        return Result.success(conversation)
    }
}
