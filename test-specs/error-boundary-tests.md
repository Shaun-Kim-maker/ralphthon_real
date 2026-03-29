# Error Boundary & Failure Scenario Test Specification

**App**: Physical AI Sales Context App
**Framework**: JUnit 5 + MockK + MockWebServer
**Target**: 200 test cases
**File paths**:
- `app/src/test/java/com/ralphthon/app/data/api/CustomerApiErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/api/CardApiErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/api/KnowledgeApiErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/api/UploadApiErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/CustomerRepositoryErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/CardRepositoryErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/KnowledgeRepositoryErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/UploadRepositoryErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCustomersErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCardsByCustomerErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCardDetailErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/SearchCardsErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetKnowledgeErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/UploadConversationErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/CustomerListViewModelErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/CardNewsListViewModelErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/CardDetailViewModelErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/SearchViewModelErrorTest.kt`

---

## 1. HTTP Error Code Exhaustive Tests (40 tests)

**File paths**:
- `app/src/test/java/com/ralphthon/app/data/api/CustomerApiErrorTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/data/api/CardApiErrorTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/data/api/KnowledgeApiErrorTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/data/api/UploadApiErrorTest.kt` (10 tests)

Each file uses `MockWebServer` to enqueue HTTP responses and asserts the thrown domain exception or error message.

```kotlin
// Shared setup pattern for all HTTP error tests
@BeforeEach
fun setUp() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    val retrofit = Retrofit.Builder()
        .baseUrl(mockWebServer.url("/"))
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    apiService = retrofit.create(XxxApiService::class.java)
}
```

### 1.1 CustomerApiService HTTP Error Tests

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 1 | `should_throwValidationException_when_400BadRequest` | MockWebServer enqueues HTTP 400 with body `{"error":"bad request"}` | `getCustomers()` is called | Throws `ValidationException` or result contains message "잘못된 요청입니다" |
| 2 | `should_throwUnauthorizedException_when_401Unauthorized` | MockWebServer enqueues HTTP 401 | `getCustomers()` is called | Error message equals "인증이 필요합니다" |
| 3 | `should_throwForbiddenException_when_403Forbidden` | MockWebServer enqueues HTTP 403 | `getCustomers()` is called | Error message equals "접근 권한이 없습니다" |
| 4 | `should_throwNotFoundException_when_404NotFound` | MockWebServer enqueues HTTP 404 | `getCustomerById(id = 999L)` is called | Throws `NotFoundException` |
| 5 | `should_throwTimeoutException_when_408RequestTimeout` | MockWebServer enqueues HTTP 408 | `getCustomers()` is called | Throws `TimeoutException` |
| 6 | `should_throwRateLimitException_when_429TooManyRequests` | MockWebServer enqueues HTTP 429 | `getCustomers()` is called | Error message equals "잠시 후 다시 시도해주세요" |
| 7 | `should_throwServerException_when_500InternalServerError` | MockWebServer enqueues HTTP 500 | `getCustomers()` is called | Error message equals "서버 오류가 발생했습니다" |
| 8 | `should_throwNetworkException_when_502BadGateway` | MockWebServer enqueues HTTP 502 | `getCustomers()` is called | Throws `NetworkException` |
| 9 | `should_throwServiceUnavailableException_when_503ServiceUnavailable` | MockWebServer enqueues HTTP 503 | `getCustomers()` is called | Error message equals "서버 점검 중입니다" |
| 10 | `should_handleGracefully_when_emptyBodyWith200` | MockWebServer enqueues HTTP 200 with empty body `""` | `getCustomers()` is called | Returns empty list or default value without crashing |

### 1.2 CardApiService HTTP Error Tests

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 11 | `should_throwValidationException_when_400BadRequest` | MockWebServer enqueues HTTP 400 | `getCardsByCustomer(customerId = -1L)` is called | Throws `ValidationException` or message "잘못된 요청입니다" |
| 12 | `should_throwUnauthorizedException_when_401Unauthorized` | MockWebServer enqueues HTTP 401 | `getCardsByCustomer(customerId = 1L)` is called | Error message equals "인증이 필요합니다" |
| 13 | `should_throwForbiddenException_when_403Forbidden` | MockWebServer enqueues HTTP 403 | `getCardsByCustomer(customerId = 1L)` is called | Error message equals "접근 권한이 없습니다" |
| 14 | `should_throwNotFoundException_when_404NotFound` | MockWebServer enqueues HTTP 404 | `getCardById(cardId = 999L)` is called | Throws `NotFoundException` |
| 15 | `should_throwTimeoutException_when_408RequestTimeout` | MockWebServer enqueues HTTP 408 | `getCardsByCustomer(customerId = 1L)` is called | Throws `TimeoutException` |
| 16 | `should_throwRateLimitException_when_429TooManyRequests` | MockWebServer enqueues HTTP 429 | `searchCards(query = "test")` is called | Error message equals "잠시 후 다시 시도해주세요" |
| 17 | `should_throwServerException_when_500InternalServerError` | MockWebServer enqueues HTTP 500 | `getCardsByCustomer(customerId = 1L)` is called | Error message equals "서버 오류가 발생했습니다" |
| 18 | `should_throwNetworkException_when_502BadGateway` | MockWebServer enqueues HTTP 502 | `getCardsByCustomer(customerId = 1L)` is called | Throws `NetworkException` |
| 19 | `should_throwServiceUnavailableException_when_503ServiceUnavailable` | MockWebServer enqueues HTTP 503 | `getCardsByCustomer(customerId = 1L)` is called | Error message equals "서버 점검 중입니다" |
| 20 | `should_handleGracefully_when_emptyBodyWith200` | MockWebServer enqueues HTTP 200 with empty body `""` | `getCardsByCustomer(customerId = 1L)` is called | Returns `CardListResult` with empty list or handles null gracefully |

### 1.3 KnowledgeApiService HTTP Error Tests

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 21 | `should_throwValidationException_when_400BadRequest` | MockWebServer enqueues HTTP 400 | `getKnowledge(keywordId = -1L)` is called | Throws `ValidationException` or message "잘못된 요청입니다" |
| 22 | `should_throwUnauthorizedException_when_401Unauthorized` | MockWebServer enqueues HTTP 401 | `getKnowledge(keywordId = 1L)` is called | Error message equals "인증이 필요합니다" |
| 23 | `should_throwForbiddenException_when_403Forbidden` | MockWebServer enqueues HTTP 403 | `getKnowledge(keywordId = 1L)` is called | Error message equals "접근 권한이 없습니다" |
| 24 | `should_throwNotFoundException_when_404NotFound` | MockWebServer enqueues HTTP 404 | `getKnowledge(keywordId = 999L)` is called | Throws `NotFoundException` |
| 25 | `should_throwTimeoutException_when_408RequestTimeout` | MockWebServer enqueues HTTP 408 | `getKnowledge(keywordId = 1L)` is called | Throws `TimeoutException` |
| 26 | `should_throwRateLimitException_when_429TooManyRequests` | MockWebServer enqueues HTTP 429 | `getKnowledge(keywordId = 1L)` is called | Error message equals "잠시 후 다시 시도해주세요" |
| 27 | `should_throwServerException_when_500InternalServerError` | MockWebServer enqueues HTTP 500 | `getKnowledge(keywordId = 1L)` is called | Error message equals "서버 오류가 발생했습니다" |
| 28 | `should_throwNetworkException_when_502BadGateway` | MockWebServer enqueues HTTP 502 | `getKnowledge(keywordId = 1L)` is called | Throws `NetworkException` |
| 29 | `should_throwServiceUnavailableException_when_503ServiceUnavailable` | MockWebServer enqueues HTTP 503 | `getKnowledge(keywordId = 1L)` is called | Error message equals "서버 점검 중입니다" |
| 30 | `should_handleGracefully_when_emptyBodyWith200` | MockWebServer enqueues HTTP 200 with empty body `""` | `getKnowledge(keywordId = 1L)` is called | Returns `KnowledgeResult` with empty articles list or handles gracefully |

