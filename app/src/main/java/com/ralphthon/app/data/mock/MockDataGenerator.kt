package com.ralphthon.app.data.mock

import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.Conversation
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.model.KeyStatement
import com.ralphthon.app.domain.model.Keyword
import com.ralphthon.app.domain.model.KeywordCategory
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.domain.model.Sentiment

object MockDataGenerator {

    private val customerData = listOf(
        Triple("삼성전자", "김민수", "반도체"),
        Triple("LG전자", "이정훈", "가전"),
        Triple("현대자동차", "박서연", "자동차"),
        Triple("SK하이닉스", "최동현", "반도체"),
        Triple("네이버", "정유진", "IT"),
        Triple("카카오", "한지민", "IT"),
        Triple("쿠팡", "오승우", "이커머스"),
        Triple("배달의민족", "윤하늘", "푸드테크"),
        Triple("토스", "신재호", "핀테크"),
        Triple("KT", "강미래", "통신")
    )

    private val meetingTitles = listOf(
        "Physical AI 로봇 솔루션 초기 미팅",
        "자율주행 로봇 데모 시연",
        "AI 비전 시스템 기술 검토",
        "로봇 팔 자동화 PoC 논의",
        "스마트 팩토리 로봇 도입 제안",
        "물류 자동화 로봇 가격 협상",
        "AI 품질검사 시스템 계약 논의",
        "로봇 유지보수 서비스 계약",
        "차세대 로봇 플랫폼 로드맵 공유",
        "AI 로봇 안전 인증 논의",
        "로봇 시뮬레이션 환경 구축 협의",
        "엣지 AI 프로세서 도입 검토",
        "로봇 원격 제어 시스템 시연",
        "AI 기반 예지보전 솔루션 소개",
        "협동 로봇 라인 투입 계획",
        "디지털 트윈 연동 방안 논의",
        "AI 로봇 교육 프로그램 제안",
        "로봇 OS 업그레이드 일정 협의",
        "센서 퓨전 기술 파트너십 논의",
        "로봇 Fleet 관리 플랫폼 소개"
    )

    private val internalTitles = listOf(
        "사내 전략 회의 - 고객사 대응 방안",
        "기술팀 리뷰 - PoC 결과 분석",
        "영업팀 주간 미팅 - 파이프라인 리뷰",
        "가격 정책 내부 논의",
        "경쟁사 동향 분석 회의",
        "제품 로드맵 내부 공유",
        "고객 피드백 종합 분석",
        "기술 지원 이슈 리뷰",
        "분기 매출 목표 조정 회의",
        "파트너십 전략 수립 회의"
    )

