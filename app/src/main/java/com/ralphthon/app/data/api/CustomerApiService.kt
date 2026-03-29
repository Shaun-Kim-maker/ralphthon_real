package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.CustomerDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CustomerApiService {
    @GET("api/customers")
    suspend fun getCustomers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "last_interaction_date,desc"
    ): Response<List<CustomerDto>>

    @GET("api/customers/{id}")
    suspend fun getCustomerById(@Path("id") id: Long): Response<CustomerDto>
}