### 1.4 UploadApiService HTTP Error Tests

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 31 | `should_throwValidationException_when_400BadRequest` | MockWebServer enqueues HTTP 400 with `{"error":"missing required field"}` | `uploadConversation(...)` is called with missing fields | Throws `ValidationException` |
| 32 | `should_throwUnauthorizedException_when_401Unauthorized` | MockWebServer enqueues HTTP 401 | `uploadConversation(customerId = 1L, type = "audio")` is called | Error message equals "인증이 필요합니다" |
| 33 | `should_throwForbiddenException_when_403Forbidden` | MockWebServer enqueues HTTP 403 | `uploadConversation(customerId = 1L, type = "audio")` is called | Error message equals "접근 권한이 없습니다" |
| 34 | `should_throwNotFoundException_when_404NotFound` | MockWebServer enqueues HTTP 404 | `uploadConversation(customerId = 999L, type = "audio")` is called | Throws `NotFoundException` |
| 35 | `should_throwTimeoutException_when_408RequestTimeout` | MockWebServer enqueues HTTP 408 | `uploadConversation(customerId = 1L, type = "audio")` is called | Throws `TimeoutException` |
| 36 | `should_throwRateLimitException_when_429TooManyRequests` | MockWebServer enqueues HTTP 429 | `uploadConversation(customerId = 1L, type = "audio")` is called | Error message equals "잠시 후 다시 시도해주세요" |
| 37 | `should_throwServerException_when_500InternalServerError` | MockWebServer enqueues HTTP 500 | `uploadConversation(customerId = 1L, type = "audio")` is called | Error message equals "서버 오류가 발생했습니다" |
| 38 | `should_throwNetworkException_when_502BadGateway` | MockWebServer enqueues HTTP 502 | `uploadConversation(customerId = 1L, type = "audio")` is called | Throws `NetworkException` |
| 39 | `should_throwServiceUnavailableException_when_503ServiceUnavailable` | MockWebServer enqueues HTTP 503 | `uploadConversation(customerId = 1L, type = "audio")` is called | Error message equals "서버 점검 중입니다" |
| 40 | `should_handleGracefully_when_emptyBodyWith200` | MockWebServer enqueues HTTP 200 with empty body `""` | `uploadConversation(customerId = 1L, type = "notes", notes = "test")` is called | Returns default `UploadResult` or handles null body without crashing |

---

## 2. Network Failure Scenarios (30 tests)

**File paths**:
- `app/src/test/java/com/ralphthon/app/data/api/CustomerApiErrorTest.kt` (continued, or separate `CustomerApiNetworkTest.kt`)
- Pattern is the same for all 4 services; each service gets a dedicated block of 7–8 tests

Each test injects a custom `Interceptor` or `SocketFactory` into OkHttpClient that throws the target exception, or uses `MockWebServer.shutdown()` to simulate connection failure.

### 2.1 CustomerApiService Network Failures

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 41 | `should_throwTimeoutException_when_socketTimeoutException` | OkHttpClient interceptor throws `SocketTimeoutException` | `getCustomers()` is called | Throws domain `TimeoutException` |
| 42 | `should_throwNetworkException_when_unknownHostException` | OkHttpClient interceptor throws `UnknownHostException` | `getCustomers()` is called | Throws `NetworkException` with message "인터넷 연결을 확인해주세요" |
| 43 | `should_throwNetworkException_when_connectException` | OkHttpClient interceptor throws `ConnectException` | `getCustomers()` is called | Throws `NetworkException` |
| 44 | `should_throwNetworkException_when_sslHandshakeException` | OkHttpClient interceptor throws `SSLHandshakeException` | `getCustomers()` is called | Throws `NetworkException` with message "보안 연결에 실패했습니다" |
| 45 | `should_throwNetworkException_when_genericIOException` | OkHttpClient interceptor throws generic `IOException` | `getCustomers()` is called | Throws `NetworkException` |
| 46 | `should_propagateCancellationException_when_requestInterrupted` | Coroutine job is cancelled mid-flight | `getCustomers()` is called inside a cancellable scope that is then cancelled | `CancellationException` propagates (not caught by error handler) |
| 47 | `should_succeedOrTimeout_when_slowResponseNearDeadline` | MockWebServer delays response by 900ms; client timeout is 1000ms | `getCustomers()` is called | Either returns valid data (if under timeout) or throws `TimeoutException` (never hangs indefinitely) |

### 2.2 CardApiService Network Failures

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 48 | `should_throwTimeoutException_when_socketTimeoutException` | OkHttpClient interceptor throws `SocketTimeoutException` | `getCardsByCustomer(customerId = 1L)` is called | Throws domain `TimeoutException` |
| 49 | `should_throwNetworkException_when_unknownHostException` | OkHttpClient interceptor throws `UnknownHostException` | `getCardsByCustomer(customerId = 1L)` is called | Throws `NetworkException` with message "인터넷 연결을 확인해주세요" |
| 50 | `should_throwNetworkException_when_connectException` | OkHttpClient interceptor throws `ConnectException` | `getCardById(cardId = 1L)` is called | Throws `NetworkException` |
| 51 | `should_throwNetworkException_when_sslHandshakeException` | OkHttpClient interceptor throws `SSLHandshakeException` | `getCardsByCustomer(customerId = 1L)` is called | Throws `NetworkException` with message "보안 연결에 실패했습니다" |
| 52 | `should_throwNetworkException_when_genericIOException` | OkHttpClient interceptor throws generic `IOException` | `searchCards(query = "test")` is called | Throws `NetworkException` |
| 53 | `should_propagateCancellationException_when_requestInterrupted` | Coroutine job is cancelled mid-flight | `getCardsByCustomer(customerId = 1L)` is called inside a cancellable scope that is then cancelled | `CancellationException` propagates unchanged |
| 54 | `should_succeedOrTimeout_when_slowResponseNearDeadline` | MockWebServer delays response by 900ms; client timeout is 1000ms | `getCardsByCustomer(customerId = 1L)` is called | Either returns valid `CardListResult` or throws `TimeoutException` |