    private val summaries = listOf(
        "고객사에서 Physical AI 로봇의 생산라인 적용에 높은 관심을 보임. 특히 비전 기반 품질검사 모듈에 대해 구체적인 PoC 일정을 요청함.",
        "자율주행 로봇의 물류센터 적용 데모를 진행. 경로 최적화 알고리즘의 성능에 만족하며 2단계 확대 도입을 검토 중.",
        "AI 로봇 팔의 정밀도 테스트 결과를 공유. 0.01mm 오차 범위 달성으로 반도체 공정 적용 가능성 확인.",
        "스마트 팩토리 전환 프로젝트에서 로봇 도입 우선순위를 논의. 조립 공정부터 단계적 도입 합의.",
        "물류 자동화 로봇 50대 도입 건에 대한 가격 협상 진행. 대량 구매 할인 조건으로 대당 15% 할인 제안.",
        "AI 품질검사 시스템의 불량률 감소 효과를 데이터로 제시. 기존 대비 40% 불량 검출률 향상 확인.",
        "로봇 유지보수 연간 계약 조건 협의. 24시간 원격 모니터링 및 4시간 내 현장 출동 SLA 합의.",
        "차세대 로봇 플랫폼의 AI 학습 기능 시연. 고객사 특화 모델 훈련 서비스에 높은 관심.",
        "협동 로봇의 안전 인증(ISO 10218, ISO/TS 15066) 획득 현황 공유. 추가 인증 필요 사항 논의.",
        "엣지 AI 프로세서를 통한 실시간 처리 성능 개선 방안 논의. 지연시간 50ms 이하 목표 설정.",
        "디지털 트윈 환경에서의 로봇 시뮬레이션 데모. 실제 환경 대비 95% 정확도 달성.",
        "센서 퓨전 기술을 활용한 환경 인식 정확도 향상 방안 협의. LiDAR + 카메라 통합 솔루션 제안.",
        "로봇 Fleet 관리 플랫폼의 실시간 대시보드 시연. 100대 이상 동시 관리 가능성 확인.",
        "AI 기반 예지보전 솔루션으로 장비 다운타임 30% 감소 효과 제시. 파일럿 프로젝트 제안.",
        "로봇 원격 제어 시스템의 5G 기반 저지연 통신 테스트 결과 공유. 10ms 이하 지연 달성.",
        "내부 기술 검토 결과 고객 요구사항 충족 가능. 추가 개발 2주 소요 예상.",
        "경쟁사 대비 가격 경쟁력 분석 완료. 기능 대비 20% 저렴한 가격 포지셔닝 가능.",
        "고객 피드백 기반 UX 개선 사항 도출. 대시보드 직관성 향상 및 알림 기능 추가 예정.",
        "분기 매출 목표 대비 85% 달성 중. 신규 2건 수주 시 목표 초과 달성 전망.",
        "파트너 에코시스템 확장 전략 수립. 시스템 인테그레이터 3사와 협력 MOU 추진."
    )

    private val keywordPool = listOf(
        Keyword("Physical AI", KeywordCategory.PRODUCT, 5),
        Keyword("로봇 자동화", KeywordCategory.PRODUCT, 4),
        Keyword("가격 협상", KeywordCategory.PRICE, 3),
        Keyword("비전 시스템", KeywordCategory.TECHNICAL, 3),
        Keyword("경쟁사 분석", KeywordCategory.COMPETITOR, 2),
        Keyword("스마트 팩토리", KeywordCategory.BUSINESS, 4),
        Keyword("자율주행", KeywordCategory.TECHNICAL, 3),
        Keyword("PoC", KeywordCategory.BUSINESS, 2),
        Keyword("ROI", KeywordCategory.BUSINESS, 3),
        Keyword("안전 인증", KeywordCategory.TECHNICAL, 2),
        Keyword("엣지 AI", KeywordCategory.TECHNICAL, 2),
        Keyword("디지털 트윈", KeywordCategory.PRODUCT, 3),
        Keyword("센서 퓨전", KeywordCategory.TECHNICAL, 2),
        Keyword("Fleet 관리", KeywordCategory.PRODUCT, 2),
        Keyword("예지보전", KeywordCategory.PRODUCT, 2),
        Keyword("5G 통신", KeywordCategory.TECHNICAL, 1),
        Keyword("대량 구매", KeywordCategory.PRICE, 2),
        Keyword("SLA", KeywordCategory.BUSINESS, 2),
        Keyword("유지보수", KeywordCategory.BUSINESS, 3),
        Keyword("품질검사", KeywordCategory.PRODUCT, 4)
    )

    private val speakers = listOf("김민수", "이정훈", "박서연", "최동현", "정유진", "한지민", "오승우", "윤하늘", "신재호", "강미래", "영업팀 김대리", "기술팀 박과장", "CTO 이사")

