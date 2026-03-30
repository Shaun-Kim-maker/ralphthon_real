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
        "Physical AI 솔루션 관련해서 지분 20% 내놓기로 말씀하셨던거 기억나시죠? 해당 조건으로 시리즈 A 50억 투자를 검토 중. 가격 조건 및 기술 실사 후 최종 결정 예정. 로봇 자동화 시장 TAM 분석 자료 추가 요청.",
        "시리즈 A 라운드 목표 100억. 베이스 벤처스가 리드 투자자로 50억, 나머지는 팔로우온 투자자 조합 구성 예정. Pre-money 밸류에이션 300억 기준으로 논의. 투자자 측에서 400억 이상 가능 시사.",
        "로봇 팔 자동화 PoC 결과 공유. 생산성 40% 향상 데이터 제시. 투자자 반응 매우 긍정적. 'Physical AI가 다음 빅 웨이브'라는 의견에 공감. 추가 기술 실사 일정 조율.",
        "지분 20% 기준 밸류에이션 재논의. 투자자 측에서 25% 지분 요구. 우리 측은 20%가 마지노선이라고 전달. 우선주 전환 비율 1:1 기본, 희석 방지 조항 Full Ratchet은 수용 불가.",
        "기술 실사(Tech DD) 진행. 핵심 IP 3건 확인, 특허 출원 중 2건 검토. 코드 리뷰에서 ROS2 기반 아키텍처 완성도에 대해 높은 평가. 데이터 파이프라인 확장성에 대한 추가 질문.",
        "파일럿 고객 3사(삼성전자, 현대차, SK) 계약 현황 공유. MRR 3억 달성 데이터 제시. 투자자가 'unit economics가 건전하다'고 코멘트. LTV/CAC 비율 5:1 이상 유지 확인.",
        "공동 투자 구조 논의. 카카오 벤처스 30억 + 소프트뱅크 벤처스 20억 + 알토스 벤처스 50억 조합 제안. 리드 투자자 선정 문제로 조율 필요. 이사회 좌석 배분 논의 시작.",
        "후속 라운드(시리즈 B) 계획 공유. 12개월 내 시리즈 B 200억 목표. 해외 진출(일본, 동남아) 자금 용도. 투자자가 브릿지 라운드 가능성도 열어두자고 제안.",
        "이사회 구성 논의. 투자자 1석, 경영진 2석, 사외이사 1석 구조 제안. 투자자 측에서 관찰자(Observer) 1석 추가 요청. 의결권 없는 조건으로 수용 가능.",
        "Exit 전략 논의. IPO는 3~5년 내 코스닥 상장 목표. M&A 옵션도 열어두되, 경영진 Lock-up 기간 협의 필요. 투자자가 Tag-along/Drag-along 조항 포함 요청."
    )

    private val internalMeetingSummaries = listOf(
        "베이스 벤처스가 리드로 확정되면 나머지 투자자 조합이 빠르게 구성될 것. 지분 20%는 사수해야 한다. 25% 이상 넘기면 시리즈 B에서 경영권 이슈 발생 가능. 창업팀 지분 60% 이상 유지 원칙.",
        "밸류에이션 300억은 보수적. 경쟁사 대비 기술 우위와 파일럿 매출 고려하면 400억도 가능. 다만 시장 상황 고려해 350억 선에서 타협 가능. CFO가 재무 모델 업데이트 예정.",
        "지분 희석 시나리오: 시리즈 A 20% + 시리즈 B 15% → 창업팀 최종 지분 55%. ESOP 10% 풀 별도. 시리즈 C까지 가면 창업팀 45%까지 희석 가능. 의결권은 차등의결권으로 보호.",
        "Term Sheet 비교: 베이스 벤처스(우호적 조건), 카카오 벤처스(전략적 시너지), 소프트뱅크(글로벌 네트워크). 베이스+카카오 조합이 최적. 소프트뱅크는 팔로우온으로.",
        "기술 실사 대응 자료 준비 완료. 특허 포트폴리오 3건, 핵심 알고리즘 성능 벤치마크, 기술 로드맵 18개월 버전. CTO가 직접 발표 예정.",
        "시리즈 A 클로징 목표: 4월 말. Term Sheet 서명 → 실사 → 주금 납입까지 6주 소요 예상. 법무법인 계약서 초안 검토 중. 변호사 비용 5천만원 예산 확보.",
        "IR 자료 v3 업데이트. Physical AI 시장 전망(2030년 50조 시장), 경쟁 우위 3가지, 재무 전망(3년 BEP). 투자자 미팅 시 첫 10분 안에 핵심 메시지 전달 필수.",
        "우선주 조건 검토: 1배 참가형 우선 분배. Full Ratchet 희석 방지는 거부, Weighted Average로 역제안. 이사 해임 조항은 2/3 특별결의로.",
        "공동 투자자 조합: 전략적 투자자(카카오벤처스) + 재무적 투자자(베이스, 알토스) 믹스가 이상적. 전략적 투자자 비중 30% 이하로 유지해야 향후 M&A 유연성 확보.",
        "Post-money 시뮬레이션: 350억 기준 시리즈 A 후 창업팀 68.6%, 투자자 20%, ESOP 10%, 엔젤 1.4%. 시리즈 B까지 고려하면 창업팀 55% 예상. 건전한 구조."
    )

    // Combined pool: customer meeting summaries first, then internal
    private val allSummaries = customerMeetingSummaries + internalMeetingSummaries

    private val keywordPool = listOf(
        Keyword("Physical AI", KeywordCategory.PRODUCT, 5),
        Keyword("로봇 자동화", KeywordCategory.PRODUCT, 4),
        Keyword("시리즈 A", KeywordCategory.BUSINESS, 5),
        Keyword("밸류에이션", KeywordCategory.PRICE, 4),
        Keyword("지분 구조", KeywordCategory.BUSINESS, 4),
        Keyword("Term Sheet", KeywordCategory.BUSINESS, 3),
        Keyword("기술 실사", KeywordCategory.TECHNICAL, 4),
        Keyword("우선주", KeywordCategory.PRICE, 3),
        Keyword("IPO", KeywordCategory.BUSINESS, 4),
        Keyword("MRR", KeywordCategory.PRICE, 3),
        Keyword("Anti-dilution", KeywordCategory.PRICE, 3),
        Keyword("ESOP", KeywordCategory.BUSINESS, 3),
        Keyword("경쟁사", KeywordCategory.COMPETITOR, 3),
        Keyword("Co-invest", KeywordCategory.BUSINESS, 2),
        Keyword("리드 투자자", KeywordCategory.BUSINESS, 3),
        Keyword("ROS2", KeywordCategory.TECHNICAL, 2),
        Keyword("특허", KeywordCategory.TECHNICAL, 3),
        Keyword("Exit 전략", KeywordCategory.BUSINESS, 3),
        Keyword("브릿지 라운드", KeywordCategory.BUSINESS, 2),
        Keyword("이사회", KeywordCategory.BUSINESS, 2),
        Keyword("LTV/CAC", KeywordCategory.BUSINESS, 4),
        Keyword("파일럿 고객", KeywordCategory.BUSINESS, 3),
        Keyword("Lock-up", KeywordCategory.PRICE, 3),
        Keyword("Tag-along", KeywordCategory.PRICE, 2),
        Keyword("Drag-along", KeywordCategory.PRICE, 2)
    )

    private val customerStatements = listOf(
        "Physical AI 솔루션 관련해서 지분 20% 내놓기로 말씀하셨던거 기억나시죠? 그 조건 그대로 갈 수 있으면 빠르게 진행하고 싶습니다.",
        "시리즈 A 50억 투자는 확정적인데, 밸류에이션을 좀 더 논의해야 할 것 같아요. Pre-money 300억은 좀 낮지 않나요?",
        "기술 실사 결과가 좋으면 추가 20억까지 가능합니다. 다만 우선주 전환 조건을 좀 더 유리하게 해주셔야 합니다.",
        "파일럿 고객 레퍼런스가 인상적이에요. 삼성전자랑 현대차가 쓰고 있다는 건 신뢰도가 높습니다.",
        "공동 투자자 조합에 전략적 투자자를 꼭 포함시켜야 합니다. 카카오 벤처스가 들어오면 사업 시너지가 클 거예요.",
        "이사회 좌석은 최소 1석은 확보해야 합니다. 관찰자 자리로는 부족해요.",
        "Exit 전략이 명확해야 투자 결정을 할 수 있어요. IPO 타임라인을 좀 더 구체적으로 보여주세요.",
        "Anti-dilution 조항은 Full Ratchet으로 해주셔야 합니다. Weighted Average로는 투자 보호가 부족합니다.",
        "로봇 자동화 시장이 커지고 있는 건 맞는데, 경쟁사 대비 기술 차별성을 더 명확하게 보여주셔야 해요.",
        "브릿지 라운드 가능성도 열어두고 싶어요. 시리즈 B 전에 시장 상황에 따라 유연하게 대응할 수 있어야 합니다.",
        "Lock-up 기간은 1년이 적당하다고 봅니다. 2년은 너무 길어요.",
        "Tag-along 권리는 반드시 포함되어야 합니다. 대주주 매각 시 우리도 같이 나갈 수 있어야 해요.",
        "MRR 3억이면 ARR 36억인데, PSR 10배 적용하면 360억 밸류에이션이 맞지 않나요?",
        "다른 VC들도 관심을 보이고 있어서, 의사결정을 빨리 해주시면 좋겠습니다.",
        "Physical AI 분야에서 이 정도 기술력을 가진 팀은 국내에 없다고 봅니다. 투자하고 싶습니다."
    )

    private val salesStatements = listOf(
        "지분 20%가 저희 마지노선입니다. 25% 이상은 시리즈 B에서 경영권 이슈가 생길 수 있어요.",
        "Pre-money 밸류에이션 350억으로 제안드립니다. 파일럿 매출과 기술 특허를 감안하면 적정합니다.",
        "우선주 전환은 1:1 기본이고, Anti-dilution은 Weighted Average로 가겠습니다. Full Ratchet은 수용 불가입니다.",
        "리드 투자자에게 이사회 1석을 드립니다. 나머지 투자자는 관찰자(Observer) 자격으로 참여 가능합니다.",
        "시리즈 B는 12개월 내에 진행할 계획이며, 해외 진출 자금으로 사용할 예정입니다.",
        "기술 실사는 언제든 환영합니다. CTO가 직접 브리핑하겠습니다.",
        "현재 MRR 3억이고, 연말까지 MRR 10억 달성이 목표입니다.",
        "IPO는 3~5년 내 코스닥 상장을 목표로 하고 있습니다.",
        "Lock-up 기간은 18개월로 제안드립니다. 1년은 시장 신호로 부정적일 수 있습니다.",
        "ESOP 풀은 10%로 설정했고, 핵심 인력 유지에 필수적입니다.",
        "창업팀 지분 60% 이상 유지가 원칙입니다. 차등의결권도 검토 중입니다.",
        "공동 투자 구조에서 전략적 투자자 비중은 30% 이하로 유지하려 합니다.",
        "현재 특허 3건 등록, 2건 출원 중이며, 핵심 알고리즘은 영업비밀로 보호합니다.",
        "파일럿 고객 3사 모두 본계약으로 전환 예정이며, 신규 고객 5사 파이프라인이 있습니다.",
        "Physical AI 시장은 2030년까지 50조 규모로 성장할 것으로 전망하고 있습니다."
    )

    private val knowledgeArticles = listOf(
        KnowledgeArticle(1L, "Physical AI 시장 전망 2030", "글로벌 Physical AI 시장 규모 2030년 50조원 전망. 로봇 자동화, 자율주행, 드론 물류 포함. CAGR 35%.", "시장분석", 0.95f),
        KnowledgeArticle(2L, "시리즈 A 투자 가이드라인", "Pre-money 200~500억 구간. 리드 투자자 비중 40~60%. 우선주 1배 참가형 표준. Anti-dilution Weighted Average 권장.", "투자", 0.88f),
        KnowledgeArticle(3L, "경쟁사 기술 비교 분석", "경쟁사 A: ROS2 미지원, 시뮬레이션 정확도 90%. 경쟁사 B: Fleet 관리 미흡. 당사 우위: 정확도 99.7%, 지연 50ms.", "경쟁", 0.82f),
        KnowledgeArticle(4L, "파일럿 고객 성과 보고서", "삼성전자: 생산성 40% 향상. 현대차: 불량률 30% 감소. SK: ROI 18개월 달성. 3사 모두 본계약 전환 예정.", "레퍼런스", 0.90f),
        KnowledgeArticle(5L, "지분 희석 시나리오 가이드", "시리즈 A 20% → 시리즈 B 15% → 시리즈 C 10%. 창업팀 최종 지분 45~55%. 차등의결권으로 경영권 보호.", "재무", 0.85f),
        KnowledgeArticle(6L, "특허 포트폴리오 현황", "등록 특허 3건(로봇 제어 알고리즘, 센서 퓨전, 디지털 트윈). 출원 중 2건. PCT 국제 출원 1건.", "IP", 0.92f),
        KnowledgeArticle(7L, "VC 투자 Term Sheet 표준 조항", "우선주 전환 1:1, 희석 방지 Weighted Average, 이사회 1석, Lock-up 18개월, Tag-along/Drag-along 포함.", "법무", 0.78f),
        KnowledgeArticle(8L, "Unit Economics 분석", "LTV/CAC 5:1. MRR 3억(ARR 36억). Gross Margin 75%. Net Revenue Retention 130%. 건전한 SaaS 지표.", "재무", 0.91f)
    )

    private val predictedQuestionPool = listOf(
        PredictedQuestion(1L, "지분 20%가 최종 조건인가요, 협상 여지가 있나요?", "20%가 마지노선입니다. 시리즈 B까지 고려하면 창업팀 지분 60% 이상 유지가 원칙이며, 25% 이상 양보는 경영권 이슈로 이어질 수 있습니다.", listOf("지분 구조", "경영권", "시리즈 B"), 0.93f),
        PredictedQuestion(2L, "밸류에이션 300억의 산정 근거가 무엇인가요?", "PSR 10배 기준(ARR 36억), 파일럿 고객 3사 매출, 특허 3건 보유, Physical AI 시장 성장률 CAGR 35%를 종합적으로 반영했습니다.", listOf("밸류에이션", "PSR", "ARR"), 0.89f),
        PredictedQuestion(3L, "Anti-dilution 조항을 Full Ratchet으로 변경 가능한가요?", "Full Ratchet은 수용 불가합니다. Broad-based Weighted Average로 제안하며, 이는 업계 표준 조건입니다.", listOf("Anti-dilution", "우선주", "투자 보호"), 0.85f),
        PredictedQuestion(4L, "시리즈 B는 언제 예정인가요?", "12개월 내 시리즈 B 200억 목표입니다. 해외 진출(일본, 동남아) 자금으로 사용할 예정이며, MRR 10억 달성 후 진행합니다.", listOf("시리즈 B", "해외 진출", "후속 라운드"), 0.87f),
        PredictedQuestion(5L, "경쟁사 대비 기술적 우위가 구체적으로 무엇인가요?", "핵심 우위: ROS2 완전 지원, 추론 지연 50ms 이하, 시뮬레이션 정확도 99.7%, 온프레미스 단독 운용. 경쟁사는 이 중 1~2개만 충족합니다.", listOf("기술 우위", "ROS2", "경쟁사"), 0.91f),
        PredictedQuestion(6L, "Lock-up 기간을 1년으로 줄일 수 있나요?", "18개월을 제안합니다. 1년 Lock-up은 시장에 '빠른 Exit 의도'로 해석될 수 있어 주가에 부정적 영향을 줄 수 있습니다.", listOf("Lock-up", "IPO", "Exit"), 0.84f)
    )

    // Price commitment data: (amount, currency, condition, timeOffset)
    private data class PriceData(val amount: Double, val currency: String, val condition: String, val timeOffset: String)

    private val priceDataPool = listOf(
        PriceData(5_000_000_000.0, "KRW", "시리즈 A 리드 투자 (지분 20% 기준)", "15:30"),
        PriceData(3_000_000_000.0, "KRW", "팔로우온 투자 (카카오 벤처스)", "22:15"),
        PriceData(2_000_000_000.0, "KRW", "공동 투자 참여분 (소프트뱅크)", "18:40"),
        PriceData(35_000_000_000.0, "KRW", "Pre-money 밸류에이션 기준", "10:05"),
        PriceData(10_000_000_000.0, "KRW", "시리즈 A 총 라운드 규모 목표", "14:20"),
        PriceData(5_000_000_000.0, "KRW", "알토스 벤처스 공동 투자 제안", "11:35"),
        PriceData(20_000_000_000.0, "KRW", "시리즈 B 후속 라운드 목표 (12개월 후)", "16:50"),
        PriceData(1_000_000_000.0, "KRW", "브릿지 라운드 가능 규모", "09:15"),
        PriceData(50_000_000.0, "KRW", "법무 자문 비용 예산", "13:25"),
        PriceData(3_600_000_000.0, "KRW", "ARR 기준 (MRR 3억 x 12)", "17:10")
    )

    // Action item data: (description, assignee, dueDayOffset, status)
    private data class ActionData(val description: String, val assignee: String, val dueDayOffset: Int, val status: ActionItemStatus)

    private val actionDataPool = listOf(
        ActionData("Term Sheet 초안 검토 및 수정", "대표이사 CEO", 3, ActionItemStatus.OPEN),
        ActionData("기술 실사 대응 자료 준비 (특허 포트폴리오)", "CTO", 5, ActionItemStatus.OPEN),
        ActionData("재무 모델 업데이트 (시리즈 A 반영)", "CFO", 7, ActionItemStatus.OPEN),
        ActionData("법무법인 계약서 초안 의뢰", "법무팀", 3, ActionItemStatus.DONE),
        ActionData("IR 자료 v3 업데이트 (Physical AI 시장 전망)", "전략기획실", 10, ActionItemStatus.OPEN),
        ActionData("파일럿 고객 레퍼런스 레터 확보 (삼성전자)", "영업팀", 5, ActionItemStatus.DONE),
        ActionData("밸류에이션 산정 근거 자료 정리", "CFO", 7, ActionItemStatus.OPEN),
        ActionData("우선주 조건 비교표 작성", "법무팀", 14, ActionItemStatus.OPEN),
        ActionData("이사회 구성안 내부 확정", "대표이사 CEO", 5, ActionItemStatus.DONE),
        ActionData("Post-money 지분 희석 시뮬레이션 완성", "CFO", 7, ActionItemStatus.OPEN)
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
                summary = "$company - Physical AI 로봇 스타트업 시리즈 A 투자 검토 중. 담당: $contact. 주요 논점: 지분 구조, 밸류에이션, Term Sheet 조건."
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
