# Domain Layer Test Specification

**Target:** 150+ test cases for ViewModels, UseCases, Repositories
**Framework:** JUnit 5 + MockK
**Location:** `app/src/test/java/com/ralphthon/app/`

---

## 1. CustomerListViewModel (12 tests)

### File: `ui/customer/CustomerListViewModelTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_showLoading_when_screenOpened | VM initialized | init block runs | uiState = Loading |
| 2 | should_showCustomerList_when_apiReturnsData | repo returns 3 customers | loadCustomers() | uiState = Data(3 customers) |
| 3 | should_showEmpty_when_apiReturnsEmptyList | repo returns [] | loadCustomers() | uiState = Empty |
| 4 | should_showError_when_apiThrowsException | repo throws IOException | loadCustomers() | uiState = Error("서버 연결에 실패했습니다") |
| 5 | should_showError_when_apiTimesOut | repo throws TimeoutException | loadCustomers() | uiState = Error("서버 응답 시간이 초과되었습니다") |
| 6 | should_refreshData_when_pullToRefresh | uiState = Data | refresh() | Loading → new Data |
| 7 | should_preserveData_when_refreshFails | uiState = Data(3 items) | refresh() throws | uiState stays Data(3 items) + snackbar error |
| 8 | should_sortByLastInteraction_when_defaultOrder | 3 customers with different dates | loadCustomers() | customers sorted newest first |
| 9 | should_retryLoad_when_retryClicked | uiState = Error | retry() | uiState = Loading → then Data or Error |
| 10 | should_navigateToCards_when_customerClicked | uiState = Data | onCustomerClick(id=1) | navigation event with customerId=1 |
| 11 | should_cancelPreviousLoad_when_refreshCalledTwice | first load in progress | refresh() called again | first job cancelled, second proceeds |
| 12 | should_handleNullName_when_customerNameIsNull | repo returns customer with null name | loadCustomers() | display "이름 없음" |

---

## 2. CardNewsListViewModel (14 tests)

### File: `ui/card/CardNewsListViewModelTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_showLoading_when_screenOpened | customerId = 1 | init | uiState = Loading |
| 2 | should_showCards_when_apiReturnsData | repo returns 5 cards | loadCards(1) | uiState = Data(5 cards, hasMore=false) |
| 3 | should_showEmpty_when_noCardsExist | repo returns [] | loadCards(1) | uiState = Empty |
| 4 | should_showError_when_apiFails | repo throws | loadCards(1) | uiState = Error |
| 5 | should_loadNextPage_when_scrolledToBottom | page 0 loaded, hasMore=true | loadNextPage() | Data(cards + newCards, page=1) |
| 6 | should_notLoadMore_when_noMorePages | hasMore=false | loadNextPage() | no API call, state unchanged |
| 7 | should_showLoadingMore_when_paginationInProgress | page 0 loaded | loadNextPage() | Data(isLoadingMore=true) |
| 8 | should_filterByDate7Days_when_filterChipSelected | all cards loaded | filterByDate(7) | only cards from last 7 days |
| 9 | should_filterByDate30Days_when_filterChipSelected | all cards loaded | filterByDate(30) | only cards from last 30 days |
| 10 | should_showAllCards_when_filterCleared | filtered state | filterByDate(null) | all cards shown |
| 11 | should_resetPagination_when_filterChanged | page 2, filtered | filterByDate(7) | page resets to 0 |
| 12 | should_navigateToDetail_when_cardClicked | uiState = Data | onCardClick(id=5) | navigation event with cardId=5 |
| 13 | should_showCustomerName_when_loaded | customerId = 1 | init | customerName set in state |
| 14 | should_handleTimeout_when_apiSlow | repo throws TimeoutException | loadCards(1) | Error("서버 응답 시간이 초과되었습니다") |

---

## 3. CardDetailViewModel (12 tests)

