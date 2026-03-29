RALPH_BACKLOG.md를 읽고 첫 번째 미완료(- [ ]) 마일스톤을 찾아서 실행하라.
이미 완료된(- [x]) 마일스톤은 건너뛴다.
이 Phase의 마일스톤이 모두 완료되면 세션을 종료한다.

세션 시작 체크:
1. RALPH_BACKLOG.md 읽기
2. git log --oneline -3
3. ./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3

---

## Phase 2: Data Layer
마일스톤 M-09 ~ M-16을 순서대로 실행한다.
각 마일스톤 완료 시 RALPH_BACKLOG.md의 해당 항목을 `- [x]`로 업데이트하라.
같은 에러 3회 반복 시 `- [SKIP]`으로 표시하고 다음 마일스톤으로 진행하라.

**전제**: Phase 1(M-01~M-08)이 완료된 상태. 아래 클래스들이 이미 존재한다:
- `domain/model/`: Customer, Contact, Conversation, ContextCard, KeyStatement, Keyword, KnowledgeArticle, ConversationType, Sentiment, KeywordCategory, SearchResult
- `data/dto/`: CustomerDtos, CardDtos, KnowledgeDtos, SearchDtos, UploadDtos, ErrorResponse
- `di/AppModule.kt`

---

### M-09: Repository 인터페이스 4개 + Domain Exceptions

**목표**: 인터페이스 4개 + 예외 클래스 생성, 컴파일 성공

**파일 위치**: `app/src/main/java/com/ralphthon/app/domain/`

`domain/repository/CustomerRepository.kt`:
```kotlin
package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.Customer

interface CustomerRepository {
    suspend fun getCustomers(): Result<List<Customer>>
    suspend fun getCustomerById(id: Long): Result<Customer>
}
```

`domain/repository/CardRepository.kt`:
```kotlin
package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.ContextCard

data class CardListResult(
    val cards: List<ContextCard>,
    val totalCount: Int,
    val hasMore: Boolean
)

interface CardRepository {
    suspend fun getCardsByCustomer(
        customerId: Long,
        page: Int = 0,
        size: Int = 20
    ): Result<CardListResult>

    suspend fun getCardById(cardId: Long): Result<ContextCard>

    suspend fun searchCards(
        query: String,
        customerId: Long? = null,
        dateFrom: String? = null,
        dateTo: String? = null,
        page: Int = 0,
        size: Int = 20
    ): Result<CardListResult>
}
```

`domain/repository/KnowledgeRepository.kt`:
```kotlin
package com.ralphthon.app.domain.repository

import com.ralphthon.app.domain.model.KnowledgeArticle

data class KnowledgeResult(
    val keywordId: Long,
    val keywordTerm: String,
    val articles: List<KnowledgeArticle>
)

interface KnowledgeRepository {
    suspend fun getKnowledge(keywordId: Long): Result<KnowledgeResult>
}
```

`domain/repository/UploadRepository.kt`:
```kotlin
package com.ralphthon.app.domain.repository

import java.io.File

data class UploadResult(
    val conversationId: Long,
    val cardsGenerated: Int
)

interface UploadRepository {
    suspend fun uploadConversation(
        customerId: Long,
        type: String,
        audioFile: File? = null,
        transcriptFile: File? = null,
        notes: String? = null
    ): Result<UploadResult>
}
```

