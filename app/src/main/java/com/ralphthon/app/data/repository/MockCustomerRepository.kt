package com.ralphthon.app.data.repository

import com.ralphthon.app.data.mock.MockDataGenerator
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.repository.CustomerRepository
import javax.inject.Inject

class MockCustomerRepository @Inject constructor() : CustomerRepository {

    override suspend fun getCustomers(): Result<List<Customer>> {
        return Result.success(MockDataGenerator.generateCustomers())
    }

    override suspend fun getCustomerById(id: Long): Result<Customer> {
        val customer = MockDataGenerator.getCustomerById(id)
        return if (customer != null) {
            Result.success(customer)
        } else {
            Result.failure(DomainException.NotFoundException())
        }
    }
}
