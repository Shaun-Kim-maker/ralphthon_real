# ViewModel State Machine / Transition Tests

Framework: JUnit 5 + MockK + Turbine (Flow testing)
Target: 120 tests

Naming convention: `should_[transitionTo]_when_[event]_from_[state]`

---

## 1. CustomerListViewModel Transitions (25 tests)

### Sealed Class

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Data(val customers: List<Customer>, val isRefreshing: Boolean) : UiState()
    object Empty : UiState()
    data class Error(val message: String) : UiState()
}
```

### Transition Table

| # | Test Name | From State | Event | Expected States (in order) |
|---|-----------|-----------|-------|---------------------------|
| 1 | `should_emitLoadingThenData_when_fetchSucceeds_from_init` | (init) | `loadCustomers()` | Loading → Data(customers, isRefreshing=false) |
| 2 | `should_emitLoadingThenEmpty_when_fetchReturnsEmpty_from_init` | (init) | `loadCustomers()` | Loading → Empty |
| 3 | `should_emitLoadingThenError_when_fetchFails_from_init` | (init) | `loadCustomers()` | Loading → Error(message) |
| 4 | `should_emitLoadingWithIsRefreshing_when_refreshCalled_from_data` | Data | `refresh()` | Data(isRefreshing=true) → Data(newCustomers, isRefreshing=false) |
| 5 | `should_preserveOldDataDuringRefresh_when_refreshStarts_from_data` | Data | `refresh()` | Data(old, isRefreshing=true) — old list still visible |
| 6 | `should_emitErrorAfterData_when_refreshFails_from_data` | Data | `refresh()` → failure | Data(isRefreshing=true) → Error(message) |
| 7 | `should_preserveDataUnderneath_when_refreshFails_from_data` | Data | `refresh()` → failure | Error state carries previous data OR ViewModel keeps lastData field |
| 8 | `should_emitLoadingThenData_when_retrySucceeds_from_error` | Error | `retry()` | Loading → Data(customers, isRefreshing=false) |
| 9 | `should_emitLoadingThenEmpty_when_retryReturnsEmpty_from_error` | Error | `retry()` | Loading → Empty |
| 10 | `should_emitNewError_when_retryFails_from_error` | Error | `retry()` → failure | Loading → Error(newMessage) |
| 11 | `should_emitNewError_when_retryFailsAgain_from_error` | Error | `retry()` → failure | Error(msg1) → Loading → Error(msg2) — message may differ |
| 12 | `should_emitExactlyTwoStates_when_fetchSucceeds_from_init` | (init) | `loadCustomers()` | Exactly [Loading, Data] — no extra emissions |
| 13 | `should_emitExactlyTwoStates_when_fetchFails_from_init` | (init) | `loadCustomers()` | Exactly [Loading, Error] — no extra emissions |
| 14 | `should_notEmitLoading_when_alreadyLoading` | Loading | second `loadCustomers()` call | No duplicate Loading emitted |
| 15 | `should_emitDataWithCorrectCount_when_fetchSucceeds_from_init` | (init) | `loadCustomers()` (3 items) | Data.customers.size == 3 |
| 16 | `should_emitEmptyNotData_when_fetchReturnsEmptyList_from_init` | (init) | `loadCustomers()` (0 items) | Empty (not Data([]) ) |
| 17 | `should_emitDataAfterEmpty_when_retrySucceeds_from_empty` | Empty | `retry()` → success | Loading → Data |
| 18 | `should_emitErrorAfterEmpty_when_retryFails_from_empty` | Empty | `retry()` → failure | Loading → Error |
| 19 | `should_emitIsRefreshingTrue_when_refreshStarts_from_data` | Data | `refresh()` | First emission has isRefreshing=true |
| 20 | `should_emitIsRefreshingFalse_when_refreshCompletes_from_data` | Data | `refresh()` → success | Final Data emission has isRefreshing=false |
| 21 | `should_notCrash_when_viewModelClearedDuringLoading` | Loading | `onCleared()` | No crash, coroutine cancelled cleanly |
| 22 | `should_emitCorrectErrorMessage_when_networkExceptionThrown_from_init` | (init) | Network timeout | Error.message contains network error text |
| 23 | `should_emitCorrectErrorMessage_when_serverErrorReturned_from_init` | (init) | HTTP 500 | Error.message contains server error description |
| 24 | `should_emitLoadingBeforeAnyNetworkCall_when_init` | (init) | ViewModel initialized | Loading is first state |
| 25 | `should_replaceExistingData_when_refreshSucceeds_from_data` | Data(oldList) | `refresh()` → newList | Final Data.customers equals newList (not merged) |

### Implementation Notes

```kotlin
class CustomerListViewModelTest {

