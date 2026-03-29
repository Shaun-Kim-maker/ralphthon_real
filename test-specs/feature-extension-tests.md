# Feature Extension Tests — Safe Independent Features (30% Strategy)

**Target:** 150 tests
**Strategy:** Independent features that do not risk build stability. Each feature is self-contained with no cross-feature dependencies.
**Framework:** JUnit 5 + MockK + Turbine + Compose Testing (androidTest)

---

## 1. Customer Favorites (35 tests)

### FavoritesRepository (8 tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/favorites/FavoritesRepositoryTest.kt`
**Impl:** `app/src/main/java/com/ralphthon/app/data/favorites/FavoritesRepositoryImpl.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | `should_saveFavorite_when_customerIdProvided` | DataStore mock; customer ID `"cust-001"` | `saveFavorite("cust-001")` called | DataStore `updateData` invoked with set containing `"cust-001"` |
| 2 | `should_removeFavorite_when_customerIdProvided` | DataStore contains `"cust-001"` | `removeFavorite("cust-001")` called | DataStore `updateData` invoked; resulting set does NOT contain `"cust-001"` |
| 3 | `should_returnTrue_when_customerIsFavorite` | DataStore contains `"cust-001"` | `isFavorite("cust-001")` called | Returns `true` |
| 4 | `should_returnFalse_when_customerNotFavorite` | DataStore contains `"cust-002"` only | `isFavorite("cust-001")` called | Returns `false` |
| 5 | `should_returnAllFavorites_when_requested` | DataStore contains `{"cust-001", "cust-002", "cust-003"}` | `getAllFavorites()` called | Returns set of 3 IDs matching stored values |
| 6 | `should_persistAcrossRestart_when_saved` | DataStore (in-memory test impl) with persistence | Save favorite; create new repository instance pointing to same DataStore | New instance `isFavorite("cust-001")` returns `true` |
| 7 | `should_handleDuplicateAdd_when_alreadyFavorite` | DataStore already contains `"cust-001"` | `saveFavorite("cust-001")` called again | Set still contains exactly one entry for `"cust-001"`; no exception |
| 8 | `should_handleRemoveNonexistent_when_notFavorite` | DataStore is empty | `removeFavorite("cust-999")` called | No exception thrown; DataStore state unchanged |

---

### ToggleFavoriteUseCase (7 tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/favorites/ToggleFavoriteUseCaseTest.kt`
**Impl:** `app/src/main/java/com/ralphthon/app/domain/usecase/ToggleFavoriteUseCase.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 9 | `should_addFavorite_when_notFavorited` | Repository mock; `isFavorite("cust-001")` returns `false` | `toggle("cust-001")` called | `saveFavorite("cust-001")` called on repository; returns `true` |
| 10 | `should_removeFavorite_when_alreadyFavorited` | Repository mock; `isFavorite("cust-001")` returns `true` | `toggle("cust-001")` called | `removeFavorite("cust-001")` called on repository; returns `false` |
| 11 | `should_returnNewState_when_toggled` | `isFavorite` returns `false` | `toggle("cust-001")` | Return value is `true` (new state after add) |
| 12 | `should_throw_when_customerIdBlank` | Any repository mock | `toggle("")` called | Throws `IllegalArgumentException` with message about blank ID |
| 13 | `should_throw_when_customerIdIsWhitespace` | Any repository mock | `toggle("   ")` called | Throws `IllegalArgumentException` |
| 14 | `should_notCallRepository_when_idInvalid` | Repository mock | `toggle("")` called | Neither `saveFavorite` nor `removeFavorite` invoked on repository |
| 15 | `should_delegateToRepository_when_validId` | Repository mock; `isFavorite` returns `false` | `toggle("cust-abc")` | Repository `saveFavorite` called exactly once with `"cust-abc"` |

---

### CustomerListViewModel + Favorites (10 tests)

**File:** `app/src/test/java/com/ralphthon/app/ui/customerlist/CustomerListViewModelFavoritesTest.kt`
**Impl:** `app/src/main/java/com/ralphthon/app/ui/customerlist/CustomerListViewModel.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 16 | `should_showFavoriteIcon_when_customerIsFavorite` | ViewModel loaded; `isFavorite("cust-001")` returns `true` | `uiState` collected | Customer item for `"cust-001"` has `isFavorite = true` |
| 17 | `should_showNonFavoriteIcon_when_customerNotFavorite` | `isFavorite("cust-002")` returns `false` | `uiState` collected | Customer item for `"cust-002"` has `isFavorite = false` |
| 18 | `should_toggleFavorite_when_iconClicked` | Customer `"cust-001"` is not favorite | `onToggleFavorite("cust-001")` called | `toggleFavoriteUseCase` invoked; state updated to `isFavorite = true` |
| 19 | `should_sortFavoritesFirst_when_favoriteFilterEnabled` | 5 customers; `"cust-001"` and `"cust-003"` are favorites | `setFavoriteFilterOn(true)` | Customers `"cust-001"` and `"cust-003"` appear at top of list |
| 20 | `should_showAllCustomers_when_favoriteFilterDisabled` | Favorite filter was on | `setFavoriteFilterOn(false)` | All 5 customers shown; favorites not necessarily first |
| 21 | `should_persistFavoriteAcrossReload_when_appRestarted` | ToggleFavoriteUseCase backed by real DataStore test double | Toggle favorite; call `viewModel.loadCustomers()` again | Customer still shows `isFavorite = true` after reload |
| 22 | `should_reflectToggleImmediately_when_optimisticUpdate` | Customer list loaded | Toggle favorite | UI state updated before repository confirmation (optimistic) |
| 23 | `should_revertToggle_when_repositoryThrows` | Repository throws on `saveFavorite` | Toggle favorite | State reverts to original `isFavorite` value; error event emitted |
| 24 | `should_notCrash_when_multipleTogglesRapid` | Customer `"cust-001"` | Toggle 5 times in rapid succession | No crash; final state matches parity of toggle count (odd = added, even = original) |
| 25 | `should_emitOneOffEvent_when_favoriteAddedSuccessfully` | Customer not favorited | Toggle favorite (success) | `events` SharedFlow emits `FavoriteAdded` event exactly once |

