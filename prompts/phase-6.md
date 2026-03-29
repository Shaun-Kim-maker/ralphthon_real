RALPH_BACKLOG.md를 읽고 첫 번째 미완료(- [ ]) 마일스톤을 찾아서 실행하라.
이미 완료된(- [x]) 마일스톤은 건너뛴다.
이 Phase의 마일스톤이 모두 완료되면 세션을 종료한다.

세션 시작 체크:
1. RALPH_BACKLOG.md 읽기
2. git log --oneline -3
3. ./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3

---

## Phase 6: Deep Testing
마일스톤 M-45 ~ M-52를 순서대로 실행한다.
각 마일스톤 완료 시 RALPH_BACKLOG.md의 해당 항목을 `- [x]`로 업데이트하라.
같은 에러 3회 반복 시 `- [SKIP]`으로 표시하고 다음 마일스톤으로 진행하라.

**이 Phase는 프로덕션 코드 변경 없이 테스트만 추가한다.**
Phase 1~5(M-01~M-44)가 완료된 상태. 기존 프로덕션 코드를 더 철저하게 검증한다.

**전제**: 기존 test 디렉토리에 다음 파일들이 이미 존재:
- `domain/model/ModelValidationTest`, `ModelEqualityTest`
- `data/api/CustomerApiClientTest`, `CardApiClientTest`, `KnowledgeApiClientTest`, `UploadApiClientTest`
- `data/mapper/*MapperTest`
- `domain/usecase/*UseCaseTest`
- `ui/customer/CustomerListViewModelTest`, `ui/card/CardNewsListViewModelTest` 등

---

### M-45: Parameterized Tests - Customer Input Validation 30 tests

**목표**: 30개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/parameterized-tests.md` 의 "§ 1 Customer Input Validation" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/domain/model/CustomerParameterizedTest.kt`

**@ParameterizedTest 패턴** (JUnit 5):
```kotlin
@ExtendWith(...)
class CustomerParameterizedTest {

    @ParameterizedTest(name = "id={0}, expectValid={1}")
    @CsvSource(
        "0, true",
        "1, true",
        "999999, true",
        "-1, false",
        "-999, false"
    )
    fun should_validateCustomerId_when_variousInputs(id: Long, expectValid: Boolean) {
        val customer = Customer.withDefaults(id = id)
        assertEquals(expectValid, customer.id >= 0)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "  ", "\t", "\n"])
    fun should_identifyBlankName_when_whitespaceInput(name: String) {
        assertTrue(name.isBlank())
    }

    @ParameterizedTest
    @MethodSource("validNameProvider")
    fun should_acceptValidName_when_normalString(name: String) {
        val customer = Customer.withDefaults(name = name)
        assertFalse(customer.name.isEmpty())
    }

    companion object {
        @JvmStatic
        fun validNameProvider() = listOf("홍길동", "John Doe", "山田太郎", "O'Brien")
    }
}
```

**30개 테스트 분배**:
- Customer.id 경계값: 8개 (음수, 0, 양수, Long.MAX_VALUE 등)
- Customer.name 유효성: 6개 (빈 문자열, 공백, 특수문자, 한국어, 영어, 긴 문자열)
- Customer.company 유효성: 6개
- Customer.cardCount 범위: 5개 (0, 음수, 큰 값)
- Customer.contacts 리스트: 5개 (null 대신 emptyList, 단일 항목, 여러 항목)

**검증**: `./gradlew.bat test --tests "*.CustomerParameterizedTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-45 체크.
커밋: `git commit -m "test(PARAM-001): Customer input validation parameterized 30"`

---

### M-46: Parameterized Tests - ContextCard Field Validation 30 tests

**목표**: 30개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/parameterized-tests.md` 의 "§ 2 ContextCard Validation" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/domain/model/CardParameterizedTest.kt`

**30개 테스트 분배**:
- ContextCard.id 경계값: 6개
- ContextCard.sentiment 전체 enum값 순회: 6개 (Sentiment.values() 각각)
- ContextCard.conversationType 전체 enum값: 5개
- KeyStatement.timestampInSeconds 범위: 5개 (0, 음수, 매우 큰 값)
- Keyword.frequency 범위: 4개
- Sentiment.fromString() 입력 변환: 4개 (대소문자, 알 수 없는 값 → NEUTRAL)

**@EnumSource 활용**:
```kotlin
@ParameterizedTest
@EnumSource(Sentiment::class)
fun should_createCardWithAnySentiment_when_sentimentProvided(sentiment: Sentiment) {
    val card = ContextCard.withDefaults(sentiment = sentiment)
    assertEquals(sentiment, card.sentiment)
}
```

**검증**: `./gradlew.bat test --tests "*.CardParameterizedTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-46 체크.
커밋: `git commit -m "test(PARAM-002): ContextCard field validation parameterized 30"`

