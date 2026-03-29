# Parameterized Test Specifications
## Physical AI Sales Context App — JUnit 5 Data-Driven Test Suite

**Target:** 200 parameterized test cases across 6 sections
**Framework:** JUnit 5 `@ParameterizedTest` + `@CsvSource` / `@MethodSource` + MockK
**Package root:** `com.ralphthon.app`
**Naming convention:** `should_[expectedBehavior]_when_[condition]`

---

## Framework Setup

### build.gradle.kts (app) — required dependencies

```kotlin
// JUnit 5 parameterized tests (already pulled in via junit-jupiter-params)
testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")

// MockK for coroutine / suspend-function mocking
testImplementation("io.mockk:mockk:1.13.10")

// kotlinx-coroutines-test for runTest { }
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
```

### Standard test-class skeleton

```kotlin
@ExtendWith(MockKExtension::class)
class SomeClassParameterizedTest {

    // @MethodSource providers live in companion object
    companion object {
        @JvmStatic
        fun provideXxx(): Stream<Arguments> = Stream.of(
            Arguments.of(...),
        )
    }
}
```

---

## Section 1 — Customer Input Validation (30 parameterized tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/model/CustomerValidationParameterizedTest.kt`
**Class under test:** `Customer.withDefaults()`
**Mechanism:** `@CsvSource` for scalar inputs; `@MethodSource` for inputs that cannot be expressed as CSV literals

### 1.1 — name field validation (14 tests)

```kotlin
@ParameterizedTest(name = "[{index}] {2}")
@CsvSource(
    // input,            expected,          description
    "'',                 이름 없음,          blank name defaults"
    "'   ',              이름 없음,          spaces-only name defaults"
    "a,                  a,                 single-char name accepted"
    // 50-char boundary built via @MethodSource — see provider below
    // 51-char boundary built via @MethodSource — see provider below
    "'홍길동',            홍길동,             Korean name accepted"
    "'John Smith',       John Smith,        English name accepted"
    "'홍 길동 Smith',    홍 길동 Smith,     mixed name accepted"
    "'O''Brien',         O'Brien,           apostrophe in name accepted"
    "'-= special =-',   -= special =-,     special chars accepted"
)
fun should_sanitizeName_when_givenVariousInputs(input: String, expected: String, desc: String) {
    val customer = Customer.withDefaults(name = input)
    assertEquals(expected, customer.name)
}
```

| # | Test Name | Data | Expected |
|---|-----------|------|----------|
| 1 | `should_defaultName_when_nameIsBlank` | `""` | `"이름 없음"` |
| 2 | `should_defaultName_when_nameIsSpacesOnly` | `"   "` | `"이름 없음"` |
| 3 | `should_acceptName_when_singleChar` | `"A"` | `"A"` |
| 4 | `should_acceptName_when_exactly50Chars` | `"가".repeat(50)` | same 50-char string |
| 5 | `should_truncateName_when_51Chars` | `"가".repeat(51)` | `"가".repeat(50)` |
| 6 | `should_acceptName_when_Korean` | `"홍길동"` | `"홍길동"` |
| 7 | `should_acceptName_when_English` | `"John Smith"` | `"John Smith"` |
| 8 | `should_acceptName_when_Mixed` | `"홍 길동 Smith"` | `"홍 길동 Smith"` |
| 9 | `should_acceptName_when_specialChars` | `"O'Brien-Jr."` | `"O'Brien-Jr."` |
| 10 | `should_acceptName_when_emojiPresent` | `"김😊철수"` | `"김😊철수"` |
| 11 | `should_truncateName_when_sqlInjectionOver50` | `"'; DROP TABLE customers; --".padEnd(51, 'x')` | first 50 chars |
| 12 | `should_acceptName_when_sqlInjectionUnder50` | `"'; DROP TABLE"` | stored as-is |
| 13 | `should_acceptName_when_xssPayload` | `"<script>alert(1)</script>"` | stored as-is (no HTML context) |
| 14 | `should_acceptName_when_maxUnicodeCodePoints` | string of 50 high-plane Unicode chars | same string |

```kotlin
// @MethodSource provider for boundary inputs that cannot be expressed in @CsvSource
companion object {
    @JvmStatic
    fun provideNameBoundaryInputs(): Stream<Arguments> = Stream.of(
        Arguments.of("가".repeat(50), "가".repeat(50), "exactly 50 chars"),
        Arguments.of("가".repeat(51), "가".repeat(50), "51 chars truncated"),
        Arguments.of("김😊철수", "김😊철수", "emoji in name"),
        Arguments.of("\u{1F600}".repeat(10), "\u{1F600}".repeat(10), "emoji-only name"),
        Arguments.of("<script>alert(1)</script>", "<script>alert(1)</script>", "XSS payload stored raw"),
        Arguments.of("'; DROP TABLE customers; --".padEnd(60, 'x'),
                     "'; DROP TABLE customers; --".padEnd(60, 'x').take(50),
                     "SQL injection over 50 chars truncated"),
    )
}

@ParameterizedTest(name = "[{index}] {2}")
@MethodSource("provideNameBoundaryInputs")
fun should_sanitizeName_when_boundaryInput(input: String, expected: String, desc: String) {
    val customer = Customer.withDefaults(name = input)
    assertEquals(expected, customer.name)
}
```

### 1.2 — company field validation (8 tests)

| # | Test Name | Input | Expected |
|---|-----------|-------|----------|
| 15 | `should_defaultCompany_when_blank` | `""` | `"회사 미등록"` |
| 16 | `should_defaultCompany_when_spacesOnly` | `"   "` | `"회사 미등록"` |
| 17 | `should_acceptCompany_when_singleChar` | `"A"` | `"A"` |
| 18 | `should_acceptCompany_when_Korean` | `"삼성전자"` | `"삼성전자"` |
| 19 | `should_acceptCompany_when_English` | `"Google LLC"` | `"Google LLC"` |
| 20 | `should_acceptCompany_when_specialChars` | `"AT&T, Inc."` | `"AT&T, Inc."` |
| 21 | `should_acceptCompany_when_emojiInName` | `"스타트업🚀"` | `"스타트업🚀"` |
| 22 | `should_acceptCompany_when_xssPayload` | `"<b>Corp</b>"` | stored as-is |

```kotlin
@ParameterizedTest(name = "[{index}] {2}")
@CsvSource(
    "'',          '회사 미등록', blank company defaults",
    "'   ',       '회사 미등록', spaces-only company defaults",
    "A,           A,            single char accepted",
    "'삼성전자',   '삼성전자',   Korean company name",
    "'Google LLC', 'Google LLC', English company name",
    "'AT&T Inc',  'AT&T Inc',   ampersand in name",
)
fun should_sanitizeCompany_when_givenVariousInputs(input: String, expected: String, desc: String) {
    val customer = Customer.withDefaults(company = input)
    assertEquals(expected, customer.company)
}
```

### 1.3 — cardCount / totalConversations boundary (8 tests)

