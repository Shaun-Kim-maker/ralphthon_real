# Concurrency Tests — Coroutine Race Conditions & Dispatcher Verification

**Target:** 80 tests
**Framework:** JUnit 5 + MockK + kotlinx-coroutines-test (runTest, TestDispatcher, advanceTimeBy, advanceUntilIdle) + Turbine
**File path:** `app/src/test/java/com/ralphthon/app/concurrency/`

---

## 1. Dispatcher Tests (15 tests)

**File:** `app/src/test/java/com/ralphthon/app/concurrency/DispatcherTest.kt`

| # | Test Name | Setup | Action | Assertion |
|---|-----------|-------|--------|-----------|
| 1 | `should_runOnIODispatcher_when_useCaseExecutes` | `StandardTestDispatcher`, mock repository, `UnconfinedTestDispatcher` as IO | Call `useCase.invoke()` | Verify thread name contains `IO` or dispatcher is IO via `TestDispatcher` context capture |
| 2 | `should_updateStateOnMainDispatcher_when_viewModelReceivesData` | `StandardTestDispatcher` set as Main via `Dispatchers.setMain`, mock useCase returns data | Call `viewModel.loadData()` then `advanceUntilIdle()` | `viewModel.uiState.value` is updated; no `IllegalStateException` from wrong dispatcher |
| 3 | `should_notBlockMainThread_when_repositoryFetches` | `StandardTestDispatcher`, mock repo with 100ms delay via `delay()` | Launch `viewModel.load()`, check state before `advanceUntilIdle()` | State is `Loading` immediately; `Success` only after `advanceUntilIdle()` |
| 4 | `should_substituteTestDispatcher_when_dispatcherInjected` | Inject `TestDispatcher` via `CoroutineDispatchers` wrapper | Run any UseCase | No coroutine runs before `advanceUntilIdle()` — confirms test dispatcher is in control |
| 5 | `should_runMapperOnDefaultDispatcher_when_mappingLargePayload` | Inject `Dispatchers.Default` via test wrapper, heavy mapper mock | Call mapper inside `withContext(Dispatchers.Default)` | Dispatcher context captured equals `Default`; mapping completes without error |
| 6 | `should_propagateDispatcherToChildren_when_parentCoroutineLaunches` | `StandardTestDispatcher` as parent scope | `launch` child coroutines inside parent scope | All children inherit same `TestDispatcher`; controlled by `advanceUntilIdle()` |
| 7 | `should_notLeakCoroutine_when_dispatcherSwitched` | `StandardTestDispatcher`, count active coroutines before and after | Call use case with `withContext(IO)` then await | Active coroutine count after completion equals count before |
| 8 | `should_switchToMain_when_viewModelCollectsFlow` | Flow emitting on IO, collected in ViewModel on Main | `advanceUntilIdle()` | `StateFlow` updated without dispatcher mismatch exception |
| 9 | `should_completeOnIODispatcher_when_repositoryWritesToDb` | Mock DAO with delay; `UnconfinedTestDispatcher` | Call repository write, `advanceUntilIdle()` | Repository completes; no exception; result propagated to ViewModel |
| 10 | `should_respectDispatcherHierarchy_when_nestedWithContext` | Three nested `withContext` blocks: Default → IO → Main | Execute nested chain | Each block runs; final result on Main dispatcher |
| 11 | `should_notRunEagerly_when_standardTestDispatcherUsed` | `StandardTestDispatcher` (NOT Unconfined) | Launch coroutine without `advanceUntilIdle()` | Coroutine body has NOT executed yet |
| 12 | `should_runEagerly_when_unconfinedTestDispatcherUsed` | `UnconfinedTestDispatcher` | Launch coroutine | Coroutine body executes immediately without manual advance |
| 13 | `should_handleDispatcherException_when_IOThrows` | Mock repository throws on IO dispatcher | Call use case | Exception caught; `uiState` emits `Error`; no crash |
| 14 | `should_cleanUpDispatcher_when_testTearDown` | `Dispatchers.setMain(testDispatcher)` in `@BeforeEach` | Run test | `Dispatchers.resetMain()` called in `@AfterEach`; no leftover test dispatcher |
| 15 | `should_runPaginationOnIO_when_nextPageRequested` | Mock paginated API; `StandardTestDispatcher` | Call `viewModel.loadNextPage()`, `advanceUntilIdle()` | Data appended; dispatcher for API call confirmed as IO |

