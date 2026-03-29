RALPH_BACKLOG.md를 읽고 첫 번째 미완료(- [ ]) 마일스톤을 찾아서 실행하라.
이미 완료된(- [x]) 마일스톤은 건너뛴다.
이 Phase의 마일스톤이 모두 완료되면 세션을 종료한다.

세션 시작 체크:
1. RALPH_BACKLOG.md 읽기
2. git log --oneline -3
3. ./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3

---

## Phase 4: ViewModels
마일스톤 M-25 ~ M-34를 순서대로 실행한다.
각 마일스톤 완료 시 RALPH_BACKLOG.md의 해당 항목을 `- [x]`로 업데이트하라.
같은 에러 3회 반복 시 `- [SKIP]`으로 표시하고 다음 마일스톤으로 진행하라.

**전제**: Phase 1~3(M-01~M-24)가 완료된 상태.
존재하는 UseCase들:
- `domain/usecase/GetCustomersUseCase`
- `domain/usecase/GetCardsByCustomerUseCase`
- `domain/usecase/GetCardDetailUseCase`
- `domain/usecase/SearchCardsUseCase`
- `domain/usecase/GetKnowledgeUseCase`
- `domain/usecase/UploadConversationUseCase`

---

## ViewModel 공통 패턴

모든 ViewModel은 다음 패턴을 따른다:

```kotlin
sealed class XxxUiState {
    object Loading : XxxUiState()
    data class Data(val items: List<T>) : XxxUiState()
    object Empty : XxxUiState()
    data class Error(val message: String) : XxxUiState()
}

@HiltViewModel
class XxxViewModel @Inject constructor(
    private val xxxUseCase: XxxUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<XxxUiState>(XxxUiState.Loading)
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = XxxUiState.Loading
            xxxUseCase().fold(
                onSuccess = { data ->
                    _uiState.value = if (data.isEmpty()) XxxUiState.Empty
                                     else XxxUiState.Data(data)
                },
                onFailure = { error ->
                    _uiState.value = XxxUiState.Error(
                        error.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                }
            )
        }
    }
}
```

## ViewModel 테스트 공통 패턴

```kotlin
@ExtendWith(MockKExtension::class)
class XxxViewModelTest {
    @MockK
    private lateinit var xxxUseCase: XxxUseCase
    private lateinit var viewModel: XxxViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // useCase 기본 mock 설정
        coEvery { xxxUseCase() } returns Result.success(mockData)
        viewModel = XxxViewModel(xxxUseCase)
    }

    @AfterEach
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun should_showLoading_when_loadingStarts() = runTest(testDispatcher) {
        viewModel.uiState.test {
            assertEquals(XxxUiState.Loading, awaitItem())
            // ... 추가 assertion
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

**Turbine 임포트**: `import app.cash.turbine.test`

---

### M-25: TDD CustomerListViewModel 12 tests

**목표**: 12개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 1 CustomerListViewModel" 섹션

**Tier 1 TDD**: 테스트 먼저

**파일들**:
- `app/src/test/java/com/ralphthon/app/ui/customer/CustomerListViewModelTest.kt`
- `app/src/main/java/com/ralphthon/app/ui/customer/CustomerListViewModel.kt`

**12개 테스트 커버리지**:
1. `should_emitLoading_when_loadCustomersStarts`
2. `should_emitData_when_customersLoadedSuccessfully`
3. `should_emitEmpty_when_noCustomersReturned`
4. `should_emitError_when_networkExceptionThrown`
5. `should_emitError_when_repositoryFails`
6. `should_callGetCustomersUseCase_when_initialized`
7. `should_reloadData_when_refreshCalled`
8. `should_emitLoadingThenData_when_refreshCalled`
9. `should_preserveErrorMessage_when_exceptionHasMessage`
10. `should_showDefaultErrorMessage_when_exceptionHasNoMessage`
11. `should_emitLoadingThenEmpty_when_emptyListReturned`
12. `should_emitLoadingThenError_when_exceptionThrown`

**CustomerListViewModel**:
- `sealed class CustomerListUiState`: Loading, Data(customers: List<Customer>), Empty, Error(message: String)
- `fun refresh()` — 데이터 재로드
- init 블록에서 `loadCustomers()` 호출

**검증**: `./gradlew.bat test --tests "*.CustomerListViewModelTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-25 체크.
커밋: `git commit -m "test(VM-001): CustomerListViewModel 12 tests"`

