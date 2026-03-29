RALPH_BACKLOG.md를 읽고 첫 번째 미완료(- [ ]) 마일스톤을 찾아서 실행하라.
이미 완료된(- [x]) 마일스톤은 건너뛴다.
이 Phase의 마일스톤이 모두 완료되면 세션을 종료한다.

세션 시작 체크:
1. RALPH_BACKLOG.md 읽기
2. git log --oneline -3
3. ./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3

---

## Phase 3: Domain UseCases
마일스톤 M-17 ~ M-24를 순서대로 실행한다.
각 마일스톤 완료 시 RALPH_BACKLOG.md의 해당 항목을 `- [x]`로 업데이트하라.
같은 에러 3회 반복 시 `- [SKIP]`으로 표시하고 다음 마일스톤으로 진행하라.

**전제**: Phase 1 + Phase 2(M-01~M-16)가 완료된 상태.
존재하는 파일들:
- `domain/repository/`: CustomerRepository, CardRepository, KnowledgeRepository, UploadRepository
- `domain/model/Exceptions.kt`: NotFoundException, NetworkException, TimeoutException, ValidationException
- `data/api/`: CustomerApiClient, CardApiClient, KnowledgeApiClient, UploadApiClient
- `data/mapper/`: CustomerMapper, CardMapper, KnowledgeMapper, SearchResponseMapper
- `di/AppModule.kt`: @IoDispatcher, @MainDispatcher qualifiers 정의됨

---

## UseCase 공통 구현 패턴

모든 UseCase는 다음 패턴을 따른다:

```kotlin
class GetXxxUseCase @Inject constructor(
    private val repository: XxxRepository,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(param: Type): Result<T> {
        if (param < 0) return Result.failure(IllegalArgumentException("ID must be non-negative"))
        return withContext(dispatcher) {
            repository.getXxx(param)
        }
    }
}
```

**규칙**:
- 반환 타입: `Result<T>` (kotlinx 또는 kotlin stdlib)
- 코루틴 디스패처: `withContext(dispatcher)` 사용
- 입력 검증: invoke() 시작부에서 수행, 실패 시 `Result.failure(IllegalArgumentException(...))`
- 레포지토리 호출은 dispatcher 컨텍스트 내에서만

## UseCase 테스트 공통 패턴

```kotlin
@ExtendWith(MockKExtension::class)
class GetXxxUseCaseTest {
    @MockK
    private lateinit var repository: XxxRepository
    private lateinit var useCase: GetXxxUseCase
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        useCase = GetXxxUseCase(repository, testDispatcher)
    }

    @Test
    fun should_returnData_when_repositorySucceeds() = runTest(testDispatcher) {
        coEvery { repository.getXxx(any()) } returns Result.success(mockData)
        val result = useCase(validParam)
        assertTrue(result.isSuccess)
        assertEquals(mockData, result.getOrNull())
    }

    @Test
    fun should_returnFailure_when_repositoryThrows() = runTest(testDispatcher) {
        coEvery { repository.getXxx(any()) } returns Result.failure(NetworkException("error"))
        val result = useCase(validParam)
        assertTrue(result.isFailure)
    }
}
```

---

### M-17: TDD GetCustomersUseCase 8 tests

**목표**: 8개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 5 GetCustomersUseCase" 섹션

**Tier 1 TDD**: 테스트 파일 먼저 작성

**파일들**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCustomersUseCaseTest.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/GetCustomersUseCase.kt`

**8개 테스트 커버리지**:
1. `should_returnCustomerList_when_repositorySucceeds`
2. `should_returnEmptyList_when_noCustomers`
3. `should_returnFailure_when_networkException`
4. `should_returnFailure_when_repositoryFails`
5. `should_callRepository_when_invoked`
6. `should_returnCustomersWithContacts_when_detailsAvailable`
7. `should_useIoDispatcher_when_callingRepository`
8. `should_propagateException_when_repositoryThrows`

**GetCustomersUseCase** 특이사항: 파라미터 없음 (`operator fun invoke(): Result<List<Customer>>`)

**검증**: `./gradlew.bat test --tests "*.GetCustomersUseCaseTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-17 체크.
커밋: `git commit -m "test(DOM-001): GetCustomersUseCase 8 tests"`

---

### M-18: TDD GetCardsByCustomerUseCase 10 tests

**목표**: 10개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 6 GetCardsByCustomerUseCase" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCardsByCustomerUseCaseTest.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/GetCardsByCustomerUseCase.kt`