| # | Test Name | Input | Expected |
|---|-----------|-------|----------|
| 23 | `should_acceptCardCount_when_zero` | `cardCount = 0` | `0` |
| 24 | `should_acceptCardCount_when_one` | `cardCount = 1` | `1` |
| 25 | `should_acceptCardCount_when_large` | `cardCount = 1000` | `1000` |
| 26 | `should_acceptCardCount_when_maxInt` | `cardCount = Int.MAX_VALUE` | `Int.MAX_VALUE` |
| 27 | `should_acceptTotalConversations_when_zero` | `totalConversations = 0` | `0` |
| 28 | `should_acceptTotalConversations_when_one` | `totalConversations = 1` | `1` |
| 29 | `should_acceptTotalConversations_when_large` | `totalConversations = 500` | `500` |
| 30 | `should_acceptTotalConversations_when_maxInt` | `totalConversations = Int.MAX_VALUE` | `Int.MAX_VALUE` |

```kotlin
@ParameterizedTest(name = "cardCount={0} → {0}")
@ValueSource(ints = [0, 1, 10, 100, 1000, Int.MAX_VALUE])
fun should_acceptCardCount_when_nonNegative(count: Int) {
    val customer = Customer.withDefaults(cardCount = count)
    assertEquals(count, customer.cardCount)
}

@ParameterizedTest(name = "totalConversations={0} → {0}")
@ValueSource(ints = [0, 1, 10, 500, Int.MAX_VALUE])
fun should_acceptTotalConversations_when_nonNegative(count: Int) {
    val customer = Customer.withDefaults(totalConversations = count)
    assertEquals(count, customer.totalConversations)
}
```

---

## Section 2 — ContextCard Field Validation (30 parameterized tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/model/ContextCardValidationParameterizedTest.kt`
**Class under test:** `ContextCard.withDefaults()`, `Sentiment.fromString()`, `KeywordCategory.fromString()`

### 2.1 — title field validation (8 tests)

| # | Test Name | Input (title) | Expected |
|---|-----------|---------------|----------|
| 1 | `should_acceptTitle_when_blank` | `""` | `""` (blank allowed, no default) |
| 2 | `should_acceptTitle_when_exactly80Chars` | `"가".repeat(80)` | 80-char string |
| 3 | `should_truncateTitle_when_81Chars` | `"가".repeat(81)` | `"가".repeat(80)` |
| 4 | `should_truncateTitle_when_200Chars` | `"가".repeat(200)` | `"가".repeat(80)` |
| 5 | `should_acceptTitle_when_specialChars` | `"[미팅] AI 논의 #1"` | same |
| 6 | `should_acceptTitle_when_emojiInTitle` | `"🤖 로봇 미팅"` | same |
| 7 | `should_acceptTitle_when_englishTitle` | `"Q1 Strategy Review"` | same |
| 8 | `should_acceptTitle_when_mixedKoreanEnglish` | `"AI 전략 Q1 Review"` | same |

```kotlin
companion object {
    @JvmStatic
    fun provideTitleInputs(): Stream<Arguments> = Stream.of(
        Arguments.of("", "", "blank title preserved"),
        Arguments.of("가".repeat(80), "가".repeat(80), "exactly 80 chars ok"),
        Arguments.of("가".repeat(81), "가".repeat(80), "81 chars truncated to 80"),
        Arguments.of("가".repeat(200), "가".repeat(80), "200 chars truncated to 80"),
        Arguments.of("[미팅] AI 논의 #1", "[미팅] AI 논의 #1", "special chars preserved"),
        Arguments.of("🤖 로봇 미팅", "🤖 로봇 미팅", "emoji in title preserved"),
        Arguments.of("Q1 Strategy Review", "Q1 Strategy Review", "english title ok"),
        Arguments.of("AI 전략 Q1 Review", "AI 전략 Q1 Review", "mixed KO+EN ok"),
    )
}

@ParameterizedTest(name = "[{index}] {2}")
@MethodSource("provideTitleInputs")
fun should_sanitizeTitle_when_givenVariousInputs(input: String, expected: String, desc: String) {
    val card = ContextCard.withDefaults(title = input)
    assertEquals(expected, card.title)
}
```

### 2.2 — summary field validation (7 tests)

| # | Test Name | Input (summary) | Expected |
|---|-----------|-----------------|----------|
| 9 | `should_acceptSummary_when_blank` | `""` | `""` |
| 10 | `should_acceptSummary_when_exactly300Chars` | `"가".repeat(300)` | 300-char string |
| 11 | `should_truncateSummary_when_301Chars` | `"가".repeat(301)` | `"가".repeat(300)` |
| 12 | `should_truncateSummary_when_1000Chars` | `"가".repeat(1000)` | `"가".repeat(300)` |
| 13 | `should_acceptSummary_when_multiLine` | `"줄1\n줄2\n줄3"` | same |
| 14 | `should_acceptSummary_when_englishText` | `"Customer prefers on-device AI."` | same |
| 15 | `should_acceptSummary_when_emojiIncluded` | `"결론: 채택 예정 ✅"` | same |

```kotlin
@ParameterizedTest(name = "summaryLen={0} → resultLen={1}")
@CsvSource(
    "0,   0",
    "1,   1",
    "299, 299",
    "300, 300",
    "301, 300",
    "500, 300",
    "1000, 300",
)
fun should_truncateSummary_when_exceedsLimit(inputLen: Int, expectedLen: Int) {
    val input = "가".repeat(inputLen)
    val card = ContextCard.withDefaults(summary = input)
    assertEquals(expectedLen, card.summary.length)
}
```

### 2.3 — topic field validation (5 tests)

| # | Test Name | Input (topic) | Expected |
|---|-----------|---------------|----------|
| 16 | `should_acceptTopic_when_blank` | `""` | `""` |
| 17 | `should_acceptTopic_when_Korean` | `"아키텍처"` | `"아키텍처"` |
| 18 | `should_acceptTopic_when_English` | `"Architecture"` | `"Architecture"` |
| 19 | `should_acceptTopic_when_specialChars` | `"AI/ML & Robotics"` | `"AI/ML & Robotics"` |
| 20 | `should_acceptTopic_when_longString` | `"가".repeat(200)` | same 200-char string |

```kotlin
@ParameterizedTest(name = "[{index}] topic=''{0}'' → ''{0}''")
@ValueSource(strings = ["", "아키텍처", "Architecture", "AI/ML & Robotics"])
fun should_acceptTopic_when_variousInputs(topic: String) {
    val card = ContextCard.withDefaults(topic = topic)
    assertEquals(topic, card.topic)
}
```

### 2.4 — Sentiment enum mapping (10 tests)

