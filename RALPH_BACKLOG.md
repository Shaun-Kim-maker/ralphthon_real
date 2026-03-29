# RALPH_BACKLOG.md — 자율 실행 마스터 플랜 (UI 우선 속도전)

## 사용법
이 파일은 자동 실행의 "바통"입니다.
- 매 마일스톤 완료 시 `- [ ]`를 `- [x]`로 변경하고 타임스탬프 추가
- 새 세션은 첫 번째 `- [ ]` 마일스톤부터 시작
- 같은 에러 3회 반복 시 해당 마일스톤 `- [SKIP]`으로 표시하고 다음으로
- 매 마일스톤 완료 시 반드시 git add → git commit → git push origin master
- push 실패 시 1회 재시도 후 로컬 커밋만 하고 진행

## Target Metrics (속도 우선)
- TDD 엄격 준수 불필요 — 컴파일 성공이면 진행
- 테스트는 기존 작성된 것만 유지, 신규 테스트 작성 불필요
- UI를 최대한 빠르고 예쁘게 완성하는 것이 최우선 목표

---

## Phase 1: Architecture + Models + Mock Data (완료)
예상 시간: 5분 | 마일스톤: M-01 ~ M-12

- [x] **M-01**: Gradle 프로젝트 스캐폴딩 + Version Catalog 설정 ← DONE 2026-03-29T10:05
- [x] **M-02**: Domain 모델 11개 ← DONE 2026-03-29T10:15
- [x] **M-03**: Domain 열거형 4개 ← DONE 2026-03-29T10:15
- [x] **M-04**: Domain 모델 검증 테스트 40개 ← DONE 2026-03-29T10:25
- [x] **M-05**: Domain 모델 동등성/복사 테스트 25개 ← DONE 2026-03-29T10:35
- [x] **M-06**: DTO 클래스 10개 ← DONE 2026-03-29T10:40
- [x] **M-07**: Domain Exceptions + Repository 인터페이스 5개 ← DONE 2026-03-29T10:45
- [x] **M-08**: API Service 인터페이스 5개 ← DONE 2026-03-29T10:50
- [x] **M-09**: DI Module (Retrofit + OkHttp + Gson + Dispatchers) ← DONE 2026-03-29T10:55
- [x] **M-10**: UI Theme (Color + Type + Theme) + Navigation skeleton + strings.xml ← DONE 2026-03-29T11:00
- [x] **M-11**: Mock 데이터 생성기 (고객 10명 × 대화 20건 = 200건) ← DONE 2026-03-29T11:10
- [x] **M-12**: Mock 데이터 직렬화/역직렬화 테스트 20개 ← DONE 2026-03-29T11:20

---

## Phase 2: Data Layer + API Clients + Mappers (완료)
예상 시간: 5분 | 마일스톤: M-13 ~ M-24

- [x] **M-13**: TDD CustomerApiClient ← DONE 2026-03-29T11:30
- [x] **M-14**: TDD CardApiClient ← DONE 2026-03-29T11:40
- [x] **M-15**: TDD BriefApiClient ← DONE 2026-03-29T11:50
- [x] **M-16**: TDD KnowledgeApiClient + UploadApiClient ← DONE 2026-03-29T12:00
- [x] **M-17**: TDD CustomerMapper + CardMapper ← DONE 2026-03-29T12:10
- [x] **M-18**: TDD PriceCommitmentMapper + ActionItemMapper + PredictedQuestionMapper ← DONE 2026-03-29T12:20
- [x] **M-19**: TDD KnowledgeMapper + SearchResponseMapper ← DONE 2026-03-29T12:30
- [x] **M-20**: TDD API Request Contract Tests ← DONE 2026-03-29T12:40
- [x] **M-21**: TDD Response Schema Validation ← DONE 2026-03-29T12:50
- [x] **M-22**: TDD Null Safety Contract ← DONE 2026-03-29T13:00
- [x] **M-23**: TDD Backward Compatibility Contract ← DONE 2026-03-29T13:10
- [x] **M-24**: TDD JSON Payload 파싱 테스트 ← DONE 2026-03-29T13:20

---

## Phase 3: Domain UseCases (완료)
예상 시간: 5분 | 마일스톤: M-25 ~ M-36

- [x] **M-25**: GetCustomersUseCase ← DONE 2026-03-29T13:30
- [x] **M-26**: GetCardsByCustomerUseCase ← DONE 2026-03-29T13:40
- [x] **M-27**: GetCardDetailUseCase ← DONE 2026-03-29T13:50
- [x] **M-28**: GetCustomerBriefUseCase (킬링 포인트) ← DONE 2026-03-29T14:00
- [x] **M-29**: SearchCardsUseCase ← DONE 2026-03-29T14:10
- [x] **M-30**: GetKnowledgeUseCase + UploadConversationUseCase ← DONE 2026-03-29T14:20
- [x] **M-31**: GetPriceHistoryUseCase ← DONE 2026-03-29T14:30
- [x] **M-32**: GetPredictedQuestionsUseCase ← DONE 2026-03-29T14:40
- [x] **M-33**: UseCase Parameterized Tests ← DONE 2026-03-29T14:50
- [x] **M-34**: UseCase Error Handling ← DONE 2026-03-29T15:00
- [x] **M-35**: UseCase Concurrency Tests ← DONE 2026-03-29T15:10
- [x] **M-36**: Domain Layer Integration Sanity ← DONE 2026-03-29T15:20

---