### File: `ui/card/CardDetailViewModelTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_showLoading_when_screenOpened | cardId = 1 | init | uiState = Loading |
| 2 | should_showCardDetail_when_apiReturnsData | repo returns card with 3 statements, 2 keywords | loadCard(1) | Data(card) |
| 3 | should_showError_when_cardNotFound | repo throws 404 | loadCard(999) | Error("카드를 찾을 수 없습니다") |
| 4 | should_showError_when_networkFails | repo throws IOException | loadCard(1) | Error("인터넷 연결을 확인해주세요") |
| 5 | should_showKnowledgeLoading_when_keywordTapped | card loaded | onKeywordClick(kwId=1) | panelState = Loading |
| 6 | should_showKnowledge_when_articlesExist | keyword has 2 articles | onKeywordClick(kwId=1) | panelState = Data(2 articles) |
| 7 | should_showEmptyKnowledge_when_noArticles | keyword has 0 articles | onKeywordClick(kwId=2) | panelState = Empty |
| 8 | should_showKnowledgeError_when_apiFails | knowledge API fails | onKeywordClick(kwId=1) | panelState = Error |
| 9 | should_hidePanel_when_dismissed | panel showing | dismissPanel() | panelState = null / hidden |
| 10 | should_switchKeyword_when_relatedKeywordTapped | panel showing kw=1 | onRelatedKeywordClick("엣지컴퓨팅") | new panel Loading → Data |
| 11 | should_sortStatementsByTime_when_displayed | 3 statements with timestamps 300,120,600 | loadCard(1) | statements sorted: 120, 300, 600 |
| 12 | should_retryLoad_when_retryClicked | Error state | retry() | Loading → Data or Error |

---

## 4. SearchViewModel (14 tests)

### File: `ui/search/SearchViewModelTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_showInitial_when_screenOpened | VM initialized | init | uiState = Initial |
| 2 | should_showResults_when_searchReturnsData | query="온디바이스" | search("온디바이스") | Results(3 cards, totalCount=3) |
| 3 | should_showEmpty_when_searchReturnsNothing | query="없는키워드" | search("없는키워드") | Empty |
| 4 | should_showError_when_searchFails | API throws | search("test") | Error |
| 5 | should_showLoading_when_searchInProgress | idle | search("test") | Loading before result |
| 6 | should_notSearch_when_queryIsBlank | query="" | search("") | stay Initial, no API call |
| 7 | should_notSearch_when_queryIsWhitespace | query="   " | search("   ") | stay Initial, no API call |
| 8 | should_debounceSearch_when_typingFast | type "온", "온디", "온디바" fast | 3 rapid calls | only 1 API call after debounce (300ms) |
| 9 | should_cancelPrevious_when_newSearchStarted | search("A") in progress | search("B") | cancel A, proceed B |
| 10 | should_filterByCustomer_when_customerSelected | results showing | setCustomerFilter(id=1) | new search with customerId=1 |
| 11 | should_filterByDateRange_when_datesSelected | results showing | setDateRange(from, to) | new search with dates |
| 12 | should_clearFilters_when_resetClicked | filters active | clearFilters() | search with no filters |
| 13 | should_paginateResults_when_scrolledToBottom | results with hasMore=true | loadNextPage() | append new results |
| 14 | should_urlEncodeQuery_when_specialCharsUsed | query="C++ & 로봇" | search("C++ & 로봇") | encoded properly, results shown |

---

## 5. GetCustomersUseCase (8 tests)

### File: `domain/usecase/GetCustomersUseCaseTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnCustomers_when_repoSucceeds | repo returns list | invoke() | Result.success(list) |
| 2 | should_returnError_when_repoThrows | repo throws IOException | invoke() | Result.failure(IOException) |
| 3 | should_returnEmpty_when_repoReturnsEmpty | repo returns [] | invoke() | Result.success(emptyList) |
| 4 | should_sortByLastInteraction_when_defaultSort | 3 customers unsorted | invoke() | sorted by lastInteractionAt desc |
| 5 | should_callRepoOnce_when_invoked | — | invoke() | verify repo.getCustomers() called once |
| 6 | should_propagateTimeout_when_repoTimesOut | repo throws TimeoutException | invoke() | Result.failure(TimeoutException) |
| 7 | should_handleNullFields_when_customerIncomplete | customer with nulls | invoke() | defaults applied (name="이름 없음") |
| 8 | should_runOnIoDispatcher_when_invoked | — | invoke() | runs on Dispatchers.IO |

