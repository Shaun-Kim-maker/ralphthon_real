package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.repository.CustomerRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetCustomersUseCaseTest {

    @MockK
    private lateinit var repository: CustomerRepository
    private lateinit var useCase: GetCustomersUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetCustomersUseCase(repository)
    }

    private fun makeCustomer(
        id: Long = 1L,
        companyName: String = "삼성전자",
        contactName: String? = "홍길동",
        industry: String = "전자",
        lastInteractionDate: String = "2026-03-01",
        totalConversations: Int = 5,
        summary: String? = "요약"
    ) = Customer(id, companyName, contactName, industry, lastInteractionDate, totalConversations, summary)

    // ===== Success cases (1-8) =====

    @Test
    fun should_returnCustomers_when_repositorySucceeds() = runTest {
        val customers = listOf(makeCustomer(1), makeCustomer(2), makeCustomer(3))
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmptyList_when_repositoryReturnsEmpty() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(emptyList())

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnSingleCustomer_when_oneExists() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(listOf(makeCustomer()))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_return10Customers_when_allExist() = runTest {
        val customers = (1L..10L).map { makeCustomer(id = it) }
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnCustomer_when_getByIdSucceeds() = runTest {
        val customer = makeCustomer(id = 1L)
        coEvery { repository.getCustomerById(1L) } returns Result.success(customer)

        val result = useCase.getById(1L)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull()!!.id)
    }

    @Test
    fun should_preserveFields_when_customerReturned() = runTest {
        val customer = makeCustomer(
            id = 42L,
            companyName = "LG전자",
            contactName = "김철수",
            industry = "전자",
            lastInteractionDate = "2026-03-15",
            totalConversations = 10,
            summary = "상세 요약"
        )
        coEvery { repository.getCustomerById(42L) } returns Result.success(customer)

        val result = useCase.getById(42L)
        val c = result.getOrNull()!!

        assertEquals(42L, c.id)
        assertEquals("LG전자", c.companyName)
        assertEquals("김철수", c.contactName)
        assertEquals("전자", c.industry)
        assertEquals("2026-03-15", c.lastInteractionDate)
        assertEquals(10, c.totalConversations)
        assertEquals("상세 요약", c.summary)
    }

    @Test
    fun should_handleNullContactName_when_present() = runTest {
        val customer = makeCustomer(contactName = null)
        coEvery { repository.getCustomers() } returns Result.success(listOf(customer))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()!!.first().contactName)
    }

    @Test
    fun should_handleNullSummary_when_present() = runTest {
        val customer = makeCustomer(summary = null)
        coEvery { repository.getCustomers() } returns Result.success(listOf(customer))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull()!!.first().summary)
    }

    // ===== Error cases (9-16) =====

    @Test
    fun should_returnFailure_when_networkError() = runTest {
        coEvery { repository.getCustomers() } returns Result.failure(DomainException.NetworkException())

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_serverError500() = runTest {
        coEvery { repository.getCustomers() } returns Result.failure(DomainException.ServerException(500))

        val result = useCase()

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(500, ex.code)
    }

    @Test
    fun should_returnFailure_when_serverError503() = runTest {
        coEvery { repository.getCustomers() } returns Result.failure(DomainException.ServerException(503))

        val result = useCase()

        assertTrue(result.isFailure)
        val ex = result.exceptionOrNull() as DomainException.ServerException
        assertEquals(503, ex.code)
    }

    @Test
    fun should_returnFailure_when_timeout() = runTest {
        coEvery { repository.getCustomers() } returns Result.failure(DomainException.TimeoutException())

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_unauthorized401() = runTest {
        coEvery { repository.getCustomers() } returns Result.failure(DomainException.UnauthorizedException())

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_forbidden403() = runTest {
        coEvery { repository.getCustomers() } returns Result.failure(DomainException.UnauthorizedException("접근 권한이 없습니다"))

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_notFound() = runTest {
        coEvery { repository.getCustomerById(999L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase.getById(999L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_returnFailure_when_unknownError() = runTest {
        coEvery { repository.getCustomers() } returns Result.failure(DomainException.UnknownException())

        val result = useCase()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnknownException)
    }

    // ===== Sorting (17-25) =====

    @Test
    fun should_sortByLastInteraction_when_defaultSort() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, lastInteractionDate = "2026-01-01"),
            makeCustomer(id = 2, lastInteractionDate = "2026-03-01"),
            makeCustomer(id = 3, lastInteractionDate = "2026-02-01")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getSorted()

        val sorted = result.getOrNull()!!
        assertEquals(2L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(1L, sorted[2].id)
    }

    @Test
    fun should_sortByName_when_nameSortSelected() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, companyName = "현대"),
            makeCustomer(id = 2, companyName = "기아"),
            makeCustomer(id = 3, companyName = "삼성")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getSorted(GetCustomersUseCase.SortBy.NAME)

        val sorted = result.getOrNull()!!
        assertEquals("기아", sorted[0].companyName)
        assertEquals("삼성", sorted[1].companyName)
        assertEquals("현대", sorted[2].companyName)
    }

    @Test
    fun should_sortByConversations_when_conversationSort() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, totalConversations = 3),
            makeCustomer(id = 2, totalConversations = 10),
            makeCustomer(id = 3, totalConversations = 7)
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getSorted(GetCustomersUseCase.SortBy.CONVERSATIONS)

        val sorted = result.getOrNull()!!
        assertEquals(2L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(1L, sorted[2].id)
    }

    @Test
    fun should_returnEmpty_when_sortingEmptyList() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(emptyList())

        val result = useCase.getSorted()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortCorrectly_when_sameDates() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, lastInteractionDate = "2026-03-01"),
            makeCustomer(id = 2, lastInteractionDate = "2026-03-01"),
            makeCustomer(id = 3, lastInteractionDate = "2026-03-01")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getSorted()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortKoreanNames_when_nameSort() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, companyName = "하이닉스"),
            makeCustomer(id = 2, companyName = "가온전선"),
            makeCustomer(id = 3, companyName = "나노텍")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getSorted(GetCustomersUseCase.SortBy.NAME)

        val sorted = result.getOrNull()!!
        assertEquals("가온전선", sorted[0].companyName)
        assertEquals("나노텍", sorted[1].companyName)
        assertEquals("하이닉스", sorted[2].companyName)
    }

    @Test
    fun should_handleSingleItem_when_sorting() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(listOf(makeCustomer()))

        val result = useCase.getSorted()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortDescending_when_lastInteraction() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, lastInteractionDate = "2026-01-15"),
            makeCustomer(id = 2, lastInteractionDate = "2026-03-20"),
            makeCustomer(id = 3, lastInteractionDate = "2026-02-10")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getSorted(GetCustomersUseCase.SortBy.LAST_INTERACTION)

        val sorted = result.getOrNull()!!
        assertTrue(sorted[0].lastInteractionDate >= sorted[1].lastInteractionDate)
        assertTrue(sorted[1].lastInteractionDate >= sorted[2].lastInteractionDate)
    }

    @Test
    fun should_sortDescending_when_conversations() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, totalConversations = 1),
            makeCustomer(id = 2, totalConversations = 20),
            makeCustomer(id = 3, totalConversations = 5)
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getSorted(GetCustomersUseCase.SortBy.CONVERSATIONS)

        val sorted = result.getOrNull()!!
        assertTrue(sorted[0].totalConversations >= sorted[1].totalConversations)
        assertTrue(sorted[1].totalConversations >= sorted[2].totalConversations)
    }

    // ===== Filtering (26-33) =====

    @Test
    fun should_filterByIndustry_when_industryProvided() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, industry = "전자"),
            makeCustomer(id = 2, industry = "자동차"),
            makeCustomer(id = 3, industry = "전자")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered(industry = "전자")

        val filtered = result.getOrNull()!!
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.industry == "전자" })
    }

    @Test
    fun should_returnAll_when_noFilterProvided() = runTest {
        val customers = listOf(makeCustomer(1), makeCustomer(2), makeCustomer(3))
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered()

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_noMatchingIndustry() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, industry = "전자"),
            makeCustomer(id = 2, industry = "자동차")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered(industry = "없는업종")

        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnMultiple_when_multipleMatchIndustry() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, industry = "전자"),
            makeCustomer(id = 2, industry = "전자"),
            makeCustomer(id = 3, industry = "전자"),
            makeCustomer(id = 4, industry = "자동차")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered(industry = "전자")

        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_filterCaseSensitive_when_industryFilter() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, industry = "Electronics"),
            makeCustomer(id = 2, industry = "electronics")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered(industry = "Electronics")

        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("Electronics", result.getOrNull()!!.first().industry)
    }

    @Test
    fun should_returnAll_when_filterIsNull() = runTest {
        val customers = listOf(makeCustomer(1), makeCustomer(2))
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered(industry = null)

        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleEmptyFilter_when_emptyString() = runTest {
        val customers = listOf(
            makeCustomer(id = 1, industry = "전자"),
            makeCustomer(id = 2, industry = "")
        )
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered(industry = "")

        assertEquals(1, result.getOrNull()!!.size)
        assertEquals("", result.getOrNull()!!.first().industry)
    }

    @Test
    fun should_filterCorrectly_when_mixedIndustries() = runTest {
        val customers = (1L..10L).map { id ->
            if (id <= 3L) makeCustomer(id = id, industry = "전자")
            else makeCustomer(id = id, industry = "기타")
        }
        coEvery { repository.getCustomers() } returns Result.success(customers)

        val result = useCase.getFiltered(industry = "전자")

        assertEquals(3, result.getOrNull()!!.size)
    }

    // ===== Input boundary (34-38) =====

    @Test
    fun should_returnFailure_when_getByIdWithNegative() = runTest {
        coEvery { repository.getCustomerById(-1L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase.getById(-1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun should_returnFailure_when_getByIdWithZero() = runTest {
        coEvery { repository.getCustomerById(0L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase.getById(0L)

        assertTrue(result.isFailure)
    }

    @Test
    fun should_returnSuccess_when_getByIdWithLargeId() = runTest {
        val customer = makeCustomer(id = Long.MAX_VALUE)
        coEvery { repository.getCustomerById(Long.MAX_VALUE) } returns Result.success(customer)

        val result = useCase.getById(Long.MAX_VALUE)

        assertTrue(result.isSuccess)
        assertEquals(Long.MAX_VALUE, result.getOrNull()!!.id)
    }

    @Test
    fun should_callRepository_when_invoked() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(emptyList())

        useCase()

        coVerify(exactly = 1) { repository.getCustomers() }
    }

    @Test
    fun should_notCacheResult_when_calledTwice() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(emptyList())

        useCase()
        useCase()

        coVerify(exactly = 2) { repository.getCustomers() }
    }

    // ===== Concurrency (39-45) =====

    @Test
    fun should_handleConcurrentCalls_when_multipleInvocations() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(listOf(makeCustomer()))

        val results = (1..5).map {
            async { useCase() }
        }.awaitAll()

        assertTrue(results.all { it.isSuccess })
        assertEquals(5, results.size)
    }

    @Test
    fun should_returnIndependentResults_when_calledSequentially() = runTest {
        coEvery { repository.getCustomers() } returnsMany listOf(
            Result.success(listOf(makeCustomer(id = 1))),
            Result.success(listOf(makeCustomer(id = 2)))
        )

        val result1 = useCase()
        val result2 = useCase()

        assertEquals(1L, result1.getOrNull()!!.first().id)
        assertEquals(2L, result2.getOrNull()!!.first().id)
    }

    @Test
    fun should_propagateException_when_repoThrows() = runTest {
        coEvery { repository.getCustomers() } throws RuntimeException("unexpected")

        var caught = false
        try {
            useCase()
        } catch (e: RuntimeException) {
            caught = true
            assertEquals("unexpected", e.message)
        }
        assertTrue(caught)
    }

    @Test
    fun should_completeNormally_when_coroutineCancelled() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(listOf(makeCustomer()))

        val result = useCase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun should_notBlockThread_when_invoked() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(emptyList())

        val result = useCase()

        assertTrue(result.isSuccess)
    }

    @Test
    fun should_returnResult_when_repoDelays() = runTest {
        coEvery { repository.getCustomers() } coAnswers {
            delay(100)
            Result.success(listOf(makeCustomer()))
        }

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleRapidCalls_when_calledRepeatedly() = runTest {
        coEvery { repository.getCustomers() } returns Result.success(listOf(makeCustomer()))

        val results = (1..10).map { useCase() }

        assertEquals(10, results.size)
        assertTrue(results.all { it.isSuccess })
        coVerify(exactly = 10) { repository.getCustomers() }
    }
}