---

### UI Favorites Tests (10 tests)

**File:** `app/src/androidTest/java/com/ralphthon/app/ui/favorites/FavoritesUiTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 26 | `should_showFilledHeart_when_customerIsFavorite` | Compose test; customer item with `isFavorite = true` | Screen rendered | Node with `contentDescription = "Remove from favorites"` exists |
| 27 | `should_showOutlineHeart_when_customerNotFavorite` | Customer item with `isFavorite = false` | Screen rendered | Node with `contentDescription = "Add to favorites"` exists |
| 28 | `should_toggleHeartIcon_when_clicked` | Customer item `isFavorite = false` | Click heart icon | Icon changes to filled; `contentDescription` updates to "Remove from favorites" |
| 29 | `should_animateToggle_when_iconClicked` | Heart icon in idle state | Click icon | Icon scale animation plays (semantic tag `"heart_animating"` briefly present) |
| 30 | `should_showFavoritesAtTop_when_filterEnabled` | 3 customers; 1 favorited | Enable favorite filter chip | Favorited customer card is first in list |
| 31 | `should_showFilterChip_when_favoritesFeatureEnabled` | Customer list screen | Screen rendered | `onNodeWithTag("favorites_filter_chip")` exists and is displayed |
| 32 | `should_showEmptyState_when_filterOnButNoFavorites` | No favorites saved; filter enabled | Toggle filter chip | `onNodeWithText("No favorites yet")` displayed |
| 33 | `should_showCorrectCount_when_favoritesExist` | 3 favorites saved | Favorites filter chip rendered | Chip label shows `"Favorites (3)"` |
| 34 | `should_maintainScrollPosition_when_favoriteToggled` | List of 20 customers; scrolled to item 15 | Toggle favorite on item 15 | List does not scroll to top; item 15 still visible |
| 35 | `should_accessibilityAnnounce_when_favoriteToggled` | Accessibility services active | Toggle favorite | Accessibility announcement `"Added to favorites"` or `"Removed from favorites"` fired |

---

## 2. Card Sort Options (25 tests)

### SortCardsUseCase (10 tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/sort/SortCardsUseCaseTest.kt`
**Impl:** `app/src/main/java/com/ralphthon/app/domain/usecase/SortCardsUseCase.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 36 | `should_sortByDateDesc_when_newestFirst` | List of 5 cards with distinct dates | `sort(cards, SortOption.NEWEST_FIRST)` | First card has latest date; last card has earliest date |
| 37 | `should_sortByDateAsc_when_oldestFirst` | Same 5 cards | `sort(cards, SortOption.OLDEST_FIRST)` | First card has earliest date |
| 38 | `should_sortBySentiment_when_sentimentSort` | Cards with `POSITIVE`, `NEUTRAL`, `NEGATIVE` sentiment | `sort(cards, SortOption.SENTIMENT)` | `POSITIVE` cards first, then `NEUTRAL`, then `NEGATIVE` |
| 39 | `should_sortByTopicAlphabetically_when_topicSort` | Cards with topics `"Zebra"`, `"Apple"`, `"Mango"` | `sort(cards, SortOption.TOPIC)` | Order: `"Apple"`, `"Mango"`, `"Zebra"` |
| 40 | `should_maintainRelativeOrder_when_sameDate` | 3 cards with identical dates | `sort(cards, SortOption.NEWEST_FIRST)` | Relative order of equal-date cards preserved (stable sort) |
| 41 | `should_returnEmptyList_when_inputEmpty` | Empty list | `sort(emptyList(), SortOption.NEWEST_FIRST)` | Returns empty list; no exception |
| 42 | `should_returnSingleItem_when_oneCard` | List with one card | `sort(listOf(card), SortOption.TOPIC)` | Returns list with same single card |
| 43 | `should_sortByPrimaryThenSecondary_when_combinedSort` | Cards: same topic, different dates | `sort(cards, SortOption.TOPIC_THEN_NEWEST)` | Cards grouped by topic; within group sorted by date desc |
| 44 | `should_resetToPage0_when_sortChanged` | ViewModel with current page = 2 | Change sort option | `currentPage` resets to 0; new load triggered from page 0 |
| 45 | `should_placeNullDatesLast_when_dateSort` | Cards: 2 with dates, 1 with `null` date | `sort(cards, SortOption.NEWEST_FIRST)` | Cards with dates appear first; null-date card is last |

---

### CardNewsListViewModel + Sort (10 tests)

**File:** `app/src/test/java/com/ralphthon/app/ui/cardnewslist/CardNewsListViewModelSortTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 46 | `should_showSortMenu_when_sortButtonClicked` | ViewModel with `showSortMenu = false` | `onSortButtonClicked()` | `uiState.showSortMenu == true` |
| 47 | `should_hideSortMenu_when_optionSelected` | Sort menu open | `onSortOptionSelected(SortOption.OLDEST_FIRST)` | `uiState.showSortMenu == false` |
| 48 | `should_applySortAndReload_when_sortSelected` | ViewModel loaded with default sort | `onSortOptionSelected(SortOption.TOPIC)` | API called with `sort = TOPIC`; state updated with sorted cards |
| 49 | `should_showActiveSortIndicator_when_sortApplied` | Default sort active | Apply `SENTIMENT` sort | `uiState.activeSortOption == SortOption.SENTIMENT` |
| 50 | `should_resetToDefaultSort_when_clearSortCalled` | `TOPIC` sort active | `onClearSort()` | `activeSortOption` resets to `NEWEST_FIRST`; data reloaded |
| 51 | `should_preserveSortWhenPaginating_when_sortActive` | `OLDEST_FIRST` sort applied; page 1 loaded | `loadNextPage()` | API called for page 2 with `sort = OLDEST_FIRST` |
| 52 | `should_showSortOptionList_when_menuOpened` | Any state | `onSortButtonClicked()` | `uiState.sortOptions` contains all 4 options (NEWEST, OLDEST, SENTIMENT, TOPIC) |
| 53 | `should_cancelActiveLoad_when_sortChangedDuringLoad` | Data load in progress | Change sort option | Previous load job cancelled; new load with new sort starts |
| 54 | `should_notDuplicateCards_when_sortChangedOnPage2` | Page 2 loaded; sort changes | Sort change triggers reload from page 0 | List contains only page 0 results; no page 1/2 data retained |
| 55 | `should_emitSortChangedEvent_when_sortApplied` | ViewModel | Apply sort | `events` emits `SortChanged(option)` for analytics/scrolling reset |

