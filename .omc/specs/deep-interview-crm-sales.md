# Deep Interview Spec: Zipi — Physical AI Sales CRM
# 지피지기면 백전백승 (知彼知己 百戰百勝)

## Metadata
- Rounds: 17
- Final Ambiguity Score: 5%
- Type: Brownfield
- Generated: 2026-03-29
- Threshold: 0.05
- Status: PASSED

## Clarity Breakdown
| Dimension | Score | Weight | Weighted |
|-----------|-------|--------|----------|
| Goal Clarity | 0.98 | 0.35 | 0.34 |
| Constraint Clarity | 0.93 | 0.25 | 0.23 |
| Success Criteria | 0.95 | 0.25 | 0.24 |
| Context Clarity | 0.93 | 0.15 | 0.14 |
| **Total Clarity** | | | **0.95** |
| **Ambiguity** | | | **0.05** |

## Goal
Physical AI를 세일즈하는 영업 담당자가, 고객과의 대화 녹음을 업로드하면 자동 분석된 카드뉴스를 통해 **10초 안에 고객 맥락을 파악**할 수 있는 Android CRM 앱. 갑작스러운 고객 전화에도 즉시 대응 가능.

## 핵심 사용자
- **Physical AI 세일즈맨**: 다수의 고객과 대화하며, 각 고객의 관심사/가격/약속을 기억하기 어려움
- **현재 워크플로우**: 휴대폰 녹음 → 나중에 다시 듣기 → 노션에 기록 → 다시 찾기
- **목표 워크플로우**: 녹음 업로드 → 자동 분석 → 앱에서 즉시 브리핑

## 킬링 포인트 (데모)
고객을 탭 한 번 누르면:
1. **예상 질문**: 다음 미팅에서 고객이 물어볼 것으로 예상되는 질문
2. **추천 답변**: 예상 질문에 대한 추천 답변 + 관련 지식 + 키워드
3. 이 정보가 즉시 화면에 표시 → "와, 이거 진짜 쓸 만하다"

## 앱이 하는 것
1. **녹음 업로드**: 세일즈맨이 휴대폰 녹음 파일(m4a/mp3) 업로드
2. **카드뉴스 표시**: 분석 결과를 구조화된 카드뉴스로 표시
   - 감정(Sentiment): 긍정/부정/중립
   - 키워드(Keywords): 카테고리별 태그
   - 핵심 발언(KeyStatements): 화자별 중요 발언
   - 가격/약속(PriceCommitments): 언급된 가격, 조건
   - 액션 아이템(ActionItems): 다음에 해야 할 일
   - 관련 지식(KnowledgeArticles): 제품 문서 연결
   - 예상 질문(PredictedQuestions): AI가 예측한 다음 질문 + 추천 답변
3. **고객별 즉시 브리핑** (10초 맥락 파악):
   - 1순위: 마지막 대화 요약 (고객 대화 + **해당 고객사 건으로 진행한 사내 회의 내용** 포함)
   - 2순위: 예상 질문 + 추천 답변/지식
   - 3순위: 가격/조건 히스토리
4. **검색**: 키워드, 발언, 가격 등 전체 검색 + 하이라이트

## 대화 유형
1. **고객 대화**: 세일즈맨 ↔ 고객 간 외부 대화 (녹음 기반)
2. **사내 회의**: 해당 고객사 건에 대한 내부 팀 회의 (녹음 기반)
- 두 유형 모두 업로드 → 분석 → 카드뉴스 생성
- 고객 브리핑 시 두 유형이 통합되어 표시

## Constraints
- **백엔드 API**: 존재하지 않음 → Mock API (MockWebServer/로컬 JSON)
- **STT**: 백엔드가 처리 (앱은 업로드/수신만)
- **데이터 소스**: 휴대폰 녹음 앱 (m4a/mp3)
- **Mock 데이터 규모**: 고객 10명 × 대화 20건 = 200건
- **자동 녹음**: 이번 대회 범위 밖 (미래 기능)
- **예상 질문/추천 답변**: Mock 데이터에 미리 포함 (AI 생성 아님)
- **Mock 시연용 가격 히스토리 예시**: "Isaac Sim 시뮬레이터는 제외한다. 소스코드는 주지 않는다. 우리 팀에서 사용하고 싶은 내용만 활용한다. 시뮬레이터 패키징 및 이관은 별도 옵션이다." — 실제 Physical AI 세일즈 협상 맥락 반영

## Non-Goals
- 실제 STT 처리 (백엔드 영역)
- 실제 AI 분석 (백엔드 영역)
- 자동 녹음
- 다국어 지원
- 사용자 인증/로그인