### 2.3 KnowledgeApiService Network Failures

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 55 | `should_throwTimeoutException_when_socketTimeoutException` | OkHttpClient interceptor throws `SocketTimeoutException` | `getKnowledge(keywordId = 1L)` is called | Throws domain `TimeoutException` |
| 56 | `should_throwNetworkException_when_unknownHostException` | OkHttpClient interceptor throws `UnknownHostException` | `getKnowledge(keywordId = 1L)` is called | Throws `NetworkException` with message "인터넷 연결을 확인해주세요" |
| 57 | `should_throwNetworkException_when_connectException` | OkHttpClient interceptor throws `ConnectException` | `getKnowledge(keywordId = 1L)` is called | Throws `NetworkException` |
| 58 | `should_throwNetworkException_when_sslHandshakeException` | OkHttpClient interceptor throws `SSLHandshakeException` | `getKnowledge(keywordId = 1L)` is called | Throws `NetworkException` with message "보안 연결에 실패했습니다" |
| 59 | `should_throwNetworkException_when_genericIOException` | OkHttpClient interceptor throws generic `IOException` | `getKnowledge(keywordId = 1L)` is called | Throws `NetworkException` |
| 60 | `should_propagateCancellationException_when_requestInterrupted` | Coroutine job is cancelled mid-flight | `getKnowledge(keywordId = 1L)` called inside a cancellable scope that is then cancelled | `CancellationException` propagates unchanged |
| 61 | `should_succeedOrTimeout_when_slowResponseNearDeadline` | MockWebServer delays response by 900ms; client timeout is 1000ms | `getKnowledge(keywordId = 1L)` is called | Either returns valid `KnowledgeResult` or throws `TimeoutException` |

### 2.4 UploadApiService Network Failures

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 62 | `should_throwTimeoutException_when_socketTimeoutException` | OkHttpClient interceptor throws `SocketTimeoutException` | `uploadConversation(customerId = 1L, type = "audio")` is called | Throws domain `TimeoutException` |
| 63 | `should_throwNetworkException_when_unknownHostException` | OkHttpClient interceptor throws `UnknownHostException` | `uploadConversation(customerId = 1L, type = "audio")` is called | Throws `NetworkException` with message "인터넷 연결을 확인해주세요" |
| 64 | `should_throwNetworkException_when_connectException` | OkHttpClient interceptor throws `ConnectException` | `uploadConversation(customerId = 1L, type = "audio")` is called | Throws `NetworkException` |
| 65 | `should_throwNetworkException_when_sslHandshakeException` | OkHttpClient interceptor throws `SSLHandshakeException` | `uploadConversation(customerId = 1L, type = "audio")` is called | Throws `NetworkException` with message "보안 연결에 실패했습니다" |
| 66 | `should_throwNetworkException_when_genericIOException` | OkHttpClient interceptor throws generic `IOException` | `uploadConversation(customerId = 1L, type = "audio")` is called | Throws `NetworkException` |
| 67 | `should_propagateCancellationException_when_requestInterrupted` | Coroutine job is cancelled mid-flight | `uploadConversation(...)` called inside a cancellable scope that is then cancelled | `CancellationException` propagates unchanged |
| 68 | `should_succeedOrTimeout_when_slowResponseNearDeadline` | MockWebServer delays response by 900ms; client timeout is 1000ms | `uploadConversation(customerId = 1L, type = "notes", notes = "text")` is called | Either returns valid `UploadResult` or throws `TimeoutException` |
| 69 | `should_throwNetworkException_when_largeFileUploadDropped` | OkHttpClient interceptor drops connection mid-upload for a 10MB audio file | `uploadConversation(customerId = 1L, type = "audio", audioFile = largeFile)` is called | Throws `NetworkException` (not a partial success) |
| 70 | `should_throwNetworkException_when_serverClosesConnectionMidUpload` | MockWebServer closes connection after reading only headers | `uploadConversation(customerId = 1L, type = "audio", audioFile = validFile)` is called | Throws `NetworkException` |

---

## 3. JSON Parsing Failure Tests (30 tests)

**File paths**:
- `app/src/test/java/com/ralphthon/app/data/api/CustomerApiErrorTest.kt` (continued)
- `app/src/test/java/com/ralphthon/app/data/api/CardApiErrorTest.kt` (continued)
- `app/src/test/java/com/ralphthon/app/data/api/KnowledgeApiErrorTest.kt` (continued)
- `app/src/test/java/com/ralphthon/app/data/api/UploadApiErrorTest.kt` (continued)

Each test enqueues a HTTP 200 response with a deliberately malformed or edge-case JSON body.

### 3.1 CustomerApiService JSON Parsing Failures

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 71 | `should_throwParsingException_when_malformedJson` | MockWebServer returns 200 with body `{invalid json` | `getCustomers()` is called | Throws a parsing exception (e.g., `JsonSyntaxException` wrapped in domain error); does not crash |
| 72 | `should_useDefaultValues_when_requiredFieldsMissing` | MockWebServer returns 200 with `[{"id":1}]` (name field missing) | `getCustomers()` is called | Returns list where customer name is `""` or `null`; no crash |
| 73 | `should_handleGracefully_when_wrongTypeForIdField` | MockWebServer returns 200 with `[{"id":"not-a-number","name":"A"}]` | `getCustomers()` is called | Throws parsing exception or maps gracefully; does not produce corrupted data |
| 74 | `should_ignoreExtraFields_when_unexpectedJsonFields` | MockWebServer returns 200 with `[{"id":1,"name":"A","unknownField":"x"}]` | `getCustomers()` is called | Returns customer list successfully; extra field silently ignored |
| 75 | `should_returnEmptyList_when_emptyJsonObject` | MockWebServer returns 200 with `{}` where a list is expected | `getCustomers()` is called | Returns empty list or throws a clear parsing exception; no crash |
| 76 | `should_throwParsingException_when_jsonArrayWhereObjectExpected` | MockWebServer returns 200 with `[[1,2,3]]` for a customer detail endpoint | `getCustomerById(id = 1L)` is called | Throws parsing exception; never returns corrupted `Customer` object |
| 77 | `should_parseCorrectly_when_unicodeInJsonValues` | MockWebServer returns 200 with `[{"id":1,"name":"홍길동 \u0041\u0042\u0043"}]` | `getCustomers()` is called | Returns customer with name `"홍길동 ABC"` parsed correctly |
| 78 | `should_handleWithoutCrash_when_veryLargeJsonPayload` | MockWebServer returns 200 with a JSON array of 10,000 customer objects (approx 2MB) | `getCustomers()` is called | Returns the full list without OOM or crash; completes in under 5 seconds |

### 3.2 CardApiService JSON Parsing Failures

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 79 | `should_throwParsingException_when_malformedJson` | MockWebServer returns 200 with body `{cards: [` (truncated) | `getCardsByCustomer(customerId = 1L)` is called | Throws parsing exception; does not crash |
| 80 | `should_useDefaultValues_when_requiredCardFieldsMissing` | MockWebServer returns 200 with `{"cards":[{"id":1}],"totalCount":1,"hasMore":false}` (title missing) | `getCardsByCustomer(customerId = 1L)` is called | Returns `CardListResult` where card title is `""` or `null`; no crash |
| 81 | `should_handleGracefully_when_wrongTypeForCardIdField` | MockWebServer returns 200 with `{"cards":[{"id":"abc","title":"T"}],"totalCount":1,"hasMore":false}` | `getCardsByCustomer(customerId = 1L)` is called | Throws parsing exception or maps gracefully; does not produce a card with an invalid id |
| 82 | `should_ignoreExtraFields_when_unexpectedCardJsonFields` | MockWebServer returns 200 with cards containing `"experimentalFeature": true` extra field | `getCardsByCustomer(customerId = 1L)` is called | Returns card list successfully; extra field silently ignored |
| 83 | `should_returnDefault_when_emptyJsonObject` | MockWebServer returns 200 with `{}` for card list | `getCardsByCustomer(customerId = 1L)` is called | Returns `CardListResult(cards=[], totalCount=0, hasMore=false)` or throws clear exception |
| 84 | `should_throwParsingException_when_jsonArrayWhereObjectExpected` | MockWebServer returns 200 with `[1,2,3]` for card detail endpoint | `getCardById(cardId = 1L)` is called | Throws parsing exception; never returns corrupted `ContextCard` |
| 85 | `should_parseCorrectly_when_unicodeInCardContent` | Card content contains Korean, Chinese, and emoji characters in JSON | `getCardsByCustomer(customerId = 1L)` is called | All unicode characters preserved exactly in returned `ContextCard.content` |
| 86 | `should_handleWithoutCrash_when_veryLargeCardPayload` | Single card with `content` field containing 500KB of text | `getCardById(cardId = 1L)` is called | Returns `ContextCard` without OOM; content field is complete |

