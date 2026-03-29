# Data Layer Test Specification

**Target:** 55+ test cases for API, Database, Mappers
**Framework:** JUnit 5 + MockK + MockWebServer
**Location:** `app/src/test/java/com/ralphthon/app/data/`

---

## 1. CustomerApiClient (7 tests)

### File: `data/api/CustomerApiClientTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnCustomerList_when_apiReturns200 | MockWebServer returns 200 + JSON | getCustomers() | parsed CustomerListResponse |
| 2 | should_throwIOException_when_serverDown | MockWebServer not running | getCustomers() | IOException |
| 3 | should_throwTimeout_when_serverSlow | MockWebServer delays 31s | getCustomers() | TimeoutException |
| 4 | should_returnCustomerDetail_when_apiReturns200 | 200 + customer JSON | getCustomerById(1) | parsed CustomerDetailDto |
| 5 | should_throwNotFound_when_apiReturns404 | 404 response | getCustomerById(999) | HttpException(404) |
| 6 | should_throwServerError_when_apiReturns500 | 500 response | getCustomers() | HttpException(500) |
| 7 | should_parseMalformedJson_when_apiReturnsBadData | invalid JSON body | getCustomers() | JsonParseException |

---

## 2. CardApiClient (8 tests)

### File: `data/api/CardApiClientTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnCards_when_apiReturns200 | 200 + paginated JSON | getCardsByCustomer(1, 0, 20) | CardListResponse |
| 2 | should_returnEmptyCards_when_apiReturnsEmpty | 200 + empty cards array | getCardsByCustomer(1, 0, 20) | empty list, totalCount=0 |
| 3 | should_returnCardDetail_when_apiReturns200 | 200 + card JSON | getCardById(1) | ContextCardDto |
| 4 | should_throwNotFound_when_cardMissing | 404 | getCardById(999) | HttpException(404) |
| 5 | should_returnSearchResults_when_apiReturns200 | 200 + search JSON | searchCards("온디바이스") | SearchResponse |
| 6 | should_passQueryParams_when_searchWithFilters | — | searchCards(q, customerId, from, to) | request URL contains all params |
| 7 | should_throwBadRequest_when_queryEmpty | 400 response | searchCards("") | HttpException(400) |
| 8 | should_throwTimeout_when_searchSlow | 31s delay | searchCards("test") | TimeoutException |

---

## 3. KnowledgeApiClient (5 tests)

### File: `data/api/KnowledgeApiClientTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnKnowledge_when_apiReturns200 | 200 + knowledge JSON | getKnowledge(1) | KnowledgeResponse |
| 2 | should_returnEmptyArticles_when_noKnowledge | 200 + empty articles | getKnowledge(2) | empty articles list |
| 3 | should_throwNotFound_when_keywordMissing | 404 | getKnowledge(999) | HttpException(404) |
| 4 | should_throwServerError_when_api500 | 500 | getKnowledge(1) | HttpException(500) |
| 5 | should_parseContextualExplanation_when_present | JSON has explanation | getKnowledge(1) | field parsed |

---

## 4. UploadApiClient (5 tests)

### File: `data/api/UploadApiClientTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_returnSuccess_when_uploadOk | 201 + UploadResponse | uploadConversation(multipart) | UploadResponse(cardsGenerated=3) |
| 2 | should_throwBadRequest_when_noFile | 400 | uploadConversation(no file) | HttpException(400) |
| 3 | should_throwNotFound_when_customerMissing | 404 | uploadConversation(customerId=999) | HttpException(404) |
| 4 | should_throwPayloadTooLarge_when_fileTooBig | 413 | uploadConversation(bigFile) | HttpException(413) |
| 5 | should_sendMultipart_when_audioProvided | audio file | uploadConversation() | request is multipart/form-data |

---

## 5. CustomerDtoMapper (6 tests)

### File: `data/mapper/CustomerDtoMapperTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_mapAllFields_when_dtoComplete | CustomerSummaryDto with all fields | toDomain() | Customer with matching fields |
| 2 | should_mapContacts_when_detailDto | CustomerDetailDto with 2 contacts | toDomain() | Customer with 2 Contact objects |
| 3 | should_parseDate_when_iso8601String | "2026-03-25T09:30:00Z" | toDomain() | Instant parsed correctly |
| 4 | should_defaultName_when_nameNull | dto.name = null | toDomain() | customer.name = "이름 없음" |
| 5 | should_defaultCompany_when_companyNull | dto.company = null | toDomain() | customer.company = "회사 미등록" |
| 6 | should_handleEmptyContacts_when_noContacts | dto.contacts = [] | toDomain() | customer.contacts = emptyList() |

