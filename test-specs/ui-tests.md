# UI Layer Test Specification

**Target:** 80+ test cases for Compose Screens, Navigation, Accessibility
**Framework:** JUnit 4 + AndroidJUnit4 + Compose Testing
**Location:** `app/src/test/java/com/ralphthon/app/ui/` (Robolectric) and `app/src/androidTest/` (instrumented)

---

## 1. CustomerListScreen (10 tests)

### File: `ui/customer/CustomerListScreenTest.kt`

| # | Test Name | Assert On | Action | Expected |
|---|-----------|-----------|--------|----------|
| 1 | should_showLoadingIndicator_when_stateIsLoading | Loading state | — | CircularProgressIndicator displayed |
| 2 | should_showCustomerCards_when_stateIsData | Data(3 customers) | — | 3 items in list, each showing name + company |
| 3 | should_showEmptyMessage_when_stateIsEmpty | Empty state | — | "등록된 고객이 없습니다" text visible |
| 4 | should_showErrorWithRetry_when_stateIsError | Error("서버 오류") | — | error text + "다시 시도" button visible |
| 5 | should_triggerRetry_when_retryButtonClicked | Error state | click retry button | onRetry callback invoked |
| 6 | should_navigateToCards_when_customerCardClicked | Data(customer id=1) | click first card | onCustomerClick(1) invoked |
| 7 | should_showCustomerName_when_dataLoaded | Data(name="삼성전자") | — | "삼성전자" text visible |
| 8 | should_showCardCount_when_dataLoaded | Data(cardCount=15) | — | "15개 대화" text visible |
| 9 | should_truncateLongName_when_nameExceeds30Chars | name = "가"×35 | — | text truncated with ellipsis |
| 10 | should_showSearchIcon_when_screenDisplayed | any state | — | search icon in TopAppBar |

---

## 2. CardNewsListScreen (12 tests)

### File: `ui/card/CardNewsListScreenTest.kt`

| # | Test Name | Assert On | Action | Expected |
|---|-----------|-----------|--------|----------|
| 1 | should_showLoading_when_stateIsLoading | Loading | — | progress indicator |
| 2 | should_showCards_when_stateIsData | Data(5 cards) | — | 5 card items visible |
| 3 | should_showEmpty_when_noCards | Empty | — | "대화 기록이 없습니다" |
| 4 | should_showError_when_apiFails | Error | — | error message + retry |
| 5 | should_showFilterChips_when_screenDisplayed | Data | — | "최근 7일", "최근 30일", "전체" chips |
| 6 | should_triggerFilter_when_chipClicked | Data | click "최근 7일" | onFilterChange(7) invoked |
| 7 | should_showCardTitle_when_dataLoaded | card.title="온디바이스 논의" | — | title text visible |
| 8 | should_showCardSummary_when_dataLoaded | card with summary | — | summary text visible, maxLines=3 |
| 9 | should_showTopicChip_when_cardHasTopic | card.topic="아키텍처" | — | topic chip visible |
| 10 | should_showSentimentIcon_when_commitmentDetected | COMMITMENT sentiment | — | green check icon visible |
| 11 | should_navigateToDetail_when_cardClicked | Data | click card | onCardClick(cardId) invoked |
| 12 | should_showCustomerName_when_topBarLoaded | customerName="삼성전자" | — | TopAppBar shows "삼성전자" |

---

## 3. CardDetailScreen (10 tests)

### File: `ui/card/CardDetailScreenTest.kt`

| # | Test Name | Assert On | Action | Expected |
|---|-----------|-----------|--------|----------|
| 1 | should_showLoading_when_stateIsLoading | Loading | — | progress indicator |
| 2 | should_showTitle_when_dataLoaded | Data(title="온디바이스") | — | title in TopAppBar |
| 3 | should_showSummary_when_dataLoaded | Data(summary="고객이...") | — | summary text visible |
| 4 | should_showKeyStatements_when_present | Data(3 statements) | — | 3 statement items visible |
| 5 | should_showSpeakerName_when_statementDisplayed | stmt.speaker="고객: 김철수" | — | "고객: 김철수" text |
| 6 | should_showSentimentBadge_when_statementHasSentiment | stmt.sentiment=COMMITMENT | — | commitment badge visible |
| 7 | should_showKeywordChips_when_keywordsExist | 2 keywords | — | 2 keyword chips in FlowRow |
| 8 | should_openKnowledgePanel_when_keywordChipClicked | Data | click keyword chip | onKeywordClick invoked |
| 9 | should_showError_when_loadFails | Error("not found") | — | error message + retry |
| 10 | should_hideKeywordsSection_when_noKeywords | Data(keywords=[]) | — | "키워드" header not visible |