## Success Criteria
- [ ] 고객 탭 → 10초 안에 맥락 파악 가능 (브리핑 화면)
- [ ] 카드뉴스에 감정/키워드/핵심발언/가격/액션아이템/지식 모두 표시
- [ ] 예상 질문 + 추천 답변이 고객 화면에서 즉시 표시
- [ ] 마지막 대화 요약에 고객 대화 + 사내 회의 내용 통합 표시
- [ ] 검색에서 키워드/발언/가격 검색 + 하이라이트
- [ ] Mock 데이터 200건이 자연스럽게 표시
- [ ] 4가지 상태 처리: Loading, Data, Empty, Error
- [ ] 심사 기준: 비즈니스 임팩트 (실제 세일즈맨 페인 해결)

## Domain Models (확정)

### 기존
- **Customer**: id, name, company, contactInfo
- **Conversation**: id, customerId, type(CUSTOMER_MEETING | INTERNAL_MEETING), date, duration
- **ContextCard**: id, conversationId, title, summary, sentiment, keywords, keyStatements, knowledgeArticles, priceCommitments, actionItems, predictedQuestions
- **KeyStatement**: speaker, content, sentiment, timestamp
- **Keyword**: text, category, frequency
- **KnowledgeArticle**: id, title, content, relatedKeywords
- **Sentiment**: POSITIVE, NEGATIVE, NEUTRAL (enum)

### 신규
- **PriceCommitment**: amount, currency, condition, mentionedAt
- **ActionItem**: description, assignee, dueDate, status(OPEN | DONE)
- **PredictedQuestion**: question, suggestedAnswer, relatedKnowledge, confidence
- **ConversationType**: CUSTOMER_MEETING, INTERNAL_MEETING (enum)

## UI 화면 우선순위
1. **CustomerListScreen**: 고객 목록 + 감정 색상 배지
2. **CustomerBriefScreen**: 10초 브리핑 (마지막 대화+회의 요약, 예상 질문, 가격 히스토리)
3. **CardNewsListScreen**: 고객별 카드뉴스 목록 (필터/정렬)
4. **CardDetailScreen**: 카드뉴스 상세 (발언 타임라인, 감정 차트, 지식 패널)
5. **SearchScreen**: 전체 검색 + 하이라이트
6. **UploadScreen**: 녹음 파일 업로드

## 심사 기준
- **1순위: 비즈니스 임팩트** — 세일즈맨의 실제 문제를 해결하는가
- 2순위: 기술 완성도 — TDD, 아키텍처, 테스트 커버리지
- 3순위: 자율 실행 — 키보드 안 만진 정도
- 4순위: 데모 퀄리티 — 시각적 완성도

## Assumptions Exposed & Resolved
| Assumption | Challenge | Resolution |
|------------|-----------|------------|
| 녹음 다시 듣기가 핵심 문제 | Contrarian: 미팅 전 준비가 진짜 문제 아닌가? | 미팅 전 준비 + 갑작스러운 전화 대응이 핵심 |
| 모든 기능이 동등 | Simplifier: 하나만 완벽하면? | 카드뉴스가 1순위, 즉시 브리핑이 2순위 |
| KeyStatement에 태그로 확장 | 3-agent debate (Architect/Critic/Planner) | 별도 도메인 모델로 분리 (SRP, 타입 안전, 테스트 용이) |
| 고객 대화만 분석 | 사내 회의도 포함해야? | 고객 대화 + 사내 회의 모두 업로드/분석/통합 표시 |

## Ontology (Key Entities)
| Entity | Type | Fields | Relationships |
|--------|------|--------|---------------|
| Salesperson | core | name, team | uses App |
| Customer | core | id, name, company | has many Conversations |
| Conversation | core | id, type, date, duration | belongs to Customer, has one ContextCard |
| ContextCard | core | title, summary, sentiment | has KeyStatements, Keywords, Knowledge, Prices, Actions, Questions |
| KeyStatement | core | speaker, content, sentiment, timestamp | belongs to ContextCard |
| Keyword | core | text, category, frequency | belongs to ContextCard |
| KnowledgeArticle | supporting | title, content, relatedKeywords | linked from ContextCard |
| PriceCommitment | core | amount, currency, condition, mentionedAt | belongs to ContextCard |
| ActionItem | core | description, assignee, dueDate, status | belongs to ContextCard |
| PredictedQuestion | core | question, suggestedAnswer, confidence | belongs to Customer, linked to Knowledge |
| CustomerBrief | derived | lastConversation, predictedQuestions, priceHistory | derived from Customer's data |
| Recording | supporting | filePath, format, duration | uploaded to create Conversation |
| BackendAPI | external | endpoints | Mock implementation |
| ProductDocument | external | title, content | source for KnowledgeArticle |
| IncomingCall | event | callerId, timestamp | triggers CustomerBrief display |
| ConversationType | enum | CUSTOMER_MEETING, INTERNAL_MEETING | classifies Conversation |
| InternalMeeting | core | participants, agenda, decisions | subtype of Conversation |
| AutoRecording | future | - | out of scope for this competition |
| SearchResult | derived | matchedCards, highlights | derived from search query |
