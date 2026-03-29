package com.ralphthon.app.domain.usecase

import com.ralphthon.app.domain.model.DomainException
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.repository.BriefRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SearchCardsUseCaseTest {

    @MockK
    private lateinit var repository: BriefRepository
    private lateinit var useCase: SearchCardsUseCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = SearchCardsUseCase(repository)
    }

    private fun createResult(
        id: Long,
        type: String = "CONVERSATION",
        title: String = "결과 $id",
        relevanceScore: Float = 0.9f
    ) = SearchResult(id, type, title, "snippet", listOf(IntRange(0, 2)), id, relevanceScore)

    // ===== Query handling (1-10) =====

    @Test
    fun should_returnResults_when_validQuery() = runTest {
        val results = listOf(createResult(1), createResult(2))
        coEvery { repository.search("가격") } returns Result.success(results)

        val result = useCase("가격")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_emptyQuery() = runTest {
        val result = useCase("")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
        coVerify(exactly = 0) { repository.search(any()) }
    }

    @Test
    fun should_returnEmpty_when_blankQuery() = runTest {
        val result = useCase("   ")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
        coVerify(exactly = 0) { repository.search(any()) }
    }

    @Test
    fun should_trimQuery_when_hasWhitespace() = runTest {
        coEvery { repository.search("가격") } returns Result.success(listOf(createResult(1)))

        val result = useCase(" 가격 ")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.search("가격") }
    }

    @Test
    fun should_searchKorean_when_koreanQuery() = runTest {
        coEvery { repository.search("로봇") } returns Result.success(listOf(createResult(1)))

        val result = useCase("로봇")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_searchSpecialChars_when_specialQuery() = runTest {
        coEvery { repository.search("@#\$%") } returns Result.success(listOf(createResult(1)))

        val result = useCase("@#\$%")

        assertTrue(result.isSuccess)
    }

    @Test
    fun should_passQueryToRepo_when_invoked() = runTest {
        val query = "테스트쿼리"
        coEvery { repository.search(query) } returns Result.success(emptyList())

        useCase(query)

        coVerify(exactly = 1) { repository.search(query) }
    }

    @Test
    fun should_notCallRepo_when_emptyQuery() = runTest {
        useCase("")

        coVerify(exactly = 0) { repository.search(any()) }
    }

    @Test
    fun should_notCallRepo_when_blankQuery() = runTest {
        useCase("  \t  ")

        coVerify(exactly = 0) { repository.search(any()) }
    }

    @Test
    fun should_handleLongQuery_when_veryLong() = runTest {
        val longQuery = "가".repeat(500)
        coEvery { repository.search(longQuery) } returns Result.success(listOf(createResult(1)))

        val result = useCase(longQuery)

        assertTrue(result.isSuccess)
    }

    // ===== Success results (11-18) =====

    @Test
    fun should_returnMultipleResults_when_found() = runTest {
        val results = (1L..5L).map { createResult(it) }
        coEvery { repository.search("검색") } returns Result.success(results)

        val result = useCase("검색")

        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnSingleResult_when_oneFound() = runTest {
        coEvery { repository.search("단일") } returns Result.success(listOf(createResult(1)))

        val result = useCase("단일")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmptyResults_when_noMatch() = runTest {
        coEvery { repository.search("없는검색어") } returns Result.success(emptyList())

        val result = useCase("없는검색어")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_preserveType_when_resultReturned() = runTest {
        val expected = createResult(1, type = "INTERNAL_MEETING")
        coEvery { repository.search("회의") } returns Result.success(listOf(expected))

        val result = useCase("회의")

        assertEquals("INTERNAL_MEETING", result.getOrNull()!!.first().type)
    }

    @Test
    fun should_preserveTitle_when_resultReturned() = runTest {
        val expected = createResult(1, title = "중요한 제목")
        coEvery { repository.search("제목") } returns Result.success(listOf(expected))

        val result = useCase("제목")

        assertEquals("중요한 제목", result.getOrNull()!!.first().title)
    }

    @Test
    fun should_preserveSnippet_when_resultReturned() = runTest {
        val sr = SearchResult(1L, "CONVERSATION", "제목", "스니펫 내용", listOf(IntRange(0, 2)), 1L, 0.9f)
        coEvery { repository.search("스니펫") } returns Result.success(listOf(sr))

        val result = useCase("스니펫")

        assertEquals("스니펫 내용", result.getOrNull()!!.first().snippet)
    }

    @Test
    fun should_preserveHighlightRanges_when_returned() = runTest {
        val ranges = listOf(IntRange(0, 3), IntRange(5, 8))
        val sr = SearchResult(1L, "CONVERSATION", "제목", "snippet", ranges, 1L, 0.9f)
        coEvery { repository.search("하이라이트") } returns Result.success(listOf(sr))

        val result = useCase("하이라이트")

        assertEquals(ranges, result.getOrNull()!!.first().highlightRanges)
    }

    @Test
    fun should_preserveRelevanceScore_when_returned() = runTest {
        val sr = createResult(1, relevanceScore = 0.75f)
        coEvery { repository.search("점수") } returns Result.success(listOf(sr))

        val result = useCase("점수")

        assertEquals(0.75f, result.getOrNull()!!.first().relevanceScore)
    }

    // ===== Sorting (19-25) =====

    @Test
    fun should_sortByRelevance_when_defaultSort() = runTest {
        val results = listOf(
            createResult(1, relevanceScore = 0.3f),
            createResult(2, relevanceScore = 0.9f),
            createResult(3, relevanceScore = 0.6f)
        )
        coEvery { repository.search("정렬") } returns Result.success(results)

        val result = useCase.searchSorted("정렬")

        val scores = result.getOrNull()!!.map { it.relevanceScore }
        assertEquals(listOf(0.9f, 0.6f, 0.3f), scores)
    }

    @Test
    fun should_sortByTitle_when_titleSort() = runTest {
        val results = listOf(
            createResult(1, title = "C 제목"),
            createResult(2, title = "A 제목"),
            createResult(3, title = "B 제목")
        )
        coEvery { repository.search("제목") } returns Result.success(results)

        val result = useCase.searchSorted("제목", SearchCardsUseCase.SortBy.TITLE)

        val titles = result.getOrNull()!!.map { it.title }
        assertEquals(listOf("A 제목", "B 제목", "C 제목"), titles)
    }

    @Test
    fun should_handleEmptyResults_when_sorting() = runTest {
        coEvery { repository.search("빈") } returns Result.success(emptyList())

        val result = useCase.searchSorted("빈")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_handleSingleResult_when_sorting() = runTest {
        coEvery { repository.search("단일") } returns Result.success(listOf(createResult(1)))

        val result = useCase.searchSorted("단일")

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortDescending_when_relevanceSort() = runTest {
        val results = listOf(
            createResult(1, relevanceScore = 0.5f),
            createResult(2, relevanceScore = 1.0f),
            createResult(3, relevanceScore = 0.1f)
        )
        coEvery { repository.search("내림차순") } returns Result.success(results)

        val result = useCase.searchSorted("내림차순", SearchCardsUseCase.SortBy.RELEVANCE)

        val first = result.getOrNull()!!.first()
        val last = result.getOrNull()!!.last()
        assertTrue(first.relevanceScore >= last.relevanceScore)
    }

    @Test
    fun should_sortKoreanTitles_when_titleSort() = runTest {
        val results = listOf(
            createResult(1, title = "하늘"),
            createResult(2, title = "가을"),
            createResult(3, title = "나무")
        )
        coEvery { repository.search("한국어") } returns Result.success(results)

        val result = useCase.searchSorted("한국어", SearchCardsUseCase.SortBy.TITLE)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.size)
    }

    @Test
    fun should_sortStable_when_sameRelevance() = runTest {
        val results = listOf(
            createResult(1, relevanceScore = 0.5f),
            createResult(2, relevanceScore = 0.5f),
            createResult(3, relevanceScore = 0.5f)
        )
        coEvery { repository.search("동점") } returns Result.success(results)

        val result = useCase.searchSorted("동점", SearchCardsUseCase.SortBy.RELEVANCE)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.size)
    }

    // ===== Filtering (26-31) =====

    @Test
    fun should_filterByType_when_typeProvided() = runTest {
        val results = listOf(
            createResult(1, type = "CONVERSATION"),
            createResult(2, type = "INTERNAL_MEETING"),
            createResult(3, type = "CONVERSATION")
        )
        coEvery { repository.search("필터") } returns Result.success(results)

        val result = useCase.searchFiltered("필터", type = "CONVERSATION")

        assertEquals(2, result.getOrNull()!!.size)
        assertTrue(result.getOrNull()!!.all { it.type == "CONVERSATION" })
    }

    @Test
    fun should_returnAll_when_noTypeFilter() = runTest {
        val results = listOf(
            createResult(1, type = "CONVERSATION"),
            createResult(2, type = "INTERNAL_MEETING")
        )
        coEvery { repository.search("전체") } returns Result.success(results)

        val result = useCase.searchFiltered("전체", type = null)

        assertEquals(2, result.getOrNull()!!.size)
    }

    @Test
    fun should_returnEmpty_when_noMatchingType() = runTest {
        val results = listOf(
            createResult(1, type = "CONVERSATION"),
            createResult(2, type = "CONVERSATION")
        )
        coEvery { repository.search("없는타입") } returns Result.success(results)

        val result = useCase.searchFiltered("없는타입", type = "INTERNAL_MEETING")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_filterCorrectly_when_mixedTypes() = runTest {
        val results = listOf(
            createResult(1, type = "CONVERSATION"),
            createResult(2, type = "INTERNAL_MEETING"),
            createResult(3, type = "CONVERSATION"),
            createResult(4, type = "INTERNAL_MEETING")
        )
        coEvery { repository.search("혼합") } returns Result.success(results)

        val result = useCase.searchFiltered("혼합", type = "INTERNAL_MEETING")

        assertEquals(2, result.getOrNull()!!.size)
        assertTrue(result.getOrNull()!!.all { it.type == "INTERNAL_MEETING" })
    }

    @Test
    fun should_handleEmptyResults_when_filtering() = runTest {
        coEvery { repository.search("빈필터") } returns Result.success(emptyList())

        val result = useCase.searchFiltered("빈필터", type = "CONVERSATION")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }

    @Test
    fun should_preserveOtherFields_when_filtered() = runTest {
        val sr = SearchResult(42L, "CONVERSATION", "특별 제목", "특별 snippet", listOf(IntRange(1, 3)), 42L, 0.88f)
        coEvery { repository.search("필드보존") } returns Result.success(listOf(sr))

        val result = useCase.searchFiltered("필드보존", type = "CONVERSATION")

        val item = result.getOrNull()!!.first()
        assertEquals(42L, item.id)
        assertEquals("특별 제목", item.title)
        assertEquals(0.88f, item.relevanceScore)
    }

    // ===== Error cases (32-38) =====

    @Test
    fun should_returnFailure_when_networkError() = runTest {
        coEvery { repository.search(any()) } returns Result.failure(DomainException.NetworkException("네트워크 오류"))

        val result = useCase("에러")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NetworkException)
    }

    @Test
    fun should_returnFailure_when_serverError() = runTest {
        coEvery { repository.search(any()) } returns Result.failure(DomainException.ServerException(500, "서버 오류"))

        val result = useCase("서버에러")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.ServerException)
    }

    @Test
    fun should_returnFailure_when_timeout() = runTest {
        coEvery { repository.search(any()) } returns Result.failure(DomainException.TimeoutException("타임아웃"))

        val result = useCase("타임아웃")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.TimeoutException)
    }

    @Test
    fun should_returnFailure_when_unauthorized() = runTest {
        coEvery { repository.search(any()) } returns Result.failure(DomainException.UnauthorizedException("인증 실패"))

        val result = useCase("인증")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnauthorizedException)
    }

    @Test
    fun should_returnFailure_when_notFound() = runTest {
        coEvery { repository.search(any()) } returns Result.failure(DomainException.NotFoundException("찾을 수 없음"))

        val result = useCase("없음")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.NotFoundException)
    }

    @Test
    fun should_returnFailure_when_unknownError() = runTest {
        coEvery { repository.search(any()) } returns Result.failure(DomainException.UnknownException("알 수 없는 오류"))

        val result = useCase("알수없음")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is DomainException.UnknownException)
    }

    @Test
    fun should_propagateError_when_sortFails() = runTest {
        coEvery { repository.search(any()) } returns Result.failure(DomainException.NetworkException("정렬 오류"))

        val result = useCase.searchSorted("오류", SearchCardsUseCase.SortBy.RELEVANCE)

        assertTrue(result.isFailure)
    }

    // ===== Concurrency + edge (39-45) =====

    @Test
    fun should_handleConcurrentSearches_when_parallel() = runTest {
        coEvery { repository.search("쿼리1") } returns Result.success(listOf(createResult(1)))
        coEvery { repository.search("쿼리2") } returns Result.success(listOf(createResult(2)))

        val results = listOf(
            async { useCase("쿼리1") },
            async { useCase("쿼리2") }
        ).awaitAll()

        assertTrue(results.all { it.isSuccess })
    }

    @Test
    fun should_returnIndependentResults_when_sequentialCalls() = runTest {
        coEvery { repository.search("첫번째") } returns Result.success(listOf(createResult(1)))
        coEvery { repository.search("두번째") } returns Result.success(listOf(createResult(2), createResult(3)))

        val result1 = useCase("첫번째")
        val result2 = useCase("두번째")

        assertEquals(1, result1.getOrNull()!!.size)
        assertEquals(2, result2.getOrNull()!!.size)
    }

    @Test
    fun should_handleRapidCalls_when_rapidInput() = runTest {
        coEvery { repository.search(any()) } returns Result.success(listOf(createResult(1)))

        val results = (1..5).map { useCase("검색$it") }

        assertTrue(results.all { it.isSuccess })
    }

    @Test
    fun should_callRepoOnce_when_invoked() = runTest {
        coEvery { repository.search("단일호출") } returns Result.success(listOf(createResult(1)))

        useCase("단일호출")

        coVerify(exactly = 1) { repository.search("단일호출") }
    }

    @Test
    fun should_handleUrlEncodedChars_when_specialCharsInQuery() = runTest {
        val query = "검색%20쿼리"
        coEvery { repository.search(query) } returns Result.success(listOf(createResult(1)))

        val result = useCase(query)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { repository.search(query) }
    }

    @Test
    fun should_handleNumericQuery_when_numberSearched() = runTest {
        coEvery { repository.search("500") } returns Result.success(listOf(createResult(1)))

        val result = useCase("500")

        assertTrue(result.isSuccess)
    }

    @Test
    fun should_handleMixedQuery_when_koreanAndNumbers() = runTest {
        coEvery { repository.search("가격 500만") } returns Result.success(listOf(createResult(1), createResult(2)))

        val result = useCase("가격 500만")

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }
}