---

### M-47: Parameterized Tests - API Response + Mapper Edge Cases 70 tests

**목표**: 70개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/parameterized-tests.md` 의 "§ 4-5 API Response + Mapper" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/ParameterizedApiMapperTest.kt`

**70개 테스트 분배**:

*API Response 경계값 (30개)*:
```kotlin
@ParameterizedTest
@CsvSource(
    "200, true",
    "201, true",
    "400, false",
    "401, false",
    "403, false",
    "404, false",
    "429, false",
    "500, false",
    "503, false"
)
fun should_handleHttpStatusCodes_when_variousCodesReturned(
    statusCode: Int,
    expectSuccess: Boolean
) = runTest {
    mockWebServer.enqueue(MockResponse().setBody(validJson).setResponseCode(statusCode))
    val result = client.getCustomers()
    assertEquals(expectSuccess, result.isSuccess)
}
```
- HTTP 상태 코드별 처리: 9개
- 응답 바디 크기 변형: 5개 (빈 배열, 단일 항목, 100개 항목)
- 페이지네이션 파라미터 조합: 8개
- 타임아웃 시나리오: 4개
- 빈 응답 바디: 4개

*Mapper 엣지 케이스 (40개)*:
- null 필드 처리 (optional fields): 10개
- 특수문자 포함 문자열 매핑: 5개
- Enum 문자열 변환 (대소문자, 알 수 없는 값): 8개
- 중첩 객체 (contacts, statements, keywords) 매핑: 8개
- 빈 리스트 매핑: 5개
- 숫자 경계값 매핑 (Long.MAX_VALUE, 0, negative): 4개

**검증**: `./gradlew.bat test --tests "*.ParameterizedApiMapperTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-47 체크.
커밋: `git commit -m "test(PARAM-003): API response + mapper edge cases 70"`

---

### M-48: Parameterized Tests - ViewModel State Transition 30 tests

**목표**: 30개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/parameterized-tests.md` 의 "§ 6 ViewModel State Transition" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/ui/viewmodel/ParameterizedStateTest.kt`

**30개 테스트 분배**:

*각 ViewModel에 대해 공통 케이스 (각 5개 × 4 ViewModel + 10개 추가)*:
```kotlin
@ParameterizedTest
@MethodSource("customerCountProvider")
fun should_emitDataWithCorrectCount_when_customersLoaded(
    customerCount: Int
) = runTest(testDispatcher) {
    val customers = List(customerCount) { Customer.withDefaults(id = it.toLong()) }
    coEvery { useCase() } returns Result.success(customers)
    viewModel.uiState.test {
        skipItems(1) // Loading
        val dataState = awaitItem() as CustomerListUiState.Data
        assertEquals(customerCount, dataState.customers.size)
        cancelAndIgnoreRemainingEvents()
    }
}

companion object {
    @JvmStatic
    fun customerCountProvider() = listOf(0, 1, 5, 20, 100)
}
```

- CustomerListViewModel: 5개 (0, 1, 5, 20, 100 고객)
- CardNewsListViewModel: 5개 (다양한 카드 수)
- SearchViewModel: 8개 (다양한 쿼리 길이, 결과 수 조합)
- 에러 유형별 메시지 검증: 7개 (각 Exception 타입)
- 페이지네이션 상태 전이: 5개 (page 0, 1, 2, last page, overflow)

**검증**: `./gradlew.bat test --tests "*.ParameterizedStateTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-48 체크.
커밋: `git commit -m "test(PARAM-004): ViewModel state transition parameterized 30"`

---