---

## 4. KnowledgePanel (8 tests)

### File: `ui/card/KnowledgePanelTest.kt`

| # | Test Name | Assert On | Action | Expected |
|---|-----------|-----------|--------|----------|
| 1 | should_showLoading_when_panelOpened | Loading | — | progress indicator in sheet |
| 2 | should_showKeywordTitle_when_dataLoaded | Data(term="온디바이스") | — | "온디바이스" title |
| 3 | should_showArticles_when_articlesExist | 2 articles | — | 2 article sections |
| 4 | should_showContextualExplanation_when_present | article.contextual="..." | — | explanation text in Primary color |
| 5 | should_showSources_when_present | sources=["NVIDIA docs"] | — | source text visible |
| 6 | should_showRelatedKeywords_when_present | relatedKeywords=["엣지"] | — | suggestion chip "엣지" |
| 7 | should_showEmptyMessage_when_noArticles | Empty | — | "관련 지식이 없습니다" |
| 8 | should_showError_when_loadFails | Error | — | error + retry in sheet |

---

## 5. SearchScreen (10 tests)

### File: `ui/search/SearchScreenTest.kt`

| # | Test Name | Assert On | Action | Expected |
|---|-----------|-----------|--------|----------|
| 1 | should_showSearchBar_when_screenOpened | Initial | — | search bar with placeholder |
| 2 | should_autoFocus_when_screenOpened | Initial | — | search bar is active/focused |
| 3 | should_showInitialMessage_when_noQuery | Initial | — | "검색어를 입력하세요" |
| 4 | should_showResults_when_searchReturnsData | Results(3 cards) | — | 3 card items + "3개 결과" |
| 5 | should_showEmpty_when_searchReturnsNothing | Empty | — | "검색 결과가 없습니다" |
| 6 | should_showError_when_searchFails | Error | — | error + retry |
| 7 | should_triggerSearch_when_submitClicked | query typed | submit | onSearch("query") invoked |
| 8 | should_showFilterRow_when_screenDisplayed | any | — | customer dropdown, date selector visible |
| 9 | should_triggerFilter_when_customerSelected | Results | select customer | onCustomerFilter invoked |
| 10 | should_navigateToDetail_when_resultCardClicked | Results | click card | onCardClick invoked |

---

## 6. Navigation Tests (12 tests)

### File: `ui/navigation/NavGraphTest.kt`

| # | Test Name | Start | Action | Expected Destination |
|---|-----------|-------|--------|---------------------|
| 1 | should_startAtCustomerList_when_appLaunched | — | launch | "customers" route |
| 2 | should_navigateToCards_when_customerClicked | customers | click customer 1 | "customers/1/cards" |
| 3 | should_navigateToDetail_when_cardClicked | customers/1/cards | click card 5 | "cards/5" |
| 4 | should_navigateToSearch_when_searchClicked | customers | click search | "search" |
| 5 | should_navigateBack_when_backPressed | cards/5 | back | customers/1/cards |
| 6 | should_navigateBackToList_when_backFromCards | customers/1/cards | back | customers |
| 7 | should_preserveScrollPosition_when_returningToList | customers/1/cards scrolled | navigate to detail, back | same scroll position |
| 8 | should_passCustomerId_when_navigatingToCards | customers | click customer 3 | argument customerId=3 |
| 9 | should_passCardId_when_navigatingToDetail | cards list | click card 7 | argument cardId=7 |
| 10 | should_handleDeepLink_when_cardIdProvided | — | deep link cards/5 | CardDetailScreen(5) |
| 11 | should_handleInvalidRoute_when_unknownPath | — | navigate "unknown" | stay at customers |
| 12 | should_handleBackFromSearch_when_pressed | search | back | previous screen |

---

