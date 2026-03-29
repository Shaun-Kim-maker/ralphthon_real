# Ralphthon Competition — Autonomous Execution Rules

## Overview
해커톤(랄프톤)에서 TDD-first 방법론으로 개발하는 Android 애플리케이션.
앱 이름: **Zipi** (지피지기면 백전백승 — 知彼知己 百戰百勝)
이 프로젝트는 **사람 개입 없이 완전 자율 실행**됩니다.

## Tech Stack
- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM + Clean Architecture
- DI: Hilt
- Testing: JUnit 5 (unit), MockK, Compose Testing
- Build: Gradle Kotlin DSL with Version Catalog
- SDK: compileSdk=35, targetSdk=35, minSdk=26

## Project Structure
```
app/src/main/java/com/ralphthon/app/
├── di/          # Hilt DI modules
├── data/        # Data layer
│   ├── api/     # Retrofit API services
│   ├── dto/     # Data Transfer Objects
│   ├── mapper/  # DTO to Domain mappers
│   └── repository/ # Repository implementations
├── domain/      # Domain layer
│   ├── model/   # Domain models + exceptions
│   ├── repository/ # Repository interfaces
│   └── usecase/ # Use cases
└── ui/          # Presentation layer
    ├── theme/
    ├── navigation/
    ├── customer/
    ├── card/
    ├── search/
    ├── upload/
    └── components/
```

## 자율 실행 프로토콜

### 세션 시작 시 (매번 반드시 실행)
1. `.omc/specs/deep-interview-crm-sales.md` 읽기 → 앱의 목표/도메인모델/킬링포인트 파악
2. `RALPH_BACKLOG.md` 읽기 → 첫 번째 `- [ ]` 마일스톤 찾기
3. `git log --oneline -3` → 마지막 커밋 확인
4. `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3` → 빌드 상태 확인 (M-01 전이면 스킵)
5. 해당 마일스톤의 test-spec 섹션**만** 읽기 (전체 파일 읽지 않음)
6. 즉시 TDD 루프 시작 — 사용자에게 질문하지 않고 바로 실행

### 새 세션이 알아야 할 핵심 파일
- `.omc/specs/deep-interview-crm-sales.md`: 앱 전체 스펙 (Ambiguity 0.05)
- `RALPH_BACKLOG.md`: 마일스톤 76개 + Tier 확장 프로토콜
- `CLAUDE.md`: 자율 실행 규칙, TDD, 컨텍스트 압축, 터미널 규칙
- `docs/harness-engineering-reference.md`: 우승팀 전략 참고
- `test-specs/`: 마일스톤별 테스트 명세 (해당 섹션만 읽기)
- `.claude/settings.json`: 전체 권한 허용 (멈춤 방지)

### TDD 루프 (마일스톤별)
1. test-spec에서 해당 섹션의 테스트 목록 읽기
2. RED: 실패하는 테스트 작성
3. GREEN: 테스트 통과시키는 최소 코드
4. REFACTOR: 정리
5. `./gradlew.bat test --no-daemon 2>&1 | tail -5` 로 검증
6. RALPH_BACKLOG.md에서 해당 마일스톤 `- [x]`로 업데이트
7. git add → git commit → 다음 마일스톤으로

### Git Push 규칙 (Critical)
- 매 마일스톤 완료 시 반드시: git add → git commit → git push origin master
- 커밋 없이 다음 마일스톤으로 넘어가지 않는다
- push 실패 시: git pull --rebase origin master → 재시도 1회 → 실패 시 로컬 커밋만 하고 진행
- Phase 완료 시: 반드시 push 확인

### RALPH_BACKLOG.md 형식
```
## Phase N: [Phase Title]
- [ ] M-01: [milestone description]
- [x] M-02: [milestone description] (완료 시 x로 변경)
- [SKIP] M-03: [reason] (3회 실패 시 SKIP 표시)
```

### 컨텍스트 절약 규칙 (Critical)
- gradlew 출력: `| tail -5` 사용 (전체 출력 절대 금지)
- 파일 읽기: offset/limit 파라미터 사용, 필요한 부분만 읽기
- 에러 분석: 첫 10줄만, 나머지 생략
- 이전 마일스톤 파일 다시 읽지 않기
- 테스트 실패 3회 반복 → `- [SKIP]`으로 표시하고 다음으로 이동

### 에러 복구
- 컴파일 에러: 에러 메시지의 `파일:line` 확인 → 해당 부분만 수정
- 테스트 실패: 실패 테스트 이름 확인 → 해당 테스트/코드만 수정
- 같은 에러 3회: 마일스톤 SKIP, 이유를 RALPH_BACKLOG.md에 기록
- 빌드 전체 실패: `./gradlew.bat clean --no-daemon` 후 재시도 1회

