# Session Relay Rules (마라톤 실행 전용)

## Problem
3시간 랄프 루프 시 컨텍스트 윈도우가 가득 차서 세션이 죽거나 품질이 저하된다.

## Solution: Phase-Based Session Relay

### 핵심 원칙
- 하나의 세션이 모든 마일스톤을 처리하지 않는다
- RALPH_BACKLOG.md가 "바통"이다 — 어떤 세션이든 이 파일만 읽으면 현재 상태를 안다
- 각 Phase(8~10개 마일스톤)를 하나의 세션이 담당한다

### RALPH_BACKLOG.md 상태 관리 프로토콜

매 마일스톤 완료 시 RALPH_BACKLOG.md를 업데이트:
```
- [x] M-01: Domain models (26 tests) ← DONE 2026-03-29T10:05
- [x] M-02: API clients (51 tests) ← DONE 2026-03-29T10:20
- [ ] M-03: GetCustomersUseCase (8 tests) ← CURRENT
- [ ] M-04: GetCardsByCustomerUseCase (10 tests)
```

### Phase 분리 기준
| Phase | 마일스톤 | 예상 시간 | 세션 |
|-------|---------|----------|------|
| Phase 1: Architecture + Data | M-01 ~ M-06 | 40min | Session 1 |
| Phase 2: Domain UseCases | M-07 ~ M-14 | 40min | Session 2 |
| Phase 3: ViewModels | M-15 ~ M-22 | 40min | Session 3 |
| Phase 4: UI Screens | M-23 ~ M-30 | 30min | Session 4 |
| Phase 5: Feature Extensions | M-31 ~ M-38 | 30min | Session 5 |
| Phase 6: Deep Testing | M-39 ~ M-48 | 40min | Session 6 |
| Phase 7: Integration + Final | M-49 ~ M-55 | 20min | Session 7 |

### 세션 시작 프로토콜 (Session N > 1)
새 세션이 시작되면 반드시:
1. `RALPH_BACKLOG.md` 읽기 → 첫 번째 미완료([ ]) 마일스톤 찾기
2. `git log --oneline -5` → 마지막 커밋 확인
3. `./gradlew.bat test --no-daemon 2>&1 | tail -3` → 현재 빌드 상태 확인
4. 해당 마일스톤의 test-spec 섹션만 읽기 (전체 읽지 않음!)
5. 즉시 TDD 루프 시작

### 컨텍스트 절약 규칙
1. **gradlew 출력은 tail -5만**: `./gradlew.bat test 2>&1 | tail -5` (전체 출력 절대 금지)
2. **파일은 필요한 부분만 읽기**: `Read` with offset/limit 사용
3. **에러 스택트레이스는 첫 10줄만**: 나머지는 잘라냄
4. **이전 마일스톤 코드 다시 읽지 않기**: 현재 마일스톤 관련 파일만
5. **테스트 실패 시 3회 반복 후 스킵**: 같은 에러에 토큰 낭비 금지

### 자동 세션 전환 트리거
다음 조건 중 하나라도 충족 시 현재 Phase를 마무리하고 세션 종료를 준비:
- Phase의 마지막 마일스톤 완료
- 컨텍스트 압축 알림이 2회 이상 발생
- 같은 에러 5회 반복 (좀비 루프 징후)

### 세션 종료 프로토콜
1. 현재 작업 중인 파일 모두 저장
2. 모든 변경사항 git commit + push
3. RALPH_BACKLOG.md에 현재 상태 기록
4. `.omc/state/relay-checkpoint.md`에 인수인계 메모:
   ```
   ## Session N Handoff
   - Last completed: M-14
   - Next: M-15 (CustomerListViewModel)
   - Known issues: none
   - Test count: 130 passing
   - Build status: GREEN
   ```
5. 세션 종료

### 사람 개입 없는 릴레이 방법
**Option A: /ralph --phases** (수동 분할)
- 사전에 Phase별로 별도 프롬프트 파일 준비
- 각 Phase 프롬프트 시작부에 "RALPH_BACKLOG.md 읽고 이어서 진행" 명시
- 대회 시작 시 Phase 1 프롬프트로 /ralph 시작
- Phase 1 완료 후 자동으로 Phase 2 프롬프트 실행... (chaining)

**Option B: 단일 /ralph + 자동 컨텍스트 관리**
- RALPH_BACKLOG.md에 모든 마일스톤 나열
- /ralph가 마일스톤 하나 완료할 때마다:
  - RALPH_BACKLOG.md 체크 표시
  - git commit + push
  - 다음 마일스톤으로 즉시 진행
- 컨텍스트가 차면 시스템 자동 압축에 의존
- 위험: 압축 품질 저하 시 hallucination 가능

**Option C: ralph-relay.sh 자동 릴레이** (추천)
- 외부 쉘 스크립트가 Claude CLI 세션을 자동으로 재시작
- 세션 종료 → RALPH_BACKLOG.md 확인 → 남은 마일스톤 있으면 새 세션 시작
- 진행 없으면 2회 재시도 후 해당 마일스톤 SKIP
- 사용법: `bash scripts/ralph-relay.sh`
- 중단: Ctrl+C
- 최대 7세션, 세션 간 5초 대기

**추천: Option C** — 완전 자동 릴레이, 사람 개입 제로
