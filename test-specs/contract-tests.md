# API Contract & Schema Validation Tests

Framework: JUnit 5 + MockWebServer + Gson
Target: 100 tests

---

## 1. Request Contract Tests (25 tests)

These tests verify that each API client sends the correct HTTP method, path, query parameters, and headers.

| # | Test Name | Endpoint | What is Verified |
|---|-----------|----------|-----------------|
| 1 | `should_sendGetMethod_when_fetchingCustomerList` | GET /api/customers | HTTP method is GET |
| 2 | `should_sendCorrectPath_when_fetchingCustomerList` | GET /api/customers | Request path equals `/api/customers` |
| 3 | `should_sendAcceptHeader_when_fetchingCustomerList` | GET /api/customers | `Accept: application/json` header present |
| 4 | `should_sendGetMethod_when_fetchingCustomerDetail` | GET /api/customers/{id} | HTTP method is GET |
| 5 | `should_sendCorrectPathWithId_when_fetchingCustomerDetail` | GET /api/customers/{id} | Path equals `/api/customers/42` for id=42 |
| 6 | `should_sendGetMethod_when_fetchingCardList` | GET /api/customers/{id}/cards | HTTP method is GET |
| 7 | `should_sendCorrectPathWithCustomerId_when_fetchingCardList` | GET /api/customers/{id}/cards | Path equals `/api/customers/7/cards` for customerId=7 |
| 8 | `should_sendPageQueryParam_when_fetchingCardList` | GET /api/customers/{id}/cards?page= | `page` query param present and equals requested value |
| 9 | `should_sendSizeQueryParam_when_fetchingCardList` | GET /api/customers/{id}/cards?size= | `size` query param present and equals requested value |
| 10 | `should_sendGetMethod_when_fetchingCardDetail` | GET /api/cards/{id} | HTTP method is GET |
| 11 | `should_sendCorrectPath_when_fetchingCardDetail` | GET /api/cards/{id} | Path equals `/api/cards/99` for id=99 |
| 12 | `should_sendAcceptHeader_when_fetchingCardDetail` | GET /api/cards/{id} | `Accept: application/json` header present |
| 13 | `should_sendGetMethod_when_searchingCards` | GET /api/cards/search | HTTP method is GET |
| 14 | `should_sendQueryParam_when_searchingCards` | GET /api/cards/search?query= | `query` param present and matches input string |
| 15 | `should_sendCustomerIdParam_when_searchingCardsWithFilter` | GET /api/cards/search?customerId= | `customerId` param present when filter applied |
| 16 | `should_sendDateFromParam_when_searchingWithDateRange` | GET /api/cards/search?dateFrom= | `dateFrom` param present when date filter applied |
| 17 | `should_sendDateToParam_when_searchingWithDateRange` | GET /api/cards/search?dateTo= | `dateTo` param present when date filter applied |
| 18 | `should_sendPageAndSizeParams_when_searchingCards` | GET /api/cards/search?page=&size= | Both `page` and `size` params present |
| 19 | `should_sendGetMethod_when_fetchingKnowledge` | GET /api/knowledge/{keywordId} | HTTP method is GET |
| 20 | `should_sendCorrectPath_when_fetchingKnowledge` | GET /api/knowledge/{keywordId} | Path equals `/api/knowledge/kw-01` for keywordId=kw-01 |
| 21 | `should_sendAcceptHeader_when_fetchingKnowledge` | GET /api/knowledge/{keywordId} | `Accept: application/json` header present |
| 22 | `should_sendPostMethod_when_uploadingConversation` | POST /api/conversations | HTTP method is POST |
| 23 | `should_sendCorrectPath_when_uploadingConversation` | POST /api/conversations | Path equals `/api/conversations` |
| 24 | `should_sendMultipartContentType_when_uploadingConversation` | POST /api/conversations | `Content-Type` header starts with `multipart/form-data; boundary=` |
| 25 | `should_sendCorrectFieldNames_when_uploadingConversation` | POST /api/conversations | Multipart body contains field named `file` and `customerId` |

### Implementation Notes