| # | Test Name | Input string | Expected Sentiment |
|---|-----------|--------------|-------------------|
| 21 | `should_mapSentiment_when_POSITIVE` | `"POSITIVE"` | `Sentiment.POSITIVE` |
| 22 | `should_mapSentiment_when_NEGATIVE` | `"NEGATIVE"` | `Sentiment.NEGATIVE` |
| 23 | `should_mapSentiment_when_NEUTRAL` | `"NEUTRAL"` | `Sentiment.NEUTRAL` |
| 24 | `should_mapSentiment_when_COMMITMENT` | `"COMMITMENT"` | `Sentiment.COMMITMENT` |
| 25 | `should_mapSentiment_when_REQUEST` | `"REQUEST"` | `Sentiment.REQUEST` |
| 26 | `should_defaultSentiment_when_unknownString` | `"CONCERN"` | `Sentiment.NEUTRAL` |
| 27 | `should_defaultSentiment_when_emptyString` | `""` | `Sentiment.NEUTRAL` |
| 28 | `should_defaultSentiment_when_lowercaseInput` | `"positive"` (case-insensitive via `uppercase()`) | `Sentiment.POSITIVE` |
| 29 | `should_defaultSentiment_when_mixedCase` | `"Neutral"` | `Sentiment.NEUTRAL` |
| 30 | `should_defaultSentiment_when_randomGarbage` | `"QUESTION"` | `Sentiment.NEUTRAL` |

```kotlin
@ParameterizedTest(name = "''{0}'' → {1}")
@CsvSource(
    "POSITIVE,   POSITIVE",
    "NEGATIVE,   NEGATIVE",
    "NEUTRAL,    NEUTRAL",
    "COMMITMENT, COMMITMENT",
    "REQUEST,    REQUEST",
    "positive,   POSITIVE",
    "Neutral,    NEUTRAL",
    "CONCERN,    NEUTRAL",
    "QUESTION,   NEUTRAL",
    "'',         NEUTRAL",
)
fun should_mapSentiment_when_stringInput(input: String, expected: Sentiment) {
    assertEquals(expected, Sentiment.fromString(input))
}
```

---

## Section 3 — UseCase Input Boundary Tests (40 parameterized tests)

**File:** `app/src/test/java/com/ralphthon/app/domain/usecase/UseCaseBoundaryParameterizedTest.kt`
**Dependencies:** `@MockK lateinit var customerRepository: CustomerRepository` etc., `runTest { }` from `kotlinx-coroutines-test`

### Setup skeleton

```kotlin
@ExtendWith(MockKExtension::class)
class UseCaseBoundaryParameterizedTest {

    @MockK lateinit var customerRepository: CustomerRepository
    @MockK lateinit var cardRepository: CardRepository
    @MockK lateinit var knowledgeRepository: KnowledgeRepository
    @MockK lateinit var uploadRepository: UploadRepository

    companion object {
        /* @MethodSource providers declared here */
    }
}
```

### 3.1 — GetCustomersUseCase: list size variations (6 tests)

| # | Test Name | Mock returns N customers | Expected result size |
|---|-----------|--------------------------|----------------------|
| 1 | `should_returnEmptyList_when_noCustomers` | `0` | `0` |
| 2 | `should_returnList_when_oneCustomer` | `1` | `1` |
| 3 | `should_returnList_when_tenCustomers` | `10` | `10` |
| 4 | `should_returnList_when_hundredCustomers` | `100` | `100` |
| 5 | `should_returnList_when_thousandCustomers` | `1000` | `1000` |
| 6 | `should_returnSortedList_when_unsortedInput` | 5 customers with shuffled `lastInteractionAt` | sorted descending |

```kotlin
@ParameterizedTest(name = "listSize={0}")
@ValueSource(ints = [0, 1, 10, 100, 1000])
fun should_returnCorrectSize_when_repositoryReturnsList(size: Int) = runTest {
    val fakeList = List(size) { i ->
        Customer.withDefaults(id = i.toLong(), name = "Customer$i")
    }
    coEvery { customerRepository.getCustomers() } returns fakeList

    val result = customerRepository.getCustomers()

    assertEquals(size, result.size)
}
```

### 3.2 — GetCardsByCustomerUseCase: page/size boundary (12 tests)

| # | Test Name | customerId / page / size | Expected behavior |
|---|-----------|--------------------------|-------------------|
| 7 | `should_returnCards_when_validCustomerIdPageZero` | `id=1, page=0, size=20` | returns first page |
| 8 | `should_returnCards_when_page1` | `id=1, page=1, size=20` | returns second page |
| 9 | `should_returnCards_when_page0Size1` | `id=1, page=0, size=1` | returns 1 card |
| 10 | `should_returnCards_when_page0Size10` | `id=1, page=0, size=10` | returns up to 10 |
| 11 | `should_returnCards_when_page0Size50` | `id=1, page=0, size=50` | returns up to 50 |
| 12 | `should_returnCards_when_page0Size100` | `id=1, page=0, size=100` | returns up to 100 |
| 13 | `should_returnEmptyList_when_customerHasNoCards` | `id=999, page=0, size=20` | empty list, `hasMore=false` |
| 14 | `should_flagHasMore_when_moreCardsExist` | total=21, page=0, size=20 | `hasMore=true` |
| 15 | `should_notFlagHasMore_when_lastPage` | total=20, page=0, size=20 | `hasMore=false` |
| 16 | `should_notFlagHasMore_when_exactFit` | total=10, page=0, size=10 | `hasMore=false` |
| 17 | `should_returnEmpty_when_pageExceedsTotal` | total=5, page=1, size=10 | empty list |
| 18 | `should_returnCards_when_customerIdIsLong` | `id=Long.MAX_VALUE` | delegates to repository |

```kotlin
companion object {
    @JvmStatic
    fun providePageSizeCombinations(): Stream<Arguments> = Stream.of(
        Arguments.of(0,  1,   1,   false),
        Arguments.of(0,  10,  10,  false),
        Arguments.of(0,  20,  20,  false),
        Arguments.of(0,  50,  50,  false),
        Arguments.of(0,  100, 100, false),
        Arguments.of(0,  20,  21,  true),   // 21 total → hasMore on page 0 size 20
        Arguments.of(1,  20,  20,  false),  // page 1 of 20-per-page, exactly 40 total
    )
}

@ParameterizedTest(name = "page={0}, size={1}, total={2} → hasMore={3}")
@MethodSource("providePageSizeCombinations")
fun should_computeHasMore_when_paginating(page: Int, size: Int, totalCount: Int, expectedHasMore: Boolean) = runTest {
    val cards = List(minOf(size, totalCount - page * size).coerceAtLeast(0)) { i ->
        ContextCard.withDefaults(id = i.toLong())
    }
    coEvery {
        cardRepository.getCardsByCustomer(any(), page, size)
    } returns CardListResult(cards, totalCount, expectedHasMore)

    val result = cardRepository.getCardsByCustomer(1L, page, size)

    assertEquals(expectedHasMore, result.hasMore)
}
```

### 3.3 — SearchCardsUseCase: query length and special character boundary (12 tests)