    private val statementTexts = listOf(
        "이번 PoC 결과가 기대 이상이었습니다. 본격 도입을 검토하겠습니다.",
        "가격이 좀 더 경쟁력 있으면 좋겠는데, 대량 구매 시 할인이 가능한가요?",
        "로봇의 안전성에 대해 좀 더 자세한 데이터가 필요합니다.",
        "현재 라인에 바로 투입 가능한 모델이 있나요?",
        "유지보수 비용이 총 소유비용에서 큰 비중을 차지하는데, 연간 계약 조건은?",
        "경쟁사 제품 대비 어떤 차별점이 있는지 구체적으로 설명해주세요.",
        "AI 학습 데이터는 우리 공장 환경에 맞춤화가 가능한가요?",
        "납기 일정이 중요합니다. 계약 후 몇 주 내 설치가 가능한지요?",
        "우리 기존 MES 시스템과의 연동은 어떻게 되나요?",
        "ROI를 6개월 내 달성할 수 있다는 근거를 보여주세요.",
        "시범 운영 기간을 3개월로 제안드립니다.",
        "품질검사 정확도 99.5% 이상을 보장할 수 있나요?",
        "원격 모니터링 기능이 해외 공장에서도 사용 가능한가요?",
        "기술 지원 인력 상주가 필요한 기간은 얼마나 되나요?",
        "예산은 올해 안에 집행해야 해서, 빠른 계약 진행이 필요합니다."
    )

    private val knowledgeArticles = listOf(
        KnowledgeArticle(1L, "Physical AI 로봇 제품 라인업", "당사의 Physical AI 로봇은 산업용, 물류용, 협동 로봇 3개 라인으로 구성", "제품", 0.95f),
        KnowledgeArticle(2L, "로봇 자동화 ROI 분석 가이드", "평균 18개월 내 투자비 회수, 인건비 40% 절감 효과", "영업", 0.88f),
        KnowledgeArticle(3L, "경쟁사 비교 분석표", "주요 경쟁사 대비 가격, 성능, 서비스 비교 데이터", "경쟁", 0.82f),
        KnowledgeArticle(4L, "안전 인증 현황", "ISO 10218, ISO/TS 15066, CE, KC 인증 보유", "인증", 0.90f),
        KnowledgeArticle(5L, "스마트 팩토리 구축 사례집", "삼성전자, 현대자동차 등 주요 고객 구축 사례", "사례", 0.85f),
        KnowledgeArticle(6L, "AI 비전 시스템 기술 백서", "딥러닝 기반 품질검사 시스템의 기술 사양 및 정확도 데이터", "기술", 0.92f),
        KnowledgeArticle(7L, "가격 정책 가이드라인", "수량별 할인율, 연간 계약 할인, 번들 할인 정책", "가격", 0.78f),
        KnowledgeArticle(8L, "유지보수 SLA 기준표", "골드/실버/브론즈 등급별 SLA 조건 및 비용", "서비스", 0.80f)
    )

    private val predictedQuestionPool = listOf(
        PredictedQuestion(1L, "로봇 도입 시 기존 작업자 재배치 방안은?", "단계적 전환 프로그램을 통해 기존 작업자를 로봇 운영/관리 인력으로 재교육합니다.", listOf("재배치 프로그램", "교육 과정"), 0.85f),
        PredictedQuestion(2L, "로봇 장애 시 생산라인 중단 위험은 어떻게 대응하나요?", "이중화 구성과 24시간 원격 모니터링으로 장애 발생 시 4시간 내 복구를 보장합니다.", listOf("SLA", "이중화"), 0.90f),
        PredictedQuestion(3L, "경쟁사 대비 가격이 높은 이유는?", "AI 학습 기반 자가 최적화 기능과 예지보전 기능이 포함된 토탈 솔루션으로, TCO 기준으로는 20% 저렴합니다.", listOf("TCO 분석", "차별점"), 0.82f),
        PredictedQuestion(4L, "해외 공장 도입 시 현지 지원은 가능한가요?", "글로벌 파트너 네트워크를 통해 주요 국가에서 현지 기술 지원이 가능합니다.", listOf("글로벌 지원", "파트너"), 0.75f),
        PredictedQuestion(5L, "데이터 보안은 어떻게 보장되나요?", "온프레미스 엣지 처리로 데이터가 외부로 전송되지 않으며, ISO 27001 인증을 보유하고 있습니다.", listOf("보안 인증", "엣지 처리"), 0.88f),
        PredictedQuestion(6L, "추가 로봇 확장 시 비용 구조는?", "초기 도입 후 추가 로봇은 대당 10% 할인이 적용되며, Fleet 라이선스로 관리 비용을 절감할 수 있습니다.", listOf("확장 할인", "Fleet 라이선스"), 0.80f)
    )

