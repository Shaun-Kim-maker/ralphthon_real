package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.KnowledgeDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KnowledgeApiService {
    @GET("api/cards/{cardId}/knowledge")
    suspend fun getKnowledgeArticles(@Path("cardId") cardId: Long): Response<List<KnowledgeDto>>

    @GET("api/knowledge/search")
    suspend fun searchKnowledge(@Query("q") query: String): Response<List<KnowledgeDto>>
}
