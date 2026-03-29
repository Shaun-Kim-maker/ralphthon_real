# Integration & E2E Test Specification

**Target:** 50+ test cases
**Framework:** JUnit 4 + Compose Testing + MockWebServer
**Location:** `app/src/androidTest/` and `app/src/test/java/.../integration/`

---

## 1. E2E User Journey Tests (16 tests)

### File: `integration/UserJourneyTest.kt`

| # | Test Name | Journey | Steps | Final Assertion |
|---|-----------|---------|-------|-----------------|
| 1 | should_viewCustomerCards_when_fullFlow | 고객 → 카드 목록 | launch → tap customer → see cards | card list displayed for customer |
| 2 | should_viewCardDetail_when_fullFlow | 고객 → 카드 → 상세 | launch → customer → card → detail | detail screen with statements |
| 3 | should_viewKnowledge_when_fullFlow | 카드 → 키워드 → 지식 | detail → tap keyword → panel | knowledge panel with articles |
| 4 | should_searchAndViewCard_when_fullFlow | 검색 → 결과 → 상세 | search → type → tap result → detail | card detail from search |
| 5 | should_navigateBackToList_when_fullFlow | 상세 → 뒤로 → 목록 | detail → back → card list | card list at same position |
| 6 | should_filterAndBrowse_when_fullFlow | 필터 → 카드 목록 | cards → filter 7일 → browse | filtered cards only |
| 7 | should_handleEmptyCustomer_when_noConversations | 빈 고객 | launch → tap empty customer | empty state message |
| 8 | should_handleSearchNoResults_when_queryNoMatch | 빈 검색 | search → type "없는거" | empty search results |
| 9 | should_retryAfterError_when_networkRestored | 에러 → 재시도 | error state → retry → success | data loaded after retry |
| 10 | should_paginateCards_when_scrollingLongList | 긴 목록 | scroll to bottom → load more | additional cards appended |
| 11 | should_switchKeywords_when_relatedTapped | 키워드 전환 | panel → tap related keyword | new knowledge panel |
| 12 | should_filterByCustomerInSearch_when_selected | 검색 필터 | search → select customer filter | filtered results |
| 13 | should_filterByDateInSearch_when_selected | 날짜 필터 | search → set date range | filtered results |
| 14 | should_clearFiltersInSearch_when_resetClicked | 필터 초기화 | search with filters → clear | unfiltered results |
| 15 | should_showMultipleStatements_when_cardHasMany | 다수 발언 | open card with 10 statements | all 10 visible with scroll |
| 16 | should_displaySentimentBadges_when_statementsLoaded | 감정 분석 | open card → see statements | correct badges per sentiment |

---

## 2. Cross-Layer Integration Tests (20 tests)

### File: `integration/CrossLayerTest.kt`

These test the full stack: UI → ViewModel → UseCase → Repository → API (mocked)

