package com.ralphthon.app.data.api.contract

import com.google.gson.Gson
import com.ralphthon.app.data.dto.ActionItemDto
import com.ralphthon.app.data.dto.CardDto
import com.ralphthon.app.data.dto.CustomerDto
import com.ralphthon.app.data.dto.ErrorDto
import com.ralphthon.app.data.dto.KeyStatementDto
import com.ralphthon.app.data.dto.KeywordDto
import com.ralphthon.app.data.dto.PredictedQuestionDto
import com.ralphthon.app.data.dto.PriceCommitmentDto
import com.ralphthon.app.data.dto.SearchResponseDto
import com.ralphthon.app.data.dto.SearchResultDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NullSafetyContractTest {

    private val gson = Gson()

    // ─── CustomerDto null safety (5) ──────────────────────────────────────────

    // Test 1
    @Test
    fun should_handleNullContactName_when_missing() {
        val json = """{"id":1,"company_name":"삼성","industry":"전자","last_interaction_date":"2025-03-15","total_conversations":5}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertNull(dto.contactName)
    }

    // Test 2
    @Test
    fun should_handleNullSummary_when_missing() {
        val json = """{"id":1,"company_name":"LG","industry":"전자","last_interaction_date":"2025-03-15","total_conversations":3}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertNull(dto.summary)
    }

    // Test 3
    @Test
    fun should_handleNullContactName_when_explicit() {
        val json = """{"id":2,"company_name":"현대","contact_name":null,"industry":"자동차","last_interaction_date":"2025-03-10","total_conversations":2}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertNull(dto.contactName)
    }

    // Test 4
    @Test
    fun should_defaultCompanyName_when_emptyString() {
        val json = """{"id":3,"company_name":"","industry":"IT","last_interaction_date":"2025-03-01","total_conversations":1}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals("", dto.companyName)
    }

    // Test 5
    @Test
    fun should_defaultId_when_missing() {
        val json = """{"company_name":"카카오","industry":"IT","last_interaction_date":"2025-03-01","total_conversations":1}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(0L, dto.id)
    }

    // ─── CardDto null safety (7) ───────────────────────────────────────────────

    // Test 6
    @Test
    fun should_handleEmptyKeywords_when_emptyArray() {
        val json = buildCardJson(keywords = "[]")
        val dto = gson.fromJson(json, CardDto::class.java)
        assertTrue(dto.keywords.isEmpty())
    }

    // Test 7
    @Test
    fun should_handleNullKeywords_when_missing() {
        val json = buildCardJsonWithoutField("keywords")
        val dto = gson.fromJson(json, CardDto::class.java)
        assertNull(dto.keywords)
    }

    // Test 8
    @Test
    fun should_handleEmptyKeyStatements_when_emptyArray() {
        val json = buildCardJson(keyStatements = "[]")
        val dto = gson.fromJson(json, CardDto::class.java)
        assertTrue(dto.keyStatements.isEmpty())
    }

    // Test 9
    @Test
    fun should_handleNullKeyStatements_when_missing() {
        val json = buildCardJsonWithoutField("key_statements")
        val dto = gson.fromJson(json, CardDto::class.java)
        assertNull(dto.keyStatements)
    }

    // Test 10
    @Test
    fun should_handleEmptyPriceCommitments_when_emptyArray() {
        val json = buildCardJson(priceCommitments = "[]")
        val dto = gson.fromJson(json, CardDto::class.java)
        assertTrue(dto.priceCommitments.isEmpty())
    }

    // Test 11
    @Test
    fun should_handleEmptyActionItems_when_emptyArray() {
        val json = buildCardJson(actionItems = "[]")
        val dto = gson.fromJson(json, CardDto::class.java)
        assertTrue(dto.actionItems.isEmpty())
    }

    // Test 12
    @Test
    fun should_handleEmptyPredictedQuestions_when_emptyArray() {
        val json = buildCardJson(predictedQuestions = "[]")
        val dto = gson.fromJson(json, CardDto::class.java)
        assertTrue(dto.predictedQuestions.isEmpty())
    }

    // ─── Nested DTO null safety (8) ───────────────────────────────────────────

    // Test 13
    @Test
    fun should_handleNullKeywordCategory_when_missing() {
        val json = """{"text":"가격","frequency":3}"""
        val dto = gson.fromJson(json, KeywordDto::class.java)
        assertNull(dto.category)
    }

    // Test 14
    @Test
    fun should_handleZeroKeywordFrequency_when_missing() {
        val json = """{"text":"계약","category":"비즈니스"}"""
        val dto = gson.fromJson(json, KeywordDto::class.java)
        assertEquals(0, dto.frequency)
    }

    // Test 15
    @Test
    fun should_handleNullStatementTimestamp_when_missing() {
        val json = """{"id":1,"speaker":"김대리","text":"예산이 부족합니다","sentiment":"NEGATIVE","is_important":true}"""
        val dto = gson.fromJson(json, KeyStatementDto::class.java)
        assertNull(dto.timestamp)
    }

    // Test 16
    @Test
    fun should_handleFalseIsImportant_when_missing() {
        val json = """{"id":1,"speaker":"이팀장","text":"검토해보겠습니다","timestamp":"00:05:30","sentiment":"NEUTRAL"}"""
        val dto = gson.fromJson(json, KeyStatementDto::class.java)
        assertEquals(false, dto.isImportant)
    }

    // Test 17
    @Test
    fun should_handleZeroPriceAmount_when_missing() {
        val json = """{"id":1,"currency":"KRW","condition":"할인 조건","mentioned_at":"00:10:00"}"""
        val dto = gson.fromJson(json, PriceCommitmentDto::class.java)
        assertEquals(0.0, dto.amount)
    }

    // Test 18
    @Test
    fun should_handleNullActionDueDate_when_missing() {
        val json = """{"id":1,"description":"제안서 발송","assignee":"박과장","status":"OPEN"}"""
        val dto = gson.fromJson(json, ActionItemDto::class.java)
        assertNull(dto.dueDate)
    }

    // Test 19
    @Test
    fun should_handleNullActionDueDate_when_explicit() {
        val json = """{"id":2,"description":"견적서 작성","assignee":"김대리","due_date":null,"status":"OPEN"}"""
        val dto = gson.fromJson(json, ActionItemDto::class.java)
        assertNull(dto.dueDate)
    }

    // Test 20
    @Test
    fun should_handleZeroConfidence_when_missing() {
        val json = """{"id":1,"question":"납기일은?","suggested_answer":"협의 가능","related_knowledge":[]}"""
        val dto = gson.fromJson(json, PredictedQuestionDto::class.java)
        assertEquals(0.0f, dto.confidence)
    }

    // ─── SearchResponseDto null safety (5) ────────────────────────────────────

    // Test 21
    @Test
    fun should_handleEmptyResults_when_emptyArray() {
        val json = """{"results":[],"total_count":0,"query":"테스트"}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        assertTrue(dto.results.isEmpty())
    }

    // Test 22
    @Test
    fun should_handleNullResults_when_missing() {
        val json = """{"total_count":0,"query":"테스트"}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        assertNull(dto.results)
    }

    // Test 23
    @Test
    fun should_handleZeroTotalCount_when_missing() {
        val json = """{"results":[],"query":"검색어"}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        assertEquals(0, dto.totalCount)
    }

    // Test 24
    @Test
    fun should_handleEmptyHighlightRanges_when_emptyArray() {
        val json = """{"id":1,"type":"CARD","title":"제목","snippet":"내용","highlight_ranges":[],"source_id":10,"relevance_score":0.9}"""
        val dto = gson.fromJson(json, SearchResultDto::class.java)
        assertTrue(dto.highlightRanges.isEmpty())
    }

    // Test 25
    @Test
    fun should_handleNullHighlightRanges_when_missing() {
        val json = """{"id":1,"type":"CARD","title":"제목","snippet":"내용","source_id":10,"relevance_score":0.9}"""
        val dto = gson.fromJson(json, SearchResultDto::class.java)
        assertNull(dto.highlightRanges)
    }

    // ─── PredictedQuestionDto null safety (3) ─────────────────────────────────

    // Test 26
    @Test
    fun should_handleEmptyRelatedKnowledge_when_emptyArray() {
        val json = """{"id":1,"question":"가격은?","suggested_answer":"협의","related_knowledge":[],"confidence":0.8}"""
        val dto = gson.fromJson(json, PredictedQuestionDto::class.java)
        assertTrue(dto.relatedKnowledge.isEmpty())
    }

    // Test 27
    @Test
    fun should_handleNullRelatedKnowledge_when_missing() {
        val json = """{"id":1,"question":"납기는?","suggested_answer":"2주","confidence":0.7}"""
        val dto = gson.fromJson(json, PredictedQuestionDto::class.java)
        assertNull(dto.relatedKnowledge)
    }

    // Test 28
    @Test
    fun should_handleZeroConfidence_when_explicitZero() {
        val json = """{"id":1,"question":"할인율?","suggested_answer":"미정","related_knowledge":[],"confidence":0.0}"""
        val dto = gson.fromJson(json, PredictedQuestionDto::class.java)
        assertEquals(0.0f, dto.confidence)
    }

    // ─── ErrorDto null safety (2) ─────────────────────────────────────────────

    // Test 29
    @Test
    fun should_handleNullDetails_when_missing() {
        val json = """{"code":404,"message":"Not Found"}"""
        val dto = gson.fromJson(json, ErrorDto::class.java)
        assertNull(dto.details)
    }

    // Test 30
    @Test
    fun should_handleNullDetails_when_explicit() {
        val json = """{"code":500,"message":"Internal Server Error","details":null}"""
        val dto = gson.fromJson(json, ErrorDto::class.java)
        assertNull(dto.details)
    }

    // ─── Cross-DTO null safety (5) ────────────────────────────────────────────

    // Test 31
    @Test
    fun should_parseMinimalCustomer_when_onlyRequiredFields() {
        val json = """{"id":99,"company_name":"네이버","industry":"IT","last_interaction_date":"2025-01-01","total_conversations":0}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(99L, dto.id)
        assertEquals("네이버", dto.companyName)
        assertNull(dto.contactName)
        assertNull(dto.summary)
    }

    // Test 32
    @Test
    fun should_parseMinimalCard_when_onlyRequiredFields() {
        val json = """{"id":1,"conversation_id":10,"customer_id":5,"title":"미팅","date":"2025-03-01","conversation_type":"CUSTOMER_MEETING","summary":"요약","sentiment":"POSITIVE","sentiment_score":0.8,"keywords":[],"key_statements":[],"price_commitments":[],"action_items":[],"predicted_questions":[],"related_knowledge":[]}"""
        val dto = gson.fromJson(json, CardDto::class.java)
        assertEquals(1L, dto.id)
        assertTrue(dto.keywords.isEmpty())
        assertTrue(dto.actionItems.isEmpty())
    }

    // Test 33
    @Test
    fun should_parseEmptyObject_when_noFields() {
        val json = """{}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(0L, dto.id)
        assertNull(dto.companyName)
        assertNull(dto.contactName)
        assertNull(dto.summary)
    }

    // Test 34
    @Test
    fun should_handleMixedNullAndPresent_when_partialData() {
        val json = """{"id":7,"company_name":"SK","contact_name":null,"industry":"에너지","last_interaction_date":"2025-02-20","total_conversations":4,"summary":"일부 요약"}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertEquals(7L, dto.id)
        assertNull(dto.contactName)
        assertEquals("일부 요약", dto.summary)
    }

    // Test 35
    @Test
    fun should_handleAllNullable_when_allNull() {
        val json = """{"id":1,"company_name":"테스트","industry":"IT","last_interaction_date":"2025-01-01","total_conversations":0,"contact_name":null,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        assertNull(dto.contactName)
        assertNull(dto.summary)
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun buildCardJson(
        keywords: String = "[]",
        keyStatements: String = "[]",
        priceCommitments: String = "[]",
        actionItems: String = "[]",
        predictedQuestions: String = "[]",
        relatedKnowledge: String = "[]"
    ): String {
        return """{"id":1,"conversation_id":1,"customer_id":1,"title":"테스트","date":"2025-03-01","conversation_type":"CUSTOMER_MEETING","summary":"요약","sentiment":"POSITIVE","sentiment_score":0.8,"keywords":$keywords,"key_statements":$keyStatements,"price_commitments":$priceCommitments,"action_items":$actionItems,"predicted_questions":$predictedQuestions,"related_knowledge":$relatedKnowledge}"""
    }

    private fun buildCardJsonWithoutField(fieldName: String): String {
        val fields = mutableListOf(
            """"id":1""",
            """"conversation_id":1""",
            """"customer_id":1""",
            """"title":"테스트"""",
            """"date":"2025-03-01"""",
            """"conversation_type":"CUSTOMER_MEETING"""",
            """"summary":"요약"""",
            """"sentiment":"POSITIVE"""",
            """"sentiment_score":0.8"""
        )
        val listFields = mapOf(
            "keywords" to """"keywords":[]""",
            "key_statements" to """"key_statements":[]""",
            "price_commitments" to """"price_commitments":[]""",
            "action_items" to """"action_items":[]""",
            "predicted_questions" to """"predicted_questions":[]""",
            "related_knowledge" to """"related_knowledge":[]"""
        )
        listFields.forEach { (key, value) ->
            if (key != fieldName) fields.add(value)
        }
        return "{${fields.joinToString(",")}}"
    }
}