---

## 6. GetCardsByCustomerUseCase (10 tests)

### File: `domain/usecase/GetCardsByCustomerUseCaseTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnCards_when_repoSucceeds | repo returns 5 cards | invoke(customerId=1, page=0) | Result.success(CardListResult) |
| 2 | should_returnEmpty_when_noCards | repo returns empty | invoke(customerId=1, page=0) | Result.success(empty list, hasMore=false) |
| 3 | should_returnError_when_repoThrows | repo throws | invoke(customerId=1, page=0) | Result.failure |
| 4 | should_passPageParams_when_paginating | — | invoke(customerId=1, page=2, size=20) | repo called with page=2, size=20 |
| 5 | should_returnHasMore_when_moreCardsExist | totalCount=50, page=0, size=20 | invoke() | hasMore=true |
| 6 | should_returnNoMore_when_lastPage | totalCount=15, page=0, size=20 | invoke() | hasMore=false |
| 7 | should_filterByDays_when_dayFilterProvided | 5 cards, 2 within 7 days | invoke(customerId=1, daysFilter=7) | 2 cards returned |
| 8 | should_returnAllCards_when_noFilter | 5 cards | invoke(customerId=1, daysFilter=null) | 5 cards |
| 9 | should_throw_when_customerIdInvalid | customerId = -1 | invoke(customerId=-1) | IllegalArgumentException |
| 10 | should_propagateTimeout_when_repoSlow | repo timeout | invoke() | Result.failure(TimeoutException) |

---

## 7. GetCardDetailUseCase (8 tests)

### File: `domain/usecase/GetCardDetailUseCaseTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnCard_when_repoSucceeds | repo returns card | invoke(cardId=1) | Result.success(card) |
| 2 | should_returnError_when_cardNotFound | repo throws 404 | invoke(cardId=999) | Result.failure(NotFoundException) |
| 3 | should_returnError_when_networkFails | repo throws IOException | invoke(cardId=1) | Result.failure(IOException) |
| 4 | should_sortStatements_when_cardHasStatements | 3 statements unsorted | invoke(cardId=1) | statements sorted by timestampInSeconds |
| 5 | should_handleEmptyKeywords_when_noKeywords | card with 0 keywords | invoke(cardId=1) | card with empty keywords list |
| 6 | should_handleEmptyStatements_when_noStatements | card with 0 statements | invoke(cardId=1) | card with empty statements list |
| 7 | should_throw_when_cardIdInvalid | cardId = -1 | invoke(-1) | IllegalArgumentException |
| 8 | should_callRepoOnce_when_invoked | — | invoke(1) | verify called once |

---

## 8. SearchCardsUseCase (10 tests)

### File: `domain/usecase/SearchCardsUseCaseTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnResults_when_queryMatches | query="온디바이스" | invoke(query) | Result.success(cards) |
| 2 | should_returnEmpty_when_noMatch | query="없는거" | invoke(query) | Result.success(empty) |
| 3 | should_returnError_when_repoFails | repo throws | invoke("test") | Result.failure |
| 4 | should_throw_when_queryBlank | query="" | invoke("") | IllegalArgumentException |
| 5 | should_throw_when_queryTooShort | query="" (whitespace only) | invoke("  ") | IllegalArgumentException |
| 6 | should_trimQuery_when_extraSpaces | query="  온디바이스  " | invoke(query) | repo called with "온디바이스" |
| 7 | should_passFilters_when_provided | customerId=1, dateFrom, dateTo | invoke(query, filters) | repo called with all filters |
| 8 | should_paginateResults_when_pageProvided | page=2, size=20 | invoke(query, page=2) | repo called with page=2 |
| 9 | should_returnHasMore_when_moreResults | totalCount=50, page=0 | invoke(query) | hasMore=true |
| 10 | should_urlEncodeQuery_when_specialChars | query="C++ & test" | invoke(query) | no crash, results returned |