---

### M-26: TDD CardNewsListViewModel 14 tests

**목표**: 14개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 2 CardNewsListViewModel" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/ui/card/CardNewsListViewModelTest.kt`
- `app/src/main/java/com/ralphthon/app/ui/card/CardNewsListViewModel.kt`

**14개 테스트 커버리지**:
1. `should_emitLoading_when_loadCardsStarts`
2. `should_emitData_when_cardsLoadedSuccessfully`
3. `should_emitEmpty_when_noCardsForCustomer`
4. `should_emitError_when_networkException`
5. `should_callUseCaseWithCustomerId_when_initialized`
6. `should_emitLoadingThenData_when_refreshCalled`
7. `should_loadNextPage_when_scrolledToBottom`
8. `should_appendCards_when_nextPageLoaded`
9. `should_notLoadMore_when_hasMoreIsFalse`
10. `should_showLoadingMore_when_nextPageLoading`
11. `should_preserveExistingData_when_nextPageFails`
12. `should_emitError_when_firstPageFails`
13. `should_filterByType_when_filterApplied`
14. `should_callUseCaseWithNewFilter_when_filterChanged`

**CardNewsListViewModel**:
- 생성자: `@AssistedInject constructor(useCase, @Assisted customerId: Long)`
- 또는 SavedStateHandle에서 customerId 읽기 (더 단순한 방법 사용)
- `sealed class CardNewsListUiState`: Loading, Data(cards, hasMore, isLoadingMore), Empty, Error(message)
- `fun loadMore()`, `fun refresh()`, `fun applyFilter(type: ConversationType?)`

**검증**: `./gradlew.bat test --tests "*.CardNewsListViewModelTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-26 체크.
커밋: `git commit -m "test(VM-002): CardNewsListViewModel 14 tests"`

---

### M-27: TDD CardDetailViewModel 12 tests

**목표**: 12개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 3 CardDetailViewModel" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/ui/card/CardDetailViewModelTest.kt`
- `app/src/main/java/com/ralphthon/app/ui/card/CardDetailViewModel.kt`

**12개 테스트 커버리지**:
1. `should_emitLoading_when_loadCardStarts`
2. `should_emitData_when_cardLoadedSuccessfully`
3. `should_emitError_when_cardNotFound`
4. `should_emitError_when_networkException`
5. `should_loadKnowledge_when_keywordSelected`
6. `should_emitKnowledgeData_when_knowledgeLoaded`
7. `should_emitKnowledgeError_when_knowledgeFails`
8. `should_callCardUseCase_when_initialized`
9. `should_callKnowledgeUseCase_when_keywordTapped`
10. `should_showStatements_when_cardHasStatements`
11. `should_showKeywords_when_cardHasKeywords`
12. `should_clearKnowledge_when_knowledgePanelClosed`

**CardDetailViewModel**:
- 두 UseCase 주입: `GetCardDetailUseCase`, `GetKnowledgeUseCase`
- `sealed class CardDetailUiState`: Loading, Data(card: ContextCard), Error(message: String)
- `sealed class KnowledgeUiState`: Idle, Loading, Data(result: KnowledgeResult), Error(message: String)
- `val uiState: StateFlow<CardDetailUiState>`
- `val knowledgeState: StateFlow<KnowledgeUiState>`
- `fun loadKnowledge(keywordId: Long)`, `fun closeKnowledge()`

**검증**: `./gradlew.bat test --tests "*.CardDetailViewModelTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-27 체크.
커밋: `git commit -m "test(VM-003): CardDetailViewModel 12 tests"`

---

### M-28: TDD SearchViewModel 14 tests

**목표**: 14개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 4 SearchViewModel" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/ui/search/SearchViewModelTest.kt`
- `app/src/main/java/com/ralphthon/app/ui/search/SearchViewModel.kt`