```kotlin
@Test
fun should_sendGetMethod_when_fetchingCustomerList() {
    mockWebServer.enqueue(MockResponse().setBody(validCustomerListJson).setResponseCode(200))
    runBlocking { customerApiService.getCustomers() }
    val request = mockWebServer.takeRequest()
    assertEquals("GET", request.method)
}

@Test
fun should_sendMultipartContentType_when_uploadingConversation() {
    mockWebServer.enqueue(MockResponse().setBody(validUploadResponseJson).setResponseCode(200))
    runBlocking { uploadApiService.uploadConversation(customerId = "1", file = testFilePart) }
    val request = mockWebServer.takeRequest()
    assertTrue(request.getHeader("Content-Type")!!.startsWith("multipart/form-data; boundary="))
}
```

---

## 2. Response Schema Validation (25 tests)

These tests verify that the server response is correctly deserialized: all required fields present, correct types, pagination consistency.

| # | Test Name | DTO | What is Verified |
|---|-----------|-----|-----------------|
| 26 | `should_parseId_when_deserializingCustomerSummary` | CustomerSummaryDto | `id` field deserialized as String |
| 27 | `should_parseName_when_deserializingCustomerSummary` | CustomerSummaryDto | `name` field deserialized as non-null String |
| 28 | `should_parseCompany_when_deserializingCustomerSummary` | CustomerSummaryDto | `company` field deserialized as String |
| 29 | `should_parseContactCount_when_deserializingCustomerSummary` | CustomerSummaryDto | `contactCount` deserialized as Int |
| 30 | `should_parseLastInteractionAt_when_deserializingCustomerSummary` | CustomerSummaryDto | `lastInteractionAt` deserialized as nullable String |
| 31 | `should_parseContactsList_when_deserializingCustomerDetail` | CustomerDetailDto | `contacts` field is a List (not null) |
| 32 | `should_parseEmptyContactsList_when_serverReturnsEmptyArray` | CustomerDetailDto | `contacts` is empty list when JSON array is `[]` |
| 33 | `should_parseCards_when_deserializingCardListResponse` | CardListResponseDto | `cards` is a List |
| 34 | `should_parseTotalCount_when_deserializingCardListResponse` | CardListResponseDto | `totalCount` is Int |
| 35 | `should_parsePage_when_deserializingCardListResponse` | CardListResponseDto | `page` is Int matching requested page |
| 36 | `should_parseSize_when_deserializingCardListResponse` | CardListResponseDto | `size` is Int |
| 37 | `should_parseHasMore_when_deserializingCardListResponse` | CardListResponseDto | `hasMore` is Boolean |
| 38 | `should_parseHasMoreFalse_when_lastPage` | CardListResponseDto | `hasMore` is false when `page * size >= totalCount` |
| 39 | `should_parseStatements_when_deserializingCardDetail` | CardDetailDto | `statements` is a List |
| 40 | `should_parseKeywords_when_deserializingCardDetail` | CardDetailDto | `keywords` is a List |
| 41 | `should_parseSentiment_when_deserializingCardDetail` | CardDetailDto | `sentiment` is a non-null String |
| 42 | `should_parseArticles_when_deserializingKnowledgeResponse` | KnowledgeResponseDto | `articles` is a List |
| 43 | `should_parseKeywordTerm_when_deserializingKnowledgeResponse` | KnowledgeResponseDto | `keywordTerm` is a non-null String |
| 44 | `should_parseResults_when_deserializingSearchResponse` | SearchResponseDto | `results` is a List of CardSummaryDto |
| 45 | `should_parseQuery_when_deserializingSearchResponse` | SearchResponseDto | `query` echoed back as String |
| 46 | `should_parseEmptyResults_when_searchReturnsNoMatches` | SearchResponseDto | `results` is empty list, `totalCount` is 0 |
| 47 | `should_parseConversationId_when_deserializingUploadResponse` | UploadResponseDto | `conversationId` is a non-null String |
| 48 | `should_parseCardsGenerated_when_deserializingUploadResponse` | UploadResponseDto | `cardsGenerated` is Int >= 0 |
| 49 | `should_parseErrorCode_when_serverReturnsErrorResponse` | ErrorResponse | `code` is non-null String |
| 50 | `should_parseErrorMessage_when_serverReturnsErrorResponse` | ErrorResponse | `message` is non-null String |

### Implementation Notes