---

## 6. CardDtoMapper (7 tests)

### File: `data/mapper/CardDtoMapperTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_mapAllFields_when_dtoComplete | ContextCardDto full | toDomain() | ContextCard matching |
| 2 | should_mapKeyStatements_when_present | dto with 3 statements | toDomain() | 3 KeyStatement objects |
| 3 | should_mapKeywords_when_present | dto with 2 keywords | toDomain() | 2 Keyword objects |
| 4 | should_parseSentiment_when_validString | "COMMITMENT" | mapSentiment() | Sentiment.COMMITMENT |
| 5 | should_defaultSentiment_when_unknownString | "UNKNOWN" | mapSentiment() | Sentiment.NEUTRAL |
| 6 | should_parseCategory_when_validString | "TECHNOLOGY" | mapCategory() | KeywordCategory.TECHNOLOGY |
| 7 | should_handleEmptyStatements_when_none | dto.keyStatements = [] | toDomain() | card.keyStatements = emptyList() |

---

## 7. KnowledgeDtoMapper (4 tests)

### File: `data/mapper/KnowledgeDtoMapperTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_mapAllFields_when_dtoComplete | KnowledgeArticleDto full | toDomain() | KnowledgeArticle matching |
| 2 | should_mapSources_when_present | dto.sources = ["a", "b"] | toDomain() | article.sources = ["a", "b"] |
| 3 | should_mapRelatedKeywords_when_present | dto.relatedKeywords = ["x"] | toDomain() | article.relatedKeywords = ["x"] |
| 4 | should_handleEmptySources_when_none | dto.sources = [] | toDomain() | article.sources = emptyList() |

---

## 8. SearchResponseMapper (3 tests)

### File: `data/mapper/SearchResponseMapperTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_mapSearchResult_when_responseComplete | SearchResponse with 3 cards | toDomain() | SearchResult(3 cards, hasMore) |
| 2 | should_computeHasMore_when_totalExceedsPage | totalCount=50, page=0, size=20 | toDomain() | hasMore=true |
| 3 | should_computeNoMore_when_lastPage | totalCount=15, page=0, size=20 | toDomain() | hasMore=false |

---

## 9. JSON Payload Tests (6 tests)

### File: `data/api/JsonPayloadTest.kt`

Verify exact JSON parsing with hardcoded payloads:

| # | Test Name | JSON Payload | Expected |
|---|-----------|-------------|----------|
| 1 | should_parseCustomerList_when_validJson | `{"customers":[{"id":1,"name":"삼성전자","company":"삼성","industry":"Physical AI","cardCount":15,"lastInteraction":"2026-03-25T09:30:00Z"}],"totalCount":1}` | CustomerListResponse |
| 2 | should_parseCardDetail_when_validJson | (full card JSON from ambiguity-analysis.md) | ContextCardDto |
| 3 | should_parseKnowledge_when_validJson | (full knowledge JSON) | KnowledgeResponse |
| 4 | should_parseError_when_errorJson | `{"code":"NOT_FOUND","message":"Not found","details":null}` | ErrorResponse |
| 5 | should_parseUploadResponse_when_validJson | `{"conversationId":42,"cardsGenerated":3}` | UploadResponse |
| 6 | should_parseSearchResponse_when_validJson | (full search response JSON) | SearchResponse |

---

## Summary

| Component | Test Count |
|-----------|-----------|
| CustomerApiClient | 7 |
| CardApiClient | 8 |
| KnowledgeApiClient | 5 |
| UploadApiClient | 5 |
| CustomerDtoMapper | 6 |
| CardDtoMapper | 7 |
| KnowledgeDtoMapper | 4 |
| SearchResponseMapper | 3 |
| JSON Payload Tests | 6 |
| **Total** | **51** |

---

## 10. Room Database Tests (15 tests)