### 3.3 KnowledgeApiService JSON Parsing Failures

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 87 | `should_throwParsingException_when_malformedJson` | MockWebServer returns 200 with `{"keywordId":1,"articles":[{` (truncated) | `getKnowledge(keywordId = 1L)` is called | Throws parsing exception; does not crash |
| 88 | `should_useDefaultValues_when_requiredArticleFieldsMissing` | MockWebServer returns 200 with `{"keywordId":1,"keywordTerm":"A","articles":[{"id":1}]}` (article title missing) | `getKnowledge(keywordId = 1L)` is called | Returns `KnowledgeResult` where article title is `""` or `null`; no crash |
| 89 | `should_handleGracefully_when_wrongTypeForKeywordIdField` | MockWebServer returns 200 with `{"keywordId":"not-a-number","keywordTerm":"A","articles":[]}` | `getKnowledge(keywordId = 1L)` is called | Throws parsing exception or maps gracefully; does not produce corrupted `KnowledgeResult` |
| 90 | `should_ignoreExtraFields_when_unexpectedKnowledgeJsonFields` | MockWebServer returns 200 with `KnowledgeResult` JSON plus extra `"deprecated":true` field | `getKnowledge(keywordId = 1L)` is called | Returns `KnowledgeResult` successfully; extra field silently ignored |
| 91 | `should_returnEmptyArticles_when_emptyJsonObject` | MockWebServer returns 200 with `{}` for knowledge endpoint | `getKnowledge(keywordId = 1L)` is called | Returns `KnowledgeResult` with empty articles or throws a clear parsing exception |
| 92 | `should_throwParsingException_when_jsonArrayWhereObjectExpected` | MockWebServer returns 200 with `[[1,2,3]]` for knowledge endpoint | `getKnowledge(keywordId = 1L)` is called | Throws parsing exception; never returns corrupted `KnowledgeResult` |
| 93 | `should_parseCorrectly_when_unicodeInArticleContent` | Article content contains mixed Korean, Japanese, and special symbols in JSON | `getKnowledge(keywordId = 1L)` is called | All unicode characters preserved in returned `KnowledgeArticle.content` |
| 94 | `should_handleWithoutCrash_when_veryLargeKnowledgePayload` | Knowledge result contains 1,000 articles each with 10KB content (approx 10MB) | `getKnowledge(keywordId = 1L)` is called | Returns all articles without OOM or crash |

### 3.4 UploadApiService JSON Parsing Failures (Upload Response)

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 95 | `should_throwParsingException_when_malformedUploadResponseJson` | MockWebServer returns 200 with body `{conversationId:` (truncated) | `uploadConversation(...)` is called | Throws parsing exception; does not crash |
| 96 | `should_useDefaultValues_when_requiredUploadResponseFieldsMissing` | MockWebServer returns 200 with `{"conversationId":42}` (cardsGenerated missing) | `uploadConversation(...)` is called | Returns `UploadResult(conversationId=42, cardsGenerated=0)` or handles null gracefully |
| 97 | `should_handleGracefully_when_wrongTypeForConversationIdField` | MockWebServer returns 200 with `{"conversationId":"abc","cardsGenerated":3}` | `uploadConversation(...)` is called | Throws parsing exception; does not produce corrupted `UploadResult` |
| 98 | `should_ignoreExtraFields_when_unexpectedUploadResponseFields` | MockWebServer returns 200 with `{"conversationId":1,"cardsGenerated":3,"processingTimeMs":42}` | `uploadConversation(...)` is called | Returns `UploadResult(conversationId=1, cardsGenerated=3)` successfully |
| 99 | `should_returnDefault_when_emptyJsonObjectInUploadResponse` | MockWebServer returns 200 with `{}` | `uploadConversation(...)` is called | Returns `UploadResult` with default values or throws a clear parsing exception |
| 100 | `should_throwParsingException_when_jsonArrayWhereUploadObjectExpected` | MockWebServer returns 200 with `[1,2,3]` for upload response | `uploadConversation(...)` is called | Throws parsing exception; never returns corrupted `UploadResult` |
| 101 | `should_parseCorrectly_when_unicodeInNotes` | Upload notes contain Korean text `"오늘 미팅에서 논의한 내용"` in the request; response is valid | `uploadConversation(customerId=1L, type="notes", notes="오늘 미팅에서 논의한 내용")` is called | Request sent without encoding errors; valid `UploadResult` returned |

---

## 4. Repository Error Propagation Tests (30 tests)

**File paths**:
- `app/src/test/java/com/ralphthon/app/data/repository/CustomerRepositoryErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/CardRepositoryErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/KnowledgeRepositoryErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/UploadRepositoryErrorTest.kt`

Each test uses MockK to mock the API service and mapper, then verifies the repository wraps or propagates errors correctly.

```kotlin
// Shared setup pattern for repository error tests
@BeforeEach
fun setUp() {
    mockApiService = mockk()
    mockMapper = mockk()
    repository = XxxRepositoryImpl(mockApiService, mockMapper)
}
```

### 4.1 CustomerRepository Error Propagation

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 102 | `should_wrapInNetworkException_when_apiThrowsIOException` | `mockApiService.getCustomers()` throws `IOException` | `repository.getCustomers()` is called | Throws `NetworkException` (not raw `IOException`) |
| 103 | `should_wrapInNotFoundException_when_apiThrows404` | `mockApiService.getCustomerById(999L)` throws `HttpException` with code 404 | `repository.getCustomerById(999L)` is called | Throws `NotFoundException` |
| 104 | `should_wrapInDomainException_when_mapperThrows` | API returns valid DTO but `mockMapper.map(dto)` throws `IllegalArgumentException` | `repository.getCustomers()` is called | Throws a domain exception (not raw `IllegalArgumentException`); error message is user-friendly |
| 105 | `should_handleIndependently_when_multipleSequentialErrorsOccur` | First call to `getCustomers()` throws `NetworkException`; second call returns valid data | `repository.getCustomers()` called twice sequentially | First call throws `NetworkException`; second call returns valid list — no state bleed |
| 106 | `should_preservePartialData_when_errorAfterPartialCustomerLoad` | `getCustomers()` succeeds for page 0 but throws on page 1 | Both pages requested sequentially | Page 0 data is available; page 1 error is thrown without corrupting page 0 data |
| 107 | `should_notCorruptState_when_concurrentErrorAndSuccess` | Two coroutines: one calling `getCustomers()` that errors, one that succeeds, launched concurrently | Both coroutines run via `async` in a test scope | Successful call returns correct data; error call throws; no cross-contamination of results |