```kotlin
@Test
fun should_parseHasMoreFalse_when_lastPage() {
    val json = """{"cards":[],"totalCount":10,"page":1,"size":10,"hasMore":false}"""
    mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))
    val response = runBlocking { cardApiService.getCards(customerId = "1", page = 1, size = 10) }
    assertFalse(response.hasMore)
}

@Test
fun should_parseErrorCode_when_serverReturnsErrorResponse() {
    val json = """{"code":"NOT_FOUND","message":"Resource not found","timestamp":"2024-01-01T00:00:00Z"}"""
    mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(404))
    // Verify error body can be deserialized into ErrorResponse
    val errorBody = runBlocking {
        try { customerApiService.getCustomerById("999"); null }
        catch (e: HttpException) { gson.fromJson(e.response()!!.errorBody()!!.string(), ErrorResponse::class.java) }
    }
    assertEquals("NOT_FOUND", errorBody?.code)
}
```

---

## 3. Field-Level Null Safety (25 tests)

These tests verify that every nullable field, when null in the JSON, results in a valid domain object (no NullPointerException, no crash).

| # | Test Name | DTO | Null Field | Expected Behavior |
|---|-----------|-----|-----------|------------------|
| 51 | `should_handleNullLastInteractionAt_when_customerSummaryFieldIsNull` | CustomerSummaryDto | `lastInteractionAt` | Mapped to null in domain model, no crash |
| 52 | `should_handleNullContacts_when_customerDetailFieldIsNull` | CustomerDetailDto | `contacts` | Mapped to empty list in domain model |
| 53 | `should_handleNullCompany_when_customerSummaryFieldIsNull` | CustomerSummaryDto | `company` | Mapped to empty string or null in domain model |
| 54 | `should_handleNullSummary_when_cardDetailFieldIsNull` | CardDetailDto | `summary` | Mapped to empty string or null, no crash |
| 55 | `should_handleNullTopic_when_cardDetailFieldIsNull` | CardDetailDto | `topic` | Mapped to null, no crash |
| 56 | `should_handleNullSentiment_when_cardDetailFieldIsNull` | CardDetailDto | `sentiment` | Defaults to UNKNOWN or null safely |
| 57 | `should_handleNullConversationType_when_cardDetailFieldIsNull` | CardDetailDto | `conversationType` | Defaults to UNKNOWN or null safely |
| 58 | `should_handleNullStatements_when_cardDetailFieldIsNull` | CardDetailDto | `statements` | Mapped to empty list, no crash |
| 59 | `should_handleNullKeywords_when_cardDetailFieldIsNull` | CardDetailDto | `keywords` | Mapped to empty list, no crash |
| 60 | `should_handleNullCreatedAt_when_cardDetailFieldIsNull` | CardDetailDto | `createdAt` | Mapped to null, no crash |
| 61 | `should_handleNullArticles_when_knowledgeResponseFieldIsNull` | KnowledgeResponseDto | `articles` | Mapped to empty list, no crash |
| 62 | `should_handleNullKeywordTerm_when_knowledgeResponseFieldIsNull` | KnowledgeResponseDto | `keywordTerm` | Mapped to empty string or null safely |
| 63 | `should_handleNullResults_when_searchResponseFieldIsNull` | SearchResponseDto | `results` | Mapped to empty list, no crash |
| 64 | `should_handleNullQuery_when_searchResponseFieldIsNull` | SearchResponseDto | `query` | Mapped to empty string, no crash |
| 65 | `should_handleNullCardsGenerated_when_uploadResponseFieldIsNull` | UploadResponseDto | `cardsGenerated` | Defaults to 0, no crash |
| 66 | `should_handleNullConversationId_when_uploadResponseFieldIsNull` | UploadResponseDto | `conversationId` | Mapped to null or empty string safely |
| 67 | `should_handleNullTimestamp_when_errorResponseFieldIsNull` | ErrorResponse | `timestamp` | Mapped to null, no crash |
| 68 | `should_handleEmptyStringName_when_customerSummaryNameIsEmpty` | CustomerSummaryDto | `name` (empty) | Distinct from null; mapped as-is |
| 69 | `should_handleEmptyStringTitle_when_cardDetailTitleIsEmpty` | CardDetailDto | `title` (empty) | Distinct from null; mapped as-is |
| 70 | `should_handleEmptyStringKeywordTerm_when_knowledgeResponseTermIsEmpty` | KnowledgeResponseDto | `keywordTerm` (empty) | Distinct from null; mapped as-is |
| 71 | `should_handleAllFieldsNull_when_customerSummaryFullyNullPayload` | CustomerSummaryDto | All nullable fields | No crash; required fields use defaults |
| 72 | `should_handleAllFieldsNull_when_cardDetailFullyNullPayload` | CardDetailDto | All nullable fields | No crash; lists default to empty |
| 73 | `should_handleAllFieldsNull_when_knowledgeResponseFullyNullPayload` | KnowledgeResponseDto | All nullable fields | No crash |
| 74 | `should_handleAllFieldsNull_when_searchResponseFullyNullPayload` | SearchResponseDto | All nullable fields | No crash; lists default to empty |
| 75 | `should_handleAllFieldsNull_when_uploadResponseFullyNullPayload` | UploadResponseDto | All nullable fields | No crash |