## 7. Accessibility Tests (20 tests — 4 per screen)

### File: `ui/accessibility/AccessibilityTest.kt`

| # | Screen | Test Name | Expected |
|---|--------|-----------|----------|
| 1 | CustomerList | should_haveContentDescription_when_customerCardDisplayed | "고객: {name}, {company}" |
| 2 | CustomerList | should_haveTouchTarget_when_customerCardDisplayed | minSize 48dp × 48dp |
| 3 | CustomerList | should_announceLoading_when_loadingState | semantics: "로딩 중" |
| 4 | CustomerList | should_announceError_when_errorState | semantics: error message text |
| 5 | CardNewsList | should_haveContentDescription_when_cardItemDisplayed | "카드: {title}, {date}" |
| 6 | CardNewsList | should_haveTouchTarget_when_cardItemDisplayed | minSize 48dp |
| 7 | CardNewsList | should_announceFilterChange_when_chipSelected | semantics: "필터: 최근 7일" |
| 8 | CardNewsList | should_announceEmpty_when_emptyState | semantics: "대화 기록이 없습니다" |
| 9 | CardDetail | should_haveContentDescription_when_statementDisplayed | "발언: {speaker}, {text}" |
| 10 | CardDetail | should_haveTouchTarget_when_keywordChipDisplayed | minSize 48dp |
| 11 | CardDetail | should_announceSentiment_when_badgeDisplayed | semantics: "약속" / "요청" |
| 12 | CardDetail | should_announceTimestamp_when_statementDisplayed | semantics: "02:00" |
| 13 | KnowledgePanel | should_haveContentDescription_when_articleDisplayed | "지식: {title}" |
| 14 | KnowledgePanel | should_haveTouchTarget_when_relatedChipDisplayed | minSize 48dp |
| 15 | KnowledgePanel | should_announceDismiss_when_sheetDismissable | semantics: dismiss action |
| 16 | KnowledgePanel | should_announceEmpty_when_noArticles | semantics: "관련 지식이 없습니다" |
| 17 | Search | should_haveContentDescription_when_searchBarDisplayed | "검색" placeholder |
| 18 | Search | should_haveTouchTarget_when_filterDisplayed | minSize 48dp |
| 19 | Search | should_announceResultCount_when_resultsShown | semantics: "{N}개 결과" |
| 20 | Search | should_announceEmpty_when_noResults | semantics: "검색 결과가 없습니다" |

---

## Summary

| Component | Test Count |
|-----------|-----------|
| CustomerListScreen | 10 |
| CardNewsListScreen | 12 |
| CardDetailScreen | 10 |
| KnowledgePanel | 8 |
| SearchScreen | 10 |
| Navigation | 12 |
| Accessibility | 20 |
| State Combinations | 20 |
| Shared Components | 15 |
| Dark Mode | 8 |
| **Total** | **125** |

---

## 8. State Combination Tests (20 tests)

### File: `ui/state/StateCombinationTest.kt`

각 스크린의 복합 상태 전환을 테스트합니다.