    private val mockRepository = mockk<CustomerRepository>()
    private lateinit var viewModel: CustomerListViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = CustomerListViewModel(mockRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun should_emitLoadingThenData_when_fetchSucceeds_from_init() = runTest {
        val customers = listOf(Customer("1", "Kim", "Acme", 2, null))
        coEvery { mockRepository.getCustomers() } returns Result.success(customers)

        viewModel.uiState.test {
            viewModel.loadCustomers()
            assertEquals(UiState.Loading, awaitItem())
            val data = awaitItem() as UiState.Data
            assertEquals(1, data.customers.size)
            assertFalse(data.isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitLoadingThenError_when_fetchFails_from_init() = runTest {
        coEvery { mockRepository.getCustomers() } throws IOException("Network error")

        viewModel.uiState.test {
            viewModel.loadCustomers()
            assertEquals(UiState.Loading, awaitItem())
            val error = awaitItem() as UiState.Error
            assertTrue(error.message.isNotBlank())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## 2. CardNewsListViewModel Transitions (30 tests)

### Sealed Class

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Data(
        val cards: List<CardSummary>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean,
        val customerName: String
    ) : UiState()
    object Empty : UiState()
    data class Error(val message: String) : UiState()
}
```

### Transition Table

| # | Test Name | From State | Event | Expected States (in order) |
|---|-----------|-----------|-------|---------------------------|
| 26 | `should_emitLoadingThenData_when_fetchSucceeds_from_init` | (init) | `loadCards(customerId)` | Loading → Data(cards, hasMore, isLoadingMore=false, customerName) |
| 27 | `should_emitLoadingThenEmpty_when_fetchReturnsEmpty_from_init` | (init) | `loadCards(customerId)` (0 results) | Loading → Empty |
| 28 | `should_emitLoadingThenError_when_fetchFails_from_init` | (init) | `loadCards(customerId)` | Loading → Error(message) |
| 29 | `should_emitIsLoadingMoreTrue_when_loadNextCalled_from_data` | Data(page=0) | `loadNextPage()` | Data(isLoadingMore=true) |
| 30 | `should_appendCards_when_loadNextSucceeds_from_data` | Data(page=0, cards=[c1,c2]) | `loadNextPage()` → [c3,c4] | Data(isLoadingMore=true) → Data(cards=[c1,c2,c3,c4], isLoadingMore=false) |
| 31 | `should_preserveExistingCards_when_loadMoreFails_from_data` | Data(cards=[c1,c2]) | `loadNextPage()` → failure | Data(isLoadingMore=true) → Error / Data(cards=[c1,c2]) preserved |
| 32 | `should_setHasMoreFalse_when_lastPageLoaded_from_data` | Data(hasMore=true) | `loadNextPage()` → last page | Data(hasMore=false) |
| 33 | `should_notLoadMore_when_hasMoreIsFalse_from_data` | Data(hasMore=false) | `loadNextPage()` | No new state emissions |
| 34 | `should_notLoadMore_when_isLoadingMoreIsTrue_from_data` | Data(isLoadingMore=true) | `loadNextPage()` | No duplicate loading state |
| 35 | `should_emitLoadingThenFilteredData_when_filterApplied_from_data` | Data | `applyFilter(dateRange)` | Loading → Data(filteredCards) |
| 36 | `should_emitLoadingThenEmpty_when_filterYieldsNoResults_from_data` | Data | `applyFilter(strictRange)` | Loading → Empty |
| 37 | `should_resetToPage0_when_filterApplied_from_data` | Data(page=2) | `applyFilter(...)` | Loading → Data(page=0) |
| 38 | `should_emitPage0Data_when_initialLoad_from_init` | (init) | `loadCards()` | Data.cards is page 0 content |
| 39 | `should_emitPage1DataAppended_when_loadNextFromPage0` | Data(page=0) | `loadNextPage()` | Data(page=0+1 merged) |
| 40 | `should_emitPage2DataAppended_when_loadNextFromPage1` | Data(page=1) | `loadNextPage()` | Data(page=0+1+2 merged) |
| 41 | `should_setCustomerName_when_dataLoaded_from_init` | (init) | `loadCards(customerId="1")` | Data.customerName == "Kim" (from repository) |
| 42 | `should_preserveCustomerName_when_loadingMore_from_data` | Data(customerName="Kim") | `loadNextPage()` | All subsequent Data states retain customerName="Kim" |
| 43 | `should_preserveCustomerName_when_filterApplied_from_data` | Data(customerName="Kim") | `applyFilter(...)` | Loading → Data(customerName="Kim") |
| 44 | `should_emitError_when_loadMoreFails_from_data` | Data(isLoadingMore=true) | network failure | Error(message) |
| 45 | `should_retryFromBeginning_when_retryCalledAfterError_from_error` | Error | `retry()` | Loading → Data(page=0) |
| 46 | `should_emitLoadingThenEmpty_when_retryReturnsEmpty_from_error` | Error | `retry()` | Loading → Empty |
| 47 | `should_emitIsLoadingMoreFalse_when_loadMoreCompletes_from_data` | Data(isLoadingMore=true) | loadMore success | Final Data has isLoadingMore=false |
| 48 | `should_emitIsLoadingMoreFalse_when_loadMoreFails_from_data` | Data(isLoadingMore=true) | loadMore failure | Error or Data has isLoadingMore=false |
| 49 | `should_emitExactlyThreePageStates_when_twoLoadNextCalls` | (init) → Data → load → load | `loadNextPage()` twice | [Loading, Data(p0), Data(p0+p1), Data(p0+p1+p2)] |
| 50 | `should_notEmitDuplicateData_when_filterIsSameAsCurrent_from_data` | Data(filter=A) | `applyFilter(A)` again | No re-emission if filter unchanged |
| 51 | `should_emitEmptyOnInit_when_noCardsExistForCustomer_from_init` | (init) | `loadCards(customerId)` → 0 cards | Loading → Empty |
| 52 | `should_clearCards_when_newFilterApplied_from_data` | Data(cards=[c1..c10]) | `applyFilter(newFilter)` | Loading shows empty cards (reset), then Data(filteredCards) |
| 53 | `should_emitHasMoreTrue_when_morePageExists_from_init` | (init) | `loadCards()` (hasMore=true) | Data.hasMore == true |
| 54 | `should_notCrash_when_viewModelClearedDuringLoadMore` | Data(isLoadingMore=true) | `onCleared()` | No crash, coroutine cancelled cleanly |
| 55 | `should_emitFilteredEmpty_when_filterAppliedOnEmptyResult_from_data` | Data | `applyFilter(extremeRange)` | Loading → Empty |

---

## 3. CardDetailViewModel Transitions (35 tests)

### Sealed Classes

```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Data(val card: CardDetail, val panelState: PanelState?) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class PanelState {
    object Loading : PanelState()
    data class Data(val articles: List<Article>) : PanelState()
    object Empty : PanelState()
    data class Error(val message: String) : PanelState()
}
```

### Transition Table

| # | Test Name | From State | Event | Expected States (in order) |
|---|-----------|-----------|-------|---------------------------|
| 56 | `should_emitLoadingThenData_when_fetchSucceeds_from_init` | (init) | `loadCard(id)` | Loading → Data(card, panelState=null) |
| 57 | `should_emitLoadingThenError_when_fetchFails_from_init` | (init) | `loadCard(id)` | Loading → Error(message) |
| 58 | `should_emitDataWithNullPanel_when_cardLoadedWithNoKeyword_from_init` | (init) | `loadCard(id)` | Data.panelState is null |
| 59 | `should_emitDataWithPanelLoading_when_keywordClicked_from_data` | Data(panel=null) | `onKeywordClick(kw1)` | Data(card, panelState=PanelState.Loading) |
| 60 | `should_emitDataWithPanelData_when_knowledgeFetchSucceeds_from_data` | Data(panel=Loading) | knowledge fetch success | Data(card, panelState=PanelState.Data(articles)) |
| 61 | `should_emitDataWithPanelEmpty_when_knowledgeFetchReturnsEmpty_from_data` | Data(panel=Loading) | knowledge fetch returns 0 articles | Data(card, panelState=PanelState.Empty) |
| 62 | `should_emitDataWithPanelError_when_knowledgeFetchFails_from_data` | Data(panel=Loading) | knowledge fetch fails | Data(card, panelState=PanelState.Error(msg)) |
| 63 | `should_emitDataWithNullPanel_when_dismissPanel_from_dataWithPanel` | Data(panel=Data) | `dismissPanel()` | Data(card, panelState=null) |
| 64 | `should_emitDataWithNullPanel_when_dismissPanel_from_dataWithPanelError` | Data(panel=Error) | `dismissPanel()` | Data(card, panelState=null) |
| 65 | `should_emitDataWithNullPanel_when_dismissPanel_from_dataWithPanelEmpty` | Data(panel=Empty) | `dismissPanel()` | Data(card, panelState=null) |
| 66 | `should_preserveCard_when_panelStateChanges` | Data(card=C, panel=null) | `onKeywordClick(kw)` | Data.card == C in all subsequent emissions |
| 67 | `should_emitPanelLoadingForNewKeyword_when_keywordChanged_from_dataPanelData` | Data(panel=Data(kw1)) | `onKeywordClick(kw2)` | Data(panel=PanelState.Loading) |
| 68 | `should_emitPanelDataForNewKeyword_when_knowledgeFetchSucceeds_after_keywordSwitch` | Data(panel=Loading(kw2)) | knowledge fetch kw2 success | Data(panel=PanelState.Data(kw2Articles)) |
| 69 | `should_replacePanelArticles_when_secondKeywordLoaded_from_dataPanelData` | Data(panel=Data(kw1Articles)) | → Data(panel=Data(kw2Articles)) | kw2Articles != kw1Articles; not merged |
| 70 | `should_emitExactlyFourStates_when_keywordClickAndSuccess` | Data(panel=null) | click → fetch success | [Data(null), Data(Loading), Data(Data(articles))] — 3 total panel transitions |
| 71 | `should_cancelPreviousPanelFetch_when_secondKeywordClickedQuickly` | Data(panel=Loading(kw1)) | `onKeywordClick(kw2)` before kw1 completes | Only kw2 result applied; kw1 result discarded |
| 72 | `should_emitLoadingThenDataAgain_when_retryAfterPanelError` | Data(panel=Error) | `retryPanel()` | Data(panel=Loading) → Data(panel=Data) |
| 73 | `should_notEmitPanelLoading_when_sameKeywordClickedTwice` | Data(panel=Data(kw1)) | `onKeywordClick(kw1)` again | No re-fetch if same keywordId |
| 74 | `should_emitPanelEmpty_when_knowledgeFetchReturnsEmptyArticles` | Data(panel=Loading) | fetch returns articles=[] | Data(panel=PanelState.Empty) |
| 75 | `should_preserveCardData_when_panelFetchFails` | Data(card=C, panel=Loading) | fetch fails | Data(card=C, panel=Error) — card C unchanged |
| 76 | `should_emitLoading_when_differentCardLoaded_from_data` | Data(card=C1) | `loadCard(differentId)` | Loading → Data(card=C2, panel=null) |
| 77 | `should_resetPanelToNull_when_newCardLoaded_from_dataWithPanel` | Data(card=C1, panel=Data) | `loadCard(C2.id)` | Loading → Data(card=C2, panel=null) |
| 78 | `should_emitError_when_cardFetchFailsOnRetry_from_error` | Error | `retry()` → failure | Loading → Error(newMessage) |
| 79 | `should_emitData_when_cardFetchSucceedsOnRetry_from_error` | Error | `retry()` → success | Loading → Data(card, panel=null) |
| 80 | `should_notCrash_when_dismissCalledWithNoPanel_from_dataNullPanel` | Data(panel=null) | `dismissPanel()` | No state change, no crash |
| 81 | `should_emitPanelLoadingImmediately_when_keywordClicked_from_data` | Data(panel=null) | `onKeywordClick(kw)` | PanelState.Loading emitted before any network call completes |
| 82 | `should_notLeakPanelState_when_cardReloaded_from_dataWithPanel` | Data(panel=Data(articles)) | `loadCard(id)` (same id) | Loading → Data(panel=null) — panel cleared |
| 83 | `should_emitCorrectArticleCount_when_panelDataLoaded` | Data(panel=Loading) | fetch returns 3 articles | PanelState.Data.articles.size == 3 |
| 84 | `should_emitPanelErrorMessage_when_knowledgeFetchFails` | Data(panel=Loading) | IOException | PanelState.Error.message is non-blank |
| 85 | `should_emitPanelErrorThenRecoverToData_when_retrySucceeds` | Data(panel=Error) | `retryPanel()` → success | Data(panel=Loading) → Data(panel=Data(articles)) |
| 86 | `should_emitPanelErrorThenRecoverToEmpty_when_retryReturnsEmpty` | Data(panel=Error) | `retryPanel()` → empty | Data(panel=Loading) → Data(panel=Empty) |
| 87 | `should_emitPanelErrorThenNewError_when_retryFails` | Data(panel=Error(msg1)) | `retryPanel()` → failure | Data(panel=Loading) → Data(panel=Error(msg2)) |
| 88 | `should_emitLoadingThenDataWithPanelNull_when_initWithKeywordIdParam` | (init) | `loadCard(id)` | Data.panelState is always null on initial card load |
| 89 | `should_maintainCardIntegrityAcrossMultiplePanelSwitches` | Data(panel=null) | click kw1 → dismiss → click kw2 → dismiss | card field same in all Data emissions |
| 90 | `should_emitCorrectKeywordIdInPanelFetch_when_differentKeywordsClicked` | Data(panel=null) | `onKeywordClick("kw-99")` | Repository called with keywordId="kw-99" |

---

## 4. SearchViewModel Transitions (30 tests)

### Sealed Class

```kotlin
sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    data class Results(
        val cards: List<CardSummary>,
        val totalCount: Int,
        val hasMore: Boolean,
        val query: String,
        val filters: SearchFilters
    ) : UiState()
    data class Empty(val query: String) : UiState()
    data class Error(val message: String) : UiState()
}
```

### Transition Table

| # | Test Name | From State | Event | Expected States (in order) |
|---|-----------|-----------|-------|---------------------------|
| 91 | `should_stayInitial_when_queryIsBlank_from_initial` | Initial | `onQueryChanged("")` | Initial (no emission) |
| 92 | `should_stayInitial_when_queryIsWhitespaceOnly_from_initial` | Initial | `onQueryChanged("   ")` | Initial (no emission) |
| 93 | `should_emitLoadingAfterDebounce_when_queryTyped_from_initial` | Initial | `onQueryChanged("hello")` + 300ms | Loading |
| 94 | `should_emitResultsAfterLoading_when_searchSucceeds_from_initial` | Initial | `onQueryChanged("hello")` + 300ms → success | Loading → Results(cards, query="hello") |
| 95 | `should_emitEmptyAfterLoading_when_searchReturnsNoResults_from_initial` | Initial | `onQueryChanged("zzz")` + 300ms → 0 results | Loading → Empty(query="zzz") |
| 96 | `should_emitErrorAfterLoading_when_searchFails_from_initial` | Initial | `onQueryChanged("hello")` + 300ms → failure | Loading → Error(message) |
| 97 | `should_notEmitLoading_when_queryChangedWithinDebounce_from_initial` | Initial | type "h", "he", "hel" within 300ms | No Loading until debounce settles |
| 98 | `should_emitLoadingOnce_when_multipleKeystrokesWithinDebounce_from_initial` | Initial | 5 keystrokes within 300ms | Exactly one Loading emission after debounce |
| 99 | `should_emitResultsWithNewQuery_when_queryChangedAfterResults_from_results` | Results(query="hello") | `onQueryChanged("world")` + 300ms → success | Loading → Results(query="world") |
| 100 | `should_replaceResults_when_newQuerySucceeds_from_results` | Results(cards=[c1,c2]) | new query → [c3,c4] | Results.cards == [c3,c4] (not merged) |
| 101 | `should_emitInitial_when_queryCleared_from_results` | Results | `clearQuery()` | Initial |
| 102 | `should_emitInitial_when_queryCleared_from_empty` | Empty | `clearQuery()` | Initial |
| 103 | `should_emitInitial_when_queryCleared_from_error` | Error | `clearQuery()` | Initial |
| 104 | `should_emitLoadingThenResults_when_filterApplied_from_results` | Results(filters=none) | `applyFilter(customerId="1")` | Loading → Results(filters=customerId="1") |
| 105 | `should_emitLoadingThenEmpty_when_filterYieldsNoResults_from_results` | Results | `applyFilter(strictFilter)` | Loading → Empty(query) |
| 106 | `should_emitLoadingThenResults_when_filterCleared_from_results` | Results(filters=customerId) | `clearFilter()` | Loading → Results(filters=none) |
| 107 | `should_preserveQuery_when_filterApplied_from_results` | Results(query="hello", filters=none) | `applyFilter(dateRange)` | Results.query == "hello" in new state |
| 108 | `should_emitHasMoreTrue_when_searchHasMorePages_from_initial` | Initial | search → hasMore=true | Results.hasMore == true |
| 109 | `should_appendResults_when_loadNextPageCalled_from_results` | Results(cards=[c1,c2], hasMore=true) | `loadNextPage()` → [c3,c4] | Results(cards=[c1,c2,c3,c4]) |
| 110 | `should_setHasMoreFalse_when_lastPageLoaded_from_results` | Results(hasMore=true) | `loadNextPage()` → last page | Results.hasMore == false |
| 111 | `should_notLoadMore_when_hasMoreFalse_from_results` | Results(hasMore=false) | `loadNextPage()` | No new emissions |
| 112 | `should_emitLoadingBeforeDebounce_when_explicitSearchCalled_from_initial` | Initial | `search("hello")` (explicit, no debounce) | Loading immediately → Results |
| 113 | `should_cancelPreviousSearch_when_queryChangedBeforeDebounce_from_initial` | Initial | type "ab", then "abc" before debounce | Only "abc" search fires |
| 114 | `should_emitLoadingThenResults_when_retryAfterError_from_error` | Error | `retry()` | Loading → Results |
| 115 | `should_emitLoadingThenEmpty_when_retryAfterErrorReturnsEmpty_from_error` | Error | `retry()` → 0 results | Loading → Empty(lastQuery) |
| 116 | `should_preserveQueryInEmpty_when_searchReturnsNoResults_from_initial` | Initial | search("xyz") → 0 results | Empty.query == "xyz" |
| 117 | `should_preserveQueryInError_when_searchFails_from_initial` | Initial | search("hello") → failure | Error state retains last query for retry |
| 118 | `should_emitResultsWithCorrectTotalCount_when_searchSucceeds_from_initial` | Initial | search → totalCount=50 | Results.totalCount == 50 |
| 119 | `should_emitResultsWithCorrectFilters_when_filterApplied_from_results` | Results(filters=none) | `applyFilter(f)` | Results.filters == f |
| 120 | `should_returnToInitial_when_queryBackspacedToEmpty_from_results` | Results | `onQueryChanged("")` | Initial |

### Implementation Notes

```kotlin
class SearchViewModelTest {

    private val mockRepository = mockk<CardRepository>()
    private lateinit var viewModel: SearchViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = SearchViewModel(mockRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Debounce test: advance virtual time past debounce threshold
    @Test
    fun should_emitLoadingAfterDebounce_when_queryTyped_from_initial() = runTest {
        val cards = listOf(CardSummary("1", "c1", "Title", "2024-01-01", "POSITIVE"))
        coEvery { mockRepository.searchCards(any(), any()) } returns Result.success(
            SearchResult(cards, totalCount = 1, page = 0, size = 20, hasMore = false)
        )

        viewModel.uiState.test {
            assertEquals(UiState.Initial, awaitItem())  // initial state
            viewModel.onQueryChanged("hello")
            expectNoEvents()  // nothing before debounce
            advanceTimeBy(300)  // trigger debounce
            assertEquals(UiState.Loading, awaitItem())
            val result = awaitItem() as UiState.Results
            assertEquals("hello", result.query)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_stayInitial_when_queryIsBlank_from_initial() = runTest {
        viewModel.uiState.test {
            assertEquals(UiState.Initial, awaitItem())
            viewModel.onQueryChanged("")
            advanceTimeBy(500)
            expectNoEvents()  // no additional emissions
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_emitInitial_when_queryCleared_from_results() = runTest {
        // Setup: put viewModel in Results state
        val cards = listOf(CardSummary("1", "c1", "Title", "2024-01-01", "POSITIVE"))
        coEvery { mockRepository.searchCards(any(), any()) } returns Result.success(
            SearchResult(cards, 1, 0, 20, false)
        )
        viewModel.uiState.test {
            awaitItem()  // Initial
            viewModel.onQueryChanged("hello")
            advanceTimeBy(300)
            awaitItem()  // Loading
            awaitItem()  // Results

            viewModel.clearQuery()
            assertEquals(UiState.Initial, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun should_cancelPreviousSearch_when_queryChangedBeforeDebounce_from_initial() = runTest {
        coEvery { mockRepository.searchCards("abc", any()) } returns Result.success(
            SearchResult(emptyList(), 0, 0, 20, false)
        )

        viewModel.uiState.test {
            awaitItem()  // Initial
            viewModel.onQueryChanged("ab")
            advanceTimeBy(100)  // within debounce
            viewModel.onQueryChanged("abc")
            advanceTimeBy(300)  // debounce fires for "abc" only
            awaitItem()  // Loading
            awaitItem()  // Empty or Results

            // Verify repository called only once (for "abc", not "ab")
            coVerify(exactly = 1) { mockRepository.searchCards(any(), any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

---

## Shared Test Infrastructure

```kotlin
// build.gradle.kts (test dependencies)
testImplementation("app.cash.turbine:turbine:1.1.0")
testImplementation("io.mockk:mockk:1.13.10")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

// Base class for ViewModel tests
abstract class ViewModelTestBase {

    @BeforeEach
    fun setUpDispatchers() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDownDispatchers() {
        Dispatchers.resetMain()
    }
}

// Common state assertion helpers
fun <T> Flow<T>.assertStatesInOrder(vararg expected: T) = runTest {
    test {
        for (state in expected) {
            assertEquals(state, awaitItem())
        }
        cancelAndIgnoreRemainingEvents()
    }
}
```