### Phase 완료 시
1. 모든 변경사항 git commit + push
2. RALPH_BACKLOG.md 업데이트 확인
3. 현재 Phase의 남은 `- [ ]` 마일스톤이 없으면 세션 종료

### Phase 7: 최종 통합 테스트 + 버그 회귀 (Critical)
Phase 1~6 완료 후 마지막에 실행하는 통합 검증 단계:
1. 에뮬레이터 기동 → `./gradlew.bat connectedDebugAndroidTest --no-daemon 2>&1 | tail -10`
2. androidTest 실패 시 → 해당 버그의 원인 Phase를 식별
3. 원인 Phase의 코드로 돌아가서 수정 (unit test도 함께 보강)
4. `./gradlew.bat test --no-daemon 2>&1 | tail -5` 로 기존 unit test 회귀 없음 확인
5. 수정 완료 → git commit + push → 다시 connectedAndroidTest 재실행
6. 모든 androidTest 통과할 때까지 2~5 반복 (최대 3회, 이후 SKIP)
7. 회귀 수정 시 다른 Phase 코드를 건드리지 않는다 (해당 Phase 범위만)

## TDD Rules

### Two-Tier Strategy
**Tier 1: Strict Red-Green-Refactor** — ViewModel, UseCase, Repository, Mapper
- 프로덕션 코드 작성 전 반드시 테스트 파일 먼저 생성
- `app/src/test/java/com/.../SomeClassTest.kt` 형태

**Tier 2: Deferred UI Testing** — Composable, UI components
- Phase 1~6: UI 코드 작성 시 ViewModel unit test로 로직 검증 (src/test/)
- androidTest는 작성하지 않음 — Phase 7에서 일괄 작성/실행
- Phase 7: 에뮬레이터에서 connectedAndroidTest 일괄 실행
- `app/src/androidTest/` 에 UI 테스트 작성은 Phase 7 전용

### Test Naming Convention
```
should_[expectedBehavior]_when_[condition]
```
예시: `should_returnError_when_inputIsEmpty`

### Test Runner
- Local unit tests (`src/test/`): JUnit 5 via `de.mannodermaus.android-junit5`
- Instrumented tests (`src/androidTest/`): JUnit 4 + AndroidJUnit4

## Terminal Rules
- 절대 `$(...)` 형태의 명령어 치환 사용 금지
- 모든 터미널 명령어는 한 번에 하나씩 단순하게 실행
- `git commit -m "메시지"` 형태로 단순하게 작성
- 복잡한 파이프라인 사용 금지 (`| tail -5` 정도만 허용)
- heredoc 또는 multi-line bash 명령어 사용 금지

## Git Convention
- Conventional Commits: `feat:`, `fix:`, `test:`, `chore:`, `refactor:`
- TDD 테스트 커밋: `test(M-XX): description`
- Phase 완료 커밋: `feat: Phase N description (총 N tests)`
- 커밋 메시지는 영어로, 72자 이내
- 테스트가 통과한 상태에서만 commit 허용

## Architecture Rules

### Domain Layer Isolation
- Domain layer는 `data` 또는 `ui` 를 절대 import하지 않는다
- 모든 UseCase는 `Result<T>` 를 반환
- ViewModel은 `sealed class UiState` 사용
- 모든 API 에러는 domain exception으로 변환

## UI Quality Rules (데모 품질)
이 앱은 해커톤 데모용 — 시각적 임팩트가 심사에 영향을 줌

### 필수 의존성
- Coil: 이미지 로딩 (아바타, 플레이스홀더)
- Accompanist: Shimmer/Placeholder 로딩
- Material3: ElevatedCard, ModalBottomSheet, FilterChip

### UI 구현 기준
- 모든 리스트: Shimmer skeleton 로딩 필수
- 모든 Screen: Loading, Data, Empty, Error 4가지 상태 처리
- Customer 아바타: 이니셜 기반 Circle (회사명 첫 2자)
- Card News: 좌측 감정 색상 악센트, 감정 미니바, 키워드 칩
- Card Detail: 그라디언트 헤더, 타임라인 발언 뷰, 감정 차트
- 검색: 하이라이트, 최근 검색어 칩
- 빈 상태: 아이콘 + 메시지 + 재시도 버튼
- Bottom Navigation: 3탭 (고객, 검색, 업로드)

### UI 디자인 참조
test-specs/ui-design-spec.md에 전체 디자인 사양이 있다.
UI Screen 구현 시 반드시 이 파일을 참조하라.