    private var cachedCustomers: List<Customer>? = null
    private var cachedConversations: List<Conversation>? = null
    private var cachedCards: List<ContextCard>? = null

    fun generateCustomers(): List<Customer> {
        cachedCustomers?.let { return it }
        val customers = customerData.mapIndexed { index, (company, contact, industry) ->
            Customer(
                id = (index + 1).toLong(),
                companyName = company,
                contactName = contact,
                industry = industry,
                lastInteractionDate = "2026-03-${(10 + index).toString().padStart(2, '0')}",
                totalConversations = 20,
                summary = "Physical AI 로봇 솔루션 도입 검토 중인 $company 담당자 $contact"
            )
        }
        cachedCustomers = customers
        return customers
    }

    fun generateConversations(): List<Conversation> {
        cachedConversations?.let { return it }
        val customers = generateCustomers()
        val conversations = mutableListOf<Conversation>()
        var conversationId = 1L
        var statementId = 1L
        var priceId = 1L
        var actionId = 1L

        for (customer in customers) {
            val count = 20
            for (i in 0 until count) {
                val isCustomerMeeting = i % 3 != 2
                val type = if (isCustomerMeeting) ConversationType.CUSTOMER_MEETING else ConversationType.INTERNAL_MEETING
                val title = if (isCustomerMeeting) meetingTitles[i % meetingTitles.size] else internalTitles[i % internalTitles.size]
                val sentiment = when (i % 3) {
                    0 -> Sentiment.POSITIVE
                    1 -> Sentiment.NEUTRAL
                    else -> Sentiment.NEGATIVE
                }
                val day = (1 + i).toString().padStart(2, '0')
                val month = if (i < 10) "02" else "03"

                val keywords = listOf(
                    keywordPool[i % keywordPool.size],
                    keywordPool[(i + 3) % keywordPool.size]
                )

                val statements = listOf(
                    KeyStatement(
                        id = statementId++,
                        speaker = customer.contactName ?: customer.companyName,
                        text = statementTexts[i % statementTexts.size],
                        timestamp = "${(i * 3).toString().padStart(2, '0')}:${(i * 7 % 60).toString().padStart(2, '0')}",
                        sentiment = sentiment,
                        isImportant = i % 4 == 0
                    ),
                    KeyStatement(
                        id = statementId++,
                        speaker = "영업팀 김대리",
                        text = "네, 해당 부분은 저희가 충분히 지원 가능합니다.",
                        timestamp = "${((i * 3) + 1).toString().padStart(2, '0')}:${((i * 7 + 15) % 60).toString().padStart(2, '0')}",
                        sentiment = Sentiment.POSITIVE,
                        isImportant = false
                    )
                )

                val priceCommitments = if (i % 5 == 0) {
                    listOf(
                        PriceCommitment(
                            id = priceId++,
                            amount = (i + 1) * 5_0000_0000.0,
                            currency = "KRW",
                            condition = "연간 유지보수 계약 포함 시",
                            mentionedAt = "${(i * 3).toString().padStart(2, '0')}:30"
                        )
                    )
                } else emptyList()

                val actionItems = if (i % 3 == 0) {
                    listOf(
                        ActionItem(
                            id = actionId++,
                            description = "PoC 환경 세팅 및 테스트 일정 확정",
                            assignee = "기술팀 박과장",
                            dueDate = "2026-04-${(10 + i).toString().padStart(2, '0')}",
                            status = if (i < 10) ActionItemStatus.DONE else ActionItemStatus.OPEN
                        )
                    )
                } else emptyList()

                val predictedQuestions = if (i % 4 == 0) {
                    listOf(predictedQuestionPool[i % predictedQuestionPool.size])
                } else emptyList()

                conversations.add(
                    Conversation(
                        id = conversationId++,
                        customerId = customer.id,
                        title = "${customer.companyName} - $title",
                        date = "2026-$month-$day",
                        type = type,
                        duration = 30 + (i * 5) % 60,
                        summary = summaries[i % summaries.size],
                        sentiment = sentiment,
                        keywords = keywords,
                        keyStatements = statements,
                        priceCommitments = priceCommitments,
                        actionItems = actionItems,
                        predictedQuestions = predictedQuestions
                    )
                )
            }
        }
        cachedConversations = conversations
        return conversations
    }