### M-49: Error Boundary - HTTP Error Exhaustive 40 tests

**목표**: 40개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/error-boundary-tests.md` 의 "§ 1 HTTP Error" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/api/HttpErrorExhaustiveTest.kt`

**MockWebServer 기반 HTTP 에러 시나리오**:
```kotlin
class HttpErrorExhaustiveTest {
    private lateinit var mockWebServer: MockWebServer
    // ... 각 ApiClient 초기화

    @ParameterizedTest
    @CsvSource("400, ValidationException", "401, NetworkException", "403, NetworkException",
               "404, NotFoundException", "408, TimeoutException", "429, NetworkException",
               "500, NetworkException", "502, NetworkException", "503, NetworkException", "504, TimeoutException")
    fun should_throwCorrectException_when_httpErrorReceived(
        statusCode: Int,
        expectedExceptionType: String
    ) = runTest {
        mockWebServer.enqueue(MockResponse()
            .setBody("""{"code":"ERR","message":"error"}""")
            .setResponseCode(statusCode))
        val result = customerApiClient.getCustomers()
        assertTrue(result.isFailure)
        // 예외 타입 검증
    }
}
```

**40개 테스트 분배**:
- CustomerApiClient: 10개 HTTP 오류 케이스
- CardApiClient: 10개 HTTP 오류 케이스
- KnowledgeApiClient: 10개 HTTP 오류 케이스
- UploadApiClient: 10개 HTTP 오류 케이스

**주의**: `4xx` → 적절한 domain exception으로 매핑되어야 함. ApiClient 구현에서 이미 처리되어 있어야 한다.
만약 예외 매핑이 구현되지 않았다면 ApiClient에 추가:
```kotlin
when (response.code()) {
    404 -> throw NotFoundException("Not found")
    408, 504 -> throw TimeoutException("Request timed out")
    else -> throw NetworkException("HTTP ${response.code()}")
}
```

**검증**: `./gradlew.bat test --tests "*.HttpErrorExhaustiveTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-49 체크.
커밋: `git commit -m "test(ERR-001): HTTP error exhaustive 40 tests"`

---

### M-50: Error Boundary - Network + JSON Failures 60 tests

**목표**: 60개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/error-boundary-tests.md` 의 "§ 2-3 Network + JSON Failures" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/api/NetworkJsonFailureTest.kt`

**네트워크 실패 시나리오 (30개)**:
```kotlin
@Test
fun should_returnNetworkException_when_serverShutdownMidResponse() = runTest {
    mockWebServer.enqueue(MockResponse()
        .setBody("partial_json")
        .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY))
    val result = client.getCustomers()
    assertTrue(result.isFailure)
}

@Test
fun should_returnNetworkException_when_connectionRefused() = runTest {
    mockWebServer.shutdown()
    val result = client.getCustomers() // closed server
    assertTrue(result.isFailure)
}
```
- 서버 연결 거부 (MockWebServer shutdown)
- 응답 중 연결 끊김
- 소켓 타임아웃 (DelayedResponse)
- 빈 응답 바디
- 응답 지연 (slow body)

**JSON 파싱 실패 시나리오 (30개)**:
```kotlin
@Test
fun should_returnFailure_when_malformedJsonReceived() = runTest {
    mockWebServer.enqueue(MockResponse().setBody("{invalid json}").setResponseCode(200))
    val result = client.getCustomers()
    assertTrue(result.isFailure)
}
```
- 잘못된 JSON 구문
- 타입 불일치 (숫자 필드에 문자열)
- 필수 필드 누락
- 예상과 다른 JSON 구조
- 빈 JSON `{}`

**검증**: `./gradlew.bat test --tests "*.NetworkJsonFailureTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-50 체크.
커밋: `git commit -m "test(ERR-002): Network + JSON failure boundary 60 tests"`

---

### M-51: Error Boundary - Repository Error Propagation 30 tests

**목표**: 30개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/error-boundary-tests.md` 의 "§ 4 Repository Error Propagation" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/repository/ErrorPropagationTest.kt`

**에러 전파 체인 검증**: `API → Repository → UseCase → ViewModel`

