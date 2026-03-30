# RALPH_BACKLOG.md — 자율 실행 마스터 플랜 (우승팀 기준)

## 사용법
이 파일은 자동 실행의 "바통"입니다.
- 매 마일스톤 완료 시 `- [ ]`를 `- [x]`로 변경하고 타임스탬프 추가
- 새 세션은 첫 번째 `- [ ]` 마일스톤부터 시작
- 같은 에러 3회 반복 시 해당 마일스톤 `- [SKIP]`으로 표시하고 다음으로
- 매 마일스톤 완료 시 반드시 git add → git commit → git push origin master
- push 실패 시 1회 재시도 후 로컬 커밋만 하고 진행
- Self-Healing: 5회 테스트 실패 시 스펙은 유지하되 구현 방식만 완전히 교체

## Target Metrics (우승팀 기준)
- 총 코드: ~110,000줄
- 테스트 코드: ~80,000줄 (70%)
- 프로덕션 코드: ~20,000줄
- Phase 1~7 테스트: ~2,900개
- Ambiguity Score: 0.05 (17-round deep interview 완료)

---

## Phase 1: Architecture + Models + Mock Data (Session 1)
예상 시간: 50분 | 마일스톤: M-01 ~ M-12

- [x] **M-01**: Gradle 프로젝트 스캐폴딩 + Version Catalog 설정 ← DONE 2026-03-29T10:05
  - 테스트: 0 | 스펙: N/A
  - 산출물: build.gradle.kts, app/build.gradle.kts, settings.gradle.kts, gradle/libs.versions.toml
  - 의존성: Hilt, Compose, Retrofit, OkHttp, Gson, MockWebServer, JUnit5, MockK, Turbine, Coil, Room
  - 검증: `./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5` BUILD SUCCESSFUL, git commit + push