---

## 9. GetKnowledgeUseCase (8 tests)

### File: `domain/usecase/GetKnowledgeUseCaseTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnArticles_when_keywordHasKnowledge | keyword has 2 articles | invoke(kwId=1) | Result.success(2 articles) |
| 2 | should_returnEmpty_when_noArticles | keyword has 0 articles | invoke(kwId=2) | Result.success(empty) |
| 3 | should_returnError_when_keywordNotFound | repo throws 404 | invoke(kwId=999) | Result.failure |
| 4 | should_returnError_when_networkFails | repo throws IOException | invoke(kwId=1) | Result.failure |
| 5 | should_includeContextualExplanation_when_present | article has explanation | invoke(kwId=1) | explanation field not empty |
| 6 | should_includeRelatedKeywords_when_present | article has 3 related | invoke(kwId=1) | relatedKeywords size = 3 |
| 7 | should_throw_when_keywordIdInvalid | kwId = -1 | invoke(-1) | IllegalArgumentException |
| 8 | should_callRepoOnce_when_invoked | — | invoke(1) | verify called once |

---

## 10. CustomerRepository (6 tests)

### File: `domain/repository/CustomerRepositoryTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnCustomers_when_apiSucceeds | API returns 200 with data | getCustomers() | mapped domain list |
| 2 | should_throwIOException_when_networkFails | API throws SocketException | getCustomers() | IOException |
| 3 | should_throwTimeout_when_apiSlow | API takes > 30s | getCustomers() | TimeoutException |
| 4 | should_returnEmptyList_when_apiReturnsEmpty | API returns 200 with [] | getCustomers() | emptyList() |
| 5 | should_returnCustomer_when_getById | API returns 200 | getCustomerById(1) | mapped Customer |
| 6 | should_throwNotFound_when_customerMissing | API returns 404 | getCustomerById(999) | NotFoundException |

---

## 11. CardRepository (8 tests)

### File: `domain/repository/CardRepositoryTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnCards_when_apiSucceeds | API returns paginated cards | getCardsByCustomer(1, 0, 20) | CardListResult |
| 2 | should_returnEmpty_when_noCards | API returns empty list | getCardsByCustomer(1, 0, 20) | empty result |
| 3 | should_throwOnNetworkError_when_apiFails | API throws | getCardsByCustomer() | IOException |
| 4 | should_returnCardDetail_when_cardExists | API returns card | getCardById(1) | ContextCard |
| 5 | should_throwNotFound_when_cardMissing | API returns 404 | getCardById(999) | NotFoundException |
| 6 | should_returnSearchResults_when_queryMatches | API returns results | searchCards("온디바이스") | SearchResult |
| 7 | should_returnEmptySearch_when_noMatch | API returns empty | searchCards("없는거") | empty SearchResult |
| 8 | should_passAllParams_when_searchWithFilters | filters provided | searchCards(q, customerId, dateFrom, dateTo) | API called with all params |

---

## 12. KnowledgeRepository (6 tests)

### File: `domain/repository/KnowledgeRepositoryTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnKnowledge_when_apiSucceeds | API returns articles | getKnowledge(kwId=1) | KnowledgeResult |
| 2 | should_returnEmpty_when_noArticles | API returns empty | getKnowledge(kwId=2) | empty result |
| 3 | should_throwNotFound_when_keywordMissing | API returns 404 | getKnowledge(999) | NotFoundException |
| 4 | should_throwOnNetwork_when_apiFails | API throws | getKnowledge(1) | IOException |
| 5 | should_mapContextualExplanation_when_present | API has explanation | getKnowledge(1) | field mapped |
| 6 | should_mapRelatedKeywords_when_present | API has related kws | getKnowledge(1) | list mapped |

---

## 13. UploadConversationUseCase (8 tests)

