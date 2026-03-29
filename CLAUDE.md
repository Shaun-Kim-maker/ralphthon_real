# Ralphthon Competition — Autonomous Execution Rules

## Overview
해커톤(랄프톤)에서 TDD-first 방법론으로 개발하는 Android 애플리케이션.
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
1. `RALPH_BACKLOG.md` 읽기 → 첫 번째 `- [ ]` 마일스톤 찾기
2. `git log --oneline -3` → 마지막 커밋 확인
3. `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3` → 빌드 상태 확인
4. 해당 마일스톤의 test-spec 섹션**만** 읽기 (전체 파일 읽지 않음)
5. 즉시 TDD 루프 시작

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

### 마일스톤 간 컨텍스트 리셋
- 하나의 마일스톤을 성공적으로 git push 하고 나면, 이전 마일스톤에서 겪었던 시행착오나 에러 로그, 토론 내역은 모두 잊는다
- 오직 RALPH_BACKLOG.md의 다음 목표와 해당 test-spec 섹션만 새로 읽어들인 후 완전히 새로운 마음으로 루프를 재시작한다
- 이전 마일스톤에서 생성한 파일을 다시 읽지 않는다 (현재 마일스톤에서 import해야 하는 경우만 예외)

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
