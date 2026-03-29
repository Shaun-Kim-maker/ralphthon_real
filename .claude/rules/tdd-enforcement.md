# TDD Enforcement Rules (랄프톤 전용)

## Two-Tier TDD Strategy

### Tier 1: Strict Red-Green-Refactor (비즈니스 로직)
적용 대상: ViewModel, Repository, UseCase, Mapper, Utility 등 순수 Kotlin 코드
- RED: 실패하는 테스트를 먼저 작성
- GREEN: 테스트를 통과시키는 최소한의 코드 작성
- REFACTOR: 테스트가 통과하는 상태에서 코드 개선
- 프로덕션 코드 없이 테스트가 먼저 존재해야 함

### Tier 2: Pragmatic Test-Alongside (Compose UI)
적용 대상: Composable 함수, UI 컴포넌트
- UI 코드와 테스트를 같은 마일스톤에서 함께 작성
- UI 코드가 먼저 올 수 있지만, 마일스톤 완료 전에 반드시 테스트 존재
- Semantic assertion과 state 검증에 집중 (픽셀 단위 테스트 X)

## Test Runner Configuration
- Local unit tests (`src/test/`): JUnit 5 via `de.mannodermaus.android-junit5`
- Instrumented tests (`src/androidTest/`): JUnit 4 + AndroidJUnit4

## Test Naming Convention
- `should_[expectedBehavior]_when_[condition]`
- 예: `should_returnError_when_inputIsEmpty`

## Minimum Requirements
- 모든 ViewModel에 대해 최소 1개 이상의 단위 테스트
- 모든 Repository에 대해 최소 1개 이상의 단위 테스트
- 핵심 화면에 대해 최소 1개 이상의 UI 테스트
