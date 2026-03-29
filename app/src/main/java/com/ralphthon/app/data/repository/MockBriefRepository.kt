package com.ralphthon.app.data.repository

import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.repository.BriefRepository
import javax.inject.Inject

class MockBriefRepository @Inject constructor() : BriefRepository {

    override suspend fun search(query: String): Result<List<SearchResult>> {
        return Result.success(emptyList())
    }
}