| # | Test Name | query input | Expected behavior |
|---|-----------|-------------|-------------------|
| 19 | `should_executeSearch_when_query1Char` | `"A"` | search executes |
| 20 | `should_executeSearch_when_query2Chars` | `"AI"` | search executes |
| 21 | `should_executeSearch_when_query3Chars` | `"AI "` | search executes |
| 22 | `should_executeSearch_when_query50Chars` | `"A".repeat(50)` | search executes |
| 23 | `should_executeSearch_when_query100Chars` | `"A".repeat(100)` | search executes |
| 24 | `should_executeSearch_when_query256Chars` | `"A".repeat(256)` | search executes (no client-side truncation) |
| 25 | `should_executeSearch_when_queryHasPercent` | `"100%"` | search executes |
| 26 | `should_executeSearch_when_queryHasUnderscore` | `"on_device"` | search executes |
| 27 | `should_executeSearch_when_queryHasAmpersand` | `"AI & ML"` | search executes |
| 28 | `should_executeSearch_when_queryHasPlus` | `"AI+Robot"` | search executes |
| 29 | `should_executeSearch_when_queryIsKorean` | `"온디바이스 AI"` | search executes |
| 30 | `should_executeSearch_when_queryHasEmoji` | `"로봇🤖"` | search executes |

```kotlin
companion object {
    @JvmStatic
    fun provideSearchQueries(): Stream<Arguments> = Stream.of(
        Arguments.of("A",           "1-char query"),
        Arguments.of("AI",          "2-char query"),
        Arguments.of("AI ",         "3-char query with trailing space"),
        Arguments.of("A".repeat(50),  "50-char query"),
        Arguments.of("A".repeat(100), "100-char query"),
        Arguments.of("A".repeat(256), "256-char query — no client truncation"),
        Arguments.of("100%",          "percent sign in query"),
        Arguments.of("on_device",     "underscore in query"),
        Arguments.of("AI & ML",       "ampersand in query"),
        Arguments.of("AI+Robot",      "plus sign in query"),
        Arguments.of("온디바이스 AI",  "Korean query"),
        Arguments.of("로봇🤖",        "emoji in query"),
    )
}

@ParameterizedTest(name = "[{index}] {1}")
@MethodSource("provideSearchQueries")
fun should_delegateSearch_when_queryProvided(query: String, desc: String) = runTest {
    val expected = SearchResult(emptyList(), 0, query, 0, false)
    coEvery { cardRepository.searchCards(query) } returns expected

    val result = cardRepository.searchCards(query)

    assertEquals(query, result.query)
    coVerify(exactly = 1) { cardRepository.searchCards(query) }
}
```

### 3.4 — UploadConversationUseCase: file size boundary (10 tests)

| # | Test Name | file size | Expected |
|---|-----------|-----------|----------|
| 31 | `should_rejectUpload_when_fileSizeIsZero` | `0 bytes` | throws or returns error |
| 32 | `should_acceptUpload_when_fileSizeIs1KB` | `1 * 1024` bytes | delegates to repository |
| 33 | `should_acceptUpload_when_fileSizeIs1MB` | `1 * 1024 * 1024` bytes | delegates to repository |
| 34 | `should_acceptUpload_when_fileSizeIs50MB` | `50 * 1024 * 1024` bytes | delegates to repository |
| 35 | `should_acceptUpload_when_fileSizeIs99MB` | `99 * 1024 * 1024` bytes | delegates to repository |
| 36 | `should_acceptUpload_when_fileSizeIs100MB` | `100 * 1024 * 1024` bytes | delegates to repository |
| 37 | `should_rejectUpload_when_fileSizeExceeds100MB` | `101 * 1024 * 1024` bytes | throws `FileTooLargeException` |
| 38 | `should_acceptUpload_when_transcriptFile` | text file any size | delegates to repository |
| 39 | `should_acceptUpload_when_bothFilesNull` | both null, notes provided | delegates to repository |
| 40 | `should_rejectUpload_when_allInputsNull` | all null | throws `InvalidInputException` |

```kotlin
companion object {
    @JvmStatic
    fun provideFileSizes(): Stream<Arguments> = Stream.of(
        Arguments.of(1L * 1024,            true,  "1KB — accepted"),
        Arguments.of(1L * 1024 * 1024,     true,  "1MB — accepted"),
        Arguments.of(50L * 1024 * 1024,    true,  "50MB — accepted"),
        Arguments.of(99L * 1024 * 1024,    true,  "99MB — accepted"),
        Arguments.of(100L * 1024 * 1024,   true,  "100MB — boundary accepted"),
        Arguments.of(101L * 1024 * 1024,   false, "101MB — rejected"),
    )
}

@ParameterizedTest(name = "[{index}] {2}")
@MethodSource("provideFileSizes")
fun should_validateFileSize_when_audioFileProvided(fileSizeBytes: Long, shouldSucceed: Boolean, desc: String) = runTest {
    val tempFile = mockk<File>()
    every { tempFile.length() } returns fileSizeBytes

    if (shouldSucceed) {
        coEvery {
            uploadRepository.uploadConversation(any(), any(), tempFile, null, null)
        } returns UploadResult(1L, 3)

        val result = uploadRepository.uploadConversation(1L, "MEETING", tempFile)
        assertEquals(1L, result.conversationId)
    } else {
        coEvery {
            uploadRepository.uploadConversation(any(), any(), tempFile, null, null)
        } throws FileTooLargeException("File exceeds 100MB limit")

        assertThrows<FileTooLargeException> {
            uploadRepository.uploadConversation(1L, "MEETING", tempFile)
        }
    }
}
```

---

## Section 4 — API Response Parsing Parameterized (40 parameterized tests)

**File:** `app/src/test/java/com/ralphthon/app/data/api/ApiResponseParsingParameterizedTest.kt`
**Dependencies:** MockWebServer, Retrofit, OkHttp — same setup as `CardApiClientTest.kt`

### 4.1 — HTTP status code handling (12 tests)

| # | Test Name | HTTP Status | Expected behavior |
|---|-----------|-------------|-------------------|
| 1 | `should_returnSuccess_when_status200` | `200` | parses body, returns domain object |
| 2 | `should_returnSuccess_when_status201` | `201` | parses body (POST create response) |
| 3 | `should_returnSuccess_when_status204` | `204` | returns empty/unit (no body) |
| 4 | `should_throwBadRequest_when_status400` | `400` | throws `BadRequestException` |
| 5 | `should_throwUnauthorized_when_status401` | `401` | throws `UnauthorizedException` |
| 6 | `should_throwForbidden_when_status403` | `403` | throws `ForbiddenException` |
| 7 | `should_throwNotFound_when_status404` | `404` | throws `NotFoundException` |
| 8 | `should_throwTimeout_when_status408` | `408` | throws `RequestTimeoutException` |
| 9 | `should_throwRateLimit_when_status429` | `429` | throws `RateLimitException` |
| 10 | `should_throwServerError_when_status500` | `500` | throws `ServerException` |
| 11 | `should_throwBadGateway_when_status502` | `502` | throws `ServerException` |
| 12 | `should_throwServiceUnavailable_when_status503` | `503` | throws `ServiceUnavailableException` |