### 4.2 CardRepository Error Propagation

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 108 | `should_wrapInNetworkException_when_apiThrowsIOException` | `mockApiService.getCardsByCustomer(1L, 0, 20)` throws `IOException` | `repository.getCardsByCustomer(1L)` is called | Throws `NetworkException` |
| 109 | `should_wrapInNotFoundException_when_apiThrows404ForCardById` | `mockApiService.getCardById(999L)` throws `HttpException` with code 404 | `repository.getCardById(999L)` is called | Throws `NotFoundException` |
| 110 | `should_wrapInDomainException_when_mapperThrowsDuringCardMapping` | API returns valid card DTO list but mapper throws on the second item | `repository.getCardsByCustomer(1L)` is called | Throws domain exception; first card is NOT returned as partial result (atomic operation) |
| 111 | `should_handleIndependently_when_multipleSequentialCardErrors` | First search throws `TimeoutException`; second search returns valid results | `repository.searchCards("q")` called twice | First call throws `TimeoutException`; second call returns `SearchResult`; no state bleed |
| 112 | `should_preservePageOneData_when_pageTwoFails` | Page 0 of `getCardsByCustomer` succeeds with 20 cards; page 1 throws `NetworkException` | Both pages requested sequentially | Page 0 `CardListResult` accessible; page 1 error thrown without corrupting page 0 result |
| 113 | `should_notCorruptState_when_concurrentCardErrorAndSuccess` | Two coroutines calling `getCardById` concurrently — one for a valid id (returns card), one for invalid id (throws) | Both launched via `async` in test scope | Valid card returned for valid id; `NotFoundException` thrown for invalid id; results independent |
| 114 | `should_wrapInTimeoutException_when_apiThrowsSocketTimeout` | `mockApiService.getCardsByCustomer(...)` throws `SocketTimeoutException` | `repository.getCardsByCustomer(1L)` is called | Throws domain `TimeoutException` (not raw `SocketTimeoutException`) |

### 4.3 KnowledgeRepository Error Propagation

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 115 | `should_wrapInNetworkException_when_apiThrowsIOException` | `mockApiService.getKnowledge(1L)` throws `IOException` | `repository.getKnowledge(1L)` is called | Throws `NetworkException` |
| 116 | `should_wrapInNotFoundException_when_apiThrows404` | `mockApiService.getKnowledge(999L)` throws `HttpException` with code 404 | `repository.getKnowledge(999L)` is called | Throws `NotFoundException` |
| 117 | `should_wrapInDomainException_when_mapperThrowsDuringArticleMapping` | API returns valid knowledge DTO but mapper throws on article mapping | `repository.getKnowledge(1L)` is called | Throws domain exception; does not return partial `KnowledgeResult` |
| 118 | `should_handleIndependently_when_multipleSequentialKnowledgeErrors` | First call throws `NetworkException`; second call with different keyword returns valid data | `repository.getKnowledge(1L)` then `repository.getKnowledge(2L)` | Each call independent; second call unaffected by first call's error |
| 119 | `should_notCorruptState_when_concurrentKnowledgeErrorAndSuccess` | Two coroutines: `getKnowledge(1L)` succeeds, `getKnowledge(999L)` throws `NotFoundException` | Both launched via `async` concurrently | Results independent; no state corruption |

### 4.4 UploadRepository Error Propagation

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 120 | `should_wrapInNetworkException_when_apiThrowsIOException` | `mockApiService.uploadConversation(...)` throws `IOException` | `repository.uploadConversation(1L, "audio", audioFile)` is called | Throws `NetworkException` |
| 121 | `should_wrapInValidationException_when_apiThrows400` | `mockApiService.uploadConversation(...)` throws `HttpException` with code 400 | `repository.uploadConversation(1L, "audio")` is called with invalid params | Throws `ValidationException` |
| 122 | `should_wrapInDomainException_when_mapperThrowsDuringResultMapping` | API returns valid upload response DTO but mapper throws | `repository.uploadConversation(1L, "audio", audioFile)` is called | Throws domain exception; does not return corrupted `UploadResult` |
| 123 | `should_handleIndependently_when_multipleSequentialUploadErrors` | First upload throws `TimeoutException`; second upload succeeds | Two sequential uploads | First throws `TimeoutException`; second returns `UploadResult`; no state bleed |
| 124 | `should_notCorruptState_when_concurrentUploadErrorAndSuccess` | Two concurrent uploads: one for valid customer (succeeds), one for invalid customer (throws) | Both launched via `async` | Results independent; successful upload result not contaminated by failed upload |
| 125 | `should_wrapInTimeoutException_when_apiThrowsSocketTimeoutDuringUpload` | `mockApiService.uploadConversation(...)` throws `SocketTimeoutException` (large file upload) | `repository.uploadConversation(1L, "audio", largeAudioFile)` is called | Throws domain `TimeoutException` |
| 126 | `should_wrapInNetworkException_when_apiThrowsSSLException` | `mockApiService.uploadConversation(...)` throws `SSLHandshakeException` | `repository.uploadConversation(1L, "audio", audioFile)` is called | Throws `NetworkException` with message "보안 연결에 실패했습니다" |
| 127 | `should_wrapInNotFoundException_when_customerNotFoundDuringUpload` | `mockApiService.uploadConversation(customerId = 999L, ...)` throws `HttpException` with code 404 | `repository.uploadConversation(999L, "notes", notes = "test")` is called | Throws `NotFoundException` |
| 128 | `should_handleGracefully_when_nullFilesProvided` | `audioFile = null` and `transcriptFile = null` both provided; API accepts notes-only upload | `repository.uploadConversation(1L, "notes", audioFile = null, transcriptFile = null, notes = "text")` | Returns `UploadResult` without crash; null files handled at API layer |
| 129 | `should_wrapInValidationException_when_allOptionalFieldsNull` | `audioFile = null`, `transcriptFile = null`, `notes = null` — no content provided | `repository.uploadConversation(1L, "audio", audioFile = null)` | Throws `ValidationException` (no upload content provided) |
| 130 | `should_handleGracefully_when_emptyNotesString` | `notes = ""` (empty string); API may return 400 | `repository.uploadConversation(1L, "notes", notes = "")` | Throws `ValidationException` or returns `UploadResult`; no crash |
| 131 | `should_wrapInNetworkException_when_serverClosesConnectionAfterPartialUpload` | Mock causes connection close after partial data received | `repository.uploadConversation(1L, "audio", audioFile = largeFile)` | Throws `NetworkException`; no partial `UploadResult` returned |

---

## 5. UseCase Error Handling Tests (30 tests)

