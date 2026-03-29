package com.ralphthon.app.data.api.contract

import com.google.gson.Gson
import com.ralphthon.app.data.dto.ActionItemDto
import com.ralphthon.app.data.dto.CardDto
import com.ralphthon.app.data.dto.CustomerDto
import com.ralphthon.app.data.dto.ErrorDto
import com.ralphthon.app.data.dto.KeyStatementDto
import com.ralphthon.app.data.dto.KeywordDto
import com.ralphthon.app.data.dto.KnowledgeDto
import com.ralphthon.app.data.dto.PredictedQuestionDto
import com.ralphthon.app.data.dto.PriceCommitmentDto
import com.ralphthon.app.data.dto.SearchResponseDto
import com.ralphthon.app.data.dto.UploadResponseDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResponseSchemaTest {

    private val gson = Gson()

    // ─── CustomerDto schema (5) ────────────────────────────────────────────────

    // Test 1
    @Test
    fun should_parseId_asLong_when_customerJson() {
        val json = """{"id":1,"company_name":"삼성","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(1L, dto.id)
    }

    // Test 2
    @Test
    fun should_parseCompanyName_asString_when_customerJson() {
        val json = """{"id":1,"company_name":"LG전자","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals("LG전자", dto.companyName)
    }

    // Test 3
    @Test
    fun should_parseContactName_asNullable_when_customerJson() {
        val json = """{"id":1,"company_name":"삼성","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertNull(dto.contactName)
    }

    // Test 4
    @Test
    fun should_parseTotalConversations_asInt_when_customerJson() {
        val json = """{"id":1,"company_name":"삼성","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":12,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(12, dto.totalConversations)
    }

    // Test 5
    @Test
    fun should_parseSummary_asNullable_when_customerJson() {
        val jsonNull = """{"id":1,"company_name":"삼성","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5,"summary":null}"""
        val dtoNull = gson.fromJson(jsonNull, CustomerDto::class.java)
        assertNull(dtoNull.summary)

        val jsonVal = """{"id":1,"company_name":"삼성","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5,"summary":"요약내용"}"""
        val dtoVal = gson.fromJson(jsonVal, CustomerDto::class.java)
        assertEquals("요약내용", dtoVal.summary)
    }

    // ─── CardDto schema (8) ───────────────────────────────────────────────────

    private fun cardJson(): String = """{
        "id":10,"conversation_id":20,"customer_id":30,"title":"미팅","date":"2025-03-15",
        "conversation_type":"CUSTOMER_MEETING","summary":"요약","sentiment":"POSITIVE",
        "sentiment_score":0.85,
        "keywords":[{"text":"가격","category":"PRICE","frequency":3}],
        "key_statements":[{"id":1,"speaker":"홍길동","text":"확인","timestamp":"00:01:00","sentiment":"POSITIVE","is_important":true}],
        "price_commitments":[{"id":1,"amount":5000000.0,"currency":"KRW","condition":"정상가","mentioned_at":"00:02:00"}],
        "action_items":[{"id":1,"description":"후속 연락","assignee":"홍길동","due_date":"2025-04-01","status":"OPEN"}],
        "predicted_questions":[{"id":1,"question":"언제 납품?","suggested_answer":"4월 예정","related_knowledge":["납품일정"],"confidence":0.9}],
        "related_knowledge":[{"id":1,"title":"가격정책","content":"내용","category":"PRICE","relevance_score":0.95}]
    }"""

    // Test 6
    @Test
    fun should_parseId_asLong_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertEquals(10L, dto.id)
    }

    // Test 7
    @Test
    fun should_parseSentimentScore_asFloat_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertEquals(0.85f, dto.sentimentScore, 0.001f)
    }

    // Test 8
    @Test
    fun should_parseKeywords_asList_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertNotNull(dto.keywords)
        assertEquals(1, dto.keywords.size)
    }

    // Test 9
    @Test
    fun should_parseKeyStatements_asList_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertNotNull(dto.keyStatements)
        assertEquals(1, dto.keyStatements.size)
    }

    // Test 10
    @Test
    fun should_parsePriceCommitments_asList_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertNotNull(dto.priceCommitments)
        assertEquals(1, dto.priceCommitments.size)
    }

    // Test 11
    @Test
    fun should_parseActionItems_asList_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertNotNull(dto.actionItems)
        assertEquals(1, dto.actionItems.size)
    }

    // Test 12
    @Test
    fun should_parsePredictedQuestions_asList_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertNotNull(dto.predictedQuestions)
        assertEquals(1, dto.predictedQuestions.size)
    }

    // Test 13
    @Test
    fun should_parseRelatedKnowledge_asList_when_cardJson() {
        val dto = gson.fromJson(cardJson(), CardDto::class.java)
        assertNotNull(dto.relatedKnowledge)
        assertEquals(1, dto.relatedKnowledge.size)
    }

    // ─── Nested DTO schemas (10) ──────────────────────────────────────────────

    // Test 14
    @Test
    fun should_parseKeywordText_asString_when_nested() {
        val json = """{"text":"가격","category":"PRICE","frequency":3}"""
        val dto = gson.fromJson(json, KeywordDto::class.java)
        assertEquals("가격", dto.text)
    }

    // Test 15
    @Test
    fun should_parseKeywordFrequency_asInt_when_nested() {
        val json = """{"text":"가격","category":"PRICE","frequency":7}"""
        val dto = gson.fromJson(json, KeywordDto::class.java)
        assertEquals(7, dto.frequency)
    }

    // Test 16
    @Test
    fun should_parseStatementSentiment_asString_when_nested() {
        val json = """{"id":1,"speaker":"홍길동","text":"확인","timestamp":"00:01:00","sentiment":"NEGATIVE","is_important":false}"""
        val dto = gson.fromJson(json, KeyStatementDto::class.java)
        assertEquals("NEGATIVE", dto.sentiment)
    }

    // Test 17
    @Test
    fun should_parseStatementIsImportant_asBoolean_when_nested() {
        val json = """{"id":1,"speaker":"홍길동","text":"확인","timestamp":"00:01:00","sentiment":"POSITIVE","is_important":true}"""
        val dto = gson.fromJson(json, KeyStatementDto::class.java)
        assertEquals(true, dto.isImportant)
    }

    // Test 18
    @Test
    fun should_parsePriceAmount_asDouble_when_nested() {
        val json = """{"id":1,"amount":1234567.89,"currency":"KRW","condition":"정상가","mentioned_at":"00:02:00"}"""
        val dto = gson.fromJson(json, PriceCommitmentDto::class.java)
        assertEquals(1234567.89, dto.amount, 0.001)
    }

    // Test 19
    @Test
    fun should_parsePriceCurrency_asString_when_nested() {
        val json = """{"id":1,"amount":5000000.0,"currency":"USD","condition":"할인가","mentioned_at":"00:02:00"}"""
        val dto = gson.fromJson(json, PriceCommitmentDto::class.java)
        assertEquals("USD", dto.currency)
    }

    // Test 20
    @Test
    fun should_parseActionDueDate_asNullable_when_nested() {
        val jsonNull = """{"id":1,"description":"후속 연락","assignee":"홍길동","due_date":null,"status":"OPEN"}"""
        val dtoNull = gson.fromJson(jsonNull, ActionItemDto::class.java)
        assertNull(dtoNull.dueDate)

        val jsonVal = """{"id":1,"description":"후속 연락","assignee":"홍길동","due_date":"2025-04-01","status":"OPEN"}"""
        val dtoVal = gson.fromJson(jsonVal, ActionItemDto::class.java)
        assertEquals("2025-04-01", dtoVal.dueDate)
    }

    // Test 21
    @Test
    fun should_parseActionStatus_asString_when_nested() {
        val json = """{"id":1,"description":"후속 연락","assignee":"홍길동","due_date":null,"status":"DONE"}"""
        val dto = gson.fromJson(json, ActionItemDto::class.java)
        assertEquals("DONE", dto.status)
    }

    // Test 22
    @Test
    fun should_parseQuestionConfidence_asFloat_when_nested() {
        val json = """{"id":1,"question":"언제?","suggested_answer":"4월","related_knowledge":[],"confidence":0.75}"""
        val dto = gson.fromJson(json, PredictedQuestionDto::class.java)
        assertEquals(0.75f, dto.confidence, 0.001f)
    }

    // Test 23
    @Test
    fun should_parseQuestionRelatedKnowledge_asList_when_nested() {
        val json = """{"id":1,"question":"언제?","suggested_answer":"4월","related_knowledge":["납품일정","가격정책"],"confidence":0.9}"""
        val dto = gson.fromJson(json, PredictedQuestionDto::class.java)
        assertEquals(2, dto.relatedKnowledge.size)
        assertEquals("납품일정", dto.relatedKnowledge[0])
        assertEquals("가격정책", dto.relatedKnowledge[1])
    }

    // ─── SearchResponseDto schema (5) ─────────────────────────────────────────

    private fun searchJson(): String = """{
        "results":[{"id":1,"type":"CARD","title":"미팅","snippet":"요약","highlight_ranges":[[0,3],[5,8]],"source_id":10,"relevance_score":0.92}],
        "total_count":1,
        "query":"미팅"
    }"""

    // Test 24
    @Test
    fun should_parseTotalCount_asInt_when_searchJson() {
        val dto = gson.fromJson(searchJson(), SearchResponseDto::class.java)
        assertEquals(1, dto.totalCount)
    }

    // Test 25
    @Test
    fun should_parseQuery_asString_when_searchJson() {
        val dto = gson.fromJson(searchJson(), SearchResponseDto::class.java)
        assertEquals("미팅", dto.query)
    }

    // Test 26
    @Test
    fun should_parseResults_asList_when_searchJson() {
        val dto = gson.fromJson(searchJson(), SearchResponseDto::class.java)
        assertNotNull(dto.results)
        assertEquals(1, dto.results.size)
    }

    // Test 27
    @Test
    fun should_parseHighlightRanges_asNestedList_when_searchJson() {
        val dto = gson.fromJson(searchJson(), SearchResponseDto::class.java)
        val ranges = dto.results[0].highlightRanges
        assertEquals(2, ranges.size)
        assertEquals(listOf(0, 3), ranges[0])
        assertEquals(listOf(5, 8), ranges[1])
    }

    // Test 28
    @Test
    fun should_parseRelevanceScore_asFloat_when_searchJson() {
        val dto = gson.fromJson(searchJson(), SearchResponseDto::class.java)
        assertEquals(0.92f, dto.results[0].relevanceScore, 0.001f)
    }

    // ─── UploadResponseDto schema (3) ─────────────────────────────────────────

    private fun uploadJson(): String = """{"conversation_id":42,"status":"SUCCESS","message":"업로드 완료"}"""

    // Test 29
    @Test
    fun should_parseConversationId_asLong_when_uploadJson() {
        val dto = gson.fromJson(uploadJson(), UploadResponseDto::class.java)
        assertEquals(42L, dto.conversationId)
    }

    // Test 30
    @Test
    fun should_parseStatus_asString_when_uploadJson() {
        val dto = gson.fromJson(uploadJson(), UploadResponseDto::class.java)
        assertEquals("SUCCESS", dto.status)
    }

    // Test 31
    @Test
    fun should_parseMessage_asString_when_uploadJson() {
        val dto = gson.fromJson(uploadJson(), UploadResponseDto::class.java)
        assertEquals("업로드 완료", dto.message)
    }

    // ─── ErrorDto schema (3) ──────────────────────────────────────────────────

    // Test 32
    @Test
    fun should_parseCode_asInt_when_errorJson() {
        val json = """{"code":404,"message":"Not Found","details":null}"""
        val dto = gson.fromJson(json, ErrorDto::class.java)
        assertEquals(404, dto.code)
    }

    // Test 33
    @Test
    fun should_parseMessage_asString_when_errorJson() {
        val json = """{"code":500,"message":"Internal Server Error","details":null}"""
        val dto = gson.fromJson(json, ErrorDto::class.java)
        assertEquals("Internal Server Error", dto.message)
    }

    // Test 34
    @Test
    fun should_parseDetails_asNullable_when_errorJson() {
        val jsonNull = """{"code":404,"message":"Not Found","details":null}"""
        val dtoNull = gson.fromJson(jsonNull, ErrorDto::class.java)
        assertNull(dtoNull.details)

        val jsonVal = """{"code":400,"message":"Bad Request","details":"field validation failed"}"""
        val dtoVal = gson.fromJson(jsonVal, ErrorDto::class.java)
        assertEquals("field validation failed", dtoVal.details)
    }

    // ─── Edge cases (6) ───────────────────────────────────────────────────────

    // Test 35
    @Test
    fun should_handleEmptyKeywords_when_emptyArray() {
        val json = """{
            "id":1,"conversation_id":2,"customer_id":3,"title":"T","date":"2025-01-01",
            "conversation_type":"CUSTOMER_MEETING","summary":"S","sentiment":"NEUTRAL",
            "sentiment_score":0.5,
            "keywords":[],"key_statements":[],"price_commitments":[],"action_items":[],
            "predicted_questions":[],"related_knowledge":[]
        }"""
        val dto = gson.fromJson(json, CardDto::class.java)
        assertTrue(dto.keywords.isEmpty())
        assertTrue(dto.actionItems.isEmpty())
    }

    // Test 36
    @Test
    fun should_handleMissingOptionalField_when_notPresent() {
        val json = """{"id":1,"company_name":"삼성","industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertNull(dto.contactName)
        assertNull(dto.summary)
    }

    // Test 37
    @Test
    fun should_handleExtraFields_when_unknownFieldPresent() {
        val json = """{"id":1,"company_name":"삼성","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5,"summary":null,"unknown_extra_field":"ignored"}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(1L, dto.id)
        assertEquals("삼성", dto.companyName)
    }

    // Test 38
    @Test
    fun should_handleIntAsLong_when_smallNumber() {
        val json = """{"id":1,"company_name":"삼성","contact_name":null,"industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        val id: Long = dto.id
        assertEquals(1L, id)
    }

    // Test 39
    @Test
    fun should_handleFloatPrecision_when_highPrecision() {
        val json = """{"id":1,"question":"Q","suggested_answer":"A","related_knowledge":[],"confidence":0.123456}"""
        val dto = gson.fromJson(json, PredictedQuestionDto::class.java)
        assertTrue(dto.confidence > 0.12f)
        assertTrue(dto.confidence < 0.13f)
    }

    // Test 40
    @Test
    fun should_handleUnicodeText_when_koreanPresent() {
        val korean = "안녕하세요 지피지기면 백전백승"
        val json = """{"id":1,"company_name":"$korean","contact_name":null,"industry":"IT","last_interaction_date":"2025-03-15","total_conversations":1,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(korean, dto.companyName)
    }
}