```kotlin
companion object {
    @JvmStatic
    fun provideErrorStatusCodes(): Stream<Arguments> = Stream.of(
        Arguments.of(400, "BadRequestException"),
        Arguments.of(401, "UnauthorizedException"),
        Arguments.of(403, "ForbiddenException"),
        Arguments.of(404, "NotFoundException"),
        Arguments.of(408, "RequestTimeoutException"),
        Arguments.of(429, "RateLimitException"),
        Arguments.of(500, "ServerException"),
        Arguments.of(502, "ServerException"),
        Arguments.of(503, "ServiceUnavailableException"),
    )
}

@ParameterizedTest(name = "HTTP {0} → throws {1}")
@MethodSource("provideErrorStatusCodes")
fun should_throwAppException_when_errorStatusCode(statusCode: Int, exceptionName: String) = runTest {
    mockWebServer.enqueue(MockResponse().setResponseCode(statusCode).setBody("""{"error":"test"}"""))

    assertThrows<Exception> {
        runBlocking { apiClient.getCustomers() }
    }
}
```

### 4.2 — Response body variations (16 tests)

| # | Test Name | Response body | Expected behavior |
|---|-----------|---------------|-------------------|
| 13 | `should_parseResponse_when_allFieldsPresent` | full valid JSON | returns complete domain object |
| 14 | `should_parseResponse_when_extraFieldsPresent` | JSON with unknown fields | ignores extra fields |
| 15 | `should_handleEmptyBody_when_204Response` | empty string `""` | returns null / Unit |
| 16 | `should_parseResponse_when_nameFieldNull` | `{"name": null, ...}` | maps to `"이름 없음"` |
| 17 | `should_parseResponse_when_companyFieldNull` | `{"company": null, ...}` | maps to `"회사 미등록"` |
| 18 | `should_parseResponse_when_contactsFieldNull` | `{"contacts": null, ...}` | maps to empty list |
| 19 | `should_parseResponse_when_contactsFieldEmpty` | `{"contacts": [], ...}` | maps to empty list |
| 20 | `should_handleMalformedJson_when_invalidSyntax` | `{broken json` | throws `JsonParseException` |
| 21 | `should_handleWrongType_when_idIsString` | `{"id": "abc", ...}` | throws parse exception |
| 22 | `should_parseResponse_when_keyStatementsEmpty` | `{"keyStatements": []}` | maps to empty list |
| 23 | `should_parseResponse_when_keywordsEmpty` | `{"keywords": []}` | maps to empty list |
| 24 | `should_parseResponse_when_nestedObjectNull` | `{"stats": null}` | throws or uses defaults |
| 25 | `should_parseResponse_when_totalCountZero` | `{"totalCount": 0, "customers": []}` | returns empty list result |
| 26 | `should_parseResponse_when_largeList` | 100-item array | maps all 100 items |
| 27 | `should_parseResponse_when_unicodeContent` | JSON with Korean/emoji strings | preserves Unicode |
| 28 | `should_parseResponse_when_summaryFieldAbsent` | JSON without `"summary"` key | uses default or throws |

```kotlin
companion object {
    @JvmStatic
    fun provideJsonBodies(): Stream<Arguments> = Stream.of(
        Arguments.of(
            """{"id":1,"name":"테스트","company":"회사","industry":"","cardCount":0,"lastInteraction":"2026-03-01T00:00:00Z"}""",
            "테스트", "valid full body"
        ),
        Arguments.of(
            """{"id":1,"name":"테스트","company":"회사","industry":"","cardCount":0,"lastInteraction":"2026-03-01T00:00:00Z","unknownField":"ignored"}""",
            "테스트", "extra field ignored"
        ),
        Arguments.of(
            """{"id":1,"name":null,"company":"회사","industry":"","cardCount":0,"lastInteraction":"2026-03-01T00:00:00Z"}""",
            "이름 없음", "null name defaults"  // only if DTO allows nullable + mapper handles it
        ),
    )
}

@ParameterizedTest(name = "[{index}] {2}")
@MethodSource("provideJsonBodies")
fun should_parseName_when_variousJsonBodies(json: String, expectedName: String, desc: String) = runTest {
    mockWebServer.enqueue(
        MockResponse().setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(json)
    )
    val dto = apiClient.getCustomerById(1L)
    assertEquals(expectedName, CustomerDtoMapper.toDomain(dto).name)
}
```

### 4.3 — Date format parsing (12 tests)

| # | Test Name | Date string | Expected result |
|---|-----------|-------------|-----------------|
| 29 | `should_parseDate_when_iso8601UTC` | `"2026-03-25T09:30:00Z"` | `Instant.parse("2026-03-25T09:30:00Z")` |
| 30 | `should_parseDate_when_iso8601WithOffset` | `"2026-03-25T18:30:00+09:00"` | equivalent UTC instant |
| 31 | `should_parseDate_when_iso8601WithMillis` | `"2026-03-25T09:30:00.123Z"` | parsed correctly |
| 32 | `should_parseDate_when_iso8601WithMicros` | `"2026-03-25T09:30:00.123456Z"` | parsed correctly |
| 33 | `should_parseDate_when_epochString` | `"1970-01-01T00:00:00Z"` | `Instant.EPOCH` |
| 34 | `should_parseDate_when_farFutureDate` | `"2099-12-31T23:59:59Z"` | parsed correctly |
| 35 | `should_fallbackToEpoch_when_invalidDate` | `"not-a-date"` | `Instant.EPOCH` |
| 36 | `should_fallbackToEpoch_when_emptyString` | `""` | `Instant.EPOCH` |
| 37 | `should_fallbackToEpoch_when_partialDate` | `"2026-03-25"` | `Instant.EPOCH` (no time part) |
| 38 | `should_fallbackToEpoch_when_unixTimestamp` | `"1711360200"` (numeric string) | `Instant.EPOCH` (not ISO) |
| 39 | `should_parseDate_when_iso8601NoMillis` | `"2026-03-25T09:30:00Z"` | parsed correctly |
| 40 | `should_parseDate_when_iso8601SpaceDelimiter` | `"2026-03-25 09:30:00Z"` | `Instant.EPOCH` (not standard ISO) |

```kotlin
// Uses the same parseInstant() logic exposed through CustomerDtoMapper / CardDtoMapper
companion object {
    @JvmStatic
    fun provideDateInputs(): Stream<Arguments> = Stream.of(
        Arguments.of("2026-03-25T09:30:00Z",        Instant.parse("2026-03-25T09:30:00Z"),  "UTC ISO 8601"),
        Arguments.of("2026-03-25T09:30:00.123Z",    Instant.parse("2026-03-25T09:30:00.123Z"), "millis"),
        Arguments.of("2026-03-25T09:30:00.123456Z", Instant.parse("2026-03-25T09:30:00.123456Z"), "micros"),
        Arguments.of("1970-01-01T00:00:00Z",        Instant.EPOCH,                          "epoch"),
        Arguments.of("2099-12-31T23:59:59Z",        Instant.parse("2099-12-31T23:59:59Z"),  "far future"),
        Arguments.of("not-a-date",                  Instant.EPOCH,                          "invalid — fallback"),
        Arguments.of("",                            Instant.EPOCH,                          "empty — fallback"),
        Arguments.of("2026-03-25",                  Instant.EPOCH,                          "date only — fallback"),
        Arguments.of("1711360200",                  Instant.EPOCH,                          "unix number — fallback"),
        Arguments.of("2026-03-25 09:30:00Z",        Instant.EPOCH,                          "space delimiter — fallback"),
    )
}

@ParameterizedTest(name = "[{index}] ''{0}'' → {2}")
@MethodSource("provideDateInputs")
fun should_parseOrFallback_when_dateStringProvided(input: String, expected: Instant, desc: String) {
    val dto = CustomerSummaryDto(1L, "테스트", "회사", "", 0, input)
    val result = CustomerDtoMapper.toDomain(dto)
    assertEquals(expected, result.lastInteractionAt)
}
```