### Implementation Notes

```kotlin
@Test
fun should_handleNullContacts_when_customerDetailFieldIsNull() {
    val json = """{"id":"1","name":"Kim","company":"Acme","contactCount":0,"lastInteractionAt":null,"contacts":null}"""
    mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))
    val dto = runBlocking { customerApiService.getCustomerById("1") }
    val domain = dto.toDomainModel()
    assertNotNull(domain)
    assertEquals(emptyList(), domain.contacts)
}

@Test
fun should_handleEmptyStringName_when_customerSummaryNameIsEmpty() {
    val json = """{"id":"1","name":"","company":"Acme","contactCount":0,"lastInteractionAt":null}"""
    val dto = gson.fromJson(json, CustomerSummaryDto::class.java)
    assertEquals("", dto.name)   // empty string != null
}
```

---

## 4. Backward Compatibility (25 tests)

These tests verify that the client gracefully handles schema evolution: new fields, missing optional fields, type changes, and new enum values.

| # | Test Name | Scenario | Expected Behavior |
|---|-----------|----------|------------------|
| 76 | `should_ignoreUnknownField_when_customerSummaryHasExtraField` | CustomerSummaryDto + `"newField":"value"` | Parsed without exception; known fields intact |
| 77 | `should_ignoreUnknownField_when_customerDetailHasExtraField` | CustomerDetailDto + `"legacyId":123` | Parsed without exception |
| 78 | `should_ignoreUnknownField_when_cardListResponseHasExtraField` | CardListResponseDto + `"cursor":"abc"` | Parsed without exception; pagination fields intact |
| 79 | `should_ignoreUnknownField_when_cardDetailHasExtraField` | CardDetailDto + `"aiScore":0.95` | Parsed without exception |
| 80 | `should_ignoreUnknownField_when_knowledgeResponseHasExtraField` | KnowledgeResponseDto + `"relatedKeywords":[]` | Parsed without exception |
| 81 | `should_ignoreUnknownField_when_searchResponseHasExtraField` | SearchResponseDto + `"facets":{}` | Parsed without exception |
| 82 | `should_ignoreUnknownField_when_uploadResponseHasExtraField` | UploadResponseDto + `"processingTime":120` | Parsed without exception |
| 83 | `should_ignoreUnknownField_when_errorResponseHasExtraField` | ErrorResponse + `"traceId":"abc-123"` | Parsed without exception |
| 84 | `should_applyDefault_when_hasMoreMissingFromCardListResponse` | CardListResponseDto missing `hasMore` | Defaults to false |
| 85 | `should_applyDefault_when_pageMissingFromCardListResponse` | CardListResponseDto missing `page` | Defaults to 0 |
| 86 | `should_applyDefault_when_sizeMissingFromCardListResponse` | CardListResponseDto missing `size` | Defaults to 0 or configured page size |
| 87 | `should_applyDefault_when_contactCountMissingFromCustomerSummary` | CustomerSummaryDto missing `contactCount` | Defaults to 0 |
| 88 | `should_applyDefault_when_cardsGeneratedMissingFromUploadResponse` | UploadResponseDto missing `cardsGenerated` | Defaults to 0 |
| 89 | `should_applyDefault_when_articlesMissingFromKnowledgeResponse` | KnowledgeResponseDto missing `articles` | Defaults to empty list |
| 90 | `should_applyDefault_when_resultsMissingFromSearchResponse` | SearchResponseDto missing `results` | Defaults to empty list |
| 91 | `should_parseIntAsString_when_idIsReturnedAsNumber` | CustomerSummaryDto `id` as JSON number `42` | Parsed to String `"42"` without crash |
| 92 | `should_parseIntAsString_when_cardIdIsReturnedAsNumber` | CardDetailDto `id` as JSON number | Parsed to String without crash |
| 93 | `should_parseStringAsInt_when_contactCountIsReturnedAsString` | CustomerSummaryDto `contactCount` as `"5"` | Parsed to Int 5 without crash |
| 94 | `should_parseStringAsInt_when_totalCountIsReturnedAsString` | CardListResponseDto `totalCount` as `"100"` | Parsed to Int 100 without crash |
| 95 | `should_parseStringAsBoolean_when_hasMoreIsReturnedAsString` | CardListResponseDto `hasMore` as `"true"` | Parsed to Boolean true without crash |
| 96 | `should_fallbackToDefault_when_unknownSentimentEnumValue` | CardDetailDto `sentiment` = `"VERY_POSITIVE"` (unknown) | Falls back to UNKNOWN/NEUTRAL, no crash |
| 97 | `should_fallbackToDefault_when_unknownConversationTypeEnumValue` | CardDetailDto `conversationType` = `"VOICE_MEMO"` (unknown) | Falls back to UNKNOWN, no crash |
| 98 | `should_parseCorrectly_when_emptyCardsArrayInListResponse` | CardListResponseDto `cards` = `[]` | `cards` is empty list, no crash |
| 99 | `should_parseCorrectly_when_emptyArticlesArrayInKnowledgeResponse` | KnowledgeResponseDto `articles` = `[]` | `articles` is empty list, no crash |
| 100 | `should_remainStable_when_multipleBackwardCompatChangesApplied` | CustomerDetailDto with extra fields + missing optional fields + numeric id | All known fields parsed; no crash |