**File paths**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCustomersErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCardsByCustomerErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetCardDetailErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/SearchCardsErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/GetKnowledgeErrorTest.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/UploadConversationErrorTest.kt`

Each test uses MockK to mock the repository, verifying the UseCase wraps, passes through, or handles errors according to its contract.

```kotlin
// Shared setup pattern
@BeforeEach
fun setUp() {
    mockRepository = mockk()
    useCase = XxxUseCase(mockRepository)
}
```

### 5.1 GetCustomers UseCase

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 132 | `should_returnFailure_when_repositoryReturnsFailure` | `mockRepository.getCustomers()` throws `NetworkException` | `useCase.invoke()` is called | Returns `Result.failure` containing `NetworkException` |
| 133 | `should_wrapUncheckedExceptions_when_repositoryThrowsRuntimeException` | `mockRepository.getCustomers()` throws `RuntimeException("unexpected")` | `useCase.invoke()` is called | Returns `Result.failure` with a domain exception; raw `RuntimeException` not exposed |
| 134 | `should_notCallRepository_when_validationFails` | UseCase validates that caller context is valid (e.g., user is authenticated) before calling repo | `useCase.invoke()` is called with invalid context | Returns `Result.failure(ValidationException(...))` without calling `mockRepository.getCustomers()` (verified with `verify { mockRepository wasNot called }`) |
| 135 | `should_retryOnceAndFail_when_timeoutOccurs` | `mockRepository.getCustomers()` throws `TimeoutException` on first call, then again on retry | `useCase.invoke()` is called with retry-enabled configuration | After 2 attempts, returns `Result.failure(TimeoutException)` |
| 136 | `should_propagateCancellation_when_coroutineIsCancelled` | Coroutine scope is cancelled while `mockRepository.getCustomers()` is suspended | `useCase.invoke()` is called inside a cancellable scope that is then cancelled | `CancellationException` propagates; not caught by `Result.failure` wrapping |

### 5.2 GetCardsByCustomer UseCase

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 137 | `should_returnFailure_when_repositoryReturnsFailure` | `mockRepository.getCardsByCustomer(1L)` throws `NetworkException` | `useCase.invoke(customerId = 1L)` is called | Returns `Result.failure(NetworkException)` |
| 138 | `should_wrapUncheckedExceptions_when_repositoryThrowsRuntimeException` | `mockRepository.getCardsByCustomer(1L)` throws `RuntimeException` | `useCase.invoke(customerId = 1L)` | Returns `Result.failure` with domain exception |
| 139 | `should_returnFailure_when_customerIdIsNegative` | `customerId = -1L` provided | `useCase.invoke(customerId = -1L)` is called | Returns `Result.failure(ValidationException)` without calling repository |
| 140 | `should_retryOnceAndFail_when_timeoutOccurs` | `mockRepository.getCardsByCustomer(1L)` throws `TimeoutException` on both attempts | `useCase.invoke(customerId = 1L)` | After 2 attempts, returns `Result.failure(TimeoutException)` |
| 141 | `should_propagateCancellation_when_coroutineIsCancelled` | Coroutine scope cancelled while repository is suspended | `useCase.invoke(customerId = 1L)` in cancellable scope | `CancellationException` propagates unmodified |

### 5.3 GetCardDetail UseCase

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 142 | `should_returnFailure_when_repositoryThrowsNotFoundException` | `mockRepository.getCardById(999L)` throws `NotFoundException` | `useCase.invoke(cardId = 999L)` is called | Returns `Result.failure(NotFoundException)` |
| 143 | `should_returnFailure_when_repositoryThrowsNetworkException` | `mockRepository.getCardById(1L)` throws `NetworkException` | `useCase.invoke(cardId = 1L)` is called | Returns `Result.failure(NetworkException)` |
| 144 | `should_returnFailure_when_cardIdIsZero` | `cardId = 0L` provided | `useCase.invoke(cardId = 0L)` is called | Returns `Result.failure(ValidationException)` without calling repository |
| 145 | `should_propagateCancellation_when_coroutineIsCancelled` | Coroutine scope cancelled while repository is suspended | `useCase.invoke(cardId = 1L)` in cancellable scope | `CancellationException` propagates unmodified |

### 5.4 SearchCards UseCase

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 146 | `should_returnFailure_when_repositoryThrowsNetworkException` | `mockRepository.searchCards("q")` throws `NetworkException` | `useCase.invoke(query = "q")` is called | Returns `Result.failure(NetworkException)` |
| 147 | `should_returnFailure_when_queryIsBlank` | `query = "   "` (whitespace only) | `useCase.invoke(query = "   ")` is called | Returns `Result.failure(ValidationException)` without calling repository |
| 148 | `should_returnFailure_when_queryIsEmpty` | `query = ""` | `useCase.invoke(query = "")` is called | Returns `Result.failure(ValidationException)` without calling repository |
| 149 | `should_retryOnceAndFail_when_timeoutOccurs` | `mockRepository.searchCards("q")` throws `TimeoutException` on both attempts | `useCase.invoke(query = "q")` | After 2 attempts, returns `Result.failure(TimeoutException)` |
| 150 | `should_propagateCancellation_when_coroutineIsCancelled` | Coroutine scope cancelled while repository is suspended | `useCase.invoke(query = "q")` in cancellable scope | `CancellationException` propagates unmodified |

### 5.5 GetKnowledge UseCase

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 151 | `should_returnFailure_when_repositoryThrowsNotFoundException` | `mockRepository.getKnowledge(999L)` throws `NotFoundException` | `useCase.invoke(keywordId = 999L)` is called | Returns `Result.failure(NotFoundException)` |
| 152 | `should_returnFailure_when_repositoryThrowsNetworkException` | `mockRepository.getKnowledge(1L)` throws `NetworkException` | `useCase.invoke(keywordId = 1L)` is called | Returns `Result.failure(NetworkException)` |
| 153 | `should_returnFailure_when_keywordIdIsNegative` | `keywordId = -1L` | `useCase.invoke(keywordId = -1L)` is called | Returns `Result.failure(ValidationException)` without calling repository |
| 154 | `should_propagateCancellation_when_coroutineIsCancelled` | Coroutine scope cancelled during repository suspension | `useCase.invoke(keywordId = 1L)` in cancellable scope | `CancellationException` propagates unmodified |

### 5.6 UploadConversation UseCase

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 155 | `should_returnFailure_when_repositoryThrowsNetworkException` | `mockRepository.uploadConversation(...)` throws `NetworkException` | `useCase.invoke(customerId = 1L, type = "audio", audioFile = validFile)` | Returns `Result.failure(NetworkException)` |
| 156 | `should_returnFailure_when_noContentProvided` | `audioFile = null`, `transcriptFile = null`, `notes = null` | `useCase.invoke(customerId = 1L, type = "audio")` | Returns `Result.failure(ValidationException("업로드할 내용이 없습니다"))` without calling repository |
| 157 | `should_returnFailure_when_customerIdIsInvalid` | `customerId = 0L` | `useCase.invoke(customerId = 0L, type = "audio", audioFile = validFile)` | Returns `Result.failure(ValidationException)` without calling repository |
| 158 | `should_retryOnceAndFail_when_timeoutOccurs` | `mockRepository.uploadConversation(...)` throws `TimeoutException` on both attempts | `useCase.invoke(...)` | After 2 attempts, returns `Result.failure(TimeoutException)` |
| 159 | `should_propagateCancellation_when_coroutineIsCancelled` | Coroutine scope cancelled during repository suspension | `useCase.invoke(...)` in cancellable scope | `CancellationException` propagates unmodified |
| 160 | `should_wrapUncheckedExceptions_when_repositoryThrowsRuntimeException` | `mockRepository.uploadConversation(...)` throws `RuntimeException("unexpected")` | `useCase.invoke(customerId = 1L, type = "audio", audioFile = validFile)` | Returns `Result.failure` with domain exception; raw `RuntimeException` not exposed |
| 161 | `should_returnFailure_when_typeIsBlank` | `type = ""` | `useCase.invoke(customerId = 1L, type = "", audioFile = validFile)` | Returns `Result.failure(ValidationException)` without calling repository |

---

## 6. ViewModel Error State Tests (40 tests)

**File paths**:
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/CustomerListViewModelErrorTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/CardNewsListViewModelErrorTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/CardDetailViewModelErrorTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/SearchViewModelErrorTest.kt` (10 tests)