### Hilt DI Modules
- `@Module` + `@InstallIn` 어노테이션 필수
- `*Module.kt` 파일은 TDD 예외 (integration tests로 검증)

### Compose UI
- `*Theme.kt`, `*Color.kt`, `*Type.kt` 파일은 TDD 예외
- `*Navigation.kt` 파일은 TDD 예외
- 모든 Screen Composable은 androidTest 대상

## Files Exempt from TDD
- `*Application.kt`
- `*Module.kt` (Hilt/Dagger modules)
- `*Theme.kt`, `*Color.kt`, `*Type.kt`
- `*Navigation.kt`

## Autonomous Execution Rules (질문 금지)

### 절대 질문하지 않는다
- 사용자에게 질문하거나 확인을 요청하지 않는다
- 모호한 부분은 test-spec과 RALPH_BACKLOG.md에 정의된 대로 따른다
- 판단이 필요한 경우 보수적 선택 (더 안전한 쪽)을 한다
- "~할까요?", "~하시겠습니까?" 형태의 출력 금지

### 스펙 타협 절대 불가
- Plan B(우회)를 선택하더라도 기능의 깊이(Depth)와 스펙은 절대 축소하거나 포기할 수 없다
- 외부 라이브러리를 바꾸거나 구조를 갈아엎더라도 목표 자체는 무조건 100% 달성해야만 다음 마일스톤으로 넘어갈 수 있다
- 테스트 수를 줄이거나 assertion을 약하게 만드는 것은 절대 금지

### 컨텍스트 압축 (마라톤 랄프 핵심)

#### 원칙: Subagent 격리로 메인 루프 컨텍스트를 가볍게 유지
마일스톤의 무거운 작업(코드 작성, 에러 디버깅, 테스트 반복)은 반드시 **Agent(subagent_type="oh-my-claudecode:executor")에게 위임**한다. 시행착오와 에러 로그는 subagent 내부에서 소비되고, 메인 루프에는 결과 요약만 반환된다.

#### 마일스톤별 소크라틱 리즈닝 (모호성 제거)
subagent에게 마일스톤을 위임하기 전, 메인 루프가 스스로 3~5개의 질문을 던져 모호성을 제거한다:
1. "이 마일스톤의 산출물 파일 경로가 기존 코드와 충돌하지 않는가?"
2. "테스트에서 import해야 할 클래스의 정확한 패키지 경로는?"
3. "이 마일스톤의 테스트가 이전 마일스톤의 코드에 의존하는가? 의존한다면 어떤 파일?"
4. "ConversationType 분기가 필요한 마일스톤인가? 필요하다면 어디서?"
5. "신규 모델(PriceCommitment/ActionItem/PredictedQuestion)이 이 마일스톤에 포함되는가?"
이 질문들의 답을 subagent 프롬프트에 명시적으로 포함시킨다. 질문은 사용자에게 하지 않고 코드베이스를 읽어서 스스로 답한다.

#### 메인 루프의 마일스톤 실행 패턴
```
1. RALPH_BACKLOG.md에서 다음 `- [ ]` 마일스톤 읽기
2. 해당 test-spec 섹션만 읽기 (전체 파일 X, 해당 섹션만 offset/limit)
3. 소크라틱 리즈닝: 위 5개 질문에 스스로 답하기 (코드베이스 확인)
4. Agent(executor)에게 위임:
   - 프롬프트에 마일스톤 목표 + test-spec 내용 + 파일 경로 전달
   - subagent가 TDD 루프 (RED → GREEN → REFACTOR) 수행
   - subagent가 `./gradlew.bat test --no-daemon 2>&1 | tail -5` 로 검증
   - subagent가 결과를 1~3줄 요약으로 반환
4. 메인 루프: 결과 확인 → git add → git commit → git push
5. RALPH_BACKLOG.md 업데이트 (`- [x]`)
6. 다음 마일스톤으로 (이전 마일스톤의 시행착오는 subagent와 함께 소멸)
```

#### 메인 루프에서 절대 하지 않는 것
- gradlew 전체 출력을 직접 읽지 않는다 (subagent가 처리)
- 에러 스택트레이스를 직접 분석하지 않는다 (subagent가 처리)
- 프로덕션 코드를 직접 작성하지 않는다 (subagent가 처리)
- 이전 마일스톤에서 생성한 파일을 다시 읽지 않는다

#### 메인 루프가 직접 하는 것 (가벼운 작업만)
- RALPH_BACKLOG.md 읽기/업데이트
- test-spec 해당 섹션만 읽기
- git add, git commit, git push (단순 명령어)
- subagent 위임 및 결과 수신