`domain/model/Exceptions.kt`:
```kotlin
package com.ralphthon.app.domain.model

class NotFoundException(message: String) : Exception(message)
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
class TimeoutException(message: String, cause: Throwable? = null) : Exception(message, cause)
class ValidationException(message: String) : Exception(message)
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-09 체크.

---

### M-10: API Service 인터페이스 4개

**목표**: Retrofit @GET/@POST 인터페이스 4개 생성, 컴파일 성공

**파일 위치**: `app/src/main/java/com/ralphthon/app/data/api/`

`CustomerApiService.kt`:
```kotlin
package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.CustomerDetailDto
import com.ralphthon.app.data.dto.CustomerListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CustomerApiService {
    @GET("api/customers")
    suspend fun getCustomers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<CustomerListResponse>

    @GET("api/customers/{id}")
    suspend fun getCustomerById(@Path("id") id: Long): Response<CustomerDetailDto>
}
```

`CardApiService.kt`:
```kotlin
package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.CardDetailDto
import com.ralphthon.app.data.dto.CardListResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CardApiService {
    @GET("api/customers/{customerId}/cards")
    suspend fun getCardsByCustomer(
        @Path("customerId") customerId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<CardListResponseDto>

    @GET("api/cards/{cardId}")
    suspend fun getCardById(@Path("cardId") cardId: Long): Response<CardDetailDto>

    @GET("api/cards/search")
    suspend fun searchCards(
        @Query("q") query: String,
        @Query("customer_id") customerId: Long? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<CardListResponseDto>
}
```

`KnowledgeApiService.kt`:
```kotlin
package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.KnowledgeResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface KnowledgeApiService {
    @GET("api/knowledge/{keywordId}")
    suspend fun getKnowledge(@Path("keywordId") keywordId: Long): Response<KnowledgeResponseDto>
}
```

`UploadApiService.kt`:
```kotlin
package com.ralphthon.app.data.api

import com.ralphthon.app.data.dto.UploadResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApiService {
    @Multipart
    @POST("api/conversations")
    suspend fun uploadConversation(
        @Part("customer_id") customerId: RequestBody,
        @Part("type") type: RequestBody,
        @Part audio: MultipartBody.Part? = null,
        @Part transcript: MultipartBody.Part? = null,
        @Part("notes") notes: RequestBody? = null
    ): Response<UploadResponseDto>
}
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-10 체크.

---

### M-11: TDD CustomerApiClient 7 tests

**목표**: 7개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/data-tests.md` 의 "§ 1 CustomerApiClient" 섹션 (offset 사용)

**Tier 1 TDD 순서**: 테스트 파일 먼저 → 프로덕션 코드

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/api/CustomerApiClientTest.kt`

**테스트 패턴** (MockWebServer 사용):
```kotlin
@ExtendWith(...)
class CustomerApiClientTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var customerApiService: CustomerApiService
    private lateinit var client: CustomerApiClient

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        customerApiService = retrofit.create(CustomerApiService::class.java)
        client = CustomerApiClient(customerApiService)
    }

    @AfterEach
    fun tearDown() { mockWebServer.shutdown() }

    @Test
    fun should_returnCustomerList_when_apiReturnsSuccess() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setBody("""{"customers":[{"id":1,"name":"홍길동","company":"삼성","card_count":5,"total_conversations":10,"last_interaction_at":"2024-01-01"}],"total_count":1,"has_more":false}""")
            .setResponseCode(200))
        val result = client.getCustomers()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
    }
    // ... 추가 6개 테스트
}
```

**프로덕션 파일**: `app/src/main/java/com/ralphthon/app/data/api/CustomerApiClient.kt`

**CustomerApiClient 핵심 패턴**:
```kotlin
class CustomerApiClient @Inject constructor(
    private val service: CustomerApiService
) {
    suspend fun getCustomers(): Result<List<Customer>> = runCatching {
        val response = service.getCustomers()
        if (response.isSuccessful) {
            response.body()?.customers?.map { it.toDomain() } ?: emptyList()
        } else {
            throw NetworkException("HTTP ${response.code()}")
        }
    }
}
```

**검증**: `./gradlew.bat test --tests "*.CustomerApiClientTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-11 체크.
커밋: `git commit -m "test(DATA-001): CustomerApiClient 7 tests"`

---

### M-12: TDD CardApiClient 8 tests

**목표**: 8개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/data-tests.md` 의 "§ 2 CardApiClient" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/api/CardApiClientTest.kt`
**프로덕션 파일**: `app/src/main/java/com/ralphthon/app/data/api/CardApiClient.kt`

**테스트 커버리지**:
- `should_returnCardList_when_getCardsByCustomerSucceeds`
- `should_returnCardDetail_when_getCardByIdSucceeds`
- `should_returnSearchResults_when_searchCardsSucceeds`
- `should_includePageParams_when_paginationRequested`
- `should_returnFailure_when_404Response`
- `should_returnFailure_when_500Response`
- `should_filterByCustomerId_when_customerIdProvided`
- `should_filterByDateRange_when_datesProvided`

**CardApiClient 메서드**: `getCardsByCustomer()`, `getCardById()`, `searchCards()`

**검증**: `./gradlew.bat test --tests "*.CardApiClientTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-12 체크.
커밋: `git commit -m "test(DATA-002): CardApiClient 8 tests"`

---

### M-13: TDD KnowledgeApiClient 5 + UploadApiClient 5 tests

**목표**: 10개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/data-tests.md` 의 "§ 3 KnowledgeApiClient" + "§ 4 UploadApiClient" 섹션

**테스트 파일들**:
- `app/src/test/java/com/ralphthon/app/data/api/KnowledgeApiClientTest.kt`
- `app/src/test/java/com/ralphthon/app/data/api/UploadApiClientTest.kt`

**프로덕션 파일들**:
- `app/src/main/java/com/ralphthon/app/data/api/KnowledgeApiClient.kt`
- `app/src/main/java/com/ralphthon/app/data/api/UploadApiClient.kt`

**KnowledgeApiClient 테스트 커버리지** (5개):
- `should_returnKnowledge_when_keywordIdValid`
- `should_returnArticleList_when_multipleArticlesExist`
- `should_returnFailure_when_keywordNotFound` (404)
- `should_returnFailure_when_networkError`
- `should_mapArticleFields_when_responseReceived`

**UploadApiClient 테스트 커버리지** (5개):
- `should_returnUploadResult_when_transcriptUploaded`
- `should_returnUploadResult_when_audioUploaded`
- `should_includeCustomerId_when_uploading`
- `should_returnFailure_when_uploadFails` (400)
- `should_returnCardsGenerated_when_uploadSucceeds`

**UploadApiClient.kt 구현 시 주의**: multipart 파일은 nullable로 처리

**검증**: `./gradlew.bat test --tests "*.KnowledgeApiClientTest" --tests "*.UploadApiClientTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-13 체크.
커밋: `git commit -m "test(DATA-003): KnowledgeApiClient 5 + UploadApiClient 5 tests"`

---

### M-14: TDD DTO Mappers 4개 (20 tests)

**목표**: 20개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/data-tests.md` 의 "§ 5-8 Mapper" 섹션

**Tier 1 TDD**: 각 Mapper에 대해 테스트 먼저 작성

**파일들**:
- `app/src/main/java/com/ralphthon/app/data/mapper/CustomerMapper.kt`
- `app/src/main/java/com/ralphthon/app/data/mapper/CardMapper.kt`
- `app/src/main/java/com/ralphthon/app/data/mapper/KnowledgeMapper.kt`
- `app/src/main/java/com/ralphthon/app/data/mapper/SearchResponseMapper.kt`
- `app/src/test/java/com/ralphthon/app/data/mapper/CustomerMapperTest.kt` (5 tests)
- `app/src/test/java/com/ralphthon/app/data/mapper/CardMapperTest.kt` (6 tests)
- `app/src/test/java/com/ralphthon/app/data/mapper/KnowledgeMapperTest.kt` (5 tests)
- `app/src/test/java/com/ralphthon/app/data/mapper/SearchResponseMapperTest.kt` (4 tests)

**Mapper 패턴** (extension function):
```kotlin
// CustomerMapper.kt
fun CustomerSummaryDto.toDomain(): Customer = Customer(
    id = this.id,
    name = this.name,
    company = this.company,
    cardCount = this.card_count,
    totalConversations = this.total_conversations,
    lastInteractionAt = this.last_interaction_at,
    contacts = emptyList()
)

fun CustomerDetailDto.toDomain(): Customer = Customer(
    id = this.id,
    name = this.name,
    company = this.company,
    cardCount = this.card_count,
    totalConversations = this.total_conversations,
    lastInteractionAt = this.last_interaction_at,
    contacts = this.contacts.map { it.toDomain() }
)

fun ContactDto.toDomain(): Contact = Contact(
    id = this.id, name = this.name,
    email = this.email, phone = this.phone, position = this.position
)
```

**CardMapper** 주의사항:
- `conversation_type` String → ConversationType enum (`.fromString()` 없으므로 `enumValueOf` 또는 try-catch)
- `sentiment` String → Sentiment.fromString()

**검증**: `./gradlew.bat test --tests "*MapperTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-14 체크.
커밋: `git commit -m "test(DATA-004): DTO Mappers 4 with 20 tests"`

---

### M-15: TDD JSON Payload 파싱 테스트 6개

**목표**: 6개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/data-tests.md` 의 "§ 9 JSON Payload" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/api/JsonPayloadTest.kt`

**테스트 패턴** (Gson 직접 파싱):
```kotlin
class JsonPayloadTest {
    private val gson = Gson()

    @Test
    fun should_parseCustomerList_when_jsonReceived() {
        val json = """{"customers":[...],"total_count":5,"has_more":false}"""
        val result = gson.fromJson(json, CustomerListResponse::class.java)
        assertEquals(5, result.total_count)
    }
    // ... 추가 5개 (CardList, CardDetail, Knowledge, Search, Error response)
}
```

**6개 테스트 커버리지**:
1. CustomerListResponse JSON 파싱
2. CardListResponseDto JSON 파싱 (pagination fields 포함)
3. CardDetailDto JSON 파싱 (statements + keywords 포함)
4. KnowledgeResponseDto JSON 파싱
5. SearchResponseDto JSON 파싱
6. ErrorResponse JSON 파싱 (null fields 처리)

**검증**: `./gradlew.bat test --tests "*.JsonPayloadTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-15 체크.
커밋: `git commit -m "test(DATA-005): JSON payload parsing 6 tests"`

---

### M-16: TDD API Contract Tests 25개

**목표**: 25개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/contract-tests.md` 의 "§ 1 Request Contract" 섹션

**테스트 파일**: `app/src/test/java/com/ralphthon/app/data/api/contract/RequestContractTest.kt`

**테스트 커버리지** (MockWebServer로 요청 검증):
- Request method 검증 (GET vs POST)
- Path 검증 (`/api/customers`, `/api/customers/{id}`, 등)
- Query parameter 검증 (`page`, `size`, `q`, `customer_id`, `date_from`, `date_to`)
- Path parameter 검증
- Content-Type header 검증 (multipart)
- Request body 구조 검증

**패턴**:
```kotlin
@Test
fun should_sendGetRequest_when_getCustomersCalled() = runTest {
    mockWebServer.enqueue(MockResponse().setBody("{}").setResponseCode(200))
    client.getCustomers()
    val request = mockWebServer.takeRequest()
    assertEquals("GET", request.method)
    assertTrue(request.path!!.startsWith("/api/customers"))
}
```

**검증**: `./gradlew.bat test --tests "*.RequestContractTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-16 체크.
커밋: `git commit -m "test(DATA-006): API request contract 25 tests"`

---

## Phase 2 완료 처리

모든 M-09 ~ M-16이 완료되면:
1. `git add app/src/`
2. `git commit -m "feat: Phase 2 data layer complete (76 tests)"`
3. `git push`
4. 세션 종료