Each test uses MockK for use cases, `TestCoroutineDispatcher` / `UnconfinedTestDispatcher`, and `Turbine` or manual state collection. ViewModels expose a `UiState` sealed class with `Loading`, `Success`, `Error` variants.

```kotlin
// Shared setup pattern for ViewModel error tests
@BeforeEach
fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    mockUseCase = mockk()
    viewModel = XxxViewModel(mockUseCase)
}

@AfterEach
fun tearDown() {
    Dispatchers.resetMain()
}
```

### 6.1 CustomerListViewModel Error States

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 162 | `should_showErrorMessage_when_getCustomersReturnsFailure` | `mockGetCustomers.invoke()` returns `Result.failure(NetworkException("인터넷 연결을 확인해주세요"))` | `viewModel.loadCustomers()` is called | `uiState` transitions to `Error` with `errorMessage = "인터넷 연결을 확인해주세요"` |
| 163 | `should_showRetryAction_when_errorOccurs` | `mockGetCustomers.invoke()` returns `Result.failure(NetworkException(...))` | `viewModel.loadCustomers()` is called | `uiState` is `Error` and `canRetry == true` |
| 164 | `should_preservePreviousData_when_refreshFails` | ViewModel already has 5 customers in `Success` state; refresh call returns `Result.failure(NetworkException)` | `viewModel.refresh()` is called | Previous 5 customers remain visible; `uiState` shows error overlay but `customers` list not cleared |
| 165 | `should_showTimeoutMessage_when_timeoutErrorOccurs` | `mockGetCustomers.invoke()` returns `Result.failure(TimeoutException())` | `viewModel.loadCustomers()` is called | `uiState.errorMessage` equals `TimeoutException().message` (e.g., "서버 응답 시간이 초과되었습니다") |
| 166 | `should_showNetworkMessage_when_networkErrorOccurs` | `mockGetCustomers.invoke()` returns `Result.failure(NetworkException())` | `viewModel.loadCustomers()` is called | `uiState.errorMessage` equals "인터넷 연결을 확인해주세요" |
| 167 | `should_showGenericMessage_when_unknownErrorOccurs` | `mockGetCustomers.invoke()` returns `Result.failure(RuntimeException("unexpected"))` | `viewModel.loadCustomers()` is called | `uiState.errorMessage` is non-empty generic message; raw exception message not exposed to UI |
| 168 | `should_clearErrorState_when_retrySucceeds` | First call returns `Result.failure(NetworkException)`; second call returns `Result.success(listOf(customer))` | `viewModel.loadCustomers()` called twice | After second call, `uiState` is `Success`; `errorMessage` is null or empty |
| 169 | `should_notCorruptState_when_multipleRapidErrorsOccur` | Three rapid calls to `viewModel.loadCustomers()` all return `Result.failure` with different exceptions | Three calls made in quick succession | Final `uiState` reflects the last error; no `NullPointerException` or state inconsistency |
| 170 | `should_showNotFoundMessage_when_404ErrorOccurs` | `mockGetCustomers.invoke()` returns `Result.failure(NotFoundException())` | `viewModel.loadCustomers()` is called | `uiState.errorMessage` equals `NotFoundException().message` ("리소스를 찾을 수 없습니다") |
| 171 | `should_showValidationMessage_when_validationErrorOccurs` | `mockGetCustomers.invoke()` returns `Result.failure(ValidationException("입력값이 올바르지 않습니다"))` | `viewModel.loadCustomers()` is called | `uiState.errorMessage` equals "입력값이 올바르지 않습니다" |

### 6.2 CardNewsListViewModel Error States

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 172 | `should_showErrorMessage_when_getCardsByCustomerReturnsFailure` | `mockGetCardsByCustomer.invoke(1L)` returns `Result.failure(NetworkException(...))` | `viewModel.loadCards(customerId = 1L)` is called | `uiState` transitions to `Error` with correct `errorMessage` |
| 173 | `should_showRetryAction_when_cardListErrorOccurs` | `mockGetCardsByCustomer.invoke(1L)` returns `Result.failure(NetworkException(...))` | `viewModel.loadCards(customerId = 1L)` is called | `uiState.canRetry == true` |
| 174 | `should_preserveExistingCards_when_refreshFails` | ViewModel has page 0 of 20 cards in `Success`; refresh returns `Result.failure(NetworkException)` | `viewModel.refresh()` is called | Existing 20 cards remain in `uiState.cards`; error message shown as overlay |
| 175 | `should_preserveExistingCardsOnPaginationError_when_page2Fails` | Page 0 loaded successfully with 20 cards; page 1 returns `Result.failure(NetworkException)` | `viewModel.loadNextPage()` is called | `uiState.cards` still contains the 20 cards from page 0; pagination error message shown |
| 176 | `should_showTimeoutMessage_when_timeoutErrorOccurs` | `mockGetCardsByCustomer.invoke(1L)` returns `Result.failure(TimeoutException())` | `viewModel.loadCards(customerId = 1L)` | `uiState.errorMessage` equals "서버 응답 시간이 초과되었습니다" |
| 177 | `should_showNetworkMessage_when_networkErrorOccurs` | `mockGetCardsByCustomer.invoke(1L)` returns `Result.failure(NetworkException())` | `viewModel.loadCards(customerId = 1L)` | `uiState.errorMessage` equals "인터넷 연결을 확인해주세요" |
| 178 | `should_clearErrorState_when_retrySucceeds` | First call fails with `NetworkException`; retry call returns valid `CardListResult` | `viewModel.loadCards(1L)` then `viewModel.retry()` | After retry, `uiState` is `Success`; error cleared |
| 179 | `should_notCorruptState_when_multipleRapidErrorsOccur` | Three rapid `loadCards(1L)` calls all fail with different errors | Three calls in quick succession | Final state reflects last error only; no race condition or NPE |
| 180 | `should_showNotFoundMessage_when_404ErrorOccurs` | `mockGetCardsByCustomer.invoke(1L)` returns `Result.failure(NotFoundException())` | `viewModel.loadCards(customerId = 1L)` | `uiState.errorMessage` equals "리소스를 찾을 수 없습니다" |
| 181 | `should_showGenericMessage_when_unknownErrorOccurs` | `mockGetCardsByCustomer.invoke(1L)` returns `Result.failure(RuntimeException("crash"))` | `viewModel.loadCards(customerId = 1L)` | `uiState.errorMessage` is non-empty; raw "crash" message not shown to user |