```kotlin
@Test
fun should_propagateNetworkExceptionChain_when_apiCallFails() = runTest {
    // API level: MockWebServer returns 500
    mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("{}"))

    // Repository (using real ApiClient + MockWebServer)
    val repoResult = repository.getCustomers()
    assertTrue(repoResult.isFailure)
    assertIs<NetworkException>(repoResult.exceptionOrNull())

    // UseCase (using mock repository that returns the same failure)
    coEvery { mockRepo.getCustomers() } returns repoResult
    val useCaseResult = useCase()
    assertTrue(useCaseResult.isFailure)
    assertIs<NetworkException>(useCaseResult.exceptionOrNull())
}
```

**30개 테스트 분배**:
- NetworkException 전파: API → Repo 10개
- NotFoundException 전파: API → Repo → UseCase 8개
- TimeoutException 전파: 6개
- ValidationException 발생 지점 및 전파: 6개

**검증**: `./gradlew.bat test --tests "*.ErrorPropagationTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-51 체크.
커밋: `git commit -m "test(ERR-003): Repository error propagation 30 tests"`

---

### M-52: Contract Tests - Response Schema + Null Safety + Backward Compat 75 tests

**목표**: 75개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/contract-tests.md` 의 "§ 2-4" 섹션

**테스트 파일들**:
- `app/src/test/java/com/ralphthon/app/data/api/contract/SchemaValidationTest.kt` (25 tests)
- `app/src/test/java/com/ralphthon/app/data/api/contract/NullSafetyContractTest.kt` (25 tests)
- `app/src/test/java/com/ralphthon/app/data/api/contract/BackwardCompatibilityTest.kt` (25 tests)

**SchemaValidationTest (25개)** — 응답 스키마 구조 검증:
```kotlin
@Test
fun should_haveRequiredFields_when_customerResponseParsed() {
    val json = """{"id":1,"name":"홍길동","company":"삼성","card_count":5,"total_conversations":10,"last_interaction_at":"2024-01-01"}"""
    val dto = gson.fromJson(json, CustomerSummaryDto::class.java)
    assertNotNull(dto.id)
    assertNotNull(dto.name)
    assertNotNull(dto.company)
    assertNotNull(dto.card_count)
    assertNotNull(dto.total_conversations)
    assertNotNull(dto.last_interaction_at)
}
```
- 각 DTO 필수 필드 존재 검증 (5 DTO × 5 필드 = 25)

**NullSafetyContractTest (25개)** — null 필드 처리:
```kotlin
@Test
fun should_handleNullContacts_when_customerHasNoContacts() {
    val json = """{"id":1,"name":"홍길동","company":"삼성","card_count":0,"total_conversations":0,"last_interaction_at":""}"""
    val dto = gson.fromJson(json, CustomerDetailDto::class.java)
    assertNotNull(dto.contacts) // null이 아닌 emptyList
}
```
- Optional 필드 null 처리: 25개

**BackwardCompatibilityTest (25개)** — 하위 호환성:
```kotlin
@Test
fun should_notFailParsing_when_unknownFieldsInResponse() {
    val jsonWithExtra = """{"id":1,"name":"홍길동","company":"삼성","card_count":0,
        "total_conversations":0,"last_interaction_at":"","future_field":"some_value"}"""
    val dto = gson.fromJson(jsonWithExtra, CustomerSummaryDto::class.java)
    assertNotNull(dto) // 알 수 없는 필드가 있어도 파싱 성공
}
```
- 미래 필드 추가 시 파싱 성공 검증
- 필드 순서 변경 시 파싱 성공 검증
- 대소문자 변형 처리

**검증**: `./gradlew.bat test --tests "*.SchemaValidationTest" --tests "*.NullSafetyContractTest" --tests "*.BackwardCompatibilityTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-52 체크.
커밋: `git commit -m "test(CONTRACT-001): Schema + null safety + backward compat 75"`

---

## Phase 6 완료 처리

모든 M-45 ~ M-52가 완료되면:
1. `./gradlew.bat test --no-daemon 2>&1 | tail -5` → 누적 테스트 수 확인 (목표: 981+)
2. `git add app/src/`
3. `git commit -m "feat: Phase 6 deep testing complete (365 tests)"`
4. `git push`
5. 세션 종료