---

### UI Sort Tests (5 tests)

**File:** `app/src/androidTest/java/com/ralphthon/app/ui/sort/CardSortUiTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 56 | `should_showSortButton_when_cardListVisible` | Card list screen rendered | Screen displayed | `onNodeWithTag("sort_button")` exists |
| 57 | `should_showSortBottomSheet_when_sortButtonClicked` | Card list screen | Click `"sort_button"` | Bottom sheet with sort options appears; `onNodeWithText("Newest First")` visible |
| 58 | `should_dismissBottomSheet_when_optionSelected` | Sort bottom sheet open | Click `"Oldest First"` | Bottom sheet dismissed; sort indicator updated |
| 59 | `should_showActiveIndicator_when_nonDefaultSortApplied` | Default sort (Newest First) | Select `"By Topic"` | Sort button shows active indicator (e.g., badge or filled icon) |
| 60 | `should_scrollToTopOnSortChange_when_sortApplied` | List scrolled down to item 20 | Change sort | List scrolls back to top (item 0 visible) |

---

## 3. Recent Search History (30 tests)

### SearchHistoryRepository (10 tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/search/SearchHistoryRepositoryTest.kt`
**Impl:** `app/src/main/java/com/ralphthon/app/data/search/SearchHistoryRepositoryImpl.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 61 | `should_saveQuery_when_searchPerformed` | Empty history; DataStore mock | `saveQuery("kotlin")` | DataStore updated with `"kotlin"` at head of list |
| 62 | `should_returnRecent10_when_historyHasMore` | 15 saved queries | `getHistory()` | Returns exactly 10 most recent queries |
| 63 | `should_moveToTop_when_duplicateQuerySearched` | History: `["kotlin", "java"]`; query `"java"` searched again | `saveQuery("java")` | History becomes `["java", "kotlin"]`; no duplicate entry |
| 64 | `should_removeOldest_when_exceeds10Items` | Exactly 10 items in history | `saveQuery("new_query")` | History has 10 items; oldest item removed; `"new_query"` at head |
| 65 | `should_clearAll_when_clearHistoryRequested` | 5 items in history | `clearHistory()` | `getHistory()` returns empty list |
| 66 | `should_removeOne_when_deleteItemRequested` | History: `["kotlin", "java", "android"]` | `deleteQuery("java")` | History: `["kotlin", "android"]`; `"java"` absent |
| 67 | `should_persistAcrossRestart_when_saved` | DataStore test double with persistence | Save `"flutter"`; create new repository instance | New instance `getHistory()` includes `"flutter"` |
| 68 | `should_returnEmpty_when_noHistoryExists` | Fresh DataStore (empty) | `getHistory()` | Returns empty list; no exception |
| 69 | `should_trimWhitespace_when_savingQuery` | Empty history | `saveQuery("  kotlin  ")` | Saved as `"kotlin"` (trimmed) |
| 70 | `should_notSaveBlank_when_queryIsEmpty` | Empty history | `saveQuery("")` | History remains empty; DataStore not written to |

---

### SearchViewModel + History (10 tests)

**File:** `app/src/test/java/com/ralphthon/app/ui/search/SearchViewModelHistoryTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 71 | `should_showHistory_when_searchBarFocused` | 3 history items saved | `onSearchBarFocused()` | `uiState.showHistory == true`; `uiState.historyItems.size == 3` |
| 72 | `should_hideHistory_when_userStartsTyping` | History visible | `onQueryChanged("k")` | `uiState.showHistory == false` |
| 73 | `should_fillQuery_when_historyItemClicked` | History showing `"kotlin"` | `onHistoryItemClicked("kotlin")` | `uiState.query == "kotlin"`; search triggered |
| 74 | `should_removeHistoryItem_when_deleteClicked` | History: `["kotlin", "java"]` | `onDeleteHistoryItem("kotlin")` | History updated to `["java"]`; state reflects change |
| 75 | `should_clearAllHistory_when_clearButtonClicked` | 5 history items | `onClearHistory()` | `uiState.historyItems` is empty; clear button hidden |
| 76 | `should_saveToHistory_when_searchCompleted` | Search for `"compose"` completes | Search returns results | `"compose"` added to history; `getHistory()` includes it |
| 77 | `should_showHistory_when_queryCleared` | User had typed a query; clears it | `onQueryChanged("")` AND search bar still focused | `uiState.showHistory == true` with existing history items |
| 78 | `should_notSaveToHistory_when_searchCancelled` | User types then cancels | `onSearchCancelled()` before search triggered | History not updated |
| 79 | `should_showRecentLabel_when_historyDisplayed` | History items present | `onSearchBarFocused()` | `uiState.historyLabel == "Recent Searches"` |
| 80 | `should_limitDisplayTo5_when_historyHas10Items` | 10 items in history | `onSearchBarFocused()` | `uiState.historyItems.size == 5` (display cap, not storage cap) |