**14개 테스트 커버리지**:
1. `should_emitIdle_when_initialized`
2. `should_emitLoading_when_searchStarted`
3. `should_emitResults_when_searchSucceeds`
4. `should_emitEmpty_when_noResults`
5. `should_emitError_when_queryTooShort`
6. `should_emitError_when_networkException`
7. `should_debounceSearch_when_queryChangedRapidly`
8. `should_cancelPreviousSearch_when_newQueryStarted`
9. `should_emitLoadingMore_when_nextPageRequested`
10. `should_appendResults_when_nextPageLoaded`
11. `should_notLoadMore_when_hasMoreIsFalse`
12. `should_resetResults_when_queryCleared`
13. `should_filterByCustomer_when_customerFilterApplied`
14. `should_callUseCaseWithAllFilters_when_filtersSet`

**SearchViewModel**:
- `sealed class SearchUiState`: Idle, Loading, Results(cards, hasMore, isLoadingMore), Empty, Error(message)
- `fun search(query: String)`, `fun loadMore()`, `fun clearSearch()`
- `fun setCustomerFilter(customerId: Long?)`, `fun setDateRange(from: String?, to: String?)`
- debounce 구현 (선택, 테스트에 필요하면): 300ms delay + Job cancel

**검증**: `./gradlew.bat test --tests "*.SearchViewModelTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-28 체크.
커밋: `git commit -m "test(VM-004): SearchViewModel 14 tests"`

---

### M-29: ViewModel State Machine Tests - CustomerList 25 tests

**목표**: 25개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/state-machine-tests.md` 의 "§ 1 CustomerList State Machine" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/ui/customer/CustomerListStateTest.kt`

**Turbine 사용 패턴**:
```kotlin
@Test
fun should_transitionLoadingToData_when_customersLoaded() = runTest(testDispatcher) {
    coEvery { useCase() } returns Result.success(mockCustomers)
    viewModel.uiState.test {
        assertIs<CustomerListUiState.Loading>(awaitItem())
        assertIs<CustomerListUiState.Data>(awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

**25개 상태 전이 커버리지**:
- Loading → Data (성공 케이스)
- Loading → Empty (빈 리스트)
- Loading → Error (각 예외 타입별)
- Data → Loading → Data (refresh)
- Data → Loading → Error (refresh 실패)
- Error → Loading → Data (retry)
- Error → Loading → Error (retry 실패)
- Error 메시지 내용 검증 (5개)
- Data 내 customer 수 검증 (5개)

**검증**: `./gradlew.bat test --tests "*.CustomerListStateTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-29 체크.
커밋: `git commit -m "test(VM-005): CustomerList state machine 25 tests"`

---

### M-30: ViewModel State Machine Tests - CardNewsList 30 tests

**목표**: 30개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/state-machine-tests.md` 의 "§ 2 CardNewsList State Machine" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/ui/card/CardNewsListStateTest.kt`

**30개 상태 전이 커버리지**:
- Loading → Data → LoadingMore → Data (페이지네이션)
- Loading → Data → LoadingMore → Error (페이지네이션 실패, 기존 데이터 유지)
- Loading → Empty (첫 로드 시 빈 결과)
- Data → Data (필터 변경 후 재로드)
- hasMore = false 시 loadMore() 무시 검증
- 페이지 번호 누적 검증 (page 0 → 1 → 2)
- filter 적용 후 page 리셋 검증
- 각 상태 내 데이터 필드 검증 포함

**검증**: `./gradlew.bat test --tests "*.CardNewsListStateTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-30 체크.
커밋: `git commit -m "test(VM-006): CardNewsList state machine 30 tests"`

---

### M-31: ViewModel State Machine Tests - CardDetail 35 tests

**목표**: 35개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/state-machine-tests.md` 의 "§ 3 CardDetail State Machine" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/ui/card/CardDetailStateTest.kt`

**35개 상태 전이 커버리지**:
- CardDetail: Loading → Data / Error (기본)
- Knowledge: Idle → Loading → Data (키워드 탭)
- Knowledge: Idle → Loading → Error (로드 실패)
- Knowledge: Data → Idle (패널 닫기)
- Knowledge: Data → Loading → Data (다른 키워드 탭)
- 두 StateFlow 동시 구독 검증
- 카드 데이터와 지식 데이터 독립성 검증
- 에러 후 다른 키워드 선택 시 복구 검증
- statements, keywords 목록 내용 검증

**검증**: `./gradlew.bat test --tests "*.CardDetailStateTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-31 체크.
커밋: `git commit -m "test(VM-007): CardDetail state machine 35 tests"`

---

### M-32: ViewModel State Machine Tests - Search 30 tests

**목표**: 30개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/state-machine-tests.md` 의 "§ 4 Search State Machine" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/ui/search/SearchStateTest.kt`

**30개 상태 전이 커버리지**:
- Idle → Loading → Results (검색 성공)
- Idle → Loading → Empty (결과 없음)
- Idle → Error (쿼리 너무 짧음 — validation error)
- Results → Loading → Results (새 검색어)
- Results → LoadingMore → Results (다음 페이지)
- Results → Idle (검색 초기화)
- 필터 변경 후 Results 리셋 검증
- hasMore 플래그 전이 검증
- 연속 쿼리 변경 시 최신 결과만 반영 검증

**검증**: `./gradlew.bat test --tests "*.SearchStateTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-32 체크.
커밋: `git commit -m "test(VM-008): Search state machine 30 tests"`

---

### M-33: ViewModel Error State Tests 40 tests

**목표**: 40개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/error-boundary-tests.md` 의 "§ 6 ViewModel Error States" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/ui/viewmodel/ViewModelErrorTest.kt`

**40개 에러 테스트 분배**:
- NetworkException 메시지 → 한국어 에러 메시지 검증 (10개, 각 ViewModel별)
- NotFoundException 메시지 → "찾을 수 없습니다" 포함 검증 (5개)
- TimeoutException → "시간 초과" 관련 메시지 검증 (5개)
- 예외 메시지 없을 때 기본 메시지 검증 (10개)
- 에러 후 retry 동작 검증 (10개)

**에러 메시지 매핑 예시** (ViewModel에 구현):
```kotlin
onFailure = { error ->
    val message = when (error) {
        is NetworkException -> "네트워크 오류가 발생했습니다"
        is NotFoundException -> "데이터를 찾을 수 없습니다"
        is TimeoutException -> "요청 시간이 초과되었습니다"
        else -> error.message ?: "알 수 없는 오류가 발생했습니다"
    }
    _uiState.value = XxxUiState.Error(message)
}
```

**검증**: `./gradlew.bat test --tests "*.ViewModelErrorTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-33 체크.
커밋: `git commit -m "test(VM-009): ViewModel error states 40 tests"`

---

### M-34: Coroutine & Flow Tests 20 tests

**목표**: 20개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 16 Coroutine and Flow" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/ui/viewmodel/CoroutineFlowTest.kt`

**20개 테스트 커버리지**:
- StateFlow collect 정상 동작 검증 (4개)
- viewModelScope 취소 시 코루틴 취소 검증 (4개)
- TestDispatcher로 시간 제어 검증 (4개)
- 동시 다중 collect 검증 (4개)
- Flow 완료/에러 후 재구독 검증 (4개)

**TestDispatcher 사용 패턴**:
```kotlin
@Test
fun should_cancelCoroutine_when_viewModelCleared() = runTest(testDispatcher) {
    viewModel.uiState.test {
        awaitItem() // Loading
        viewModel.onCleared() // 내부 메서드 (protected 접근 필요 or clear())
        cancelAndIgnoreRemainingEvents()
    }
}
```

**검증**: `./gradlew.bat test --tests "*.CoroutineFlowTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-34 체크.
커밋: `git commit -m "test(VM-010): Coroutine and Flow tests 20"`

---

## Phase 4 완료 처리

모든 M-25 ~ M-34가 완료되면:
1. `./gradlew.bat test --no-daemon 2>&1 | tail -5` → 누적 테스트 수 확인 (목표: 466+)
2. `git add app/src/`
3. `git commit -m "feat: Phase 4 viewmodels complete (232 tests)"`
4. `git push`
5. 세션 종료