---

## Section 5 — Mapper Edge Cases Parameterized (30 parameterized tests)

**File:** `app/src/test/java/com/ralphthon/app/data/mapper/MapperEdgeCasesParameterizedTest.kt`

### 5.1 — CustomerDtoMapper edge cases (8 tests)

| # | Test Name | Input variation | Expected |
|---|-----------|-----------------|----------|
| 1 | `should_defaultName_when_emptyStringName` | `name=""` | `"이름 없음"` |
| 2 | `should_defaultName_when_blankStringName` | `name="   "` | `"이름 없음"` |
| 3 | `should_defaultCompany_when_emptyStringCompany` | `company=""` | `"회사 미등록"` |
| 4 | `should_truncateName_when_nameExactly50` | `name="가".repeat(50)` | 50-char name unchanged |
| 5 | `should_truncateName_when_name51Chars` | `name="가".repeat(51)` | `"가".repeat(50)` |
| 6 | `should_fallbackDate_when_invalidIso` | `lastInteraction="INVALID"` | `Instant.EPOCH` |
| 7 | `should_fallbackDate_when_emptyDate` | `lastInteraction=""` | `Instant.EPOCH` |
| 8 | `should_mapZeroCardCount_when_cardCountIsZero` | `cardCount=0` | `0` |

```kotlin
companion object {
    @JvmStatic
    fun provideCustomerDtoVariations(): Stream<Arguments> = Stream.of(
        Arguments.of("",      "   ", 0,  "이름 없음", "회사 미등록", "both blank"),
        Arguments.of("홍",    "삼성", 5,  "홍",       "삼성",       "valid short name"),
        Arguments.of("가".repeat(51), "회사", 1, "가".repeat(50), "회사", "name truncated"),
        Arguments.of("이름",  "",     3,  "이름",     "회사 미등록", "company blank"),
        Arguments.of("이름",  "   ",  3,  "이름",     "회사 미등록", "company spaces"),
    )
}

@ParameterizedTest(name = "[{index}] {5}")
@MethodSource("provideCustomerDtoVariations")
fun should_mapCustomerFields_when_variousInputs(
    name: String, company: String, cardCount: Int,
    expectedName: String, expectedCompany: String, desc: String
) {
    val dto = CustomerSummaryDto(1L, name, company, "", cardCount, "2026-03-25T09:30:00Z")
    val result = CustomerDtoMapper.toDomain(dto)
    assertEquals(expectedName, result.name)
    assertEquals(expectedCompany, result.company)
    assertEquals(cardCount, result.cardCount)
}
```

### 5.2 — CardDtoMapper: sentiment and category mapping (10 tests)

| # | Test Name | Input sentiment string | Expected Sentiment |
|---|-----------|------------------------|--------------------|
| 9  | `should_mapSentiment_when_POSITIVE` | `"POSITIVE"` | `Sentiment.POSITIVE` |
| 10 | `should_mapSentiment_when_NEGATIVE` | `"NEGATIVE"` | `Sentiment.NEGATIVE` |
| 11 | `should_mapSentiment_when_NEUTRAL` | `"NEUTRAL"` | `Sentiment.NEUTRAL` |
| 12 | `should_mapSentiment_when_COMMITMENT` | `"COMMITMENT"` | `Sentiment.COMMITMENT` |
| 13 | `should_mapSentiment_when_REQUEST` | `"REQUEST"` | `Sentiment.REQUEST` |
| 14 | `should_defaultSentiment_when_unknownValue` | `"CONCERN"` | `Sentiment.NEUTRAL` |
| 15 | `should_mapCategory_when_TECHNOLOGY` | `"TECHNOLOGY"` | `KeywordCategory.TECHNOLOGY` |
| 16 | `should_mapCategory_when_ARCHITECTURE` | `"ARCHITECTURE"` | `KeywordCategory.ARCHITECTURE` |
| 17 | `should_mapCategory_when_PRODUCT` | `"PRODUCT"` | `KeywordCategory.PRODUCT` |
| 18 | `should_mapCategory_when_unknownCategory` | `"COMPETITOR"` | `KeywordCategory.TECHNOLOGY` (default) |

```kotlin
@ParameterizedTest(name = "''{0}'' → {1}")
@CsvSource(
    "POSITIVE,   POSITIVE",
    "NEGATIVE,   NEGATIVE",
    "NEUTRAL,    NEUTRAL",
    "COMMITMENT, COMMITMENT",
    "REQUEST,    REQUEST",
    "CONCERN,    NEUTRAL",
    "positive,   POSITIVE",
    "'',         NEUTRAL",
)
fun should_mapSentiment_when_stringProvided(input: String, expected: Sentiment) {
    assertEquals(expected, CardDtoMapper.mapSentiment(input))
}

@ParameterizedTest(name = "''{0}'' → {1}")
@CsvSource(
    "TECHNOLOGY,  TECHNOLOGY",
    "ARCHITECTURE,ARCHITECTURE",
    "PRODUCT,     PRODUCT",
    "BUSINESS,    BUSINESS",
    "COMPETITOR,  TECHNOLOGY",
    "'',          TECHNOLOGY",
)
fun should_mapCategory_when_stringProvided(input: String, expected: KeywordCategory) {
    assertEquals(expected, CardDtoMapper.mapCategory(input))
}
```

### 5.3 — KnowledgeDtoMapper edge cases (6 tests)

| # | Test Name | Input variation | Expected |
|---|-----------|-----------------|----------|
| 19 | `should_preserveContent_when_contentPresent` | `content="AI 기술 설명"` | `"AI 기술 설명"` |
| 20 | `should_defaultContent_when_contentBlank` | `content=""` | `"(내용 없음)"` |
| 21 | `should_defaultContent_when_contentWhitespace` | `content="   "` | `"(내용 없음)"` |
| 22 | `should_preserveRelatedKeywords_when_listPresent` | `["온디바이스", "엣지"]` | same list |
| 23 | `should_handleEmptyRelatedKeywords_when_listEmpty` | `[]` | empty list |
| 24 | `should_preserveSources_when_sourcesPresent` | `["NVIDIA docs", "LG AI"]` | same list |