| # | Test Name | Layers | Given | Then |
|---|-----------|--------|-------|------|
| 1 | should_displayCustomers_when_apiReturnsData | UI→VM→UC→Repo→API | MockWebServer returns customers | UI shows customer cards |
| 2 | should_displayCards_when_apiReturnsCards | UI→VM→UC→Repo→API | MockWebServer returns cards | UI shows card items |
| 3 | should_displayCardDetail_when_apiReturnsCard | UI→VM→UC→Repo→API | MockWebServer returns card | UI shows detail |
| 4 | should_displayKnowledge_when_apiReturnsArticles | UI→VM→UC→Repo→API | MockWebServer returns knowledge | panel shows articles |
| 5 | should_displaySearchResults_when_apiReturnsMatches | UI→VM→UC→Repo→API | MockWebServer returns search | UI shows results |
| 6 | should_mapDtoToDomain_when_fullChain | API→Mapper→Domain | raw JSON | correct domain objects in VM |
| 7 | should_handlePagination_when_multiplePages | UI→VM→UC→Repo→API | page 0 then page 1 | combined list in UI |
| 8 | should_passFilterParams_when_filterApplied | UI→VM→UC→Repo→API | filter 7 days | API receives dateFrom param |
| 9 | should_passSearchQuery_when_searching | UI→VM→UC→Repo→API | query="온디바이스" | API receives q=온디바이스 |
| 10 | should_injectDependencies_when_hiltConfigured | DI→all | Hilt test rules | all dependencies resolve |
| 11 | should_provideViewModel_when_hiltInjects | DI→VM | Hilt inject | VM created with correct UseCase |
| 12 | should_provideRepository_when_hiltInjects | DI→Repo | Hilt inject | Repo created with correct API client |
| 13 | should_provideApiClient_when_hiltInjects | DI→API | Hilt inject | API client with correct base URL |
| 14 | should_navigateCorrectly_when_hiltNavigation | DI→Nav→UI | launch with Hilt | all screens navigable |
| 15 | should_handleConcurrentLoads_when_rapidNavigation | UI→VM→API | navigate fast between screens | no crash, last data wins |
| 16 | should_cancelPreviousRequest_when_newNavigation | VM→UC→Repo→API | navigate away during load | previous coroutine cancelled |
| 17 | should_surviveRotation_when_screenRotates | VM→SavedState | rotate during data display | data preserved |
| 18 | should_mapAllSentiments_when_fullChain | API→Mapper→UI | all 5 sentiment types | correct badges for each |
| 19 | should_mapAllCategories_when_fullChain | API→Mapper→UI | all 4 keyword categories | correct colors for each |
| 20 | should_handleLargePayload_when_manyCards | API→Mapper→VM→UI | 100 cards response | all mapped and displayed |

---

## 3. Error Propagation Tests (12 tests)

### File: `integration/ErrorPropagationTest.kt`

| # | Test Name | Error Source | Propagation Path | UI Result |
|---|-----------|-------------|-----------------|-----------|
| 1 | should_showNetworkError_when_apiThrowsIOException | API: IOException | API→Repo→UC→VM→UI | "인터넷 연결을 확인해주세요" |
| 2 | should_showTimeoutError_when_apiTimesOut | API: TimeoutException | API→Repo→UC→VM→UI | "서버 응답 시간이 초과되었습니다" |
| 3 | should_showNotFoundError_when_api404 | API: 404 | API→Repo→UC→VM→UI | "카드를 찾을 수 없습니다" |
| 4 | should_showServerError_when_api500 | API: 500 | API→Repo→UC→VM→UI | "서버 오류가 발생했습니다" |
| 5 | should_showParseError_when_malformedJson | API: bad JSON | API→Mapper→Repo→UC→VM→UI | "데이터 형식 오류" |
| 6 | should_showRetryButton_when_anyError | any error | →UI | retry button visible |
| 7 | should_recoverFromError_when_retrySucceeds | first: error, second: success | UI retry→API | data displayed |
| 8 | should_showSearchError_when_searchApiFails | Search API fails | API→Repo→UC→VM→UI | search error state |
| 9 | should_showUploadError_when_uploadFails | Upload API: 400 | API→Repo→UC→VM→UI | error message |
| 10 | should_showKnowledgeError_when_knowledgeApiFails | Knowledge API fails | API→Repo→UC→VM→UI | panel error state |
| 11 | should_preserveData_when_refreshFails | Refresh API fails | API→Repo→UC→VM | existing data preserved + snackbar |
| 12 | should_preserveData_when_paginationFails | Page 2 API fails | API→Repo→UC→VM | page 1 data preserved + error |

---

## 4. Performance Tests (5 tests)

### File: `integration/PerformanceTest.kt`

