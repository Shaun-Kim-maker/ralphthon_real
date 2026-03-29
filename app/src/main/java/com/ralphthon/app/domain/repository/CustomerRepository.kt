package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.Customer

interface CustomerRepository {
    suspend fun getCustomers(): Result<List<Customer>>
    suspend fun getCustomerById(id: Long): Result<Customer>
}
