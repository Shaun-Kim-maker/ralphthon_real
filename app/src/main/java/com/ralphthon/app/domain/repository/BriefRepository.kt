package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.SearchResult

interface BriefRepository {
    suspend fun search(query: String): Result<List<SearchResult>>
}