**10개 테스트 커버리지**:
1. `should_returnCardList_when_customerIdValid`
2. `should_returnEmptyList_when_customerHasNoCards`
3. `should_returnFailure_when_customerIdNegative`
4. `should_returnFailure_when_networkException`
5. `should_applyPagination_when_pageAndSizeProvided`
6. `should_useDefaultPagination_when_noParamsProvided`
7. `should_returnHasMore_when_moreCardsAvailable`
8. `should_returnTotalCount_when_cardsExist`
9. `should_callRepository_when_invoked`
10. `should_propagateException_when_repositoryFails`

**시그니처**: `operator fun invoke(customerId: Long, page: Int = 0, size: Int = 20): Result<CardListResult>`
**입력 검증**: `customerId < 0` → `Result.failure(IllegalArgumentException(...))`

**검증**: `./gradlew.bat test --tests "*.GetCardsByCustomerUseCaseTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-18 체크.
커밋: `git commit -m "test(DOM-002): GetCardsByCustomerUseCase 10 tests"`

---

### M-19: TDD GetCardDetailUseCase 8 tests

**목표**: 8개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 7 GetCardDetailUseCase" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCardDetailUseCaseTest.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/GetCardDetailUseCase.kt`

**8개 테스트 커버리지**:
1. `should_returnCard_when_cardIdValid`
2. `should_returnCardWithStatements_when_statementsExist`
3. `should_returnCardWithKeywords_when_keywordsExist`
4. `should_returnFailure_when_cardIdNegative`
5. `should_returnFailure_when_cardNotFound` (NotFoundException)
6. `should_returnFailure_when_networkException`
7. `should_callRepository_when_invoked`
8. `should_propagateNotFoundException_when_404Received`

**시그니처**: `operator fun invoke(cardId: Long): Result<ContextCard>`
**입력 검증**: `cardId < 0` → failure

**검증**: `./gradlew.bat test --tests "*.GetCardDetailUseCaseTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-19 체크.
커밋: `git commit -m "test(DOM-003): GetCardDetailUseCase 8 tests"`

---

### M-20: TDD SearchCardsUseCase 10 tests

**목표**: 10개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 8 SearchCardsUseCase" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/SearchCardsUseCaseTest.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/SearchCardsUseCase.kt`

**10개 테스트 커버리지**:
1. `should_returnResults_when_queryNotEmpty`
2. `should_returnFailure_when_queryIsBlank`
3. `should_returnFailure_when_queryTooShort` (query.length < 2)
4. `should_filterByCustomerId_when_customerIdProvided`
5. `should_filterByDateRange_when_datesProvided`
6. `should_returnEmptyList_when_noMatches`
7. `should_returnPaginatedResults_when_pageRequested`
8. `should_returnHasMore_when_moreResultsAvailable`
9. `should_callRepository_when_invoked`
10. `should_propagateException_when_repositoryFails`

**시그니처**:
```kotlin
operator fun invoke(
    query: String,
    customerId: Long? = null,
    dateFrom: String? = null,
    dateTo: String? = null,
    page: Int = 0,
    size: Int = 20
): Result<CardListResult>
```
**입력 검증**: blank query → `Result.failure(ValidationException("Query must not be blank"))`

**검증**: `./gradlew.bat test --tests "*.SearchCardsUseCaseTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-20 체크.
커밋: `git commit -m "test(DOM-004): SearchCardsUseCase 10 tests"`

---

### M-21: TDD GetKnowledgeUseCase 8 tests

**목표**: 8개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 9 GetKnowledgeUseCase" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetKnowledgeUseCaseTest.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/GetKnowledgeUseCase.kt`

**8개 테스트 커버리지**:
1. `should_returnKnowledge_when_keywordIdValid`
2. `should_returnArticleList_when_multipleArticlesExist`
3. `should_returnFailure_when_keywordIdNegative`
4. `should_returnFailure_when_keywordNotFound`
5. `should_returnKnowledgeWithRelatedKeywords_when_available`
6. `should_returnFailure_when_networkException`
7. `should_callRepository_when_invoked`
8. `should_propagateException_when_repositoryFails`

**시그니처**: `operator fun invoke(keywordId: Long): Result<KnowledgeResult>`