### File: `domain/usecase/UploadConversationUseCaseTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnSuccess_when_uploadSucceeds | valid file + customerId | invoke(file, customerId, type) | Result.success(UploadResult) |
| 2 | should_returnError_when_networkFails | API throws | invoke() | Result.failure(IOException) |
| 3 | should_throw_when_customerIdInvalid | customerId = -1 | invoke() | IllegalArgumentException |
| 4 | should_throw_when_noFileProvided | no audio, no transcript | invoke() | IllegalArgumentException("파일을 첨부해주세요") |
| 5 | should_throw_when_fileTooLarge | file > 100MB | invoke() | IllegalArgumentException("파일 크기가 100MB를 초과합니다") |
| 6 | should_acceptAudioOnly_when_noTranscript | audio file only | invoke() | Result.success |
| 7 | should_acceptTranscriptOnly_when_noAudio | transcript file only | invoke() | Result.success |
| 8 | should_returnCardsGenerated_when_success | API returns cardsGenerated=3 | invoke() | result.cardsGenerated == 3 |

---

## Summary

| Component | Test Count |
|-----------|-----------|
| CustomerListViewModel | 12 |
| CardNewsListViewModel | 14 |
| CardDetailViewModel | 12 |
| SearchViewModel | 14 |
| GetCustomersUseCase | 8 |
| GetCardsByCustomerUseCase | 10 |
| GetCardDetailUseCase | 8 |
| SearchCardsUseCase | 10 |
| GetKnowledgeUseCase | 8 |
| UploadConversationUseCase | 8 |
| CustomerRepository | 6 |
| CardRepository | 8 |
| KnowledgeRepository | 6 |
| **Total** | **124** |

---

## 14. Domain Model Validation Tests (16 tests)

### File: `domain/model/ModelValidationTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_createCustomer_when_allFieldsValid | valid fields | Customer(...) | no exception |
| 2 | should_defaultName_when_nameIsBlank | name="" | Customer.withDefaults(name="") | name = "이름 없음" |
| 3 | should_truncateName_when_nameExceeds50 | name = "가"×60 | Customer.withDefaults(name) | name = first 50 chars |
| 4 | should_defaultCompany_when_companyIsBlank | company="" | Customer.withDefaults(company="") | company = "회사 미등록" |
| 5 | should_createCard_when_allFieldsValid | valid fields | ContextCard(...) | no exception |
| 6 | should_truncateTitle_when_titleExceeds80 | title = "가"×100 | ContextCard.withDefaults(title) | title = first 80 chars |
| 7 | should_truncateSummary_when_summaryExceeds300 | summary 400 chars | ContextCard.withDefaults(summary) | summary = first 300 chars |
| 8 | should_createKeyStatement_when_valid | valid fields | KeyStatement(...) | no exception |
| 9 | should_handleNegativeTimestamp_when_invalid | timestamp = -1 | KeyStatement.withDefaults(ts=-1) | timestamp = 0 |
| 10 | should_createKeyword_when_valid | valid fields | Keyword(...) | no exception |
| 11 | should_handleEmptyTerm_when_termBlank | term="" | Keyword.withDefaults(term="") | IllegalArgumentException |
| 12 | should_createKnowledgeArticle_when_valid | valid | KnowledgeArticle(...) | no exception |
| 13 | should_handleEmptyContent_when_contentBlank | content="" | KnowledgeArticle.withDefaults | content = "(내용 없음)" |
| 14 | should_parseSentimentFromString_when_valid | "COMMITMENT" | Sentiment.fromString("COMMITMENT") | Sentiment.COMMITMENT |
| 15 | should_defaultSentiment_when_unknownString | "INVALID" | Sentiment.fromString("INVALID") | Sentiment.NEUTRAL |
| 16 | should_parseCategoryFromString_when_valid | "TECHNOLOGY" | KeywordCategory.fromString("TECHNOLOGY") | KeywordCategory.TECHNOLOGY |

---

## 15. Domain Model Equality & Copy Tests (10 tests)