| # | Test Name | Metric | Threshold | Measurement |
|---|-----------|--------|-----------|-------------|
| 1 | should_loadCustomerList_within1Second | API→UI time | < 1000ms | measure time from init to Data state |
| 2 | should_loadCardDetail_within1Second | API→UI time | < 1000ms | measure time from navigation to Data |
| 3 | should_renderCardList_without_jank | frame time | < 16ms per frame | check no dropped frames in 100-item scroll |
| 4 | should_searchWithin2Seconds | search→results time | < 2000ms | measure time from query submit to Results |
| 5 | should_openKnowledgePanel_within1Second | API→panel time | < 1000ms | measure time from keyword tap to Data |

---

## Summary

| Component | Test Count |
|-----------|-----------|
| E2E User Journeys | 16 |
| Cross-Layer Integration | 20 |
| Error Propagation | 12 |
| Performance | 5 |
| **Total** | **53** |

---

## Grand Total Across All Specs

| Spec | File | Tests |
|------|------|-------|
| Domain Layer | domain-tests.md | 124 |
| Data Layer | data-tests.md | 51 |
| UI Layer | ui-tests.md | 82 |
| Integration | integration-tests.md | 53 |
---

## 5. Offline/Online Transition Tests (8 tests)

### File: `integration/ConnectivityTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_showCachedData_when_goingOffline | data cached, network off | loadCustomers() | cached data shown |
| 2 | should_showError_when_offlineNoCahce | no cache, network off | loadCustomers() | "인터넷 연결을 확인해주세요" |
| 3 | should_refreshFromApi_when_backOnline | offline → online | connectivity change | auto-refresh data |
| 4 | should_showStaleIndicator_when_dataFromCache | cache data old | loadCustomers() | "마지막 업데이트: 30분 전" |
| 5 | should_queueUpload_when_offlineDuringUpload | upload started, network drops | uploadConversation() | queued for retry |
| 6 | should_retryUpload_when_backOnline | queued upload, online | connectivity restored | upload retried |
| 7 | should_notCrash_when_rapidConnectivityChange | on-off-on-off fast | multiple changes | stable state, no crash |
| 8 | should_showOfflineBanner_when_noNetwork | network off | any screen | banner "오프라인" visible |

---

## 6. Session & Auth Tests (7 tests)

### File: `integration/SessionTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_addToken_when_authenticated | token stored | API request | Authorization header present |
| 2 | should_redirectToLogin_when_tokenExpired | API returns 401 | any request | navigate to login/refresh |
| 3 | should_refreshToken_when_401Received | refresh token valid | 401 response | new token, retry request |
| 4 | should_clearSession_when_logoutCalled | logged in | logout() | token cleared, back to start |
| 5 | should_persistToken_when_appRestarted | token saved | app restart | token available |
| 6 | should_handleConcurrent401_when_multipleRequests | 3 requests get 401 | concurrent 401 | only 1 refresh, others wait |
| 7 | should_showError_when_refreshFails | refresh token expired | 401 + refresh fails | "다시 로그인해주세요" |

---

## 7. Memory & Leak Tests (5 tests)

### File: `integration/MemoryTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_notLeakActivity_when_rotated | activity created | rotate 5 times | no leaked activities |
| 2 | should_notLeakViewModel_when_navigatedAway | VM in screen | navigate away | VM garbage collected |
| 3 | should_notGrowMemory_when_scrollingLongList | 500 cards | scroll up and down 10 times | memory stable (< 10MB growth) |
| 4 | should_releaseImageMemory_when_screenDestroyed | images loaded | navigate away | bitmap memory freed |
| 5 | should_notLeakCoroutine_when_screenDestroyed | coroutine running | navigate away | scope cancelled, no leak |

---

## Updated Summary

| Component | Test Count |
|-----------|-----------|
| E2E User Journeys | 16 |
| Cross-Layer Integration | 20 |
| Error Propagation | 12 |
| Performance | 5 |
| Connectivity/Offline | 8 |
| Session & Auth | 7 |
| Memory & Leak | 5 |
| **Total** | **73** |