---

### UI History Tests (10 tests)

**File:** `app/src/androidTest/java/com/ralphthon/app/ui/search/SearchHistoryUiTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 81 | `should_showHistoryList_when_searchBarFocused` | Search screen with 3 saved history items | Tap search bar | `onNodeWithTag("search_history_list")` displayed |
| 82 | `should_hideHistoryList_when_userTypes` | History list visible | Type `"a"` in search bar | `onNodeWithTag("search_history_list")` not displayed |
| 83 | `should_populateSearchBar_when_historyItemTapped` | History shows `"kotlin"` | Tap `"kotlin"` history item | Search bar text becomes `"kotlin"` |
| 84 | `should_showDeleteIcon_when_historyItemRendered` | History item `"kotlin"` | History list displayed | `onNodeWithContentDescription("Delete kotlin from history")` exists |
| 85 | `should_removeItem_when_deleteIconTapped` | History: `["kotlin", "java"]` | Tap delete on `"kotlin"` | `"kotlin"` item no longer in list; `"java"` remains |
| 86 | `should_showClearAllButton_when_historyNotEmpty` | 3 items in history | Focus search bar | `onNodeWithText("Clear all")` visible |
| 87 | `should_clearAllItems_when_clearAllTapped` | 3 history items | Tap `"Clear all"` | History list empty; `"Clear all"` button hidden |
| 88 | `should_showEmptyHistory_when_noItems` | No history saved | Focus search bar | `onNodeWithText("No recent searches")` displayed |
| 89 | `should_showRecentLabel_when_historyDisplayed` | History items exist | Focus search bar | `onNodeWithText("Recent Searches")` displayed above list |
| 90 | `should_swipeToDismissItem_when_swipeLeft` | History item visible | Swipe left on history item | Item removed from list with dismissal animation |

---

## 4. Upload Progress (25 tests)

### UploadViewModel (15 tests)

**File:** `app/src/test/java/com/ralphthon/app/ui/upload/UploadViewModelTest.kt`
**Impl:** `app/src/main/java/com/ralphthon/app/ui/upload/UploadViewModel.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 91 | `should_showIdle_when_noUploadInProgress` | Fresh ViewModel | `uiState` collected immediately | `uiState.value == UploadState.Idle` |
| 92 | `should_showSelecting_when_filePickerOpened` | Idle state | `onOpenFilePicker()` | `uiState.value == UploadState.Selecting` |
| 93 | `should_showSelected_when_fileChosen` | File picker open | `onFileSelected(file)` | `uiState.value == UploadState.Selected(file)`; file name displayed |
| 94 | `should_showUploading_when_uploadStarted` | File selected | `onStartUpload()` | `uiState` transitions to `UploadState.Uploading(progress = 0)` |
| 95 | `should_showProgress_when_progressUpdates` | Upload in progress | Repository emits progress: 25, 50, 75 | Turbine sees `Uploading(25)`, `Uploading(50)`, `Uploading(75)` in order |
| 96 | `should_showSuccess_when_uploadCompletes` | Upload at 100% | Repository emits completion | `uiState.value == UploadState.Success` |
| 97 | `should_showError_when_uploadFails` | Upload started; repository throws `IOException` | `advanceUntilIdle()` | `uiState.value == UploadState.Error("Upload failed")` |
| 98 | `should_allowCancel_when_uploading` | Upload at 50% | `onCancelUpload()` | Upload job cancelled; `uiState.value == UploadState.Idle` |
| 99 | `should_resetState_when_newUploadStarted` | Previous upload in `Success` state | `onOpenFilePicker()` | `uiState` resets to `Selecting` |
| 100 | `should_rejectFile_when_fileSizeExceedsLimit` | File size = 101 MB; limit = 100 MB | `onFileSelected(oversizedFile)` | `uiState.value == UploadState.Error("File too large")`; upload not started |
| 101 | `should_rejectFile_when_fileTypeNotAllowed` | File type = `.exe` | `onFileSelected(exeFile)` | `uiState.value == UploadState.Error("File type not supported")` |
| 102 | `should_showCustomerSelector_when_noCustomerSelected` | No customer selected in ViewModel | `onStartUpload()` | `uiState.value == UploadState.NeedsCustomer`; customer selector shown |
| 103 | `should_associateUploadWithCustomer_when_customerSelected` | Customer `"cust-001"` selected | `onStartUpload()` | Repository called with `customerId = "cust-001"` |
| 104 | `should_showRetrying_when_uploadFailsAndRetries` | Repository fails once then succeeds | `onStartUpload()` with retry | Turbine sees `Uploading` → `Retrying` → `Uploading` → `Success` |
| 105 | `should_notStartUpload_when_noFileSelected` | Idle state (no file) | `onStartUpload()` called without selecting file | State remains `Idle`; repository `upload()` never called |