---

## 2. Cancellation Tests (20 tests)

**File:** `app/src/test/java/com/ralphthon/app/concurrency/CancellationTest.kt`

| # | Test Name | Setup | Action | Assertion |
|---|-----------|-------|--------|-----------|
| 16 | `should_cancelAllJobs_when_viewModelCleared` | `StandardTestDispatcher`, ViewModel with active coroutines | Call `viewModel.onCleared()` | All jobs in `viewModelScope` are cancelled; `isActive == false` |
| 17 | `should_cancelPreviousSearch_when_newSearchIssued` | Mock API with 500ms delay; record call count | Call `search("a")` then `search("b")` without advance | Only `search("b")` completes; `search("a")` job is cancelled |
| 18 | `should_cancelPreviousPageLoad_when_newPageRequested` | Mock API with delay; ViewModel with job tracking | Request page 1, then page 2 before page 1 completes | Only page 2 result reflected in state; page 1 job is cancelled |
| 19 | `should_cancelInflightApiCall_when_navigationOccurs` | ViewModel with ongoing `loadData()` coroutine | Call `viewModel.onCleared()` mid-flight | API call job is cancelled; no state update after clear |
| 20 | `should_cancelPreviousRefresh_when_refreshCalledAgain` | Refresh job with 1000ms delay | Call `refresh()` twice rapidly | Only second refresh completes; first is cancelled; state reflects second result |
| 21 | `should_cancelKnowledgeLoad_when_panelDismissed` | Knowledge panel ViewModel with delayed API | Open panel, trigger `dismissPanel()` before load completes | `loadKnowledge` job cancelled; state resets to hidden |
| 22 | `should_notCatchCancellationException_when_coroutineCancelled` | Coroutine wrapped in try-catch catching `Exception` | Cancel coroutine | `CancellationException` is NOT swallowed; coroutine terminates cleanly |
| 23 | `should_cancelChildren_when_parentCancelled` | Parent scope with 3 child `launch` blocks | Cancel parent scope | All 3 children are cancelled; none completes |
| 24 | `should_cancelChildNotParent_when_childFails` | `supervisorScope` with 2 children; child 1 throws | Let child 1 fail | Child 1 cancelled; child 2 continues and completes; parent active |
| 25 | `should_notCancelSibling_when_oneChildCancelled` | `supervisorScope`, 2 children | Cancel child 1 manually | Child 2 still runs; completes normally |
| 26 | `should_freeResources_when_coroutineCancelled` | Mock resource that tracks open/close; used inside coroutine | Cancel coroutine after resource opened | Resource `close()` called via `finally` block |
| 27 | `should_cancelDebounceJob_when_newInputArrives` | `debounce(300ms)` search flow | Emit 3 values within 100ms intervals | Only last value triggers API; intermediate jobs cancelled |
| 28 | `should_respectCancellation_when_delayInUseCase` | UseCase with `delay(500)` | Cancel job after 100ms via `job.cancel()` | `delay` respects cancellation; `CancellationException` propagated |
| 29 | `should_cancelFilterSearch_when_filterChangedDuringLoad` | Active search job with delay | Change filter before search completes | Old search cancelled; new filter search starts |
| 30 | `should_cleanupStateOnCancel_when_loadingCancelled` | ViewModel in `Loading` state with active job | Cancel job | State reverts from `Loading` to `Idle` or previous state |
| 31 | `should_cancelRetryJob_when_userCancels` | Retry coroutine with 3 attempts, 200ms delay each | User cancels after 1st retry | Retry loop exits; no further attempts |
| 32 | `should_propagateCancellation_when_flowCancelled` | Flow collecting from upstream in ViewModel | Cancel collection scope | Upstream flow collection stops; no further emissions processed |
| 33 | `should_notUpdateState_when_cancelledBeforeComplete` | Delayed useCase; ViewModel state starts `Idle` | Cancel before `advanceUntilIdle()` | State remains `Idle`; no `Success` emitted |
| 34 | `should_cancelUpload_when_cancelButtonClicked` | Upload coroutine with progress updates | Call `viewModel.cancelUpload()` mid-upload | Upload job cancelled; state set to `Cancelled` |
| 35 | `should_handleCancellationGracefully_when_repositoryThrowsCancellation` | Repository propagates `CancellationException` | Cancel active job | Exception propagated correctly; ViewModel does not enter error state |