    fun generateContextCards(): List<ContextCard> {
        cachedCards?.let { return it }
        val conversations = generateConversations()
        val cards = conversations.mapIndexed { index, conv ->
            val sentimentScore = when (conv.sentiment) {
                Sentiment.POSITIVE -> 0.7f + (index % 3) * 0.1f
                Sentiment.NEGATIVE -> 0.1f + (index % 3) * 0.1f
                Sentiment.NEUTRAL -> 0.4f + (index % 3) * 0.1f
            }
            ContextCard(
                id = conv.id,
                conversationId = conv.id,
                customerId = conv.customerId,
                title = conv.title,
                date = conv.date,
                conversationType = conv.type,
                summary = conv.summary,
                sentiment = conv.sentiment,
                sentimentScore = sentimentScore,
                keywords = conv.keywords,
                keyStatements = conv.keyStatements,
                priceCommitments = conv.priceCommitments,
                actionItems = conv.actionItems,
                predictedQuestions = conv.predictedQuestions,
                relatedKnowledge = listOf(
                    knowledgeArticles[index % knowledgeArticles.size],
                    knowledgeArticles[(index + 1) % knowledgeArticles.size]
                )
            )
        }
        cachedCards = cards
        return cards
    }

    fun getConversationsByCustomerId(customerId: Long): List<Conversation> {
        return generateConversations().filter { it.customerId == customerId }
    }

    fun getCardsByCustomerId(customerId: Long): List<ContextCard> {
        return generateContextCards().filter { it.customerId == customerId }
    }

    fun getCustomerById(id: Long): Customer? {
        return generateCustomers().firstOrNull { it.id == id }
    }

    fun getCardById(id: Long): ContextCard? {
        return generateContextCards().firstOrNull { it.id == id }
    }

    fun search(query: String): List<SearchResult> {
        val conversations = generateConversations()
        var resultId = 1L
        val results = mutableListOf<SearchResult>()

        for (conv in conversations) {
            val matchInTitle = conv.title.contains(query, ignoreCase = true)
            val matchInSummary = conv.summary.contains(query, ignoreCase = true)
            val matchInKeywords = conv.keywords.any { it.text.contains(query, ignoreCase = true) }
            val matchInStatements = conv.keyStatements.any { it.text.contains(query, ignoreCase = true) }

            if (matchInTitle || matchInSummary || matchInKeywords || matchInStatements) {
                val snippet = when {
                    matchInTitle -> conv.title
                    matchInSummary -> conv.summary.take(100)
                    matchInKeywords -> conv.keywords.first { it.text.contains(query, ignoreCase = true) }.text
                    else -> conv.keyStatements.first { it.text.contains(query, ignoreCase = true) }.text.take(100)
                }
                val highlightStart = snippet.indexOf(query, ignoreCase = true)
                val highlightRanges = if (highlightStart >= 0) {
                    listOf(IntRange(highlightStart, highlightStart + query.length - 1))
                } else emptyList()

                results.add(
                    SearchResult(
                        id = resultId++,
                        type = "conversation",
                        title = conv.title,
                        snippet = snippet,
                        highlightRanges = highlightRanges,
                        sourceId = conv.id,
                        relevanceScore = when {
                            matchInTitle -> 1.0f
                            matchInKeywords -> 0.9f
                            matchInSummary -> 0.7f
                            else -> 0.5f
                        }
                    )
                )
            }
        }
        return results.sortedByDescending { it.relevanceScore }
    }
}
