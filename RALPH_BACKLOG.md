# RALPH_BACKLOG.md — 자율 실행 마스터 플랜

## 사용법
이 파일은 자동 실행의 "바통"입니다.
- 매 마일스톤 완료 시 `- [ ]`를 `- [x]`로 변경하고 타임스탬프 추가
- 새 세션은 첫 번째 `- [ ]` 마일스톤부터 시작
- 같은 에러 3회 반복 시 해당 마일스톤 `- [SKIP]`으로 표시하고 다음으로
- 매 마일스톤 완료 시 반드시 git add → git commit → git push origin master
- push 실패 시 1회 재시도 후 로컬 커밋만 하고 진행

---

## Phase 1: Architecture Foundation (Session 1)
예상 시간: 40분 | 마일스톤: M-01 ~ M-08

- [ ] **M-01**: Gradle 프로젝트 스캐폴딩 + Version Catalog 설정
  - 테스트: 0 | 스펙: N/A
  - 산출물: build.gradle.kts, settings.gradle.kts, libs.versions.toml
  - 검증: `./gradlew.bat assembleDebug` BUILD SUCCESSFUL, git commit + push

- [ ] **M-02**: Domain 모델 7개 (Customer, Contact, Conversation, ContextCard, KeyStatement, Keyword, KnowledgeArticle)
  - 테스트: 0 | 스펙: N/A
  - 산출물: domain/model/Customer.kt, domain/model/Contact.kt, domain/model/Conversation.kt, domain/model/ContextCard.kt, domain/model/KeyStatement.kt, domain/model/Keyword.kt, domain/model/KnowledgeArticle.kt
  - 검증: 컴파일 성공 (`./gradlew.bat compileDebugKotlin`), git commit + push

- [ ] **M-03**: Domain 열거형 3개 (ConversationType, Sentiment, KeywordCategory) + SearchResult
  - 테스트: 0 | 스펙: N/A
  - 산출물: domain/model/ConversationType.kt, domain/model/Sentiment.kt, domain/model/KeywordCategory.kt, domain/model/SearchResult.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-04**: Domain 모델 검증 테스트 16개
  - 테스트: 16 | 스펙: test-specs/domain-tests.md § 14
  - 산출물: test/.../domain/model/ModelValidationTest.kt
  - 검증: 16 tests PASS (`./gradlew.bat test`), git commit + push

- [ ] **M-05**: Domain 모델 동등성 테스트 10개
  - 테스트: 10 | 스펙: test-specs/domain-tests.md § 15
  - 산출물: test/.../domain/model/ModelEqualityTest.kt
  - 검증: 10 tests PASS, git commit + push

- [ ] **M-06**: DTO 클래스 정의 (CustomerDto, CardDto, KnowledgeDto, SearchResponseDto, UploadDto, ErrorDto)
  - 테스트: 0 | 스펙: N/A
  - 산출물: data/dto/CustomerDto.kt, data/dto/CardDto.kt, data/dto/KnowledgeDto.kt, data/dto/SearchResponseDto.kt, data/dto/UploadDto.kt, data/dto/ErrorDto.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-07**: DI Module (Retrofit + OkHttp + Gson + Dispatchers)
  - 테스트: 0 | 스펙: N/A
  - 산출물: di/AppModule.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-08**: UI Theme (Color + Type + Theme) + Navigation skeleton + strings.xml
  - 테스트: 0 | 스펙: N/A
  - 산출물: ui/theme/Color.kt, ui/theme/Type.kt, ui/theme/Theme.kt, ui/navigation/NavGraph.kt, res/values/strings.xml
  - 검증: 컴파일 성공, git commit + push
  - Phase 1 완료 시: `git add`, `git commit -m "feat: Phase 1 architecture foundation (26 tests)"`, `git push`

---

## Phase 2: Data Layer (Session 2)
예상 시간: 40분 | 마일스톤: M-09 ~ M-16