---

## 3. Race Condition Tests (20 tests)

**File:** `app/src/test/java/com/ralphthon/app/concurrency/RaceConditionTest.kt`

| # | Test Name | Setup | Action | Assertion |
|---|-----------|-------|--------|-----------|
| 36 | `should_showCorrectFinalState_when_pagesArrivePagesOutOfOrder` | Mock API: page 3 returns before page 1 (simulate via delay order) | Request pages 1, 2, 3 rapidly | Final state shows page 3 data only (latest wins); no partial mix |
| 37 | `should_triggerApiOnce_when_5RapidInputsDebounced` | `debounce(300ms)` in SearchViewModel; mock API spy | Emit 5 values in 50ms intervals | API called exactly once with last value |
| 38 | `should_cancelPaginationAndProceed_when_refreshDuringPageLoad` | Page load in flight (500ms delay); refresh called at 200ms | Trigger page load then refresh | Page load cancelled; refresh completes; state shows refreshed data |
| 39 | `should_notInterfereWithEachOther_when_twoViewModelsLoadSimultaneously` | Two separate ViewModels; each with own mock useCase | Both call `load()` simultaneously | Each ViewModel state updated independently; no shared mutation |
| 40 | `should_completeBothIndependently_when_knowledgePanelOpensDuringCardReload` | KnowledgePanelViewModel + CardNewsListViewModel with simultaneous loads | Trigger both loads at same time | Both complete successfully; states independent |
| 41 | `should_cancelPageLoadAndStartFilterSearch_when_filterChangedDuringPagination` | Active pagination with delay; filter change event | Emit filter change at page load midpoint | Page load cancelled; filter search starts; state reflects filter results |
| 42 | `should_notCorruptState_when_concurrentErrorAndSuccess` | Two concurrent requests: one returns error, one returns success | `launch` both simultaneously | Final state is either `Error` or `Success`, never a mixed/partial state |
| 43 | `should_deduplicateRequests_when_samePageRequestedTwice` | Request deduplication logic; page 2 requested twice | Call `loadPage(2)` twice before either completes | API called exactly once for page 2 |
| 44 | `should_maintainListOrder_when_concurrentMappersRun` | 10 items mapped concurrently with `async` | Map all items in parallel | Resulting list order matches original input order |
| 45 | `should_notShowStaleData_when_newSearchStartsBeforeOldCompletes` | Old search result arrives after new search started | Advance time so old completes after new | State shows new search result only |
| 46 | `should_handleBackpressure_when_flowEmitsFaster_than_collector` | Flow emitting every 10ms; collector with 50ms processing | Run for 500ms | No crash; latest values processed; no buffer overflow |
| 47 | `should_notDoubleFetch_when_configurationChangeDuringLoad` | ViewModel with `SavedStateHandle`; simulated rotation | Trigger load then simulate config change | API called once; new ViewModel instance receives same data from state |
| 48 | `should_resolveRaceCondition_when_favoriteToggledRapidly` | Favorite toggle with debounce; mock repository | Toggle favorite 5 times rapidly | Repository called once with final state |
| 49 | `should_notMixResults_when_differentCustomersLoadedConcurrently` | Two customer IDs loading simultaneously; mock returns distinct data | Load customer A and customer B at same time | Each ViewModel holds its own customer data; no cross-contamination |
| 50 | `should_preserveScrollPosition_when_refreshRacesWithScroll` | Refresh updates list; scroll state tracked separately | Trigger refresh and scroll simultaneously | Scroll state not reset by refresh; list updated correctly |
| 51 | `should_applyLatestSort_when_sortChangedDuringLoad` | Sort option changed during active data load | Change sort at midpoint of load | Data sorted by new option; old sort discarded |
| 52 | `should_notLoseLastPage_when_rapidScrollToBottom` | Pagination with 50ms delay; rapid scroll events | Trigger `loadNextPage()` 3 times in 10ms | Page loaded once; no duplicate data; list consistent |
| 53 | `should_correctlyMergeStates_when_uploadAndCardListLoadSimultaneously` | Upload ViewModel + CardList ViewModel running together | Both in active state simultaneously | Upload state and card list state each correct; no interference |
| 54 | `should_notCrash_when_viewModelClearedDuringRaceCondition` | Race between two jobs; ViewModel cleared at peak | `onCleared()` called mid-race | All jobs cancelled cleanly; no crash; no ANR |
| 55 | `should_produceAtomicStateUpdate_when_multipleFieldsChangeSimultaneously` | `UiState` data class with 3 fields; update triggered | Single `update { }` call on `MutableStateFlow` | Collectors never see partial state (only old or new, never mixed) |