```kotlin
companion object {
    @JvmStatic
    fun provideKnowledgeContentInputs(): Stream<Arguments> = Stream.of(
        Arguments.of("AI 기술 설명", "AI 기술 설명", "non-blank content preserved"),
        Arguments.of("",             "(내용 없음)",  "empty content defaults"),
        Arguments.of("   ",          "(내용 없음)",  "whitespace content defaults"),
        Arguments.of("\n\t",         "(내용 없음)",  "newline+tab content defaults"),
    )
}

@ParameterizedTest(name = "[{index}] {2}")
@MethodSource("provideKnowledgeContentInputs")
fun should_sanitizeContent_when_variousInputs(input: String, expected: String, desc: String) {
    val article = KnowledgeArticle.withDefaults(content = input)
    assertEquals(expected, article.content)
}
```

### 5.4 — SearchResponseMapper edge cases (6 tests)

| # | Test Name | Input | Expected |
|---|-----------|-------|----------|
| 25 | `should_mapEmptyCardList_when_noResults` | `cards=[], totalCount=0` | `SearchResult([], 0, ...)` |
| 26 | `should_mapSingleCard_when_oneResult` | `cards=[card], totalCount=1` | `totalCount=1, cards.size=1` |
| 27 | `should_preserveQuery_when_queryPresent` | `query="온디바이스"` | `result.query="온디바이스"` |
| 28 | `should_computeHasMore_when_moreResults` | `hasMore=true` | `result.hasMore=true` |
| 29 | `should_computeHasMore_when_noMoreResults` | `hasMore=false` | `result.hasMore=false` |
| 30 | `should_preservePage_when_page2` | `page=2` | `result.page=2` |

```kotlin
companion object {
    @JvmStatic
    fun provideSearchResponses(): Stream<Arguments> = Stream.of(
        Arguments.of(0,  false, 0,  "empty result"),
        Arguments.of(1,  false, 0,  "single result page 0"),
        Arguments.of(20, true,  0,  "full page, has more"),
        Arguments.of(20, false, 1,  "full page 1, no more"),
        Arguments.of(5,  false, 0,  "partial page no more"),
        Arguments.of(100,true,  4,  "100 results, page 4"),
    )
}

@ParameterizedTest(name = "cards={0}, hasMore={1}, page={2} — {3}")
@MethodSource("provideSearchResponses")
fun should_mapSearchResponse_when_variousInputs(
    cardCount: Int, hasMore: Boolean, page: Int, desc: String
) {
    val cards = List(cardCount) { i -> makeContextCardDto(i.toLong()) }
    val response = SearchResponse(cards, cardCount, "test", page, hasMore)
    val result = SearchResponseMapper.toDomain(response)

    assertEquals(cardCount, result.totalCount)
    assertEquals(cardCount, result.cards.size)
    assertEquals(hasMore, result.hasMore)
    assertEquals(page, result.page)
}
```

---

## Section 6 — ViewModel State Transition Parameterized (30 parameterized tests)

**File:** `app/src/test/java/com/ralphthon/app/ui/viewmodel/ViewModelStateParameterizedTest.kt`
**Dependencies:** `@MockK` repositories, `kotlinx-coroutines-test`, `Turbine` (optional for Flow testing)

> Note: ViewModels do not yet exist in the codebase. These tests follow the architecture expected from `CLAUDE.md` (MVVM + Clean Architecture). Implement ViewModel classes before running these tests.

### 6.1 — CustomerListViewModel state transitions (10 tests)

**Expected ViewModel states:** `Loading`, `Success(customers)`, `Error(message)`

| # | Test Name | Initial state | Event / Action | Expected state |
|---|-----------|---------------|----------------|----------------|
| 1 | `should_emitLoading_when_loadStarted` | `Idle` | `loadCustomers()` called | `Loading` |
| 2 | `should_emitSuccess_when_repositoryReturnsData` | `Loading` | repository returns list of 5 | `Success(5 customers)` |
| 3 | `should_emitEmptySuccess_when_repositoryReturnsEmpty` | `Loading` | repository returns `[]` | `Success([])` |
| 4 | `should_emitError_when_repositoryThrows` | `Loading` | repository throws `IOException` | `Error("network error")` |
| 5 | `should_emitError_when_repositoryThrowsNotFound` | `Loading` | repository throws `NotFoundException` | `Error("not found")` |
| 6 | `should_emitSuccess_when_refreshAfterError` | `Error` | `loadCustomers()` called again, succeeds | `Success(list)` |
| 7 | `should_emitLoading_when_refreshTriggered` | `Success` | `refresh()` called | `Loading` (then Success) |
| 8 | `should_preserveData_when_refreshFails` | `Success(3 customers)` | refresh throws, then `restorePrevious()` | `Success(3 customers)` |
| 9 | `should_emitSuccess_when_listOf100` | `Loading` | repository returns 100 items | `Success(100 customers)` |
| 10 | `should_emitSuccess_when_listOf1000` | `Loading` | repository returns 1000 items | `Success(1000 customers)` |

```kotlin
companion object {
    @JvmStatic
    fun provideCustomerListSizes(): Stream<Arguments> = Stream.of(
        Arguments.of(0,    "empty list"),
        Arguments.of(1,    "single customer"),
        Arguments.of(10,   "ten customers"),
        Arguments.of(100,  "hundred customers"),
        Arguments.of(1000, "thousand customers"),
    )
}

@ParameterizedTest(name = "listSize={0} — {1}")
@MethodSource("provideCustomerListSizes")
fun should_emitSuccess_when_repositoryReturnsList(size: Int, desc: String) = runTest {
    val customers = List(size) { i -> Customer.withDefaults(id = i.toLong(), name = "Customer$i") }
    coEvery { customerRepository.getCustomers() } returns customers

    val viewModel = CustomerListViewModel(customerRepository)
    viewModel.loadCustomers()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertIs<CustomerListUiState.Success>(state)
    assertEquals(size, state.customers.size)
}
```

### 6.2 — CardNewsListViewModel: pagination (10 tests)

| # | Test Name | (page, size, totalCount) | Expected hasMore |
|---|-----------|--------------------------|-----------------|
| 11 | `should_notHaveMore_when_totalFitsOnePage` | `(0, 20, 15)` | `false` |
| 12 | `should_notHaveMore_when_exactlyOnePage` | `(0, 20, 20)` | `false` |
| 13 | `should_haveMore_when_oneExtraCard` | `(0, 20, 21)` | `true` |
| 14 | `should_notHaveMore_when_lastPage` | `(1, 20, 40)` | `false` |
| 15 | `should_haveMore_when_manyPages` | `(0, 10, 100)` | `true` |
| 16 | `should_notHaveMore_when_emptyList` | `(0, 20, 0)` | `false` |
| 17 | `should_appendCards_when_loadNextPage` | starts with page 0 (20 cards), loads page 1 | 40 total cards in state |
| 18 | `should_notAppend_when_hasMoreFalse` | last page reached | no additional repository call |
| 19 | `should_resetPagination_when_customerChanges` | switch customerId | page resets to 0 |
| 20 | `should_haveMore_when_size1TotalGreaterThan1` | `(0, 1, 2)` | `true` |

