package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.repository.CustomerRepository
import javax.inject.Inject

class GetCustomersUseCase @Inject constructor(
    private val repository: CustomerRepository
) {
    suspend operator fun invoke(): Result<List<Customer>> {
        return repository.getCustomers()
    }

    suspend fun getById(id: Long): Result<Customer> {
        return repository.getCustomerById(id)
    }

    suspend fun getSorted(sortBy: SortBy = SortBy.LAST_INTERACTION): Result<List<Customer>> {
        return repository.getCustomers().map { customers ->
            when (sortBy) {
                SortBy.LAST_INTERACTION -> customers.sortedByDescending { it.lastInteractionDate }
                SortBy.NAME -> customers.sortedBy { it.companyName }
                SortBy.CONVERSATIONS -> customers.sortedByDescending { it.totalConversations }
            }
        }
    }

    suspend fun getFiltered(industry: String? = null): Result<List<Customer>> {
        return repository.getCustomers().map { customers ->
            if (industry != null) {
                customers.filter { it.industry == industry }
            } else {
                customers
            }
        }
    }

    enum class SortBy {
        LAST_INTERACTION, NAME, CONVERSATIONS
    }
}