#### 컨텍스트 예산
- 마일스톤당 메인 루프 소비: ~500 토큰
- 58개 마일스톤 전체: ~29,000 토큰 (1M 컨텍스트의 3%)
- Tier 확장 포함 170개 마일스톤: ~85,000 토큰 (1M의 8.5%)
- 나머지 컨텍스트: CLAUDE.md, rules, 시스템 프롬프트 등 상시 점유분

#### 압축 감지 후 복구
컴파일 에러에서 import 경로가 틀린 경우 (자동 압축으로 인한 hallucination 징후):
1. subagent에게 `find app/src/main -name "해당클래스.kt"` 로 실제 경로 확인 위임
2. subagent가 올바른 import로 수정
3. 이 패턴이 한 마일스톤 내에서 3회 이상 → 마일스톤 SKIP

#### 좀비 루프 방지
- subagent 위임 시 반드시 타임아웃 명시: 단순 작업 2분, 복잡 작업 5분
- subagent가 같은 에러를 3회 반복 보고 → 해당 마일스톤 SKIP
- SKIP 시 RALPH_BACKLOG.md에 `- [SKIP] 사유: {에러 요약}` 기록 후 다음 진행

### Self-Healing (우승팀 전략)
- 한 마일스톤에서 5번 이상 테스트를 통과하지 못하면, 요구사항(스펙)은 절대 타협하지 말고 내부 구현 방식(라이브러리 교체, 아키텍처 우회 등)만 완전히 갈아엎어 다시 시도
- 스펙 확장 절대 금지: 코딩 중 아이디어가 떠올라도 절대 스펙을 임의로 확장하지 않는다
- 70% Test-Driven: 테스트 코드가 전체 코드의 70%를 차지해야 한다
- I Ship Code I Don't Read: 사람이 읽기 좋은 주석/포맷팅보다 에이전트가 이해하고 고치기 좋은 구조를 우선한다
- 거짓 성공 방지: "테스트 통과했다"는 주장이 아니라 실제 쉘 실행 결과만 인정한다

### 백엔드 API 전략
- 백엔드 API는 존재하지 않음 — Mock API로 시연용 데이터 사용
- MockWebServer 또는 로컬 JSON 파일로 API 응답 시뮬레이션
- 모든 API Client는 실제 백엔드 연결 시 코드 변경 없이 전환 가능하도록 인터페이스 기반 설계
- Mock 데이터 규모: 고객 10명 × 대화 20건 = 200건

### 대화 유형
- CUSTOMER_MEETING: 세일즈맨 ↔ 고객 간 외부 대화 (녹음 기반)
- INTERNAL_MEETING: 해당 고객사 건에 대한 사내 팀 회의 (녹음 기반)
- 두 유형 모두 업로드 → 분석 → 카드뉴스 생성
- 고객 브리핑 시 두 유형이 통합되어 표시 (마지막 대화 요약에 사내 회의 내용 포함)

### 신규 도메인 모델
- PriceCommitment: amount, currency, condition, mentionedAt (별도 모델 — 3-agent 합의)
- ActionItem: description, assignee, dueDate, status(OPEN/DONE) (별도 모델)
- PredictedQuestion: question, suggestedAnswer, relatedKnowledge, confidence (Mock 데이터)
- ConversationType: CUSTOMER_MEETING, INTERNAL_MEETING (enum)

### 데모 킬링 포인트
고객 탭 한 번 → 10초 안에 맥락 파악:
1. 마지막 대화 요약 (고객 대화 + 사내 회의 통합)
2. 예상 질문 + 추천 답변 + 관련 지식/키워드
3. 가격/조건 히스토리

### 권한 및 네트워크
- AndroidManifest.xml에 INTERNET, ACCESS_NETWORK_STATE 권한 포함
- 모든 네트워크 요청은 OkHttp + Retrofit 기반
- 프라이빗/공용 네트워크 모두 허용 (networkSecurityConfig 불필요 — minSdk 26)

## Anti-Patterns (절대 하지 말 것)
- 테스트 없이 프로덕션 코드 작성
- `@Disabled` 또는 `@Ignore` 로 테스트 스킵
- 프로덕션 코드에 `TODO`, `FIXME`, `HACK` 남기기
- 아키텍처 스펙에 없는 신규 의존성 추가
- 현재 마일스톤 범위 밖의 파일 수정 (scope creep)
- 같은 에러에 3회 이상 반복 시도
- 사용자에게 질문하거나 승인 요청하기
- `$(...)`, heredoc, `&&` 복합 명령어 사용하기