### 6.3 CardDetailViewModel Error States

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 182 | `should_showErrorMessage_when_getCardDetailReturnsFailure` | `mockGetCardDetail.invoke(1L)` returns `Result.failure(NetworkException(...))` | `viewModel.loadCardDetail(cardId = 1L)` is called | `uiState` is `Error` with correct `errorMessage` |
| 183 | `should_showRetryAction_when_detailErrorOccurs` | `mockGetCardDetail.invoke(1L)` returns `Result.failure(NetworkException(...))` | `viewModel.loadCardDetail(cardId = 1L)` is called | `uiState.canRetry == true` |
| 184 | `should_showNotFoundMessage_when_cardDoesNotExist` | `mockGetCardDetail.invoke(999L)` returns `Result.failure(NotFoundException())` | `viewModel.loadCardDetail(cardId = 999L)` | `uiState.errorMessage` equals "리소스를 찾을 수 없습니다" |
| 185 | `should_showTimeoutMessage_when_detailLoadTimesOut` | `mockGetCardDetail.invoke(1L)` returns `Result.failure(TimeoutException())` | `viewModel.loadCardDetail(cardId = 1L)` | `uiState.errorMessage` equals "서버 응답 시간이 초과되었습니다" |
| 186 | `should_showNetworkMessage_when_networkErrorOccurs` | `mockGetCardDetail.invoke(1L)` returns `Result.failure(NetworkException())` | `viewModel.loadCardDetail(cardId = 1L)` | `uiState.errorMessage` equals "인터넷 연결을 확인해주세요" |
| 187 | `should_clearErrorState_when_retrySucceeds` | First call fails with `NotFoundException`; retry returns valid `ContextCard` | `viewModel.loadCardDetail(1L)` then `viewModel.retry()` | After retry, `uiState` is `Success` with valid card; error cleared |
| 188 | `should_notCorruptState_when_multipleRapidErrorsOccur` | Three rapid `loadCardDetail` calls with different errors | Three calls in quick succession | Final `uiState` reflects last error; no state corruption |
| 189 | `should_showGenericMessage_when_unknownErrorOccurs` | `mockGetCardDetail.invoke(1L)` returns `Result.failure(RuntimeException("boom"))` | `viewModel.loadCardDetail(cardId = 1L)` | `uiState.errorMessage` is a user-friendly generic message; not raw "boom" |
| 190 | `should_showValidationMessage_when_validationErrorOccurs` | `mockGetCardDetail.invoke(1L)` returns `Result.failure(ValidationException("잘못된 카드 ID"))` | `viewModel.loadCardDetail(cardId = 1L)` | `uiState.errorMessage` equals "잘못된 카드 ID" |
| 191 | `should_notCallUseCase_when_sameCardIdAlreadyLoaded` | ViewModel already has `cardId = 1L` loaded in `Success` state | `viewModel.loadCardDetail(cardId = 1L)` called again | UseCase is NOT called again (`verify(exactly = 1) { mockGetCardDetail.invoke(1L) }`); cached data returned |

### 6.4 SearchViewModel Error States

| # | Test Name | Given | When | Then |
|---|-----------|-------|------|------|
| 192 | `should_showErrorMessage_when_searchReturnsFailure` | `mockSearchCards.invoke("keyword")` returns `Result.failure(NetworkException(...))` | `viewModel.search(query = "keyword")` is called | `uiState` is `Error` with correct `errorMessage` |
| 193 | `should_showRetryAction_when_searchErrorOccurs` | `mockSearchCards.invoke("keyword")` returns `Result.failure(NetworkException(...))` | `viewModel.search(query = "keyword")` is called | `uiState.canRetry == true` |
| 194 | `should_preservePreviousResults_when_subsequentSearchFails` | ViewModel has results from `"first query"` in `Success`; next search for `"second query"` fails | `viewModel.search("second query")` is called | Error for "second query" shown; previous results for "first query" NOT kept visible (search result replaced with error) |
| 195 | `should_showTimeoutMessage_when_searchTimesOut` | `mockSearchCards.invoke("q")` returns `Result.failure(TimeoutException())` | `viewModel.search(query = "q")` | `uiState.errorMessage` equals "서버 응답 시간이 초과되었습니다" |
| 196 | `should_showNetworkMessage_when_networkErrorOccurs` | `mockSearchCards.invoke("q")` returns `Result.failure(NetworkException())` | `viewModel.search(query = "q")` | `uiState.errorMessage` equals "인터넷 연결을 확인해주세요" |
| 197 | `should_clearErrorState_when_retrySucceeds` | Previous search failed with `NetworkException`; retry returns valid `SearchResult` | `viewModel.retry()` is called | `uiState` transitions to `Success`; error cleared |
| 198 | `should_notCorruptState_when_multipleRapidSearchErrorsOccur` | Five rapid `search()` calls all return different `Result.failure` types | Five calls in quick succession | Final `uiState` reflects only the last error; no race condition or NPE |
| 199 | `should_showNotFoundMessage_when_noResultsThrowsNotFoundException` | `mockSearchCards.invoke("obscure query")` returns `Result.failure(NotFoundException())` | `viewModel.search(query = "obscure query")` | `uiState.errorMessage` equals "리소스를 찾을 수 없습니다" |
| 200 | `should_showGenericMessage_when_unknownErrorOccurs` | `mockSearchCards.invoke("q")` returns `Result.failure(RuntimeException("unexpected"))` | `viewModel.search(query = "q")` | `uiState.errorMessage` is a non-empty user-friendly message; raw exception message not exposed |

---

## Summary

| Section | Tests | File Count |
|---------|-------|-----------|
| 1. HTTP Error Code Exhaustive Tests | 40 | 4 |
| 2. Network Failure Scenarios | 30 | 4 (extended) |
| 3. JSON Parsing Failure Tests | 30 | 4 (extended) |
| 4. Repository Error Propagation Tests | 30 | 4 |
| 5. UseCase Error Handling Tests | 30 | 6 |
| 6. ViewModel Error State Tests | 40 | 4 |
| **Total** | **200** | **26 files** |

## Domain Exception Reference

```kotlin
// app/src/main/java/com/ralphthon/app/domain/model/Exceptions.kt
class NotFoundException(message: String = "리소스를 찾을 수 없습니다") : Exception(message)
class NetworkException(message: String = "인터넷 연결을 확인해주세요") : Exception(message)
class TimeoutException(message: String = "서버 응답 시간이 초과되었습니다") : Exception(message)
class ValidationException(message: String) : Exception(message)
```

## HTTP Status → Exception Mapping

| HTTP Status | Domain Exception / Message |
|-------------|---------------------------|
| 400 | `ValidationException` / "잘못된 요청입니다" |
| 401 | Error message: "인증이 필요합니다" |
| 403 | Error message: "접근 권한이 없습니다" |
| 404 | `NotFoundException` |
| 408 | `TimeoutException` |
| 429 | Error message: "잠시 후 다시 시도해주세요" |
| 500 | Error message: "서버 오류가 발생했습니다" |
| 502 | `NetworkException` |
| 503 | Error message: "서버 점검 중입니다" |
| 200 (empty body) | Graceful default / empty collection |

## Network Exception → Domain Exception Mapping

| Java Exception | Domain Exception | Message Override |
|---------------|-----------------|-----------------|
| `SocketTimeoutException` | `TimeoutException` | default |
| `UnknownHostException` | `NetworkException` | "인터넷 연결을 확인해주세요" |
| `ConnectException` | `NetworkException` | default |
| `SSLHandshakeException` | `NetworkException` | "보안 연결에 실패했습니다" |
| `IOException` (generic) | `NetworkException` | default |
| `CancellationException` | (not caught — propagates) | — |