```kotlin
companion object {
    @JvmStatic
    fun providePaginationCases(): Stream<Arguments> = Stream.of(
        // page, size, totalCount, expectedHasMore
        Arguments.of(0, 20, 0,   false, "empty"),
        Arguments.of(0, 20, 15,  false, "partial page"),
        Arguments.of(0, 20, 20,  false, "exact page"),
        Arguments.of(0, 20, 21,  true,  "one extra card"),
        Arguments.of(0, 10, 100, true,  "many pages"),
        Arguments.of(0, 1,  2,   true,  "size 1 total 2"),
        Arguments.of(1, 20, 40,  false, "last page"),
        Arguments.of(2, 10, 25,  false, "page 2, 5 remain"),
        Arguments.of(0, 50, 51,  true,  "size 50 total 51"),
        Arguments.of(9, 10, 100, false, "last of 10 pages"),
    )
}

@ParameterizedTest(name = "page={0}, size={1}, total={2} → hasMore={3} — {4}")
@MethodSource("providePaginationCases")
fun should_computeHasMore_when_paginationParamsProvided(
    page: Int, size: Int, totalCount: Int, expectedHasMore: Boolean, desc: String
) = runTest {
    val cards = List(minOf(size, maxOf(0, totalCount - page * size))) { i ->
        ContextCard.withDefaults(id = i.toLong())
    }
    val cardListResult = CardListResult(cards, totalCount, expectedHasMore)
    coEvery { cardRepository.getCardsByCustomer(any(), page, size) } returns cardListResult

    val viewModel = CardNewsListViewModel(cardRepository)
    viewModel.loadCards(customerId = 1L, page = page, size = size)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertIs<CardListUiState.Success>(state)
    assertEquals(expectedHasMore, state.hasMore)
}
```

### 6.3 — SearchViewModel: query input behavior (10 tests)

| # | Test Name | Query input | Expected behavior |
|---|-----------|-------------|-------------------|
| 21 | `should_notSearch_when_queryIsEmpty` | `""` | no repository call made |
| 22 | `should_notSearch_when_queryIsBlank` | `"   "` | no repository call made |
| 23 | `should_search_when_querySingleChar` | `"A"` | repository called once |
| 24 | `should_search_when_queryKorean` | `"온디바이스"` | repository called once |
| 25 | `should_search_when_queryHasSpecialChars` | `"AI & ML"` | repository called once |
| 26 | `should_emitLoading_when_searchStarts` | any valid query | `SearchUiState.Loading` emitted |
| 27 | `should_emitResults_when_searchSucceeds` | `"AI"` → 5 results | `SearchUiState.Success(5 cards)` |
| 28 | `should_emitEmptyResults_when_noMatch` | `"xyznotfound"` → 0 results | `SearchUiState.Empty` |
| 29 | `should_emitError_when_searchFails` | `"AI"` → repository throws | `SearchUiState.Error(...)` |
| 30 | `should_cancelPrevious_when_queryChangedQuickly` | rapid query changes | only last query result used |

```kotlin
companion object {
    @JvmStatic
    fun provideSearchQueryCases(): Stream<Arguments> = Stream.of(
        // query, shouldCallRepository, resultCount, desc
        Arguments.of("",          false, 0,  "empty query — skip"),
        Arguments.of("   ",       false, 0,  "blank query — skip"),
        Arguments.of("A",         true,  3,  "single char — search"),
        Arguments.of("온디바이스", true,  5,  "Korean query — search"),
        Arguments.of("AI & ML",   true,  2,  "special chars — search"),
        Arguments.of("A".repeat(256), true, 1, "256-char query — search"),
    )
}

@ParameterizedTest(name = "[{index}] ''{0}'' → callsRepo={1} — {3}")
@MethodSource("provideSearchQueryCases")
fun should_handleSearchQuery_when_inputProvided(
    query: String, shouldCallRepository: Boolean, resultCount: Int, desc: String
) = runTest {
    if (shouldCallRepository) {
        val cards = List(resultCount) { i -> ContextCard.withDefaults(id = i.toLong()) }
        coEvery {
            cardRepository.searchCards(query.trim())
        } returns SearchResult(cards, resultCount, query.trim(), 0, false)
    }

    val viewModel = SearchViewModel(cardRepository)
    viewModel.onQueryChanged(query)
    advanceUntilIdle()

    if (shouldCallRepository) {
        coVerify(exactly = 1) { cardRepository.searchCards(query.trim()) }
        val state = viewModel.uiState.value
        if (resultCount > 0) {
            assertIs<SearchUiState.Success>(state)
            assertEquals(resultCount, state.cards.size)
        } else {
            assertIs<SearchUiState.Empty>(state)
        }
    } else {
        coVerify(exactly = 0) { cardRepository.searchCards(any()) }
    }
}
```

---

## Total Test Count Summary

| Section | Tests | File |
|---------|-------|------|
| 1. Customer Input Validation | 30 | `CustomerValidationParameterizedTest.kt` |
| 2. ContextCard Field Validation | 30 | `ContextCardValidationParameterizedTest.kt` |
| 3. UseCase Input Boundary | 40 | `UseCaseBoundaryParameterizedTest.kt` |
| 4. API Response Parsing | 40 | `ApiResponseParsingParameterizedTest.kt` |
| 5. Mapper Edge Cases | 30 | `MapperEdgeCasesParameterizedTest.kt` |
| 6. ViewModel State Transitions | 30 | `ViewModelStateParameterizedTest.kt` |
| **Total** | **200** | |

---

## Known Domain Corrections (verified against actual source)

The following deviations from the task brief were corrected to match the actual codebase:

| Brief says | Actual codebase | Impact |
|------------|-----------------|--------|
| `Sentiment`: POSITIVE, NEGATIVE, NEUTRAL, COMMITMENT, CONCERN, QUESTION | `Sentiment`: POSITIVE, NEGATIVE, NEUTRAL, COMMITMENT, **REQUEST** (no CONCERN or QUESTION) | Section 2.4, 5.2 tests use REQUEST, map CONCERN/QUESTION to NEUTRAL |
| `KeywordCategory`: TECHNOLOGY, BUSINESS, PRODUCT, COMPETITOR, GENERAL | `KeywordCategory`: TECHNOLOGY, **ARCHITECTURE**, PRODUCT, BUSINESS (no COMPETITOR or GENERAL) | Section 5.2 tests map COMPETITOR → TECHNOLOGY (default) |
| `Customer.contactCount` | `Customer.cardCount` + `totalConversations` (no `contactCount` field) | Section 1.3 tests use `cardCount` and `totalConversations` |
| `ConversationType`: MEETING, CALL, EMAIL, DEMO, SUPPORT | `ConversationType`: CALL, MEETING, EMAIL only | Section 3.4 uses MEETING type string only |
| `SearchResult.hasMore, cards, totalCount, query` | Same fields + `page: Int` | Section 5.4 tests also assert `page` |
| `ContextCard.sentiment` (top-level) | `KeyStatement.sentiment` (per-statement) | Card-level sentiment tests moved to KeyStatement scope |