---

## 4. Flow / StateFlow Tests (15 tests)

**File:** `app/src/test/java/com/ralphthon/app/concurrency/FlowStateFlowTest.kt`

| # | Test Name | Setup | Action | Assertion |
|---|-----------|-------|--------|-----------|
| 56 | `should_emitDistinctValuesOnly_when_sameStateEmittedTwice` | `MutableStateFlow(Loading)`; collector with Turbine | Emit `Loading` twice | Turbine receives `Loading` exactly once (StateFlow distinctUntilChanged) |
| 57 | `should_receiveAllEmissions_when_multipleCollectorsAttached` | One `StateFlow`; two Turbine collectors | Emit 3 state values | Both collectors each receive all 3 values in order |
| 58 | `should_receiveCurrentState_when_lateCollectorSubscribes` | `StateFlow` already in `Success` state | New collector starts after state set | Late collector immediately receives `Success` (no wait needed) |
| 59 | `should_stopEmitting_when_viewModelCleared` | Turbine collecting `StateFlow`; ViewModel cleared | Call `onCleared()` | Turbine channel closed; no further emissions |
| 60 | `should_replayLastEvent_when_sharedFlowHasReplay1` | `SharedFlow(replay = 1)` for navigation events | Subscriber joins after event emitted | Late subscriber receives last navigation event |
| 61 | `should_notReplayEvents_when_sharedFlowHasReplay0` | `SharedFlow(replay = 0)` for one-shot events | Subscriber joins after event emitted | Late subscriber receives nothing |
| 62 | `should_bufferEvents_when_collectorSlow` | `SharedFlow(extraBufferCapacity = 5)`; fast emitter | Emit 5 values before collector processes any | No events dropped; all 5 received eventually |
| 63 | `should_combineFlows_when_multipleSourcesUpdate` | Two `StateFlow`s combined via `combine`; Turbine | Update both sources | Combined flow emits updated pair with correct values |
| 64 | `should_flatMapLatest_cancelsPrevious_when_inputChanges` | `flatMapLatest` on search query flow | Emit new query before previous flatMap completes | Previous inner flow cancelled; new flow starts |
| 65 | `should_debounceSearchQuery_when_userTypesRapidly` | `debounce(300ms)` on query `StateFlow` | Emit 4 values in 100ms intervals; advance 400ms | Only 1 API call triggered with last value |
| 66 | `should_distinctUntilChanged_when_filterNotChanged` | Filter `StateFlow` with `distinctUntilChanged` | Set same filter twice | Downstream only processes filter once |
| 67 | `should_transformState_when_mapOperatorApplied` | `StateFlow<List<Card>>` mapped to `StateFlow<Int>` (count) | Add 3 cards to list | Count flow emits 3 |
| 68 | `should_shareState_when_stateInSharedViewModel` | Shared ViewModel accessed by two screens | Update state from screen 1 | Screen 2 observer receives updated state |
| 69 | `should_throwIfNotCollected_when_channelFull` | `Channel(capacity = 1)` with send before receive | Send 2 items without receiving | Second send suspends or throws `ClosedSendChannelException`; no silent drop |
| 70 | `should_completeFlow_when_scopeCancelled` | `flow { }` with infinite loop; collected in cancellable scope | Cancel scope | Flow collection terminates; infinite loop stops |