### File: `domain/model/ModelEqualityTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_beEqual_when_sameCustomerId | two customers with id=1 | equals() | true |
| 2 | should_notBeEqual_when_differentCustomerId | id=1 vs id=2 | equals() | false |
| 3 | should_beEqual_when_sameCardId | two cards with id=1 | equals() | true |
| 4 | should_notBeEqual_when_differentCardId | id=1 vs id=2 | equals() | false |
| 5 | should_copyWithNewName_when_copyUsed | customer.copy(name="새이름") | copy() | name changed, rest same |
| 6 | should_copyWithNewSummary_when_copyUsed | card.copy(summary="새요약") | copy() | summary changed |
| 7 | should_hashEqual_when_sameId | two customers with id=1 | hashCode() | same hash |
| 8 | should_hashDiffer_when_differentId | id=1 vs id=2 | hashCode() | different hash |
| 9 | should_toString_when_customerCreated | customer with name="삼성" | toString() | contains "삼성" |
| 10 | should_toString_when_cardCreated | card with title="회의" | toString() | contains "회의" |

---

## 16. Coroutine & Flow Tests for ViewModels (20 tests)

### File: `ui/viewmodel/CoroutineFlowTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_emitLoading_then_data_when_customerLoadSucceeds | repo succeeds | collect stateFlow | [Loading, Data] |
| 2 | should_emitLoading_then_error_when_customerLoadFails | repo throws | collect stateFlow | [Loading, Error] |
| 3 | should_cancelJob_when_viewModelCleared | load in progress | onCleared() | job.isCancelled = true |
| 4 | should_runOnMainDispatcher_when_stateUpdated | — | loadCustomers() | state update on Main |
| 5 | should_switchToIoDispatcher_when_repoCallMade | — | loadCustomers() | repo called on IO |
| 6 | should_emitLoading_then_data_when_cardsLoadSucceeds | repo succeeds | collect stateFlow | [Loading, Data] |
| 7 | should_emitLoading_then_empty_when_noCards | repo returns [] | collect stateFlow | [Loading, Empty] |
| 8 | should_cancelPreviousJob_when_newLoadTriggered | first load running | second load() | first cancelled |
| 9 | should_debounceSearch_when_rapidInput | 3 rapid calls | collect stateFlow | only 1 result emission |
| 10 | should_notEmitDuplicate_when_sameDataReloaded | same data | reload | no new emission (distinctUntilChanged) |
| 11 | should_handleSupervisorScope_when_childFails | child coroutine throws | parent scope | parent not cancelled |
| 12 | should_retryOnce_when_firstAttemptFails | first fails, second succeeds | auto-retry logic | Data after retry |
| 13 | should_notRetry_when_nonRetryableError | 404 error | load | Error immediately, no retry |
| 14 | should_collectKnowledgeFlow_when_panelOpened | knowledge Flow | collect | [Loading, Data] |
| 15 | should_cancelKnowledgeFlow_when_panelDismissed | panel open | dismiss | knowledge job cancelled |
| 16 | should_handleConcurrentFlows_when_multipleVMs | 2 VMs loading | both collect | no interference |
| 17 | should_preserveState_when_configChange | SavedStateHandle | simulate rotation | state restored |
| 18 | should_saveScrollPosition_when_navigatingAway | scroll position in state | navigate away | position saved |
| 19 | should_restoreScrollPosition_when_navigatingBack | saved position | navigate back | scroll restored |
| 20 | should_emitPaginationState_when_loadingMore | page 0 loaded | loadNextPage | [Data(isLoadingMore=true), Data(moreItems)] |

---

## Updated Summary

| Component | Test Count |
|-----------|-----------|
| CustomerListViewModel | 12 |
| CardNewsListViewModel | 14 |
| CardDetailViewModel | 12 |
| SearchViewModel | 14 |
| GetCustomersUseCase | 8 |
| GetCardsByCustomerUseCase | 10 |
| GetCardDetailUseCase | 8 |
| SearchCardsUseCase | 10 |
| GetKnowledgeUseCase | 8 |
| UploadConversationUseCase | 8 |
| CustomerRepository | 6 |
| CardRepository | 8 |
| KnowledgeRepository | 6 |
| Model Validation | 16 |
| Model Equality & Copy | 10 |
| Coroutine & Flow | 20 |
| **Total** | **170** |