| # | Test Name | Screen | State A → State B | Assertion |
|---|-----------|--------|-------------------|-----------|
| 1 | should_transitionToData_when_loadingCompletes | CustomerList | Loading → Data | progress hidden, list shown |
| 2 | should_transitionToError_when_loadingFails | CustomerList | Loading → Error | progress hidden, error shown |
| 3 | should_transitionToLoading_when_retryClicked | CustomerList | Error → Loading | error hidden, progress shown |
| 4 | should_showRefreshIndicator_when_dataRefreshing | CardNewsList | Data → Data(refreshing) | pull indicator visible |
| 5 | should_hideRefreshIndicator_when_refreshComplete | CardNewsList | Data(refreshing) → Data | indicator hidden |
| 6 | should_showLoadingMore_when_paginating | CardNewsList | Data → Data(loadingMore) | bottom spinner visible |
| 7 | should_appendItems_when_paginationComplete | CardNewsList | Data(loadingMore) → Data(moreItems) | list grows |
| 8 | should_showPanel_when_keywordTapped | CardDetail | Data → Data+Panel(Loading) | sheet appears |
| 9 | should_showPanelData_when_knowledgeLoaded | CardDetail | Panel(Loading) → Panel(Data) | articles visible |
| 10 | should_hidePanel_when_dismissed | CardDetail | Data+Panel → Data | sheet hidden |
| 11 | should_transitionToResults_when_searchReturns | Search | Loading → Results | results list shown |
| 12 | should_transitionToEmpty_when_searchEmpty | Search | Loading → Empty | empty message shown |
| 13 | should_transitionToInitial_when_queryClearedFromResults | Search | Results → Initial | initial message shown |
| 14 | should_preserveFilters_when_stateChanges | Search | Results + filters → new Results | filters preserved |
| 15 | should_showSnackbar_when_refreshFails | CardNewsList | Data → refresh fails | snackbar + data preserved |
| 16 | should_hideKeywordsSection_when_zeroKeywords | CardDetail | Data(keywords=[]) | section not rendered |
| 17 | should_hideStatementsSection_when_zeroStatements | CardDetail | Data(statements=[]) | section not rendered |
| 18 | should_showCommitmentIcon_when_dominantSentiment | CardNewsList | card with COMMITMENT | green icon |
| 19 | should_showRequestIcon_when_dominantSentiment | CardNewsList | card with REQUEST | orange icon |
| 20 | should_showNoIcon_when_neutralSentiment | CardNewsList | card with NEUTRAL | no icon |

---

## 9. Shared UI Component Tests (15 tests)

### File: `ui/components/SharedComponentTest.kt`

| # | Test Name | Component | Given | Then |
|---|-----------|-----------|-------|------|
| 1 | should_showMessage_when_emptyStateRendered | EmptyState | icon + message | icon + text visible |
| 2 | should_showRetryButton_when_errorStateRendered | ErrorState | message + onRetry | text + button visible |
| 3 | should_invokeCallback_when_retryClicked | ErrorState | onRetry lambda | callback invoked |
| 4 | should_showText_when_topicChipRendered | TopicChip | topic="아키텍처" | chip with text |
| 5 | should_colorByCategory_when_keywordChipRendered | KeywordChip | category=TECHNOLOGY | purple background |
| 6 | should_colorGreen_when_productCategory | KeywordChip | category=PRODUCT | green background |
| 7 | should_colorCyan_when_architectureCategory | KeywordChip | category=ARCHITECTURE | cyan background |
| 8 | should_colorRed_when_businessCategory | KeywordChip | category=BUSINESS | red background |
| 9 | should_showBadge_when_sentimentBadgeRendered | SentimentBadge | COMMITMENT | "약속" text + blue |
| 10 | should_showRequestBadge_when_requestSentiment | SentimentBadge | REQUEST | "요청" text + amber |
| 11 | should_showNegativeBadge_when_negativeSentiment | SentimentBadge | NEGATIVE | "부정" text + red |
| 12 | should_formatRelativeDate_when_recent | formatRelative | 2 hours ago | "2시간 전" |
| 13 | should_formatRelativeDate_when_yesterday | formatRelative | yesterday | "어제" |
| 14 | should_formatAbsoluteDate_when_old | formatRelative | 30 days ago | "2026.02.26" |
| 15 | should_formatTimestamp_when_seconds | formatTimestamp | 125 seconds | "02:05" |

---

## 10. Dark Mode Tests (8 tests)

### File: `ui/theme/DarkModeTest.kt`

| # | Test Name | Screen | Light | Dark |
|---|-----------|--------|-------|------|
| 1 | should_useLightColors_when_lightMode | CustomerList | Background = #F9FAFB | — |
| 2 | should_useDarkColors_when_darkMode | CustomerList | — | Background = #111827 |
| 3 | should_readableText_when_darkMode | CardDetail | — | OnBackground = #F9FAFB |
| 4 | should_visibleChips_when_darkMode | CardDetail | — | chip colors contrast ratio ≥ 4.5:1 |
| 5 | should_visibleSentiment_when_darkMode | CardNewsList | — | badge colors contrast ratio ≥ 4.5:1 |
| 6 | should_visiblePanel_when_darkMode | KnowledgePanel | — | sheet surface contrasts |
| 7 | should_readableSearch_when_darkMode | Search | — | search bar text visible |
| 8 | should_switchTheme_when_systemChanges | any | light → dark | colors update |
