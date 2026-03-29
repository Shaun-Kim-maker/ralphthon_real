# Scope Guard Agent Rules

## Purpose
Prevent scope creep — AI agents adding unrequested features, "helpful" refactoring,
or improvements beyond the current story. Every line of code must trace to a story in prd.json.

## When It Runs
- At EVERY commit (pre-commit gate)
- During code review before story completion

## Scope Checks

### Check 1: File Scope Verification
Each story has a defined file scope. Changed files must fall within scope.

Story-to-scope mapping:
| Story | Allowed File Patterns |
|-------|----------------------|
| US-001 | `.omc/harness/ambiguity-analysis.md` |
| US-002 | `.omc/harness/test-specs/domain-tests.md` |
| US-003 | `.omc/harness/test-specs/data-tests.md` |
| US-004 | `.omc/harness/test-specs/ui-tests.md` |
| US-005 | `.omc/harness/test-specs/integration-tests.md` |
| US-006 | `.omc/harness/prompts/**` |
| US-007 | `.omc/harness/verification-system.md`, `.claude/rules/**` |
| US-008 | `.omc/harness/README.md` |

During app implementation (ralph execution phase), scope maps to prompt phases:
| Phase | Allowed Directories |
|-------|-------------------|
| Architecture | `app/src/main/**/di/`, `app/src/main/**/model/`, `build.gradle*`, `settings.gradle*` |
| Data Layer | `app/src/main/**/data/`, `app/src/test/**/data/`, `app/src/main/**/api/` |
| Domain Layer | `app/src/main/**/domain/`, `app/src/test/**/domain/` |
| UI Layer | `app/src/main/**/ui/`, `app/src/main/**/presentation/`, `app/src/test/**/ui/`, `app/src/androidTest/` |
| Integration | `app/src/test/**/integration/`, `app/src/androidTest/` |
| **Phase 7 Bug Regression** | 모든 디렉토리 허용 (단, 아래 조건 충족 시에만) |

**Phase 7 예외 규정:**
Phase 7(최종 통합 테스트)에서 androidTest 실패로 인한 버그 수정 시에만 다른 Phase 범위의 코드 수정을 허용한다.
- 조건 1: Phase 1~6이 모두 완료된 상태여야 한다 (RALPH_BACKLOG.md 기준)
- 조건 2: connectedAndroidTest 실패 로그에서 원인 파일이 명확히 식별되어야 한다
- 조건 3: 수정 범위는 실패 원인 파일과 해당 unit test에 한정한다 (연쇄 수정 금지)
- 조건 4: 수정 후 기존 unit test 회귀 없음을 `./gradlew.bat test` 로 확인해야 한다

Verification:
```
git diff --cached --name-only
```
Compare each changed file against allowed patterns for current story/phase.
If file is outside scope → BLOCK.

### Check 2: No Unrequested Features
Parse `prd.json` for current story's acceptance criteria.
Every new class/function/composable MUST trace to an acceptance criterion.

Red flags for scope creep:
- New utility classes not in any spec
- "Helper" functions that serve no acceptance criterion
- Analytics/logging beyond what specs require
- UI animations or transitions not in spec
- Additional API endpoints not in spec
- Performance optimizations not requested

If suspicious addition detected → FLAG for review (soft block).

### Check 3: No Drive-By Refactoring
```
git diff --cached --stat
```
If files outside the current story scope are modified:
- Check if modification is purely formatting → WARN
- Check if modification changes logic → BLOCK
- Check if modification adds new code → BLOCK

"While I was in the area, I improved..." is NEVER acceptable during ralphthon.

### Check 4: Commit Message Compliance
Every commit message MUST match:
```
^(feat|fix|test|chore|docs|refactor): .+$
```
Additionally:
- Must reference the current story context (e.g., which prompt phase or story)
- Must be in English
- Must be under 72 characters for the subject line
- Must NOT contain phrases like "also improved", "while at it", "additionally"

### Check 5: No Dependency Creep
```
git diff --cached -- build.gradle build.gradle.kts app/build.gradle app/build.gradle.kts
```
If new dependencies are added:
- Check if dependency is required by current story/prompt
- Check if dependency was specified in architecture spec
- If not → BLOCK. New dependencies require explicit approval.

## Failure Response
1. For out-of-scope file changes:
   - BLOCK commit
   - Report: "File {X} is outside scope of {current-story}. Revert this change."
   - Suggest: `git checkout -- {file}` to revert
2. For unrequested features:
   - FLAG for review (soft block)
   - Report: "New {class/function} {name} does not trace to any acceptance criterion."
3. For drive-by refactoring:
   - BLOCK commit
   - Report: "File {X} modified but not in current story scope. Revert."
4. For commit message violation:
   - BLOCK commit
   - Report: "Commit message does not follow convention. Expected: {type}: {description}"
5. For dependency creep:
   - BLOCK commit
   - Report: "New dependency {X} not in architecture spec. Remove or get approval."

## Output Format
```
SCOPE GUARD REPORT
==================
Story/Phase: {id}
Timestamp: {ISO-8601}
Files Changed: {count}

Check 1 (File Scope):      PASS/FAIL - {out-of-scope files}
Check 2 (Feature Scope):   PASS/FAIL/FLAG - {unrequested additions}
Check 3 (No Refactoring):  PASS/FAIL - {drive-by changes}
Check 4 (Commit Message):  PASS/FAIL - {violation details}
Check 5 (Dependencies):    PASS/FAIL - {new deps}

Overall: PASS | FAIL | FLAG
Blocking: {yes/no}
```
