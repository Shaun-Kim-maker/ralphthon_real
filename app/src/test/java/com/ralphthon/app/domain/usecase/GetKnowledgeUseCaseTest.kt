package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.repository.KnowledgeRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class GetKnowledgeUseCaseTest {

    @MockK
    private lateinit var repository: KnowledgeRepository
    private lateinit var useCase: GetKnowledgeUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = GetKnowledgeUseCase(repository)
    }

    private fun makeArticle(
        id: Long = 1L,
        title: String = "지식 제목",
        content: String = "지식 내용",
        category: String = "영업",
        relevanceScore: Float = 0.8f
    ) = KnowledgeArticle(id, title, content, category, relevanceScore)

    // ===== GetByCardId (1-8) =====

    @Test
    fun should_returnArticles_when_cardIdValid() = runTest {
        val articles = listOf(makeArticle(1), makeArticle(2))
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(articles)

        val result = useCase.getByCardId(1L)

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmptyList_when_noArticles() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase.getByCardId(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnMultiple_when_manyArticles() = runTest {
        val articles = (1L..5L).map { makeArticle(it) }
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(articles)

        val result = useCase.getByCardId(1L)

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()!!.size)
    }

    @Test
    fun should_preserveTitle_when_returned() = runTest {
        val article = makeArticle(title = "특별한 제목")
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(listOf(article))

        val result = useCase.getByCardId(1L)

        assertEquals("특별한 제목", result.getOrNull()!!.first().title)
    }

    @Test
    fun should_preserveContent_when_returned() = runTest {
        val article = makeArticle(content = "상세한 내용입니다")
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(listOf(article))

        val result = useCase.getByCardId(1L)

        assertEquals("상세한 내용입니다", result.getOrNull()!!.first().content)
    }

    @Test
    fun should_preserveCategory_when_returned() = runTest {
        val article = makeArticle(category = "기술지원")
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(listOf(article))

        val result = useCase.getByCardId(1L)

        assertEquals("기술지원", result.getOrNull()!!.first().category)
    }

    @Test
    fun should_preserveRelevanceScore_when_returned() = runTest {
        val article = makeArticle(relevanceScore = 0.95f)
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(listOf(article))

        val result = useCase.getByCardId(1L)

        assertEquals(0.95f, result.getOrNull()!!.first().relevanceScore)
    }

    @Test
    fun should_callRepository_when_invoked() = runTest {
        coEvery { repository.getKnowledgeArticles(5L) } returns Result.success(emptyList())

        useCase.getByCardId(5L)

        coVerify(exactly = 1) { repository.getKnowledgeArticles(5L) }
    }

    // ===== Search (9-16) =====

    @Test
    fun should_returnSearchResults_when_validQuery() = runTest {
        val articles = listOf(makeArticle(1), makeArticle(2))
        coEvery { repository.searchKnowledge("영업") } returns Result.success(articles)

        val result = useCase.search("영업")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_emptyQuery() = runTest {
        val result = useCase.search("")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
        coVerify(exactly = 0) { repository.searchKnowledge(any()) }
    }

    @Test
    fun should_returnEmpty_when_blankQuery() = runTest {
        val result = useCase.search("   ")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
        coVerify(exactly = 0) { repository.searchKnowledge(any()) }
    }

    @Test
    fun should_trimQuery_when_hasWhitespace() = runTest {
        coEvery { repository.searchKnowledge("영업") } returns Result.success(listOf(makeArticle()))

        val result = useCase.search("  영업  ")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.searchKnowledge("영업") }
    }

    @Test
    fun should_notCallRepo_when_emptyQuery() = runTest {
        useCase.search("")

        coVerify(exactly = 0) { repository.searchKnowledge(any()) }
    }

    @Test
    fun should_searchKorean_when_koreanQuery() = runTest {
        coEvery { repository.searchKnowledge("고객 미팅") } returns Result.success(listOf(makeArticle()))

        val result = useCase.search("고객 미팅")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.searchKnowledge("고객 미팅") }
    }

    @Test
    fun should_returnMultiple_when_manyResults() = runTest {
        val articles = (1L..10L).map { makeArticle(it) }
        coEvery { repository.searchKnowledge("지식") } returns Result.success(articles)

        val result = useCase.search("지식")

        assertEquals(10, result.getOrNull()!!.size)
    }

    @Test
    fun should_passQuery_when_searching() = runTest {
        coEvery { repository.searchKnowledge("특정쿼리") } returns Result.success(emptyList())

        useCase.search("특정쿼리")

        coVerify(exactly = 1) { repository.searchKnowledge("특정쿼리") }
    }

    // ===== Sorting (17-23) =====

    @Test
    fun should_sortByRelevance_when_defaultSort() = runTest {
        val articles = listOf(
            makeArticle(id = 1, relevanceScore = 0.5f),
            makeArticle(id = 2, relevanceScore = 0.9f),
            makeArticle(id = 3, relevanceScore = 0.3f)
        )
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(articles)

        val result = useCase.getByCardIdSorted(1L)

        val sorted = result.getOrNull()!!
        assertEquals(2L, sorted[0].id)
        assertEquals(1L, sorted[1].id)
        assertEquals(3L, sorted[2].id)
    }

    @Test
    fun should_sortByTitle_when_titleSort() = runTest {
        val articles = listOf(
            makeArticle(id = 1, title = "다나"),
            makeArticle(id = 2, title = "가나"),
            makeArticle(id = 3, title = "나다")
        )
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(articles)

        val result = useCase.getByCardIdSorted(1L, GetKnowledgeUseCase.SortBy.TITLE)

        val sorted = result.getOrNull()!!
        assertEquals("가나", sorted[0].title)
        assertEquals("나다", sorted[1].title)
        assertEquals("다나", sorted[2].title)
    }

    @Test
    fun should_sortByCategory_when_categorySort() = runTest {
        val articles = listOf(
            makeArticle(id = 1, category = "영업"),
            makeArticle(id = 2, category = "기술"),
            makeArticle(id = 3, category = "마케팅")
        )
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(articles)

        val result = useCase.getByCardIdSorted(1L, GetKnowledgeUseCase.SortBy.CATEGORY)

        assertTrue(result.isSuccess)
        val sorted = result.getOrNull()!!
        assertEquals(3, sorted.size)
    }

    @Test
    fun should_handleEmpty_when_sorting() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(emptyList())

        val result = useCase.getByCardIdSorted(1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleSingle_when_sorting() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(listOf(makeArticle()))

        val result = useCase.getByCardIdSorted(1L)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortDescending_when_relevance() = runTest {
        val articles = listOf(
            makeArticle(id = 1, relevanceScore = 0.2f),
            makeArticle(id = 2, relevanceScore = 0.8f),
            makeArticle(id = 3, relevanceScore = 0.5f)
        )
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(articles)

        val result = useCase.getByCardIdSorted(1L, GetKnowledgeUseCase.SortBy.RELEVANCE)

        val sorted = result.getOrNull()!!
        assertTrue(sorted[0].relevanceScore >= sorted[1].relevanceScore)
        assertTrue(sorted[1].relevanceScore >= sorted[2].relevanceScore)
    }

    @Test
    fun should_sortKorean_when_titleSort() = runTest {
        val articles = listOf(
            makeArticle(id = 1, title = "하이닉스"),
            makeArticle(id = 2, title = "가온전선"),
            makeArticle(id = 3, title = "나노텍")
        )
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.success(articles)

        val result = useCase.getByCardIdSorted(1L, GetKnowledgeUseCase.SortBy.TITLE)

        val sorted = result.getOrNull()!!
        assertEquals("가온전선", sorted[0].title)
    }

    // ===== Errors (24-30) =====

    @Test
    fun should_returnFailure_when_networkError() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.failure(DomainException.NetworkException())

        val result = useCase.getByCardId(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_serverError() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.failure(DomainException.ServerException(500))

        val result = useCase.getByCardId(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.ServerException)
    }

    @Test
    fun should_returnFailure_when_timeout() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.failure(DomainException.TimeoutException())

        val result = useCase.getByCardId(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_unauthorized() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.failure(DomainException.UnauthorizedException())

        val result = useCase.getByCardId(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_notFound() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.failure(DomainException.NotFoundException())

        val result = useCase.getByCardId(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_propagateError_when_searchFails() = runTest {
        coEvery { repository.searchKnowledge("쿼리") } returns Result.failure(DomainException.NetworkException())

        val result = useCase.search("쿼리")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_propagateError_when_sortFails() = runTest {
        coEvery { repository.getKnowledgeArticles(1L) } returns Result.failure(DomainException.ServerException(503))

        val result = useCase.getByCardIdSorted(1L)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.ServerException)
    }

    // ===== Edge (31-35) =====

    @Test
    fun should_handleNegativeCardId_when_invalid() = runTest {
        coEvery { repository.getKnowledgeArticles(-1L) } returns Result.success(emptyList())

        val result = useCase.getByCardId(-1L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.getKnowledgeArticles(-1L) }
    }

    @Test
    fun should_handleZeroCardId_when_zero() = runTest {
        coEvery { repository.getKnowledgeArticles(0L) } returns Result.success(emptyList())

        val result = useCase.getByCardId(0L)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.getKnowledgeArticles(0L) }
    }

    @Test
    fun should_handleLargeCardId_when_maxLong() = runTest {
        coEvery { repository.getKnowledgeArticles(Long.MAX_VALUE) } returns Result.success(listOf(makeArticle()))

        val result = useCase.getByCardId(Long.MAX_VALUE)

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleLongQuery_when_searching() = runTest {
        val longQuery = "a".repeat(500)
        coEvery { repository.searchKnowledge(longQuery) } returns Result.success(emptyList())

        val result = useCase.search(longQuery)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.searchKnowledge(longQuery) }
    }

    @Test
    fun should_handleSpecialChars_when_searching() = runTest {
        val specialQuery = "!@#$%^&*()"
        coEvery { repository.searchKnowledge(specialQuery) } returns Result.success(emptyList())

        val result = useCase.search(specialQuery)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.searchKnowledge(specialQuery) }
    }
}