## Phase 4: ViewModels + State Machines (완료)
예상 시간: 3분 | 마일스톤: M-37 ~ M-42

- [x] **M-37**: CustomerListViewModel ← DONE 2026-03-29T15:30
- [x] **M-38**: CardNewsListViewModel ← DONE 2026-03-29T15:45
- [x] **M-39**: CardDetailViewModel ← DONE 2026-03-29T16:00
- [x] **M-40**: CustomerBriefViewModel (킬링 포인트) ← DONE 2026-03-29T16:15
- [x] **M-41**: SearchViewModel + UploadViewModel ← DONE 2026-03-29T16:30
- [x] **M-42**: State Machine Tests ← DONE 2026-03-29T16:45

---

## Phase 5: UI 예쁘게 만들기 + 빠른 통합 (지금 즉시 실행)
예상 시간: 3분 | 마일스톤: M-43 ~ M-51

**핵심 원칙: UI를 당장 예쁘게 만들어라. 데모 품질의 시각적 임팩트가 최우선이다.**

디자인 요구사항:
- Material3 디자인 시스템 풀 활용 (ElevatedCard, ModalBottomSheet, FilterChip)
- 모든 리스트에 Shimmer skeleton 로딩 애니메이션
- 그라디언트 헤더, 부드러운 전환 애니메이션
- 감정 색상 악센트 (긍정=초록, 부정=빨강, 중립=회색)
- 고객 아바타: 이니셜 기반 Circle, 그라디언트 배경
- 빈 상태: 일러스트 아이콘 + 안내 메시지 + 재시도 버튼
- 다크모드 지원
- 카드 그림자, 둥근 모서리, 적절한 패딩/마진
- 검색 하이라이트, 최근 검색어 칩
- Bottom Navigation 애니메이션

- [ ] **M-43**: 공통 UI 컴포넌트 8개 (예쁘게)
  - 산출물: ui/components/ShimmerLoading.kt, Avatar.kt, SentimentBar.kt, EmptyState.kt, ErrorState.kt, KeywordChip.kt, SentimentBadge.kt, MeetingTypeBadge.kt
  - 요구: Material3 스타일, 애니메이션, 그라디언트 적용
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-44**: CustomerListScreen + CustomerBriefScreen (킬링 포인트, 예쁘게)
  - 산출물: ui/customer/CustomerListScreen.kt, ui/customer/CustomerBriefScreen.kt, ui/components/PredictedQuestionCard.kt, ui/components/PriceHistoryList.kt, ui/components/ActionItemChecklist.kt, ui/components/CustomerCard.kt
  - 요구: 10초 브리핑 UI, ModalBottomSheet, Shimmer 로딩, 감정 색상, 아바타
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-45**: CardNewsListScreen + CardDetailScreen (예쁘게)
  - 산출물: ui/card/CardNewsListScreen.kt, ui/card/ContextCardItem.kt, ui/card/ConversationTypeFilterChip.kt, ui/card/CardDetailScreen.kt, ui/card/KnowledgePanel.kt, ui/card/StatementTimeline.kt, ui/card/PriceCommitmentSection.kt, ui/card/ActionItemSection.kt
  - 요구: 카드뉴스 스타일, 그라디언트 헤더, 타임라인, 감정 차트
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-46**: SearchScreen + UploadScreen + BottomNavigation (예쁘게)
  - 산출물: ui/search/SearchScreen.kt, ui/upload/UploadScreen.kt, ui/navigation/BottomNavBar.kt
  - 요구: 검색 하이라이트, 최근검색어 칩, 업로드 진행률 애니메이션, Bottom Nav 전환 애니메이션
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-47**: Feature - Favorites + Sort + SearchHistory + Dark Mode
  - 산출물: domain/usecase/ToggleFavoriteUseCase.kt, domain/usecase/SortCardsUseCase.kt, data/repository/FavoritesRepository.kt, data/repository/SearchHistoryRepository.kt, data/repository/ThemeRepository.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-48**: Repository 구현체 5개 (Mock 데이터 반환)
  - 산출물: data/repository/CustomerRepositoryImpl.kt, CardRepositoryImpl.kt, KnowledgeRepositoryImpl.kt, UploadRepositoryImpl.kt, BriefRepositoryImpl.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-49**: DI DataModule + ViewModelModule (Hilt bindings)
  - 산출물: di/DataModule.kt, di/ViewModelModule.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-50**: NavGraph wiring + MainActivity + 전체 화면 연결
  - 산출물: ui/navigation/NavGraph.kt, MainActivity.kt
  - 검증: `./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5` BUILD SUCCESSFUL, git commit + push

- [ ] **M-51**: FINAL BUILD VERIFICATION
  - 검증:
    1. `./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5` → BUILD SUCCESSFUL
    2. git commit + push: `"feat: Final build - beautiful UI completed"`

---

## Summary (수정됨 — UI 예쁘게 최우선)

| Phase | Milestones | 내용 |
|-------|------------|------|
| 1. Architecture + Mock | M-01~M-12 (완료) | 모델, DTO, API, DI, Mock 데이터 |
| 2. Data Layer | M-13~M-24 (완료) | API Client, Mapper, Contract Tests |
| 3. Domain UseCases | M-25~M-36 (완료) | UseCase 9개 |
| 4. ViewModels | M-37~M-42 (완료) | ViewModel 6개 + State Machine |
| 5. UI 예쁘게 + 통합 | M-43~M-51 (진행중) | 예쁜 UI Screen, Repository, DI, Navigation, 최종 빌드 |