---

### UI Upload Tests (10 tests)

**File:** `app/src/androidTest/java/com/ralphthon/app/ui/upload/UploadUiTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 106 | `should_showUploadButton_when_screenDisplayed` | Upload screen rendered | Screen loaded | `onNodeWithTag("upload_button")` exists and enabled |
| 107 | `should_showProgressBar_when_uploading` | ViewModel in `Uploading(50)` state | Screen rendered | `onNodeWithTag("upload_progress_bar")` displayed with progress 50 |
| 108 | `should_showSuccessMessage_when_uploadCompletes` | ViewModel transitions to `Success` | State change observed | `onNodeWithText("Upload successful")` displayed |
| 109 | `should_showErrorMessage_when_uploadFails` | ViewModel in `Error("Upload failed")` state | Screen rendered | `onNodeWithText("Upload failed")` displayed |
| 110 | `should_showCancelButton_when_uploading` | Upload in progress | Screen rendered | `onNodeWithTag("cancel_upload_button")` visible and enabled |
| 111 | `should_hideCancelButton_when_idle` | Idle state | Screen rendered | `onNodeWithTag("cancel_upload_button")` does not exist |
| 112 | `should_showFileName_when_fileSelected` | File `"report.pdf"` selected | ViewModel in `Selected` state | `onNodeWithText("report.pdf")` displayed |
| 113 | `should_disableUploadButton_when_noFileSelected` | No file selected | Screen rendered | `onNodeWithTag("upload_button")` disabled (has `isNotEnabled` semantic) |
| 114 | `should_showProgressPercent_when_uploading` | Upload at 75% | Screen rendered | `onNodeWithText("75%")` or equivalent progress text visible |
| 115 | `should_resetToIdle_when_cancelConfirmed` | Cancel confirmation dialog shown | Confirm cancel | Screen returns to idle state; progress bar gone |

---

## 5. Dark Mode (35 tests)

### ThemeRepository (8 tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/theme/ThemeRepositoryTest.kt`
**Impl:** `app/src/main/java/com/ralphthon/app/data/theme/ThemeRepositoryImpl.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 116 | `should_returnLight_when_noPreferenceSaved` | DataStore empty (no saved theme) | `getTheme()` called | Returns `ThemeMode.LIGHT` (default) |
| 117 | `should_saveDarkTheme_when_darkSelected` | DataStore mock | `saveTheme(ThemeMode.DARK)` | DataStore `updateData` called with `DARK` |
| 118 | `should_saveLightTheme_when_lightSelected` | DataStore contains `DARK` | `saveTheme(ThemeMode.LIGHT)` | DataStore updated to `LIGHT` |
| 119 | `should_saveSystemTheme_when_systemSelected` | DataStore mock | `saveTheme(ThemeMode.SYSTEM)` | DataStore updated to `SYSTEM` |
| 120 | `should_persistChoice_when_saved` | `saveTheme(DARK)` called | `getTheme()` called on same instance | Returns `ThemeMode.DARK` |
| 121 | `should_returnSavedTheme_when_appRestarted` | DataStore test double with `DARK` stored | New repository instance created | `getTheme()` returns `ThemeMode.DARK` |
| 122 | `should_emitThemeChanges_when_themeFlow_collected` | Turbine collecting `themeFlow` | `saveTheme(DARK)` then `saveTheme(LIGHT)` | Turbine receives `DARK` then `LIGHT` in order |
| 123 | `should_defaultToSystem_when_configuredForSystemDefault` | Repository configured with `defaultTheme = SYSTEM` | `getTheme()` with no saved pref | Returns `ThemeMode.SYSTEM` |

---

### Theme Integration Tests (12 tests)

**File:** `app/src/test/java/com/ralphthon/app/ui/theme/ThemeIntegrationTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 124 | `should_useLightColors_when_lightModeActive` | ThemeViewModel with `ThemeMode.LIGHT` | `currentColors` collected | `colorScheme.background` equals `LightColorScheme.background` |
| 125 | `should_useDarkColors_when_darkModeActive` | ThemeViewModel with `ThemeMode.DARK` | `currentColors` collected | `colorScheme.background` equals `DarkColorScheme.background` |
| 126 | `should_followSystemSetting_when_systemModeActive` | System dark mode = `true`; `ThemeMode.SYSTEM` | `isDarkTheme` computed | Returns `true` (follows system) |
| 127 | `should_applyImmediately_when_themeChangedAtRuntime` | App running in light mode | `saveTheme(DARK)` | `themeFlow` emits `DARK`; color scheme updates in same frame |
| 128 | `should_preserveUiState_when_themeChanged` | Card list loaded with 10 items; theme changes | Switch from light to dark | Card list still shows 10 items; no data reload triggered |
| 129 | `should_usePrimaryColor_correctly_when_lightMode` | Light color scheme applied | `colorScheme.primary` accessed | Equals defined `LightPrimary` color token |
| 130 | `should_usePrimaryColor_correctly_when_darkMode` | Dark color scheme applied | `colorScheme.primary` accessed | Equals defined `DarkPrimary` color token |
| 131 | `should_useCorrectSurfaceColor_when_darkMode` | Dark mode active | `colorScheme.surface` accessed | Equals `DarkSurface` token; NOT same as `LightSurface` |
| 132 | `should_useCorrectErrorColor_when_lightMode` | Light mode | `colorScheme.error` accessed | Equals `LightError` token |
| 133 | `should_useCorrectOnPrimaryColor_when_darkMode` | Dark mode | `colorScheme.onPrimary` accessed | Equals `DarkOnPrimary` token (readable contrast on primary) |
| 134 | `should_useCorrectSecondaryColor_when_lightMode` | Light mode | `colorScheme.secondary` accessed | Equals `LightSecondary` token |
| 135 | `should_notTriggerRecomposition_when_sameThemeReapplied` | Dark mode active | `saveTheme(DARK)` again | No theme flow emission (StateFlow distinctUntilChanged); recomposition avoided |

