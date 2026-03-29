package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.CardDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CardApiService {
    @GET("api/customers/{customerId}/cards")
    suspend fun getCardsByCustomerId(
        @Path("customerId") customerId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<List<CardDto>>

    @GET("api/cards/{id}")
    suspend fun getCardById(@Path("id") id: Long): Response<CardDto>
}