- [ ] **M-09**: Repository 인터페이스 4개 + Domain Exceptions
  - 테스트: 0 | 스펙: N/A
  - 산출물: domain/repository/CustomerRepository.kt, domain/repository/CardRepository.kt, domain/repository/KnowledgeRepository.kt, domain/repository/UploadRepository.kt, domain/model/Exceptions.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-10**: API Service 인터페이스 4개 (CustomerApiService, CardApiService, KnowledgeApiService, UploadApiService)
  - 테스트: 0 | 스펙: N/A
  - 산출물: data/api/CustomerApiService.kt, data/api/CardApiService.kt, data/api/KnowledgeApiService.kt, data/api/UploadApiService.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-11**: TDD CustomerApiClient 7 tests
  - 테스트: 7 | 스펙: test-specs/data-tests.md § 1
  - 산출물: data/api/CustomerApiClient.kt, test/.../data/api/CustomerApiClientTest.kt
  - 검증: 7 tests PASS, `git commit -m "test(DATA-001): CustomerApiClient 7 tests"`

- [ ] **M-12**: TDD CardApiClient 8 tests
  - 테스트: 8 | 스펙: test-specs/data-tests.md § 2
  - 산출물: data/api/CardApiClient.kt, test/.../data/api/CardApiClientTest.kt
  - 검증: 8 tests PASS, `git commit -m "test(DATA-002): CardApiClient 8 tests"`

- [ ] **M-13**: TDD KnowledgeApiClient 5 + UploadApiClient 5 tests
  - 테스트: 10 | 스펙: test-specs/data-tests.md § 3-4
  - 산출물: data/api/KnowledgeApiClient.kt, data/api/UploadApiClient.kt, test/.../data/api/KnowledgeApiClientTest.kt, test/.../data/api/UploadApiClientTest.kt
  - 검증: 10 tests PASS, `git commit -m "test(DATA-003): KnowledgeApiClient 5 + UploadApiClient 5 tests"`