---

## 5. Timeout & Retry Tests (10 tests)

**File:** `app/src/test/java/com/ralphthon/app/concurrency/TimeoutRetryTest.kt`

| # | Test Name | Setup | Action | Assertion |
|---|-----------|-------|--------|-----------|
| 71 | `should_showTimeoutError_when_apiExceedsTimeout` | `withTimeout(5000ms)` in UseCase; mock API with `delay(6000ms)` | Call useCase; `advanceTimeBy(6000)` | `TimeoutCancellationException` caught; `uiState` emits `Error.Timeout` |
| 72 | `should_sendFreshRequest_when_retryAfterTimeout` | First call times out; second call succeeds | Timeout on first; retry triggered | Second call returns data; state is `Success` |
| 73 | `should_succeedOnRetry_when_firstCallFails` | Mock: first call throws `IOException`; second returns data | Call with retry logic | `Success` state after second attempt; API called twice |
| 74 | `should_stopRetrying_when_maxAttemptsReached` | Mock always throws; max retries = 3 | Call with retry | API called exactly 3 times; final state is `Error` |
| 75 | `should_applyExponentialBackoff_when_retrying` | Track delay between retries via `advanceTimeBy` | 3 retries with backoff: 1s, 2s, 4s | Time advanced correctly matches backoff schedule |
| 76 | `should_notRetry_when_errorIsClientError` | Mock returns HTTP 400 (client error) | Call useCase | Retry NOT attempted; state is `Error.ClientError`; API called once |
| 77 | `should_retryOnlyOnServerError_when_http500Received` | Mock returns HTTP 500 first, then 200 | Call useCase with retry | Retry attempted once; success on second; API called twice |
| 78 | `should_cancelRetry_when_scopeCancelled` | Retry with 1s delay between attempts | Cancel scope between retries | Retry loop exits; no further API calls |
| 79 | `should_resetRetryCount_when_newCallIssued` | Previous call exhausted retries | Issue new call (fresh invocation) | Retry counter reset to 0; new call gets full retry budget |
| 80 | `should_emitProgressDuringRetry_when_retryingUpload` | Upload with retry; state flow tracked via Turbine | First upload fails; retry succeeds | Turbine sees: `Uploading` → `Error` → `Retrying` → `Success` in order |

---

## Summary

| Section | Tests | File |
|---------|-------|------|
| 1. Dispatcher Tests | 15 | `DispatcherTest.kt` |
| 2. Cancellation Tests | 20 | `CancellationTest.kt` |
| 3. Race Condition Tests | 20 | `RaceConditionTest.kt` |
| 4. Flow / StateFlow Tests | 15 | `FlowStateFlowTest.kt` |
| 5. Timeout & Retry Tests | 10 | `TimeoutRetryTest.kt` |
| **Total** | **80** | |

## Key Test Infrastructure

```kotlin
// TestDispatcher setup (shared across all test files)
@BeforeEach
fun setUp() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)
}

@AfterEach
fun tearDown() {
    Dispatchers.resetMain()
}

// Turbine usage pattern
viewModel.uiState.test {
    assertThat(awaitItem()).isInstanceOf(UiState.Loading::class.java)
    assertThat(awaitItem()).isInstanceOf(UiState.Success::class.java)
    cancelAndIgnoreRemainingEvents()
}

// runTest wrapper (all tests use this)
@Test
fun should_example_when_condition() = runTest {
    // arrange
    // act
    advanceUntilIdle()
    // assert
}
```