---

### UI Theme Tests (15 tests)

**File:** `app/src/androidTest/java/com/ralphthon/app/ui/theme/ThemeUiTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 136 | `should_renderCustomerListScreen_correctly_when_lightMode` | Theme = LIGHT | CustomerListScreen displayed | No visual error; primary container visible; no crash |
| 137 | `should_renderCustomerListScreen_correctly_when_darkMode` | Theme = DARK | CustomerListScreen displayed | No visual error; dark background applied; no crash |
| 138 | `should_renderCardListScreen_correctly_when_lightMode` | Theme = LIGHT | CardNewsListScreen displayed | Screen renders without error; text readable |
| 139 | `should_renderCardListScreen_correctly_when_darkMode` | Theme = DARK | CardNewsListScreen displayed | Screen renders without error; dark color tokens applied |
| 140 | `should_renderUploadScreen_correctly_when_darkMode` | Theme = DARK | UploadScreen displayed | No crash; all UI elements visible |
| 141 | `should_renderSearchScreen_correctly_when_darkMode` | Theme = DARK | SearchScreen displayed | No crash; search bar visible and interactable |
| 142 | `should_showToggleSwitch_when_settingsScreenOpened` | Settings screen with theme toggle | Screen displayed | `onNodeWithTag("theme_toggle")` exists |
| 143 | `should_showCurrentMode_when_toggleRendered` | Current theme = DARK | Settings screen rendered | Toggle or label shows `"Dark"` as active |
| 144 | `should_switchToDark_when_darkToggleClicked` | Light mode active | Click dark mode toggle | Theme switches to dark; screen background color changes |
| 145 | `should_switchToLight_when_lightToggleClicked` | Dark mode active | Click light mode toggle | Theme switches to light; screen background color changes |
| 146 | `should_showThemePickerDialog_when_themeButtonClicked` | App bar with theme icon | Click theme icon | Dialog with options `Light`, `Dark`, `System` appears |
| 147 | `should_dismissDialog_when_optionSelected` | Theme picker dialog open | Select `"Dark"` | Dialog dismissed; theme applied |
| 148 | `should_showSystemOption_when_themePickerDisplayed` | Theme picker rendered | Dialog displayed | `onNodeWithText("Follow System")` visible |
| 149 | `should_showCheckmark_when_currentThemeHighlighted` | Current theme = `LIGHT` | Theme picker dialog opened | `onNodeWithText("Light")` has checkmark or selected semantic |
| 150 | `should_persistThemeAcrossRestart_when_darkSelected` | User selects dark mode | App relaunched (ViewModel re-created with SavedState) | Theme is still dark on relaunch; no flash of light mode |

---

## Summary

| Feature | Tests | Key Files |
|---------|-------|-----------|
| Customer Favorites | 35 | `FavoritesRepositoryTest.kt`, `ToggleFavoriteUseCaseTest.kt`, `CustomerListViewModelFavoritesTest.kt`, `FavoritesUiTest.kt` |
| Card Sort Options | 25 | `SortCardsUseCaseTest.kt`, `CardNewsListViewModelSortTest.kt`, `CardSortUiTest.kt` |
| Recent Search History | 30 | `SearchHistoryRepositoryTest.kt`, `SearchViewModelHistoryTest.kt`, `SearchHistoryUiTest.kt` |
| Upload Progress | 25 | `UploadViewModelTest.kt`, `UploadUiTest.kt` |
| Dark Mode | 35 | `ThemeRepositoryTest.kt`, `ThemeIntegrationTest.kt`, `ThemeUiTest.kt` |
| **Total** | **150** | |

## Independence Guarantee

Each feature is isolated:
- **Favorites**: Only touches `FavoritesRepository`, `ToggleFavoriteUseCase`, customer list state field `isFavorite`
- **Sort**: Only touches `SortCardsUseCase`, card list sort state field — no new API params required (client-side sort)
- **Search History**: Only touches `SearchHistoryRepository`, search bar focus state — no changes to search API
- **Upload Progress**: Only touches `UploadViewModel` UI state machine — upload API already exists
- **Dark Mode**: Only touches `ThemeRepository`, theme wrapper — zero business logic changes

No feature modifies shared domain models. Each can be implemented and reverted independently.