### Implementation Notes

```kotlin
@Test
fun should_ignoreUnknownField_when_customerSummaryHasExtraField() {
    val json = """{"id":"1","name":"Kim","company":"Acme","contactCount":2,
        |"lastInteractionAt":"2024-01-01","newField":"should-be-ignored"}""".trimMargin()
    // Gson by default ignores unknown fields; verify no JsonSyntaxException
    assertDoesNotThrow {
        val dto = gson.fromJson(json, CustomerSummaryDto::class.java)
        assertEquals("Kim", dto.name)
    }
}

@Test
fun should_fallbackToDefault_when_unknownSentimentEnumValue() {
    val json = buildCardDetailJson(sentiment = "VERY_POSITIVE")
    mockWebServer.enqueue(MockResponse().setBody(json).setResponseCode(200))
    val dto = runBlocking { cardApiService.getCardById("1") }
    val domain = dto.toDomainModel()
    // Mapper must not throw; unknown enum maps to UNKNOWN
    assertNotNull(domain)
    assertEquals(Sentiment.UNKNOWN, domain.sentiment)
}

@Test
fun should_parseIntAsString_when_idIsReturnedAsNumber() {
    val json = """{"id":42,"name":"Kim","company":"Acme","contactCount":0,"lastInteractionAt":null}"""
    // Verify Gson type adapter or lenient parsing handles numeric id
    assertDoesNotThrow {
        val dto = gson.fromJson(json, CustomerSummaryDto::class.java)
        assertEquals("42", dto.id)
    }
}
```

---

## Test Infrastructure

```kotlin
// Base test class
@ExtendWith(MockWebServerExtension::class)  // or manual setup
abstract class ContractTestBase {
    lateinit var mockWebServer: MockWebServer
    lateinit var gson: Gson
    lateinit var retrofit: Retrofit

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        gson = GsonBuilder().serializeNulls().create()
        retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }
}

// JSON fixture helpers
fun buildCardDetailJson(
    id: String = "1",
    customerId: String = "c1",
    title: String = "Test Card",
    summary: String? = "A summary",
    topic: String? = "sales",
    conversationType: String? = "MEETING",
    sentiment: String? = "POSITIVE",
    createdAt: String? = "2024-01-01T00:00:00Z",
    statements: List<String> = listOf("Statement 1"),
    keywords: List<String> = listOf("keyword1")
): String = """
    {
      "id": "$id",
      "customerId": "$customerId",
      "title": "$title",
      "summary": ${if (summary == null) "null" else "\"$summary\""},
      "topic": ${if (topic == null) "null" else "\"$topic\""},
      "conversationType": ${if (conversationType == null) "null" else "\"$conversationType\""},
      "sentiment": ${if (sentiment == null) "null" else "\"$sentiment\""},
      "createdAt": ${if (createdAt == null) "null" else "\"$createdAt\""},
      "statements": ${gson.toJson(statements)},
      "keywords": ${gson.toJson(keywords)}
    }
""".trimIndent()
```
