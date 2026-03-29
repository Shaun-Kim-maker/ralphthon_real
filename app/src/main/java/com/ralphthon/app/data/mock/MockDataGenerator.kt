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
        Triple("베이스 벤처스", "박진호 이사", "벤처캐피탈"),
        Triple("카카오 벤처스", "이수현 이사", "벤처캐피탈"),
        Triple("소프트뱅크 벤처스", "김태영 상무", "벤처캐피탈"),
        Triple("스틱 인베스트먼트", "정민아 이사", "벤처캐피탈"),
        Triple("한화 투자증권", "최재훈 부장", "증권"),
        Triple("KB 인베스트먼트", "유서진 이사", "벤처캐피탈"),
        Triple("알토스 벤처스", "신동혁 파트너", "벤처캐피탈"),
        Triple("산업은행 캐피탈", "강예린 차장", "정책금융"),
        Triple("LB 인베스트먼트", "임준영 이사", "벤처캐피탈"),
        Triple("DSC 인베스트먼트", "한소율 팀장", "벤처캐피탈")
    )

    // 10 distinct CUSTOMER_MEETING titles per customer slot (cycled)
    private val meetingTitles = listOf(
        "Physical AI 솔루션 투자 검토 미팅",
        "시리즈 A 라운드 조건 협의",
        "로봇 자동화 사업 계획 발표",
        "지분 구조 및 밸류에이션 논의",
        "기술 실사(Tech DD) 미팅",
        "파일럿 고객사 레퍼런스 공유",
        "공동 투자(Co-invest) 구조 협의",
        "후속 투자 라운드 계획 논의",
        "이사회 참여 및 거버넌스 논의",
        "Exit 전략 및 IPO 로드맵 공유"
    )

    // 10 distinct INTERNAL_MEETING titles (cycled)
    private val internalTitles = listOf(
        "투자 유치 전략 내부 회의",
        "밸류에이션 산정 근거 논의",
        "지분 희석 시나리오 분석",
        "투자자별 Term Sheet 비교 검토",
        "기술 실사 대응 자료 준비",
        "시리즈 A 클로징 타임라인 수립",
        "투자자 관계(IR) 전략 수립",
        "우선주 조건 및 보호 조항 검토",
        "공동 투자자 조합 최적화 논의",
        "Post-money 밸류에이션 시뮬레이션"
    )

    // 20 distinct summaries (10 customer-meeting + 10 internal)
    private val customerMeetingSummaries = listOf(
        "Isaac Sim 시뮬레이터 라이선스 별도 옵션 여부를 논의. 고객사는 소스코드 제공 여부를 강하게 요구했으나, 당사는 바이너리 납품만 가능하다는 입장을 고수함. 시뮬레이터 패키징은 별도 계약으로 처리하기로 합의.",
        "로봇 팔 6축 모델 PoC 일정 협의. PoC는 무료로 진행하되 본계약 시 비용에 포함하는 조건 제안. 고객사는 3개월 PoC 기간을 요청, 당사는 2개월 내 완료 가능 여부를 기술팀에 확인하기로 함.",
        "AMR 50대 물류센터 적용 데모 완료. 경로 최적화 알고리즘 성능에 만족하나 타사 AMR과의 상호운용성(interoperability) 질문 제기. 당사 Fleet 관리 플랫폼이 ROS2 표준을 지원함을 강조.",
        "엣지 AI 프로세서 NVIDIA Jetson Orin 기반 비전 시스템 검토. 추론 지연 50ms 이하 요구 충족 확인. 온프레미스 배포 가능 여부 질문에 클라우드 없이 단독 운용 가능함을 시연.",
        "디지털 트윈 환경에서 로봇 시뮬레이션 정확도 95% 달성 데이터 공유. 고객사 생산라인 3D 모델 구축에 추가 2주 소요 예상. 트윈 구축 비용을 별도 항목으로 견적에 반영 요청.",
        "협동 로봇 ISO 10218 및 ISO/TS 15066 인증 현황 공유. 조립 라인 2개 공정에 Cobot 8대 투입 계획 확정. 안전 펜스 제거 시 추가 위험성 평가 필요함을 고지.",
        "로봇 Fleet 100대 관리 플랫폼 가격 협상. 연간 3억 제안에 고객사 2억 5천 역제안. 3년 장기 계약 시 20% 할인 카드를 꺼냈으나 고객사 예산 사이클과 맞지 않아 추가 검토 필요.",
        "예지보전 AI 솔루션 도입으로 장비 다운타임 30% 감소 효과 ROI 분석 발표. 6개월 내 투자비 회수 가능 데이터 제시. 고객사 CFO가 회의에 참석해 재무적 효과에 높은 관심 표명.",
        "소프트웨어 라이선스 연간 계약 조건 세부 협의. 하드웨어는 별도이며 소프트웨어 라이선스만 포함하는 계약 구조 설명. 유지보수는 연간 계약금의 15%로 제안, 고객사는 10%를 희망.",
        "ROS2 기반 센서 퓨전(LiDAR + 카메라) 기술 파트너십 논의. SDK 제공 범위 및 API 문서화 수준을 확인. 경쟁사가 이미 유사 제안을 했다는 정보 입수, 기술 차별성 강조 필요."
    )

    private val internalMeetingSummaries = listOf(
        "이 고객은 예산이 충분하니 풀패키지로 제안하는 방향으로 가자. 시뮬레이터 제외해도 마진율 35% 이상 유지 가능. 다음 미팅 전에 견적서 수정본(시뮬레이터 제외 버전) 발송 필요.",
        "PoC 기술 난이도가 높아 기간을 3개월로 잡아야 한다는 결론. 2개월 완료 약속은 고객에게 하지 않기로 함. 기술팀 리소스 2명 추가 배정 검토.",
        "내부 마진율 40% 이상 유지 원칙 재확인. 고객 역제안 2억 5천은 마진율 28%로 수용 불가. 3억 유지하되 유지보수 비율 조정으로 패키지 가치 높이는 방향 논의.",
        "경쟁사 A사가 이미 고객사에 접촉해 유사 솔루션 제안 완료. 빠르게 움직여야 함. 기술 우위(정확도, 지연 시간) 데이터를 비교 자료로 준비해 다음 미팅에서 제시 예정.",
        "Q2 파이프라인 중 대형 딜 3건 확인. 삼성전자 건이 가장 규모가 크고 성사 가능성 높음. 현대차는 의사결정자 교체로 재접근 필요.",
        "Isaac Sim 시뮬레이터는 제외한다는 방침 재확인. 소스코드는 절대 주지 않는다. 우리 팀에서 사용하고 싶은 내용만 활용하는 형태로 납품. 시뮬레이터 패키징 및 이관은 별도 옵션.",
        "고객 피드백 취합 결과 대시보드 UX 불편 호소 3건. 기술팀이 다음 스프린트에서 수정 예정. 이 부분을 경쟁사 대비 개선 포인트로 활용 가능.",
        "NDA 법무팀 검토 완료. 고객사 제안 조항 중 지식재산권 귀속 조항은 수용 불가. 표준 계약서 기준으로 재협상 요청 예정.",
        "기술 지원 이슈 중 펌웨어 업그레이드 실패 케이스 1건 발생. 원인 분석 완료(드라이버 호환성). 다음 릴리스에 핫픽스 포함 예정. 고객 커뮤니케이션은 영업팀이 담당.",
        "시스템 인테그레이터 2사와 파트너십 MOU 추진 중. 이를 통해 설치 및 현장 지원 역량 강화. 파트너 수수료 구조는 영업팀 마진에 영향 없도록 설계."
    )

    // Combined pool: customer meeting summaries first, then internal
    private val allSummaries = customerMeetingSummaries + internalMeetingSummaries

    private val keywordPool = listOf(
        Keyword("Isaac Sim", KeywordCategory.PRODUCT, 5),
        Keyword("로봇 팔", KeywordCategory.PRODUCT, 4),
        Keyword("AMR", KeywordCategory.PRODUCT, 4),
        Keyword("디지털 트윈", KeywordCategory.PRODUCT, 3),
        Keyword("엣지 AI", KeywordCategory.TECHNICAL, 3),
        Keyword("Fleet 관리", KeywordCategory.PRODUCT, 3),
        Keyword("협동 로봇", KeywordCategory.PRODUCT, 4),
        Keyword("예지보전", KeywordCategory.PRODUCT, 3),
        Keyword("라이선스", KeywordCategory.PRICE, 4),
        Keyword("연간 계약", KeywordCategory.PRICE, 3),
        Keyword("할인", KeywordCategory.PRICE, 3),
        Keyword("유지보수", KeywordCategory.PRICE, 3),
        Keyword("경쟁사", KeywordCategory.COMPETITOR, 3),
        Keyword("대안", KeywordCategory.COMPETITOR, 2),
        Keyword("벤치마크", KeywordCategory.COMPETITOR, 2),
        Keyword("API", KeywordCategory.TECHNICAL, 2),
        Keyword("SDK", KeywordCategory.TECHNICAL, 2),
        Keyword("ROS2", KeywordCategory.TECHNICAL, 3),
        Keyword("시뮬레이션", KeywordCategory.TECHNICAL, 3),
        Keyword("센서 퓨전", KeywordCategory.TECHNICAL, 2),
        Keyword("ROI", KeywordCategory.BUSINESS, 4),
        Keyword("생산성", KeywordCategory.BUSINESS, 3),
        Keyword("도입 효과", KeywordCategory.BUSINESS, 3),
        Keyword("마진율", KeywordCategory.BUSINESS, 2),
        Keyword("PoC", KeywordCategory.BUSINESS, 4)
    )

    private val customerStatements = listOf(
        "Isaac Sim 라이선스는 별도로 구매해야 하나요, 아니면 패키지에 포함되나요?",
        "PoC 결과가 기대에 못 미치면 본계약 없이 종료할 수 있나요?",
        "온프레미스 배포가 가능한가요? 클라우드 의존 없이 운용하고 싶습니다.",
        "로봇 팔 고장 시 교체 또는 수리 기간은 얼마나 됩니까?",
        "타사 로봇 장비와의 호환성은 어느 수준으로 지원되나요?",
        "소스코드를 납품받을 수 있나요? 자체 커스터마이징이 필요합니다.",
        "연간 계약 말고 3년 장기 계약 시 추가 할인이 있나요?",
        "ROI를 실제로 달성한 레퍼런스 고객 사례를 보여줄 수 있나요?",
        "경쟁사 제품 대비 구체적인 기술 우위를 데이터로 제시해주세요.",
        "유지보수 비율 15%는 너무 높습니다. 10%로 조정 가능한가요?",
        "예산이 올해 안에 집행되어야 합니다. 계약 후 설치까지 얼마나 걸리나요?",
        "엣지 AI 추론 지연이 50ms 이하라는 것을 실제로 증명해주세요.",
        "글로벌 공장에도 동일한 솔루션을 적용할 수 있나요?",
        "데이터는 외부로 전송되지 않는다고 보장할 수 있나요?",
        "ROS2 기반이라면 우리 기존 로봇 인프라와 통합 가능합니까?"
    )

    private val salesStatements = listOf(
        "시뮬레이터는 없이 한다고 했으나 명시한 적은 없어서 고객이 헷갈릴 수 있음. 다음 미팅 전에 명확히 정리 필요.",
        "Isaac Sim 시뮬레이터는 제외합니다. 소스코드는 드리지 않습니다.",
        "시뮬레이터 패키징 및 이관은 별도 옵션으로 제공 가능합니다.",
        "가격은 연간 3억으로 제안드렸으나, 고객사는 2억 5천을 원하고 있습니다.",
        "PoC는 무료로 진행하되, 본계약 시 비용에 포함하는 구조입니다.",
        "유지보수는 연간 계약금의 15%로 제안드립니다.",
        "하드웨어는 별도이며, 소프트웨어 라이선스만 이번 계약에 포함됩니다.",
        "경쟁사 대비 기술 우위를 데이터로 강조해야 할 시점입니다.",
        "내부적으로 마진율 40% 이상은 유지해야 합니다.",
        "이 고객은 예산이 충분하니 풀패키지로 가는 것이 유리합니다.",
        "경쟁사가 이미 접촉했으니 빠르게 움직여야 합니다.",
        "기술적 난이도가 높아서 PoC 기간을 3개월로 잡겠습니다.",
        "우리 팀에서 사용하고 싶은 내용만 활용하는 형태로 납품합니다.",
        "3년 장기 계약 시 20% 할인 조건을 제시할 수 있습니다.",
        "온프레미스 단독 운용 가능하며 외부 데이터 전송 없음을 보장합니다."
    )

    private val knowledgeArticles = listOf(
        KnowledgeArticle(1L, "Physical AI 로봇 제품 라인업", "Isaac Sim 기반 시뮬레이션 환경, 6축 로봇 팔, AMR 3개 라인업 구성. 소프트웨어 라이선스와 하드웨어 별도 계약 구조.", "제품", 0.95f),
        KnowledgeArticle(2L, "로봇 자동화 ROI 분석 가이드", "평균 18개월 내 투자비 회수. 인건비 40% 절감, 불량률 30% 감소. 레퍼런스 고객 5사 데이터 포함.", "영업", 0.88f),
        KnowledgeArticle(3L, "경쟁사 비교 분석표 (2026 Q1)", "경쟁사 A: 가격 낮으나 ROS2 미지원. 경쟁사 B: Fleet 관리 미흡. 당사 기술 우위: 지연 50ms, 정확도 99.7%.", "경쟁", 0.82f),
        KnowledgeArticle(4L, "안전 인증 현황", "ISO 10218, ISO/TS 15066, CE, KC 인증 보유. Cobot 라인은 추가 위험성 평가 필요.", "인증", 0.90f),
        KnowledgeArticle(5L, "스마트 팩토리 구축 사례집", "반도체 공정 6축 로봇 팔 도입 사례, 물류센터 AMR 100대 운용 사례 포함.", "사례", 0.85f),
        KnowledgeArticle(6L, "Isaac Sim 라이선스 정책", "시뮬레이터 패키징은 별도 옵션. 소스코드 비제공 원칙. 바이너리 전용 납품. 연간 라이선스 갱신 필요.", "정책", 0.92f),
        KnowledgeArticle(7L, "가격 정책 가이드라인", "소프트웨어 라이선스 기본 3억/년. 유지보수 15%. 3년 계약 시 20% 할인. 마진율 40% 유지 기준.", "가격", 0.78f),
        KnowledgeArticle(8L, "엣지 AI 기술 백서", "NVIDIA Jetson Orin 기반. 추론 지연 50ms 이하. 온프레미스 단독 운용. 클라우드 의존도 0.", "기술", 0.91f)
    )

    private val predictedQuestionPool = listOf(
        PredictedQuestion(1L, "Isaac Sim 라이선스 별도 구매가 필요한가요?", "Isaac Sim 시뮬레이터는 별도 옵션입니다. 기본 계약에는 포함되지 않으며, 연간 추가 비용이 발생합니다. 소스코드는 제공되지 않습니다.", listOf("Isaac Sim", "라이선스", "시뮬레이터"), 0.93f),
        PredictedQuestion(2L, "로봇 팔 고장 시 교체 기간은 얼마나 됩니까?", "골드 SLA 기준 4시간 내 현장 출동, 48시간 내 부품 교체를 보장합니다. 예비 부품은 국내 물류 창고에 상시 비축합니다.", listOf("SLA", "유지보수", "교체 기간"), 0.89f),
        PredictedQuestion(3L, "타사 로봇과의 호환성은 어떻게 되나요?", "ROS2 표준을 지원하므로 ROS2 호환 로봇 장비와 통합 가능합니다. Fleet 관리 플랫폼은 벤더 중립적으로 설계되어 있습니다.", listOf("ROS2", "호환성", "Fleet 관리"), 0.85f),
        PredictedQuestion(4L, "PoC 결과가 나쁘면 계약 취소가 가능한가요?", "PoC 결과물 기준 충족 여부를 계약서에 명시합니다. 미충족 시 본계약 의무가 없는 조건부 계약 구조를 제안할 수 있습니다.", listOf("PoC", "계약 조건", "해지"), 0.87f),
        PredictedQuestion(5L, "온프레미스 배포가 가능한가요? 클라우드만 되나요?", "엣지 AI 아키텍처로 설계되어 클라우드 없이 온프레미스 단독 운용이 가능합니다. 모든 추론은 로컬에서 처리됩니다.", listOf("온프레미스", "엣지 AI", "클라우드"), 0.91f),
        PredictedQuestion(6L, "경쟁사 대비 가격이 높은 이유는 무엇인가요?", "Fleet 관리, 예지보전, 디지털 트윈 연동이 모두 포함된 토탈 솔루션입니다. TCO 기준으로 3년 누적 비용은 경쟁사 대비 20% 저렴합니다.", listOf("TCO", "경쟁사", "차별성"), 0.84f)
    )

    // Price commitment data: (amount, currency, condition, timeOffset)
    private data class PriceData(val amount: Double, val currency: String, val condition: String, val timeOffset: String)

    private val priceDataPool = listOf(
        PriceData(300_000_000.0, "KRW", "소프트웨어 라이선스 연간 계약 시", "10:30"),
        PriceData(50_000_000.0, "KRW", "PoC 성공 조건부 (본계약 포함)", "15:00"),
        PriceData(250_000_000.0, "KRW", "고객 역제안 (2억 5천)", "22:15"),
        PriceData(120_000_000.0, "KRW", "유지보수 연간 계약 (계약금의 15%)", "08:45"),
        PriceData(240_000_000.0, "KRW", "3년 장기 계약 시 20% 할인 적용", "14:00"),
        PriceData(180_000_000.0, "KRW", "AMR 50대 대량 구매 기준", "11:20"),
        PriceData(80_000_000.0, "KRW", "Isaac Sim 시뮬레이터 패키징 별도 옵션", "16:30"),
        PriceData(500_000_000.0, "KRW", "Fleet 100대 + 디지털 트윈 풀패키지", "09:00"),
        PriceData(30_000_000.0, "KRW", "엣지 AI 프로세서 파일럿 (3개월)", "13:45"),
        PriceData(90_000_000.0, "KRW", "협동 로봇 8대 설치 및 교육 포함", "17:00")
    )

    // Action item data: (description, assignee, dueDayOffset, status)
    private data class ActionData(val description: String, val assignee: String, val dueDayOffset: Int, val status: ActionItemStatus)

    private val actionDataPool = listOf(
        ActionData("견적서 수정본 발송 (시뮬레이터 제외 버전)", "영업팀 김대리", 3, ActionItemStatus.OPEN),
        ActionData("경쟁사 비교 자료 준비 (기술 우위 데이터 포함)", "기술팀 박과장", 5, ActionItemStatus.OPEN),
        ActionData("PoC 환경 세팅 일정 확정", "기술팀 박과장", 7, ActionItemStatus.OPEN),
        ActionData("법무팀 NDA 검토 요청", "영업팀 김대리", 3, ActionItemStatus.DONE),
        ActionData("기술팀 데모 환경 준비 (Isaac Sim 포함)", "기술팀 박과장", 10, ActionItemStatus.OPEN),
        ActionData("ROI 분석 자료 업데이트 (레퍼런스 2건 추가)", "영업팀 김대리", 5, ActionItemStatus.DONE),
        ActionData("유지보수 비율 재조정 안 내부 승인 요청", "CTO 이사", 7, ActionItemStatus.OPEN),
        ActionData("ROS2 SDK 호환성 검증 보고서 작성", "기술팀 박과장", 14, ActionItemStatus.OPEN),
        ActionData("온프레미스 배포 시연 환경 준비", "기술팀 박과장", 5, ActionItemStatus.DONE),
        ActionData("계약서 표준 조항 재검토 (IP 귀속 조항)", "법무팀", 7, ActionItemStatus.OPEN)
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
                summary = "$company - Physical AI 로봇 솔루션 도입 검토 중. 담당자: $contact. 주요 관심사: Isaac Sim 라이선스, PoC 조건, 가격 협상."
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
            for (i in 0 until 20) {
                // First 10 are CUSTOMER_MEETING, last 10 are INTERNAL_MEETING
                val isCustomerMeeting = i < 10
                val type = if (isCustomerMeeting) ConversationType.CUSTOMER_MEETING else ConversationType.INTERNAL_MEETING
                val meetingIndex = i % 10
                val title = if (isCustomerMeeting) meetingTitles[meetingIndex] else internalTitles[meetingIndex]
                val summaryIndex = if (isCustomerMeeting) meetingIndex else 10 + meetingIndex
                val summary = allSummaries[summaryIndex]

                val sentiment = when (i % 5) {
                    0, 1 -> Sentiment.POSITIVE
                    2, 3 -> Sentiment.NEUTRAL
                    else -> Sentiment.NEGATIVE
                }

                val day = ((i % 20) + 1).toString().padStart(2, '0')
                val month = if (i < 10) "02" else "03"

                // 3-5 keywords per conversation
                val kwCount = 3 + (i % 3)
                val keywords = (0 until kwCount).map { k ->
                    keywordPool[(i * 3 + k * 7) % keywordPool.size]
                }.distinctBy { it.text }.take(kwCount)

                // 2-4 key statements per conversation
                val stmtCount = 2 + (i % 3)
                val statements = mutableListOf<KeyStatement>()
                for (s in 0 until stmtCount) {
                    val isCustomerSpeak = s % 2 == 0
                    val textPool = if (isCustomerMeeting && isCustomerSpeak) customerStatements else salesStatements
                    val textIndex = (i * 7 + s * 13) % textPool.size
                    val speaker = if (isCustomerMeeting && isCustomerSpeak) {
                        customer.contactName ?: customer.companyName
                    } else if (isCustomerMeeting) {
                        "영업팀 김대리"
                    } else {
                        when (s % 3) {
                            0 -> "영업팀 김대리"
                            1 -> "기술팀 박과장"
                            else -> "CTO 이사"
                        }
                    }
                    val hour = (9 + s * 3 + i % 6).toString().padStart(2, '0')
                    val minute = ((s * 17 + i * 3) % 60).toString().padStart(2, '0')
                    statements.add(
                        KeyStatement(
                            id = statementId++,
                            speaker = speaker,
                            text = textPool[textIndex],
                            timestamp = "$hour:$minute",
                            sentiment = if (s % 3 == 0) sentiment else Sentiment.NEUTRAL,
                            isImportant = s == 0 || (i % 5 == 0)
                        )
                    )
                }

                // 1-2 price commitments (not all conversations)
                val priceCommitments = if (i % 3 != 1) {
                    val pc1 = priceDataPool[(i * 3) % priceDataPool.size]
                    val result = mutableListOf(
                        PriceCommitment(
                            id = priceId++,
                            amount = pc1.amount,
                            currency = pc1.currency,
                            condition = pc1.condition,
                            mentionedAt = pc1.timeOffset
                        )
                    )
                    if (i % 2 == 0) {
                        val pc2 = priceDataPool[(i * 3 + 5) % priceDataPool.size]
                        result.add(
                            PriceCommitment(
                                id = priceId++,
                                amount = pc2.amount,
                                currency = pc2.currency,
                                condition = pc2.condition,
                                mentionedAt = pc2.timeOffset
                            )
                        )
                    }
                    result
                } else emptyList()

                // 1-3 action items per conversation
                val actionCount = 1 + (i % 3)
                val actionItems = (0 until actionCount).map { a ->
                    val ad = actionDataPool[(i * 2 + a * 5) % actionDataPool.size]
                    val dueMonth = if (i < 10) "04" else "05"
                    val dueDay = ((ad.dueDayOffset + i) % 28 + 1).toString().padStart(2, '0')
                    ActionItem(
                        id = actionId++,
                        description = ad.description,
                        assignee = ad.assignee,
                        dueDate = "2026-$dueMonth-$dueDay",
                        status = ad.status
                    )
                }

                // 1-2 predicted questions (not all conversations)
                val predictedQuestions = if (i % 3 != 2) {
                    val pq1 = predictedQuestionPool[i % predictedQuestionPool.size]
                    if (i % 4 == 0) {
                        listOf(pq1, predictedQuestionPool[(i + 2) % predictedQuestionPool.size])
                    } else {
                        listOf(pq1)
                    }
                } else emptyList()

                conversations.add(
                    Conversation(
                        id = conversationId++,
                        customerId = customer.id,
                        title = "${customer.companyName} - $title",
                        date = "2026-$month-$day",
                        type = type,
                        duration = 30 + (i * 7) % 60,
                        summary = summary,
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
                Sentiment.POSITIVE -> 0.70f + (index % 3) * 0.09f
                Sentiment.NEGATIVE -> 0.10f + (index % 3) * 0.08f
                Sentiment.NEUTRAL -> 0.40f + (index % 3) * 0.09f
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
