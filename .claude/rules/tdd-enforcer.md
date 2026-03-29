# TDD Enforcer Agent Rules

## Purpose
Ensure Test-Driven Development is actually followed, not just claimed.
Production code without tests is the fastest path to silent failures in autonomous execution.

## When It Runs
- During implementation of EVERY story
- At every file creation event
- Before every commit (pre-commit gate)

## TDD Tier Definitions

### Tier 1: Strict Red-Green-Refactor
**Applies to:** ViewModel, UseCase, Repository, Mapper, Utility — all pure Kotlin in `domain/`, `data/`

**Enforcement:**
1. Test file MUST be created/modified BEFORE the corresponding production file
2. Verification method: check git diff staging order
   ```
   git diff --cached --name-only --diff-filter=A
   ```
   - For each new production file in `app/src/main/`, a corresponding test file MUST exist in `app/src/test/`
   - The test file MUST contain at least one `@Test` annotated function
   - The test MUST contain at least one assertion (`assertEquals`, `assertThat`, `verify`, `coVerify`, etc.)

3. File mapping convention:
   - `app/src/main/java/com/.../SomeClass.kt` → `app/src/test/java/com/.../SomeClassTest.kt`
   - `app/src/main/java/com/.../SomeViewModel.kt` → `app/src/test/java/com/.../SomeViewModelTest.kt`

### Tier 2: Pragmatic Test-Alongside
**Applies to:** Composable functions, UI components in `ui/`, `presentation/`

**Enforcement:**
1. Test file MUST exist in the SAME commit as the production UI file
2. Every `@Composable` function file must have a corresponding test file in `app/src/androidTest/` or `app/src/test/`
3. UI tests must use semantic assertions:
   ```
   onNodeWithText(...)
   onNodeWithContentDescription(...)
   onNodeWithTag(...)
   ```
   NOT pixel-based assertions.

## Test Quality Checks

### Naming Convention
Every `@Test` function must match pattern:
```
should_[expectedBehavior]_when_[condition]
```
Examples:
- `should_returnError_when_inputIsEmpty`
- `should_showLoading_when_fetchStarts`
- `should_navigateToDetail_when_cardClicked`

Regex for validation:
```
@Test.*fun should_\w+_when_\w+
```
If a test function does not match → WARN (soft block).

### Assertion Presence
Every `@Test` function MUST contain at least ONE of:
- `assertEquals`, `assertNotEquals`, `assertTrue`, `assertFalse`
- `assertThat` (Truth library)
- `verify`, `coVerify` (MockK)
- `expectThat` (Strikt)
- `onNode*` with `.assert*` (Compose testing)

A test with no assertion is not a test → BLOCK.

### No Empty Tests
```
grep -A5 "@Test" <file> | grep -c "{ }"
```
If any empty test body found → BLOCK.

## Production-Test Mapping Audit
At each commit, run full mapping check:
```
for each .kt file in app/src/main/java/:
  if file contains class/object/interface:
    corresponding test file MUST exist in app/src/test/ or app/src/androidTest/
```
Files exempt from test requirement:
- `*Application.kt` (Android Application class)
- `*Module.kt` (Hilt/Dagger modules — tested via integration tests)
- `*Theme.kt`, `*Color.kt`, `*Type.kt` (Compose theme definitions)
- `*Navigation.kt` (navigation graph — tested via navigation tests)

## Failure Response
1. If Tier 1 violation (production code without prior test):
   - Log violation to `.omc/logs/tdd-enforcer-{timestamp}.log`
   - BLOCK commit
   - Report: "Production file {X} has no corresponding test. Write test first."
2. If Tier 2 violation (UI code without co-located test):
   - Log violation
   - BLOCK commit
   - Report: "UI file {X} committed without test. Add test to same commit."
3. If naming violation:
   - WARN (do not block, but log for post-review)
4. If assertion-less test:
   - BLOCK commit
   - Report: "Test {functionName} in {file} has no assertions."

## Output Format
```
TDD ENFORCER REPORT
====================
Story: {story-id}
Timestamp: {ISO-8601}
Files Changed: {count}

Tier 1 Checks:
  Production files: {list}
  Test files: {list}
  Mapping: {all mapped / {N} unmapped}
  Violations: {list or "none"}

Tier 2 Checks:
  UI files: {list}
  UI test files: {list}
  Mapping: {all mapped / {N} unmapped}
  Violations: {list or "none"}

Quality Checks:
  Naming convention: {N}/{M} compliant
  Assertion presence: {N}/{M} have assertions
  Empty tests: {count}

Overall: PASS | FAIL
Blocking: {yes/no}
```
