package com.ralphthon.app.data.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
import com.ralphthon.app.data.mapper.ActionItemMapper
import com.ralphthon.app.data.mapper.CardMapper
import com.ralphthon.app.data.mapper.CustomerMapper
import com.ralphthon.app.data.mapper.PredictedQuestionMapper
import com.ralphthon.app.data.mapper.PriceCommitmentMapper
import com.ralphthon.app.data.mapper.SearchResponseMapper
import com.ralphthon.app.data.mock.MockJsonProvider
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.Sentiment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonPayloadTest {

    private val gson = Gson()

    // ---- Customer payloads (5) ----

    @Test
    fun should_parseFullCustomer_when_realisticJson() {
        val json = """{"id":1,"company_name":"삼성전자","contact_name":"김부장","industry":"전자","last_interaction_date":"2025-03-15","total_conversations":15,"summary":"Physical AI 도입 논의 중"}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        val domain = CustomerMapper.toDomain(dto)
        assertEquals(1L, domain.id)
        assertEquals("삼성전자", domain.companyName)
        assertEquals("김부장", domain.contactName)
        assertEquals("전자", domain.industry)
        assertEquals("2025-03-15", domain.lastInteractionDate)
        assertEquals(15, domain.totalConversations)
        assertEquals("Physical AI 도입 논의 중", domain.summary)
    }

    @Test
    fun should_parseCustomerList_when_arrayJson() {
        val json = """[
            {"id":1,"company_name":"삼성전자","contact_name":"김민수","industry":"반도체","last_interaction_date":"2025-03-01","total_conversations":10,"summary":null},
            {"id":2,"company_name":"LG전자","contact_name":"이정훈","industry":"가전","last_interaction_date":"2025-03-02","total_conversations":5,"summary":"계약 논의"},
            {"id":3,"company_name":"현대자동차","contact_name":"박서연","industry":"자동차","last_interaction_date":"2025-03-03","total_conversations":8,"summary":"EV 전환"}
        ]"""
        val listType = object : TypeToken<List<CustomerDto>>() {}.type
        val dtos: List<CustomerDto> = gson.fromJson(json, listType)
        val customers = CustomerMapper.toDomainList(dtos)
        assertEquals(3, customers.size)
        assertEquals("삼성전자", customers[0].companyName)
        assertEquals("LG전자", customers[1].companyName)
        assertEquals("현대자동차", customers[2].companyName)
    }

    @Test
    fun should_parseKoreanCustomer_when_koreanNames() {
        val json = """{"id":5,"company_name":"네이버 주식회사","contact_name":"정유진 팀장","industry":"IT","last_interaction_date":"2025-02-20","total_conversations":12,"summary":"AI 검색 고도화 논의"}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("네이버 주식회사", domain.companyName)
        assertEquals("정유진 팀장", domain.contactName)
        assertEquals("AI 검색 고도화 논의", domain.summary)
    }

    @Test
    fun should_parseCustomerWithNulls_when_optionalFieldsNull() {
        val json = """{"id":7,"company_name":"토스","contact_name":null,"industry":"핀테크","last_interaction_date":"2025-01-10","total_conversations":3,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        val domain = CustomerMapper.toDomain(dto)
        assertEquals(7L, domain.id)
        assertNull(domain.contactName)
        assertNull(domain.summary)
    }

    @Test
    fun should_mapCustomerDefaults_when_blankCompany() {
        val json = """{"id":99,"company_name":"","contact_name":"홍길동","industry":"기타","last_interaction_date":"2025-01-01","total_conversations":0,"summary":null}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("회사 미등록", domain.companyName)
    }

    // ---- Card payloads (10) ----

    @Test
    fun should_parseFullCard_when_realisticJson() {
        val json = buildFullCardJson(
            id = 1L, conversationId = 10L, customerId = 1L,
            title = "삼성전자 3Q 미팅", date = "2025-03-15",
            conversationType = "CUSTOMER_MEETING", summary = "반도체 공급 논의",
            sentiment = "POSITIVE", sentimentScore = 0.85f
        )
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(1L, domain.id)
        assertEquals("삼성전자 3Q 미팅", domain.title)
        assertEquals(ConversationType.CUSTOMER_MEETING, domain.conversationType)
        assertEquals(Sentiment.POSITIVE, domain.sentiment)
        assertEquals(0.85f, domain.sentimentScore)
    }

    @Test
    fun should_parseCardSentiment_when_positive() {
        val json = buildFullCardJson(sentiment = "POSITIVE", sentimentScore = 0.9f)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(Sentiment.POSITIVE, domain.sentiment)
    }

    @Test
    fun should_parseCardSentiment_when_negative() {
        val json = buildFullCardJson(sentiment = "NEGATIVE", sentimentScore = 0.2f)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(Sentiment.NEGATIVE, domain.sentiment)
    }

    @Test
    fun should_parseCardSentiment_when_neutral() {
        val json = buildFullCardJson(sentiment = "NEUTRAL", sentimentScore = 0.5f)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(Sentiment.NEUTRAL, domain.sentiment)
    }

    @Test
    fun should_parseCardKeywords_when_multipleCategories() {
        val keywordsJson = """[
            {"text":"가격","category":"PRICE","frequency":5},
            {"text":"납기","category":"DELIVERY","frequency":3},
            {"text":"품질","category":"QUALITY","frequency":4}
        ]"""
        val json = buildFullCardJson(keywordsJson = keywordsJson)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(3, domain.keywords.size)
        val texts = domain.keywords.map { it.text }
        assertTrue(texts.contains("가격"))
        assertTrue(texts.contains("납기"))
        assertTrue(texts.contains("품질"))
    }

    @Test
    fun should_parseCardStatements_when_multiSpeaker() {
        val statementsJson = """[
            {"id":1,"speaker":"김부장","text":"가격 인하 요청","timestamp":"00:05:00","sentiment":"NEGATIVE","is_important":true},
            {"id":2,"speaker":"영업팀장","text":"검토 후 답변 드리겠습니다","timestamp":"00:05:30","sentiment":"NEUTRAL","is_important":false}
        ]"""
        val json = buildFullCardJson(statementsJson = statementsJson)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(2, domain.keyStatements.size)
        assertEquals("김부장", domain.keyStatements[0].speaker)
        assertEquals("영업팀장", domain.keyStatements[1].speaker)
        assertTrue(domain.keyStatements[0].isImportant)
    }

    @Test
    fun should_parseCardPriceCommitments_when_present() {
        val priceJson = """[
            {"id":1,"amount":150000000.0,"currency":"KRW","condition":"3개월 이상 계약 시","mentioned_at":"00:12:00"},
            {"id":2,"amount":120000.0,"currency":"USD","condition":"FOB 기준","mentioned_at":"00:18:00"}
        ]"""
        val json = buildFullCardJson(priceJson = priceJson)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(2, domain.priceCommitments.size)
        assertEquals(150000000.0, domain.priceCommitments[0].amount)
        assertEquals("KRW", domain.priceCommitments[0].currency)
        assertEquals("USD", domain.priceCommitments[1].currency)
    }

    @Test
    fun should_parseCardActionItems_when_mixed() {
        val actionsJson = """[
            {"id":1,"description":"견적서 재발송","assignee":"영업팀","due_date":"2025-03-20","status":"OPEN"},
            {"id":2,"description":"이전 계약서 확인","assignee":"법무팀","due_date":null,"status":"DONE"}
        ]"""
        val json = buildFullCardJson(actionsJson = actionsJson)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(2, domain.actionItems.size)
        assertEquals(ActionItemStatus.OPEN, domain.actionItems[0].status)
        assertEquals(ActionItemStatus.DONE, domain.actionItems[1].status)
        assertNull(domain.actionItems[1].dueDate)
    }

    @Test
    fun should_parseCardPredictedQuestions_when_present() {
        val questionsJson = """[
            {"id":1,"question":"납기 일정은 언제인가요?","suggested_answer":"3주 이내 가능합니다","related_knowledge":["납기 SLA","물류 현황"],"confidence":0.88},
            {"id":2,"question":"최소 주문 수량은?","suggested_answer":"MOQ는 500개입니다","related_knowledge":["가격표"],"confidence":0.75}
        ]"""
        val json = buildFullCardJson(questionsJson = questionsJson)
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(2, domain.predictedQuestions.size)
        assertEquals(0.88f, domain.predictedQuestions[0].confidence)
        assertEquals(2, domain.predictedQuestions[0].relatedKnowledge.size)
    }

    @Test
    fun should_parseCardConversationType_when_customerMeeting() {
        val json = buildFullCardJson(conversationType = "CUSTOMER_MEETING")
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(ConversationType.CUSTOMER_MEETING, domain.conversationType)
    }

    // ---- Search payloads (5) ----

    @Test
    fun should_parseSearchResponse_when_realisticJson() {
        val json = """{"query":"삼성","total_count":2,"results":[
            {"id":1,"type":"CONVERSATION","title":"삼성전자 3Q 미팅","snippet":"삼성전자 반도체 공급 논의","highlight_ranges":[[0,3]],"source_id":10,"relevance_score":0.95},
            {"id":2,"type":"CUSTOMER","title":"삼성전자","snippet":"삼성전자 고객사","highlight_ranges":[[0,3]],"source_id":1,"relevance_score":0.88}
        ]}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        val results = SearchResponseMapper.toDomainList(dto)
        assertEquals(2, results.size)
        assertEquals("삼성전자 3Q 미팅", results[0].title)
        assertEquals(0.95f, results[0].relevanceScore)
    }

    @Test
    fun should_parseSearchHighlights_when_present() {
        val json = """{"query":"납기","total_count":1,"results":[
            {"id":1,"type":"CONVERSATION","title":"납기 논의","snippet":"납기 일정 확인 필요","highlight_ranges":[[0,2],[5,7]],"source_id":5,"relevance_score":0.9}
        ]}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        val results = SearchResponseMapper.toDomainList(dto)
        assertEquals(2, results[0].highlightRanges.size)
        assertEquals(IntRange(0, 2), results[0].highlightRanges[0])
        assertEquals(IntRange(5, 7), results[0].highlightRanges[1])
    }

    @Test
    fun should_parseSearchEmpty_when_noResults() {
        val json = """{"query":"없는검색어","total_count":0,"results":[]}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        val results = SearchResponseMapper.toDomainList(dto)
        assertTrue(results.isEmpty())
    }

    @Test
    fun should_parseSearchMultiType_when_mixedTypes() {
        val json = """{"query":"가격","total_count":2,"results":[
            {"id":1,"type":"CONVERSATION","title":"가격 협의 미팅","snippet":"가격 인하 요청","highlight_ranges":[[0,2]],"source_id":10,"relevance_score":0.92},
            {"id":2,"type":"CUSTOMER","title":"가격 민감 고객사","snippet":"가격 이슈 반복","highlight_ranges":[[0,2]],"source_id":3,"relevance_score":0.80}
        ]}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        val results = SearchResponseMapper.toDomainList(dto)
        val types = results.map { it.type }
        assertTrue(types.contains("CONVERSATION"))
        assertTrue(types.contains("CUSTOMER"))
    }

    @Test
    fun should_parseSearchKorean_when_koreanSnippets() {
        val json = """{"query":"계약","total_count":1,"results":[
            {"id":1,"type":"CONVERSATION","title":"계약 조건 협의","snippet":"장기 계약 조건에 대한 논의가 진행되었습니다","highlight_ranges":[[3,5]],"source_id":20,"relevance_score":0.87}
        ]}"""
        val dto = gson.fromJson(json, SearchResponseDto::class.java)
        val results = SearchResponseMapper.toDomainList(dto)
        assertEquals("장기 계약 조건에 대한 논의가 진행되었습니다", results[0].snippet)
    }

    // ---- Upload payloads (3) ----

    @Test
    fun should_parseUploadResponse_when_success() {
        val json = """{"conversation_id":42,"status":"SUCCESS","message":"업로드 완료"}"""
        val dto = gson.fromJson(json, UploadResponseDto::class.java)
        assertEquals(42L, dto.conversationId)
        assertEquals("SUCCESS", dto.status)
        assertEquals("업로드 완료", dto.message)
    }

    @Test
    fun should_parseUploadResponse_when_processing() {
        val json = """{"conversation_id":43,"status":"PROCESSING","message":"분석 중입니다"}"""
        val dto = gson.fromJson(json, UploadResponseDto::class.java)
        assertEquals("PROCESSING", dto.status)
        assertEquals(43L, dto.conversationId)
    }

    @Test
    fun should_parseUploadResponse_when_error() {
        val json = """{"conversation_id":0,"status":"ERROR","message":"파일 형식 오류"}"""
        val dto = gson.fromJson(json, UploadResponseDto::class.java)
        assertEquals("ERROR", dto.status)
        assertEquals("파일 형식 오류", dto.message)
    }

    // ---- Error payloads (2) ----

    @Test
    fun should_parseError_when_withDetails() {
        val json = """{"code":404,"message":"고객을 찾을 수 없습니다","details":"id=999 not found"}"""
        val dto = gson.fromJson(json, ErrorDto::class.java)
        assertEquals(404, dto.code)
        assertEquals("고객을 찾을 수 없습니다", dto.message)
        assertEquals("id=999 not found", dto.details)
    }

    @Test
    fun should_parseError_when_withoutDetails() {
        val json = """{"code":500,"message":"서버 오류","details":null}"""
        val dto = gson.fromJson(json, ErrorDto::class.java)
        assertEquals(500, dto.code)
        assertNull(dto.details)
    }

    // ---- Integration (5) ----

    @Test
    fun should_roundTrip_when_customerDtoToDomainAndBack() {
        val json = """{"id":1,"company_name":"삼성전자","contact_name":"김민수","industry":"반도체","last_interaction_date":"2025-03-15","total_conversations":20,"summary":"반도체 협력"}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        val domain = CustomerMapper.toDomain(dto)
        val backDto = CustomerMapper.toDto(domain)
        assertEquals(dto.id, backDto.id)
        assertEquals(dto.companyName, backDto.companyName)
        assertEquals(dto.contactName, backDto.contactName)
        assertEquals(dto.industry, backDto.industry)
        assertEquals(dto.totalConversations, backDto.totalConversations)
    }

    @Test
    fun should_parseNestedDeep_when_cardWithAllNesting() {
        val priceJson = """[{"id":1,"amount":50000000.0,"currency":"KRW","condition":"연간 계약","mentioned_at":"00:10:00"}]"""
        val actionsJson = """[{"id":1,"description":"계약서 검토","assignee":"법무팀","due_date":"2025-04-01","status":"OPEN"}]"""
        val questionsJson = """[{"id":1,"question":"보증 기간은?","suggested_answer":"1년 보증","related_knowledge":["품질 보증 정책"],"confidence":0.9}]"""
        val statementsJson = """[{"id":1,"speaker":"고객","text":"가격이 높습니다","timestamp":"00:08:00","sentiment":"NEGATIVE","is_important":true}]"""
        val keywordsJson = """[{"text":"보증","category":"QUALITY","frequency":2}]"""
        val knowledgeJson = """[{"id":1,"title":"품질 보증 정책","content":"1년 무상 AS","category":"POLICY","relevance_score":0.95}]"""
        val json = buildFullCardJson(
            priceJson = priceJson,
            actionsJson = actionsJson,
            questionsJson = questionsJson,
            statementsJson = statementsJson,
            keywordsJson = keywordsJson,
            knowledgeJson = knowledgeJson
        )
        val dto = gson.fromJson(json, CardDto::class.java)
        val domain = CardMapper.toDomain(dto)
        assertEquals(1, domain.priceCommitments.size)
        assertEquals(1, domain.actionItems.size)
        assertEquals(1, domain.predictedQuestions.size)
        assertEquals(1, domain.keyStatements.size)
        assertEquals(1, domain.keywords.size)
        assertEquals(1, domain.relatedKnowledge.size)
        assertTrue(domain.keyStatements[0].isImportant)
        assertEquals("보증 기간은?", domain.predictedQuestions[0].question)
    }

    @Test
    fun should_handleLargePayload_when_50Cards() {
        val cards = (1..50).map { i ->
            buildFullCardJsonObject(id = i.toLong(), customerId = 1L, conversationId = i.toLong())
        }
        val json = "[${cards.joinToString(",")}]"
        val listType = object : TypeToken<List<CardDto>>() {}.type
        val dtos: List<CardDto> = gson.fromJson(json, listType)
        val domains = CardMapper.toDomainList(dtos)
        assertEquals(50, domains.size)
        assertEquals(1L, domains[0].id)
        assertEquals(50L, domains[49].id)
    }

    @Test
    fun should_handleSpecialChars_when_jsonHasEscaped() {
        val json = """{"id":1,"company_name":"O'Brien & Sons","contact_name":"John \"The Boss\" Smith","industry":"IT","last_interaction_date":"2025-03-01","total_conversations":3,"summary":"Line1\nLine2\tTabbed"}"""
        val dto = gson.fromJson(json, CustomerDto::class.java)
        val domain = CustomerMapper.toDomain(dto)
        assertEquals("O'Brien & Sons", domain.companyName)
        assertTrue(domain.contactName!!.contains("The Boss"))
        assertTrue(domain.summary!!.contains("\n"))
    }

    @Test
    fun should_handleRealisticMockData_when_mockJsonProviderUsed() {
        val json = MockJsonProvider.customersJson()
        val listType = object : TypeToken<List<Customer>>() {}.type
        val customers: List<Customer> = gson.fromJson(json, listType)
        assertTrue(customers.isNotEmpty())
        customers.forEach { customer ->
            assertNotNull(customer.id)
            assertTrue(customer.companyName.isNotBlank())
            assertTrue(customer.industry.isNotBlank())
        }
    }

    // ---- Helpers ----

    private fun buildFullCardJson(
        id: Long = 1L,
        conversationId: Long = 10L,
        customerId: Long = 1L,
        title: String = "테스트 미팅",
        date: String = "2025-03-15",
        conversationType: String = "CUSTOMER_MEETING",
        summary: String = "요약",
        sentiment: String = "POSITIVE",
        sentimentScore: Float = 0.8f,
        keywordsJson: String = "[]",
        statementsJson: String = "[]",
        priceJson: String = "[]",
        actionsJson: String = "[]",
        questionsJson: String = "[]",
        knowledgeJson: String = "[]"
    ): String {
        return """{"id":$id,"conversation_id":$conversationId,"customer_id":$customerId,"title":"$title","date":"$date","conversation_type":"$conversationType","summary":"$summary","sentiment":"$sentiment","sentiment_score":$sentimentScore,"keywords":$keywordsJson,"key_statements":$statementsJson,"price_commitments":$priceJson,"action_items":$actionsJson,"predicted_questions":$questionsJson,"related_knowledge":$knowledgeJson}"""
    }

    private fun buildFullCardJsonObject(
        id: Long = 1L,
        conversationId: Long = 10L,
        customerId: Long = 1L
    ): String {
        return buildFullCardJson(
            id = id,
            conversationId = conversationId,
            customerId = customerId,
            title = "미팅 $id",
            summary = "요약 $id"
        )
    }
}