**검증**: `./gradlew.bat test --tests "*.GetKnowledgeUseCaseTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-21 체크.
커밋: `git commit -m "test(DOM-005): GetKnowledgeUseCase 8 tests"`

---

### M-22: TDD UploadConversationUseCase 8 tests

**목표**: 8개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 13 UploadConversationUseCase" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/UploadConversationUseCaseTest.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/UploadConversationUseCase.kt`

**8개 테스트 커버리지**:
1. `should_returnUploadResult_when_transcriptProvided`
2. `should_returnUploadResult_when_audioProvided`
3. `should_returnFailure_when_customerIdNegative`
4. `should_returnFailure_when_typeIsBlank`
5. `should_returnFailure_when_neitherAudioNorTranscriptProvided`
6. `should_returnCardsGenerated_when_uploadSucceeds`
7. `should_returnFailure_when_networkException`
8. `should_callRepository_when_validInput`

**시그니처**:
```kotlin
operator fun invoke(
    customerId: Long,
    type: String,
    audioFile: File? = null,
    transcriptFile: File? = null,
    notes: String? = null
): Result<UploadResult>
```
**입력 검증**:
- `customerId < 0` → failure
- `type.isBlank()` → failure
- `audioFile == null && transcriptFile == null` → failure

**검증**: `./gradlew.bat test --tests "*.UploadConversationUseCaseTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-22 체크.
커밋: `git commit -m "test(DOM-006): UploadConversationUseCase 8 tests"`

---

### M-23: TDD UseCase Parameterized Tests 40 tests

**목표**: 40개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/parameterized-tests.md` 의 "§ 3 UseCase Boundary Values" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/domain/usecase/UseCaseParameterizedTest.kt`

**@ParameterizedTest 패턴** (JUnit 5):
```kotlin
@ParameterizedTest
@CsvSource(
    "-1, false",
    "0, true",
    "1, true",
    "999999, true",
    "-999, false"
)
fun should_validateCustomerId_when_variousInputs(
    customerId: Long,
    expectSuccess: Boolean
) = runTest(testDispatcher) {
    coEvery { customerRepository.getCustomerById(any()) } returns Result.success(mockCustomer)
    val result = getCardsByCustomerUseCase(customerId)
    assertEquals(expectSuccess, result.isSuccess)
}
```

**40개 테스트 분배**:
- GetCardsByCustomerUseCase boundary: 8개 (customerId, page, size 경계값)
- SearchCardsUseCase boundary: 10개 (query 길이, 특수문자, 빈 문자열 조합)
- GetCardDetailUseCase boundary: 7개 (cardId 경계값)
- GetKnowledgeUseCase boundary: 7개 (keywordId 경계값)
- UploadConversationUseCase boundary: 8개 (customerId, type, file 조합)

**검증**: `./gradlew.bat test --tests "*.UseCaseParameterizedTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-23 체크.
커밋: `git commit -m "test(DOM-007): UseCase parameterized boundary tests 40"`

---

### M-24: TDD UseCase Error Handling 30 tests

**목표**: 30개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/error-boundary-tests.md` 의 "§ 5 UseCase Error Handling" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/domain/usecase/UseCaseErrorHandlingTest.kt`

**30개 테스트 분배** (각 UseCase × 에러 유형):
- NetworkException 전파 검증 (5개 UseCase × 1 = 5)
- NotFoundException 전파 검증 (해당 UseCase × 2 = 4)
- TimeoutException 처리 검증 (3)
- ValidationException 발생 검증 (6)
- RuntimeException 전파 검증 (5)
- 에러 메시지 내용 검증 (7)

**에러 전파 패턴**:
```kotlin
@Test
fun should_propagateNetworkException_when_repositoryThrows() = runTest(testDispatcher) {
    val expectedException = NetworkException("연결 실패")
    coEvery { repository.getCustomers() } returns Result.failure(expectedException)
    val result = getCustomersUseCase()
    assertTrue(result.isFailure)
    assertIs<NetworkException>(result.exceptionOrNull())
    assertEquals("연결 실패", result.exceptionOrNull()?.message)
}
```

**검증**: `./gradlew.bat test --tests "*.UseCaseErrorHandlingTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-24 체크.
커밋: `git commit -m "test(DOM-008): UseCase error handling 30 tests"`

---

## Phase 3 완료 처리

모든 M-17 ~ M-24가 완료되면:
1. `./gradlew.bat test --no-daemon 2>&1 | tail -5` → 누적 테스트 수 확인
2. `git add app/src/`
3. `git commit -m "feat: Phase 3 domain usecases complete (132 tests)"`
4. `git push`
5. 세션 종료