- [ ] **M-14**: TDD DTO Mappers 4개 (CustomerMapper, CardMapper, KnowledgeMapper, SearchResponseMapper) 20 tests
  - 테스트: 20 | 스펙: test-specs/data-tests.md § 5-8
  - 산출물: data/mapper/CustomerMapper.kt, data/mapper/CardMapper.kt, data/mapper/KnowledgeMapper.kt, data/mapper/SearchResponseMapper.kt, test/.../data/mapper/*MapperTest.kt (4개)
  - 검증: 20 tests PASS, `git commit -m "test(DATA-004): DTO Mappers 4 with 20 tests"`

- [ ] **M-15**: TDD JSON Payload 파싱 테스트 6개
  - 테스트: 6 | 스펙: test-specs/data-tests.md § 9
  - 산출물: test/.../data/api/JsonPayloadTest.kt
  - 검증: 6 tests PASS, `git commit -m "test(DATA-005): JSON payload parsing 6 tests"`

- [ ] **M-16**: TDD API Contract Tests 25개 (Request Contract)
  - 테스트: 25 | 스펙: test-specs/contract-tests.md § 1
  - 산출물: test/.../data/api/contract/RequestContractTest.kt
  - 검증: 25 tests PASS, `git commit -m "test(DATA-006): API request contract 25 tests"`
  - Phase 2 완료 시: `git push "feat: Phase 2 data layer (76 tests)"`

---

## Phase 3: Domain UseCases (Session 3)
예상 시간: 40분 | 마일스톤: M-17 ~ M-24

- [ ] **M-17**: TDD GetCustomersUseCase 8 tests
  - 테스트: 8 | 스펙: test-specs/domain-tests.md § 5
  - 산출물: domain/usecase/GetCustomersUseCase.kt, test/.../domain/usecase/GetCustomersUseCaseTest.kt
  - 검증: 8 tests PASS, `git commit -m "test(DOM-001): GetCustomersUseCase 8 tests"`

- [ ] **M-18**: TDD GetCardsByCustomerUseCase 10 tests
  - 테스트: 10 | 스펙: test-specs/domain-tests.md § 6
  - 산출물: domain/usecase/GetCardsByCustomerUseCase.kt, test/.../domain/usecase/GetCardsByCustomerUseCaseTest.kt
  - 검증: 10 tests PASS, `git commit -m "test(DOM-002): GetCardsByCustomerUseCase 10 tests"`

- [ ] **M-19**: TDD GetCardDetailUseCase 8 tests
  - 테스트: 8 | 스펙: test-specs/domain-tests.md § 7
  - 산출물: domain/usecase/GetCardDetailUseCase.kt, test/.../domain/usecase/GetCardDetailUseCaseTest.kt
  - 검증: 8 tests PASS, `git commit -m "test(DOM-003): GetCardDetailUseCase 8 tests"`

- [ ] **M-20**: TDD SearchCardsUseCase 10 tests
  - 테스트: 10 | 스펙: test-specs/domain-tests.md § 8
  - 산출물: domain/usecase/SearchCardsUseCase.kt, test/.../domain/usecase/SearchCardsUseCaseTest.kt
  - 검증: 10 tests PASS, `git commit -m "test(DOM-004): SearchCardsUseCase 10 tests"`

- [ ] **M-21**: TDD GetKnowledgeUseCase 8 tests
  - 테스트: 8 | 스펙: test-specs/domain-tests.md § 9
  - 산출물: domain/usecase/GetKnowledgeUseCase.kt, test/.../domain/usecase/GetKnowledgeUseCaseTest.kt
  - 검증: 8 tests PASS, `git commit -m "test(DOM-005): GetKnowledgeUseCase 8 tests"`

- [ ] **M-22**: TDD UploadConversationUseCase 8 tests
  - 테스트: 8 | 스펙: test-specs/domain-tests.md § 13
  - 산출물: domain/usecase/UploadConversationUseCase.kt, test/.../domain/usecase/UploadConversationUseCaseTest.kt
  - 검증: 8 tests PASS, `git commit -m "test(DOM-006): UploadConversationUseCase 8 tests"`

- [ ] **M-23**: TDD UseCase Parameterized Tests (입력 경계값) 40 tests
  - 테스트: 40 | 스펙: test-specs/parameterized-tests.md § 3
  - 산출물: test/.../domain/usecase/UseCaseParameterizedTest.kt
  - 검증: 40 tests PASS, `git commit -m "test(DOM-007): UseCase parameterized boundary tests 40"`

- [ ] **M-24**: TDD UseCase Error Handling 30 tests
  - 테스트: 30 | 스펙: test-specs/error-boundary-tests.md § 5
  - 산출물: test/.../domain/usecase/UseCaseErrorHandlingTest.kt
  - 검증: 30 tests PASS, `git commit -m "test(DOM-008): UseCase error handling 30 tests"`
  - Phase 3 완료 시: `git push "feat: Phase 3 domain usecases (132 tests)"`

---

## Phase 4: ViewModels (Session 4)
예상 시간: 40분 | 마일스톤: M-25 ~ M-34

- [ ] **M-25**: TDD CustomerListViewModel 12 tests
  - 테스트: 12 | 스펙: test-specs/domain-tests.md § 1
  - 산출물: ui/customer/CustomerListViewModel.kt, test/.../ui/customer/CustomerListViewModelTest.kt
  - 검증: 12 tests PASS, `git commit -m "test(VM-001): CustomerListViewModel 12 tests"`

- [ ] **M-26**: TDD CardNewsListViewModel 14 tests
  - 테스트: 14 | 스펙: test-specs/domain-tests.md § 2
  - 산출물: ui/card/CardNewsListViewModel.kt, test/.../ui/card/CardNewsListViewModelTest.kt
  - 검증: 14 tests PASS, `git commit -m "test(VM-002): CardNewsListViewModel 14 tests"`

- [ ] **M-27**: TDD CardDetailViewModel 12 tests
  - 테스트: 12 | 스펙: test-specs/domain-tests.md § 3
  - 산출물: ui/card/CardDetailViewModel.kt, test/.../ui/card/CardDetailViewModelTest.kt
  - 검증: 12 tests PASS, `git commit -m "test(VM-003): CardDetailViewModel 12 tests"`

- [ ] **M-28**: TDD SearchViewModel 14 tests
  - 테스트: 14 | 스펙: test-specs/domain-tests.md § 4
  - 산출물: ui/search/SearchViewModel.kt, test/.../ui/search/SearchViewModelTest.kt
  - 검증: 14 tests PASS, `git commit -m "test(VM-004): SearchViewModel 14 tests"`

- [ ] **M-29**: ViewModel State Machine Tests - CustomerList 25 tests
  - 테스트: 25 | 스펙: test-specs/state-machine-tests.md § 1
  - 산출물: test/.../ui/customer/CustomerListStateTest.kt
  - 검증: 25 tests PASS, `git commit -m "test(VM-005): CustomerList state machine 25 tests"`

- [ ] **M-30**: ViewModel State Machine Tests - CardNewsList 30 tests
  - 테스트: 30 | 스펙: test-specs/state-machine-tests.md § 2
  - 산출물: test/.../ui/card/CardNewsListStateTest.kt
  - 검증: 30 tests PASS, `git commit -m "test(VM-006): CardNewsList state machine 30 tests"`

- [ ] **M-31**: ViewModel State Machine Tests - CardDetail 35 tests
  - 테스트: 35 | 스펙: test-specs/state-machine-tests.md § 3
  - 산출물: test/.../ui/card/CardDetailStateTest.kt
  - 검증: 35 tests PASS, `git commit -m "test(VM-007): CardDetail state machine 35 tests"`

- [ ] **M-32**: ViewModel State Machine Tests - Search 30 tests
  - 테스트: 30 | 스펙: test-specs/state-machine-tests.md § 4
  - 산출물: test/.../ui/search/SearchStateTest.kt
  - 검증: 30 tests PASS, `git commit -m "test(VM-008): Search state machine 30 tests"`

- [ ] **M-33**: ViewModel Error State Tests 40 tests
  - 테스트: 40 | 스펙: test-specs/error-boundary-tests.md § 6
  - 산출물: test/.../ui/viewmodel/ViewModelErrorTest.kt
  - 검증: 40 tests PASS, `git commit -m "test(VM-009): ViewModel error states 40 tests"`

- [ ] **M-34**: Coroutine & Flow Tests 20 tests
  - 테스트: 20 | 스펙: test-specs/domain-tests.md § 16
  - 산출물: test/.../ui/viewmodel/CoroutineFlowTest.kt
  - 검증: 20 tests PASS, `git commit -m "test(VM-010): Coroutine and Flow tests 20"`
  - Phase 4 완료 시: `git push "feat: Phase 4 viewmodels (232 tests)"`

---

## Phase 5: UI Screens + Feature Extensions (Session 5)
예상 시간: 40분 | 마일스톤: M-35 ~ M-44

- [ ] **M-35**: 공통 UI 컴포넌트 + CustomerListScreen
  - 공통: ShimmerLoading, Avatar, SentimentBar, SentimentBadge, EmptyState, ErrorState, KeywordChip, StatementBubble
  - Screen: CustomerListScreen + CustomerCard + BottomNavigation
  - 디자인 참조: test-specs/ui-design-spec.md § 2 Screen 1 + § 3
  - 테스트: 0 (Tier 2 — M-39에서 테스트) | 스펙: N/A
  - 산출물: ui/customer/CustomerListScreen.kt, ui/components/CustomerCard.kt, ui/components/EmptyState.kt, ui/components/ErrorState.kt
  - 검증: 컴파일 성공 (`./gradlew.bat compileDebugKotlin`), git commit + push

- [ ] **M-36**: CardNewsListScreen + ContextCardItem + FilterChipRow
  - 디자인 참조: test-specs/ui-design-spec.md § 2 Screen 2
  - 테스트: 0 (Tier 2 — M-44에서 테스트) | 스펙: N/A
  - 산출물: ui/card/CardNewsListScreen.kt, ui/card/ContextCardItem.kt, ui/card/FilterChipRow.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-37**: CardDetailScreen + KnowledgePanel + KeywordChip + SentimentBadge
  - 디자인 참조: test-specs/ui-design-spec.md § 2 Screen 3
  - 테스트: 0 (Tier 2 — M-44에서 테스트) | 스펙: N/A
  - 산출물: ui/card/CardDetailScreen.kt, ui/card/KnowledgePanel.kt, ui/components/KeywordChip.kt, ui/components/SentimentBadge.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-38**: SearchScreen + UploadScreen
  - 디자인 참조: test-specs/ui-design-spec.md § 2 Screen 4-5
  - 테스트: 0 (Tier 2 — M-44에서 테스트) | 스펙: N/A
  - 산출물: ui/search/SearchScreen.kt, ui/upload/UploadScreen.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-39**: Feature Extension: FavoritesRepository + ToggleFavoriteUseCase + 15 tests
  - 테스트: 15 | 스펙: test-specs/feature-extension-tests.md § 1
  - 산출물: domain/usecase/ToggleFavoriteUseCase.kt, data/repository/FavoritesRepository.kt, test/.../domain/usecase/ToggleFavoriteUseCaseTest.kt, test/.../data/repository/FavoritesRepositoryTest.kt
  - 검증: 15 tests PASS, `git commit -m "test(FEAT-001): FavoritesRepository + ToggleFavoriteUseCase 15 tests"`

- [ ] **M-40**: Feature Extension: SortCardsUseCase + 10 tests
  - 테스트: 10 | 스펙: test-specs/feature-extension-tests.md § 2
  - 산출물: domain/usecase/SortCardsUseCase.kt, test/.../domain/usecase/SortCardsUseCaseTest.kt
  - 검증: 10 tests PASS, `git commit -m "test(FEAT-002): SortCardsUseCase 10 tests"`

- [ ] **M-41**: Feature Extension: SearchHistoryRepository + 20 tests
  - 테스트: 20 | 스펙: test-specs/feature-extension-tests.md § 3
  - 산출물: data/repository/SearchHistoryRepository.kt, ui/search/SearchHistoryViewModel.kt, test/.../data/repository/SearchHistoryRepositoryTest.kt, test/.../ui/search/SearchHistoryViewModelTest.kt
  - 검증: 20 tests PASS, `git commit -m "test(FEAT-003): SearchHistoryRepository 20 tests"`

- [ ] **M-42**: Feature Extension: UploadViewModel + Progress 15 tests
  - 테스트: 15 | 스펙: test-specs/feature-extension-tests.md § 4
  - 산출물: ui/upload/UploadViewModel.kt, test/.../ui/upload/UploadViewModelTest.kt
  - 검증: 15 tests PASS, `git commit -m "test(FEAT-004): UploadViewModel progress 15 tests"`

- [ ] **M-43**: Feature Extension: ThemeRepository + Dark Mode 20 tests
  - 테스트: 20 | 스펙: test-specs/feature-extension-tests.md § 5
  - 산출물: data/repository/ThemeRepository.kt, test/.../data/repository/ThemeRepositoryTest.kt, test/.../ui/theme/ThemeTest.kt
  - 검증: 20 tests PASS, `git commit -m "test(FEAT-005): ThemeRepository dark mode 20 tests"`

- [ ] **M-44**: Feature Extension: Remaining UI tests for favorites + sort + history 70 tests
  - 테스트: 70 | 스펙: test-specs/feature-extension-tests.md § 1-3 (UI 부분)
  - 산출물: test/.../ui/feature/FavoritesUiTest.kt, test/.../ui/feature/SortUiTest.kt, test/.../ui/feature/SearchHistoryUiTest.kt, test/.../ui/feature/ScreenComposeTest.kt
  - 검증: 70 tests PASS, `git commit -m "test(FEAT-006): UI feature tests 70"`
  - Phase 5 완료 시: `git push "feat: Phase 5 UI screens + features (150 tests)"`

---

## Phase 6: Deep Testing (Session 6)
예상 시간: 40분 | 마일스톤: M-45 ~ M-52

- [ ] **M-45**: Parameterized Tests - Customer Input Validation 30 tests
  - 테스트: 30 | 스펙: test-specs/parameterized-tests.md § 1
  - 산출물: test/.../domain/model/CustomerParameterizedTest.kt
  - 검증: 30 tests PASS, `git commit -m "test(PARAM-001): Customer input validation parameterized 30"`

- [ ] **M-46**: Parameterized Tests - ContextCard Field Validation 30 tests
  - 테스트: 30 | 스펙: test-specs/parameterized-tests.md § 2
  - 산출물: test/.../domain/model/CardParameterizedTest.kt
  - 검증: 30 tests PASS, `git commit -m "test(PARAM-002): ContextCard field validation parameterized 30"`

- [ ] **M-47**: Parameterized Tests - API Response + Mapper Edge Cases 70 tests
  - 테스트: 70 | 스펙: test-specs/parameterized-tests.md § 4-5
  - 산출물: test/.../data/ParameterizedApiMapperTest.kt
  - 검증: 70 tests PASS, `git commit -m "test(PARAM-003): API response + mapper edge cases 70"`

- [ ] **M-48**: Parameterized Tests - ViewModel State Transition 30 tests
  - 테스트: 30 | 스펙: test-specs/parameterized-tests.md § 6
  - 산출물: test/.../ui/viewmodel/ParameterizedStateTest.kt
  - 검증: 30 tests PASS, `git commit -m "test(PARAM-004): ViewModel state transition parameterized 30"`

- [ ] **M-49**: Error Boundary - HTTP Error Exhaustive 40 tests
  - 테스트: 40 | 스펙: test-specs/error-boundary-tests.md § 1
  - 산출물: test/.../data/api/HttpErrorExhaustiveTest.kt
  - 검증: 40 tests PASS, `git commit -m "test(ERR-001): HTTP error exhaustive 40 tests"`

- [ ] **M-50**: Error Boundary - Network + JSON Failures 60 tests
  - 테스트: 60 | 스펙: test-specs/error-boundary-tests.md § 2-3
  - 산출물: test/.../data/api/NetworkJsonFailureTest.kt
  - 검증: 60 tests PASS, `git commit -m "test(ERR-002): Network + JSON failure boundary 60 tests"`

- [ ] **M-51**: Error Boundary - Repository Error Propagation 30 tests
  - 테스트: 30 | 스펙: test-specs/error-boundary-tests.md § 4
  - 산출물: test/.../data/repository/ErrorPropagationTest.kt
  - 검증: 30 tests PASS, `git commit -m "test(ERR-003): Repository error propagation 30 tests"`

- [ ] **M-52**: Contract Tests - Response Schema + Null Safety + Backward Compatibility 75 tests
  - 테스트: 75 | 스펙: test-specs/contract-tests.md § 2-4
  - 산출물: test/.../data/api/contract/SchemaValidationTest.kt, test/.../data/api/contract/NullSafetyContractTest.kt, test/.../data/api/contract/BackwardCompatibilityTest.kt
  - 검증: 75 tests PASS, `git commit -m "test(CONTRACT-001): Schema + null safety + backward compat 75"`
  - Phase 6 완료 시: `git push "feat: Phase 6 deep testing (365 tests)"`

---

## Phase 7: Integration + Wiring + Final (Session 7)
예상 시간: 30분 | 마일스톤: M-53 ~ M-58

- [ ] **M-53**: Repository Implementations 4개 (CustomerRepositoryImpl, CardRepositoryImpl, KnowledgeRepositoryImpl, UploadRepositoryImpl)
  - 테스트: 0 | 스펙: N/A
  - 산출물: data/repository/CustomerRepositoryImpl.kt, data/repository/CardRepositoryImpl.kt, data/repository/KnowledgeRepositoryImpl.kt, data/repository/UploadRepositoryImpl.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-54**: DI DataModule (Hilt bindings for repositories)
  - 테스트: 0 | 스펙: N/A
  - 산출물: di/DataModule.kt
  - 검증: 컴파일 성공, git commit + push

- [ ] **M-55**: NavGraph wiring to real screens + MainActivity
  - 테스트: 0 | 스펙: N/A
  - 산출물: ui/navigation/NavGraph.kt (updated with all screens), MainActivity.kt (updated)
  - 검증: `./gradlew.bat assembleDebug` BUILD SUCCESSFUL

- [ ] **M-56**: Concurrency Tests 80 tests
  - 테스트: 80 | 스펙: test-specs/concurrency-tests.md § 1-5
  - 산출물: test/.../ConcurrencyTest.kt, test/.../data/api/ConcurrentApiTest.kt, test/.../ui/viewmodel/ConcurrentViewModelTest.kt
  - 검증: 80 tests PASS, `git commit -m "test(CONC-001): Concurrency tests 80"`

- [ ] **M-57**: Repository Integration Tests 20 tests
  - 테스트: 20 | 스펙: test-specs/domain-tests.md § 10-12
  - 산출물: test/.../data/repository/CustomerRepositoryImplTest.kt, test/.../data/repository/CardRepositoryImplTest.kt, test/.../data/repository/KnowledgeRepositoryImplTest.kt, test/.../data/repository/UploadRepositoryImplTest.kt
  - 검증: 20 tests PASS, `git commit -m "test(INT-001): Repository integration tests 20"`

- [ ] **M-58**: FINAL VERIFICATION
  - 테스트: ALL | 스펙: ALL
  - 산출물: N/A (검증만)
  - 검증:
    1. `./gradlew.bat test --no-daemon` → BUILD SUCCESSFUL
    2. 총 테스트 수 >= 1000 (XML 리포트 확인: `app/build/reports/tests/`)
    3. `./gradlew.bat assembleDebug --no-daemon` → BUILD SUCCESSFUL
    4. `git push` → `"feat: Phase 7 final verification - all tests pass"`

---

## Summary

| Phase | Session | Milestones | New Tests | Cumulative |
|-------|---------|------------|-----------|------------|
| 1. Architecture | 1 | M-01~M-08 | 26 | 26 |
| 2. Data Layer | 2 | M-09~M-16 | 76 | 102 |
| 3. Domain UseCases | 3 | M-17~M-24 | 132 | 234 |
| 4. ViewModels | 4 | M-25~M-34 | 232 | 466 |
| 5. UI + Features | 5 | M-35~M-44 | 150 | 616 |
| 6. Deep Testing | 6 | M-45~M-52 | 365 | 981 |
| 7. Integration | 7 | M-53~M-58 | 100 | 1,081 |

---

## Session Resume Instructions

새 세션 시작 시 Claude에게 전달할 프롬프트:

```
이 파일을 읽고 첫 번째 `- [ ]` 마일스톤부터 실행하세요.
각 마일스톤 완료 시:
1. `- [ ]`를 `- [x] (완료: YYYY-MM-DD HH:MM)`로 업데이트
2. git add, git commit, git push 실행
3. 같은 에러 3회 이상 시 `- [SKIP]`으로 표시 후 다음 마일스톤으로
4. Phase 완료 시 phase 완료 커밋 메시지로 push
```