### File: `data/local/CustomerCacheDaoTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_insertCustomer_when_valid | CustomerEntity | insert() | exists in DB |
| 2 | should_getAllCustomers_when_dataExists | 3 entities | getAll() | returns 3 |
| 3 | should_returnEmpty_when_noData | empty DB | getAll() | emptyList |
| 4 | should_getById_when_customerExists | entity id=1 | getById(1) | returns entity |
| 5 | should_returnNull_when_customerNotExists | empty DB | getById(999) | null |
| 6 | should_updateCustomer_when_exists | entity id=1 | update(modified) | returns modified |
| 7 | should_deleteCustomer_when_exists | entity id=1 | delete(1) | getById(1) = null |
| 8 | should_replaceOnConflict_when_duplicateInsert | same id inserted twice | insert() ×2 | only 1 row, latest data |
| 9 | should_deleteAll_when_clearCalled | 3 entities | deleteAll() | getAll() = empty |
| 10 | should_returnCount_when_queried | 5 entities | count() | 5 |

### File: `data/local/CardCacheDaoTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 11 | should_insertCards_when_valid | 3 CardEntities | insertAll() | 3 in DB |
| 12 | should_getByCustomerId_when_exists | cards for customer 1 | getByCustomerId(1) | filtered list |
| 13 | should_getById_when_cardExists | entity id=5 | getById(5) | returns entity |
| 14 | should_searchByQuery_when_titleMatches | card title "온디바이스" | search("온디바이스") | returns matching |
| 15 | should_searchByQuery_when_summaryMatches | card summary contains "클라우드" | search("클라우드") | returns matching |

---

## 11. Retrofit Interceptor Tests (8 tests)

### File: `data/api/interceptor/InterceptorTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_addAuthHeader_when_tokenExists | token = "abc123" | request intercepted | Authorization: Bearer abc123 |
| 2 | should_notAddAuth_when_noToken | no token | request intercepted | no Authorization header |
| 3 | should_addContentType_when_jsonRequest | JSON body | request intercepted | Content-Type: application/json |
| 4 | should_logRequest_when_loggingEnabled | debug mode | request intercepted | URL logged |
| 5 | should_retryOnce_when_serverReturns503 | first: 503, second: 200 | request | retried, returns 200 |
| 6 | should_notRetry_when_clientError400 | 400 response | request | no retry, throws |
| 7 | should_timeout_when_exceeds30Seconds | server delays 31s | request | TimeoutException |
| 8 | should_addAcceptLanguage_when_requestMade | — | request intercepted | Accept-Language: ko-KR |

---

## 12. Repository Implementation Tests (10 tests)

### File: `data/repository/CustomerRepositoryImplTest.kt`

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | should_fetchFromApi_when_cacheEmpty | cache empty | getCustomers() | API called, cache updated |
| 2 | should_returnCache_when_cacheValid | cache has data, age < 5min | getCustomers() | cache returned, no API |
| 3 | should_refreshCache_when_cacheStale | cache age > 5min | getCustomers() | API called, cache updated |
| 4 | should_fallbackToCache_when_apiFails | cache has data, API throws | getCustomers() | cache returned |
| 5 | should_throwError_when_bothFail | cache empty, API throws | getCustomers() | IOException |
| 6 | should_mapDtoToDomain_when_apiReturns | API returns DTO | getCustomers() | domain objects |
| 7 | should_saveToCacheAfterFetch_when_apiSucceeds | API returns data | getCustomers() | cache.insertAll called |
| 8 | should_clearCache_when_forceRefresh | cache has data | refresh(force=true) | cache cleared, API called |
| 9 | should_passPageParams_when_getCards | page=2, size=20 | getCards(1, 2, 20) | API called with params |
| 10 | should_mapSearchResponse_when_searching | API returns search | searchCards("q") | domain SearchResult |

---

## Updated Summary

| Component | Test Count |
|-----------|-----------|
| CustomerApiClient | 7 |
| CardApiClient | 8 |
| KnowledgeApiClient | 5 |
| UploadApiClient | 5 |
| CustomerDtoMapper | 6 |
| CardDtoMapper | 7 |
| KnowledgeDtoMapper | 4 |
| SearchResponseMapper | 3 |
| JSON Payload Tests | 6 |
| Room Database | 15 |
| Retrofit Interceptors | 8 |
| Repository Implementation | 10 |
| **Total** | **84** |
