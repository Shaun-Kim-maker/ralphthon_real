package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.SearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BriefApiService {
    @GET("api/search")
    suspend fun search(@Query("q") query: String): Response<SearchResponseDto>
}