- [x] **M-02**: Domain 모델 11개 ← DONE 2026-03-29T10:15 (Customer, Contact, Conversation, ContextCard, KeyStatement, Keyword, KnowledgeArticle, PriceCommitment, ActionItem, PredictedQuestion, SearchResult)
  - 테스트: 0 | 스펙: N/A
  - 산출물: domain/model/*.kt (11개 파일)
  - 검증: 컴파일 성공, git commit + push

- [x] **M-03**: Domain 열거형 4개 ← DONE 2026-03-29T10:15 (ConversationType, Sentiment, KeywordCategory, ActionItemStatus)
  - 테스트: 0 | 스펙: N/A
  - 산출물: domain/model/ConversationType.kt, domain/model/Sentiment.kt, domain/model/KeywordCategory.kt, domain/model/ActionItemStatus.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-04**: Domain 모델 검증 테스트 40개 ← DONE 2026-03-29T10:25
  - 테스트: 40 | 스펙: test-specs/domain-tests.md § 14
  - 산출물: test/.../domain/model/ModelValidationTest.kt
  - 포함: Customer/ContextCard/KeyStatement 기존 + PriceCommitment(amount경계/currency/condition), ActionItem(status전이/dueDate), PredictedQuestion(confidence경계/suggestedAnswer)
  - 검증: 40 tests PASS, git commit + push

- [x] **M-05**: Domain 모델 동등성/복사 테스트 25개 ← DONE 2026-03-29T10:35
  - 테스트: 25 | 스펙: test-specs/domain-tests.md § 15
  - 산출물: test/.../domain/model/ModelEqualityTest.kt
  - 포함: 전체 15개 모델의 equals/hashCode/copy/toString
  - 검증: 25 tests PASS, git commit + push

- [x] **M-06**: DTO 클래스 10개 ← DONE 2026-03-29T10:40
  - 테스트: 0 | 스펙: N/A
  - 산출물: data/dto/CustomerDto.kt, data/dto/CardDto.kt, data/dto/KnowledgeDto.kt, data/dto/SearchResponseDto.kt, data/dto/UploadDto.kt, data/dto/ErrorDto.kt, data/dto/PriceCommitmentDto.kt, data/dto/ActionItemDto.kt, data/dto/PredictedQuestionDto.kt, data/dto/ConversationDto.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-07**: Domain Exceptions + Repository 인터페이스 5개 ← DONE 2026-03-29T10:45
  - 테스트: 0 | 스펙: N/A
  - 산출물: domain/model/Exceptions.kt, domain/repository/CustomerRepository.kt, domain/repository/CardRepository.kt, domain/repository/KnowledgeRepository.kt, domain/repository/UploadRepository.kt, domain/repository/BriefRepository.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-08**: API Service 인터페이스 5개 ← DONE 2026-03-29T10:50
  - 테스트: 0 | 스펙: N/A
  - 산출물: data/api/CustomerApiService.kt, data/api/CardApiService.kt, data/api/KnowledgeApiService.kt, data/api/UploadApiService.kt, data/api/BriefApiService.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-09**: DI Module (Retrofit + OkHttp + Gson + Dispatchers) ← DONE 2026-03-29T10:55
  - 테스트: 0 | 스펙: N/A
  - 산출물: di/AppModule.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-10**: UI Theme (Color + Type + Theme) + Navigation skeleton + strings.xml ← DONE 2026-03-29T11:00
  - 테스트: 0 | 스펙: N/A
  - 산출물: ui/theme/Color.kt, ui/theme/Type.kt, ui/theme/Theme.kt, ui/navigation/NavGraph.kt, res/values/strings.xml
  - 검증: 컴파일 성공, git commit + push

- [x] **M-11**: Mock 데이터 생성기 (고객 10명 × 대화 20건 = 200건) ← DONE 2026-03-29T11:10
  - 테스트: 15 | 스펙: N/A
  - 산출물: data/mock/MockDataGenerator.kt, data/mock/MockJsonProvider.kt, test/.../data/mock/MockDataGeneratorTest.kt
  - 포함: 고객별 CUSTOMER_MEETING + INTERNAL_MEETING 혼합, PriceCommitment/ActionItem/PredictedQuestion 포함
  - 검증: 15 tests PASS, git commit + push

- [x] **M-12**: Mock 데이터 직렬화/역직렬화 테스트 20개 ← DONE 2026-03-29T11:20
  - 테스트: 20 | 스펙: test-specs/data-tests.md § 9
  - 산출물: test/.../data/mock/MockDataSerializationTest.kt
  - 포함: 200건 JSON 파싱, 신규 모델 필드 매핑, null safety, ConversationType enum
  - 검증: 20 tests PASS, git commit + push
  - Phase 1 완료: `git commit -m "feat: Phase 1 architecture + mock data (100 tests)"`, git push

---

## Phase 2: Data Layer + API Clients + Mappers (Session 2)
예상 시간: 50분 | 마일스톤: M-13 ~ M-24

- [x] **M-13**: TDD CustomerApiClient 25 tests ← DONE 2026-03-29T11:30
  - 테스트: 25 | 스펙: test-specs/data-tests.md § 1
  - 산출물: data/api/CustomerApiClient.kt, test/.../data/api/CustomerApiClientTest.kt
  - 포함: 성공(1건/10건/빈목록), HTTP에러(400/401/403/404/500/503), 네트워크(타임아웃/DNS/연결끊김), 페이징, 정렬, ConversationType필터
  - 검증: 25 tests PASS, git commit + push

- [x] **M-14**: TDD CardApiClient 30 tests ← DONE 2026-03-29T11:40
  - 테스트: 30 | 스펙: test-specs/data-tests.md § 2
  - 산출물: data/api/CardApiClient.kt, test/.../data/api/CardApiClientTest.kt
  - 포함: CRUD + ConversationType필터 + PriceCommitment/ActionItem 포함 응답 + 대용량 + 빈필드
  - 검증: 30 tests PASS, git commit + push

- [x] **M-15**: TDD BriefApiClient 25 tests ← DONE 2026-03-29T11:50
  - 테스트: 25 | 스펙: test-specs/data-tests.md § 3
  - 산출물: data/api/BriefApiClient.kt, test/.../data/api/BriefApiClientTest.kt
  - 포함: 브리핑조회(성공/실패/빈데이터), PredictedQuestion 응답, 사내회의+고객미팅 통합요약, HTTP에러
  - 검증: 25 tests PASS, git commit + push

- [x] **M-16**: TDD KnowledgeApiClient 20 + UploadApiClient 20 tests ← DONE 2026-03-29T12:00
  - 테스트: 40 | 스펙: test-specs/data-tests.md § 4-5
  - 산출물: data/api/KnowledgeApiClient.kt, data/api/UploadApiClient.kt, test/*Test.kt
  - 포함: Knowledge검색/필터 + Upload진행률/취소/ConversationType선택/대용량/에러
  - 검증: 40 tests PASS, git commit + push

- [x] **M-17**: TDD CustomerMapper 15 + CardMapper 20 tests ← DONE 2026-03-29T12:10
  - 테스트: 35 | 스펙: test-specs/data-tests.md § 6-7
  - 산출물: data/mapper/CustomerMapper.kt, data/mapper/CardMapper.kt, test/*MapperTest.kt
  - 포함: 정상매핑, null기본값, 특수문자, ContextCard 전필드, 신규모델 nested매핑
  - 검증: 35 tests PASS, git commit + push

- [x] **M-18**: TDD PriceCommitmentMapper + ActionItemMapper + PredictedQuestionMapper 30 tests ← DONE 2026-03-29T12:20
  - 테스트: 30 | 스펙: test-specs/data-tests.md § 8
  - 산출물: data/mapper/PriceCommitmentMapper.kt, data/mapper/ActionItemMapper.kt, data/mapper/PredictedQuestionMapper.kt, test/*MapperTest.kt (3개)
  - 포함: 각 모델별 정상/null/경계값/특수문자/통화형식/날짜형식
  - 검증: 30 tests PASS, git commit + push

- [x] **M-19**: TDD KnowledgeMapper + SearchResponseMapper 15 tests ← DONE 2026-03-29T12:30
  - 테스트: 15 | 스펙: test-specs/data-tests.md § 9
  - 산출물: data/mapper/KnowledgeMapper.kt, data/mapper/SearchResponseMapper.kt, test/*MapperTest.kt
  - 검증: 15 tests PASS, git commit + push

- [x] **M-20**: TDD API Request Contract Tests 40개 ← DONE 2026-03-29T12:40
  - 테스트: 40 | 스펙: test-specs/contract-tests.md § 1
  - 산출물: test/.../data/api/contract/RequestContractTest.kt
  - 포함: 5개 API HTTP method/path/query/headers + ConversationType파라미터 + Brief엔드포인트
  - 검증: 40 tests PASS, git commit + push

- [x] **M-21**: TDD Response Schema Validation 40개 ← DONE 2026-03-29T12:50
  - 테스트: 40 | 스펙: test-specs/contract-tests.md § 2
  - 산출물: test/.../data/api/contract/ResponseSchemaTest.kt
  - 포함: 전체 응답 스키마 + 신규 모델 필드 타입 검증
  - 검증: 40 tests PASS, git commit + push

- [x] **M-22**: TDD Null Safety Contract 35개 ← DONE 2026-03-29T13:00
  - 테스트: 35 | 스펙: test-specs/contract-tests.md § 3
  - 산출물: test/.../data/api/contract/NullSafetyContractTest.kt
  - 검증: 35 tests PASS, git commit + push

- [x] **M-23**: TDD Backward Compatibility Contract 35개 ← DONE 2026-03-29T13:10
  - 테스트: 35 | 스펙: test-specs/contract-tests.md § 4
  - 산출물: test/.../data/api/contract/BackwardCompatibilityTest.kt
  - 검증: 35 tests PASS, git commit + push

- [x] **M-24**: TDD JSON Payload 파싱 테스트 30개 ← DONE 2026-03-29T13:20
  - 테스트: 30 | 스펙: test-specs/data-tests.md § 9
  - 산출물: test/.../data/api/JsonPayloadTest.kt
  - 포함: 전체 DTO hardcoded JSON 파싱 + 신규 모델 + ConversationType
  - 검증: 30 tests PASS, git commit + push
  - Phase 2 완료: `git commit -m "feat: Phase 2 data layer (395 tests)"`, git push

---

## Phase 3: Domain UseCases (Session 3)
예상 시간: 50분 | 마일스톤: M-25 ~ M-36

- [x] **M-25**: TDD GetCustomersUseCase 45 tests ← DONE 2026-03-29T13:30
  - 테스트: 45 | 스펙: test-specs/domain-tests.md § 5
  - 산출물: domain/usecase/GetCustomersUseCase.kt, test/.../domain/usecase/GetCustomersUseCaseTest.kt
  - 포함: 성공(1/10/100/빈), 실패(네트워크/401/403/404/500/503/타임아웃), 입력경계(null/빈/특수문자), 정렬(이름/최근/감정), 필터(ConversationType/감정/기간), 페이징, 동시성(중복호출/취소/레이스)
  - 검증: 45 tests PASS, git commit + push

- [x] **M-26**: TDD GetCardsByCustomerUseCase 50 tests ← DONE 2026-03-29T13:40
  - 테스트: 50 | 스펙: test-specs/domain-tests.md § 6
  - 산출물: domain/usecase/GetCardsByCustomerUseCase.kt, test/.../domain/usecase/GetCardsByCustomerUseCaseTest.kt
  - 포함: ConversationType필터(CUSTOMER만/INTERNAL만/전체), 날짜범위, 감정필터, 정렬, 페이징, 동시성
  - 검증: 50 tests PASS, git commit + push

- [x] **M-27**: TDD GetCardDetailUseCase 40 tests ← DONE 2026-03-29T13:50
  - 테스트: 40 | 스펙: test-specs/domain-tests.md § 7
  - 산출물: domain/usecase/GetCardDetailUseCase.kt, test/.../domain/usecase/GetCardDetailUseCaseTest.kt
  - 포함: 전체필드, PriceCommitment/ActionItem/PredictedQuestion/Knowledge, 에러, 빈필드
  - 검증: 40 tests PASS, git commit + push

- [x] **M-28**: TDD GetCustomerBriefUseCase 50 tests (킬링 포인트) ← DONE 2026-03-29T14:00
  - 테스트: 50 | 스펙: test-specs/domain-tests.md § 16
  - 산출물: domain/usecase/GetCustomerBriefUseCase.kt, test/.../domain/usecase/GetCustomerBriefUseCaseTest.kt
  - 포함: 마지막대화요약(고객미팅+사내회의통합), PredictedQuestion, 가격히스토리, 빈데이터, 고객미팅만/사내회의만, 정렬, ActionItem미완료건수, 에러, 동시성
  - 검증: 50 tests PASS, git commit + push

- [x] **M-29**: TDD SearchCardsUseCase 45 tests ← DONE 2026-03-29T14:10
  - 테스트: 45 | 스펙: test-specs/domain-tests.md § 8
  - 산출물: domain/usecase/SearchCardsUseCase.kt, test/.../domain/usecase/SearchCardsUseCaseTest.kt
  - 포함: 쿼리(일반/빈/특수문자/URL인코딩), 필터(ConversationType/감정/키워드/기간), 페이징, 하이라이트, 디바운스
  - 검증: 45 tests PASS, git commit + push

- [x] **M-30**: TDD GetKnowledgeUseCase + UploadConversationUseCase 70 tests ← DONE 2026-03-29T14:20
  - 테스트: 70 | 스펙: test-specs/domain-tests.md § 9, 13
  - 산출물: domain/usecase/GetKnowledgeUseCase.kt, domain/usecase/UploadConversationUseCase.kt, test/*Test.kt
  - 포함: Knowledge(키워드검색/빈결과/관련도정렬) + Upload(성공/실패/취소/재시도/ConversationType선택/진행률/파일형식검증)
  - 검증: 70 tests PASS, git commit + push

- [x] **M-31**: TDD GetPriceHistoryUseCase 30 tests ← DONE 2026-03-29T14:30
  - 테스트: 30 | 스펙: test-specs/domain-tests.md § 17
  - 산출물: domain/usecase/GetPriceHistoryUseCase.kt, test/.../domain/usecase/GetPriceHistoryUseCaseTest.kt
  - 포함: 고객별가격이력(시간순), 통화별그룹핑, 조건변경추적, 빈이력, 최신가격, 범위필터
  - 검증: 30 tests PASS, git commit + push

- [x] **M-32**: TDD GetPredictedQuestionsUseCase 30 tests ← DONE 2026-03-29T14:40
  - 테스트: 30 | 스펙: test-specs/domain-tests.md § 18
  - 산출물: domain/usecase/GetPredictedQuestionsUseCase.kt, test/.../domain/usecase/GetPredictedQuestionsUseCaseTest.kt
  - 포함: 고객별예상질문, confidence순정렬, 관련지식연결, 빈결과, 중복질문제거
  - 검증: 30 tests PASS, git commit + push

- [x] **M-33**: UseCase Parameterized Tests 80 tests ← DONE 2026-03-29T14:50
  - 테스트: 80 | 스펙: test-specs/parameterized-tests.md § 3
  - 산출물: test/.../domain/usecase/UseCaseParameterizedTest.kt
  - 포함: 전체 9개 UseCase의 ID경계/페이징경계/필터조합/ConversationType조합/날짜범위/통화형식
  - 검증: 80 tests PASS, git commit + push

- [x] **M-34**: UseCase Error Handling 60 tests ← DONE 2026-03-29T15:00
  - 테스트: 60 | 스펙: test-specs/error-boundary-tests.md § 5
  - 산출물: test/.../domain/usecase/UseCaseErrorHandlingTest.kt
  - 포함: 전체 9개 UseCase의 잘못된입력/예외전파/에러매핑/Result.failure
  - 검증: 60 tests PASS, git commit + push

- [x] **M-35**: UseCase Concurrency Tests 40 tests ← DONE 2026-03-29T15:10
  - 테스트: 40 | 스펙: test-specs/concurrency-tests.md § 2
  - 산출물: test/.../domain/usecase/UseCaseConcurrencyTest.kt
  - 포함: 디스패처검증, 취소전파, 중복호출, 레이스컨디션, Flow수집취소
  - 검증: 40 tests PASS, git commit + push

- [x] **M-36**: Domain Layer Integration Sanity 20 tests ← DONE 2026-03-29T15:20
  - 테스트: 20 | 스펙: test-specs/domain-tests.md § 10
  - 산출물: test/.../domain/DomainSanityTest.kt
  - 포함: UseCase→Repository 연결 검증, 전체 9개 UseCase 기본 호출, ConversationType 전파
  - 검증: 20 tests PASS, git commit + push
  - Phase 3 완료: `git commit -m "feat: Phase 3 domain usecases (590 tests)"`, git push

---

## Phase 4: ViewModels + State Machines (Session 4-5)
예상 시간: 60분 | 마일스톤: M-37 ~ M-50

- [x] **M-37**: TDD CustomerListViewModel 30 tests ← DONE 2026-03-29T15:30
  - 테스트: 30 | 스펙: test-specs/domain-tests.md § 1
  - 산출물: ui/customer/CustomerListViewModel.kt, test/.../ui/customer/CustomerListViewModelTest.kt
  - 검증: 30 tests PASS, git commit + push

- [x] **M-38**: TDD CardNewsListViewModel 35 tests ← DONE 2026-03-29T15:45
  - 테스트: 35 | 스펙: test-specs/domain-tests.md § 2
  - 산출물: ui/card/CardNewsListViewModel.kt, test/.../ui/card/CardNewsListViewModelTest.kt
  - 포함: ConversationType 필터탭 (전체/고객미팅/사내회의)
  - 검증: 35 tests PASS, git commit + push

- [x] **M-39**: TDD CardDetailViewModel 30 tests ← DONE 2026-03-29T16:00
  - 테스트: 30 | 스펙: test-specs/domain-tests.md § 3
  - 산출물: ui/card/CardDetailViewModel.kt, test/.../ui/card/CardDetailViewModelTest.kt
  - 포함: PriceCommitment/ActionItem/PredictedQuestion/Knowledge 패널 상태
  - 검증: 30 tests PASS, git commit + push

- [x] **M-40**: TDD CustomerBriefViewModel 40 tests (킬링 포인트) ← DONE 2026-03-29T16:15
  - 테스트: 40 | 스펙: test-specs/domain-tests.md § 19
  - 산출물: ui/customer/CustomerBriefViewModel.kt, test/.../ui/customer/CustomerBriefViewModelTest.kt
  - 포함: 브리핑로딩, 마지막대화요약(고객+사내회의통합), 예상질문표시, 가격히스토리, 빈데이터, 에러, 새로고침
  - 검증: 40 tests PASS, git commit + push

- [x] **M-41**: TDD SearchViewModel 35 + UploadViewModel 30 tests ← DONE 2026-03-29T16:30
  - 테스트: 65 | 스펙: test-specs/domain-tests.md § 4, 20
  - 산출물: ui/search/SearchViewModel.kt, ui/upload/UploadViewModel.kt, test/*Test.kt
  - 포함: Search(디바운스/ConversationType필터/최근검색어) + Upload(파일선택/ConversationType선택/진행률/취소)
  - 검증: 65 tests PASS, git commit + push

- [x] **M-42**: State Machine - CustomerList + CustomerBrief 70 tests ← DONE 2026-03-29T16:45
  - 테스트: 70 | 스펙: test-specs/state-machine-tests.md § 1, 5
  - 산출물: test/.../ui/customer/CustomerListStateTest.kt, test/.../ui/customer/CustomerBriefStateTest.kt
  - 포함: Loading→Data→Empty/Error 전이, 브리핑로딩→데이터→에러, 예상질문펼치기/접기
  - 검증: 70 tests PASS, git commit + push

  - Phase 4 완료 (테스트 전용 마일스톤 스킵)

---

## Phase 5: UI Screens + Features + Integration (빠른 구현)
예상 시간: 30분 | 마일스톤: M-43 ~ M-51

- [ ] **M-43**: 공통 UI 컴포넌트 8개
  - 산출물: ui/components/ShimmerLoading.kt, Avatar.kt, SentimentBar.kt, EmptyState.kt, ErrorState.kt, KeywordChip.kt, SentimentBadge.kt, MeetingTypeBadge.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-44**: CustomerListScreen + CustomerBriefScreen (킬링 포인트) ← DONE 2026-03-30T00:10
  - 산출물: ui/customer/CustomerListScreen.kt, ui/customer/CustomerBriefScreen.kt, ui/components/PredictedQuestionCard.kt, ui/components/PriceHistoryList.kt, ui/components/ActionItemChecklist.kt, ui/components/CustomerCard.kt
  - 포함: 10초 브리핑(마지막대화+사내회의 통합, 예상질문+추천답변, 가격히스토리)
  - 검증: 컴파일 성공, git commit + push

- [x] **M-45**: CardNewsListScreen + CardDetailScreen ← DONE 2026-03-30T00:10
  - 산출물: ui/card/CardNewsListScreen.kt, ui/card/ContextCardItem.kt, ui/card/ConversationTypeFilterChip.kt, ui/card/CardDetailScreen.kt, ui/card/KnowledgePanel.kt, ui/card/StatementTimeline.kt, ui/card/PriceCommitmentSection.kt, ui/card/ActionItemSection.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-46**: SearchScreen + UploadScreen + BottomNavigation ← DONE 2026-03-30T00:20
  - 산출물: ui/search/SearchScreen.kt, ui/upload/UploadScreen.kt, ui/navigation/BottomNavBar.kt
  - 포함: UploadScreen ConversationType 라디오 (고객미팅/사내회의)
  - 검증: 컴파일 성공, git commit + push

- [x] **M-47**: Feature - Favorites + Sort + SearchHistory + Dark Mode ← DONE 2026-03-30T00:20
  - 산출물: domain/usecase/ToggleFavoriteUseCase.kt, domain/usecase/SortCardsUseCase.kt, data/repository/FavoritesRepository.kt, data/repository/SearchHistoryRepository.kt, data/repository/ThemeRepository.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-48**: Repository 구현체 5개 (Mock repos serve as impls) ← DONE 2026-03-30T00:25
  - 산출물: data/repository/CustomerRepositoryImpl.kt, CardRepositoryImpl.kt, KnowledgeRepositoryImpl.kt, UploadRepositoryImpl.kt, BriefRepositoryImpl.kt
  - 검증: 컴파일 성공, git commit + push

- [x] **M-49**: DI DataModule + ViewModelModule (Hilt bindings) ← DONE 2026-03-30T00:25
  - 산출물: di/DataModule.kt, di/ViewModelModule.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-50**: NavGraph wiring + MainActivity + 전체 화면 연결
  - 산출물: ui/navigation/NavGraph.kt, MainActivity.kt
  - 검증: `./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5` BUILD SUCCESSFUL, git commit + push

- [ ] **M-51**: FINAL BUILD VERIFICATION
  - 검증:
    1. `./gradlew.bat test --no-daemon 2>&1 | tail -5` → BUILD SUCCESSFUL
    2. `./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5` → BUILD SUCCESSFUL
    3. git commit + push: `"feat: Final build - all screens implemented"`

---

## Summary (Revised — 기능 구현 우선)

| Phase | Milestones | 내용 |
|-------|------------|------|
| 1. Architecture + Mock | M-01~M-12 (완료) | 모델, DTO, API, DI, Mock 데이터 |
| 2. Data Layer | M-13~M-24 (완료) | API Client, Mapper, Contract Tests |
| 3. Domain UseCases | M-25~M-36 (완료) | UseCase 9개 + Parameterized/Error/Concurrency |
| 4. ViewModels | M-37~M-42 (완료) | ViewModel 6개 + State Machine |
| 5. UI + Integration | M-43~M-51 (진행중) | UI Screen, Repository, DI, Navigation, 최종 빌드 |
