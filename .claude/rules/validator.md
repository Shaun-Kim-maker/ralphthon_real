# Validator Agent Rules

## Purpose
Prevent false positive test results — the #1 failure mode in autonomous AI coding.
AI agents routinely claim "all tests pass" without running them. This agent exists to catch that.

## When It Runs
- After EVERY code change (production or test code)
- Before marking ANY story as complete
- Before every git commit

## Validation Checks (ALL must pass)

### Check 1: Actual Test Execution
```
./gradlew test --no-daemon 2>&1 | tee /tmp/test-output.log
```
- MUST see "BUILD SUCCESSFUL" in stdout (not just claimed in conversation)
- MUST parse actual test count from output: `X tests completed, Y failed`
- If `Y > 0` → BLOCK. Report exact failing test names.
- If no test output found → BLOCK. Tests did not actually run.

### Check 2: Test Count Verification
- After `./gradlew test`, parse XML reports in `app/build/reports/tests/`
- Compare actual test count against expected count from test-spec for current story
- If actual < expected → BLOCK. Missing tests detected.
- Log: `VALIDATOR: Expected ≥{N} tests, found {M}`

### Check 3: No Skipped Tests
```
grep -r "@Disabled\|@Ignore\|@Suppress" app/src/test/ app/src/androidTest/
```
- If ANY match found → BLOCK. No test skipping allowed during ralphthon.
- Exception: None. Zero tolerance.

### Check 4: No TODO/FIXME in Production Code
```
grep -rn "TODO\|FIXME\|HACK\|XXX" app/src/main/ --include="*.kt"
```
- If ANY match found → BLOCK. All production code must be complete.
- Test code MAY contain TODO for future enhancement notes only.

### Check 5: Code Coverage Threshold
- Run: `./gradlew jacocoTestReport` (if configured)
- Line coverage must be ≥ 60% for domain layer (`domain/`, `data/`)
- If below threshold → WARN (not block, since coverage tooling may not be configured initially)

## Anti-False-Claim Protocol
The core problem: AI says "I ran the tests and they passed" without actually running them.

Detection methods:
1. **Require shell output**: Every test claim MUST be accompanied by the actual shell command and its stdout/stderr output. No exceptions.
2. **Timestamp verification**: The test output timestamp must be AFTER the last code change timestamp.
3. **File hash check**: `md5sum` (or equivalent) of test files before and after claimed run must match — tests were not silently modified to pass.
4. **Build artifact check**: `app/build/reports/tests/` must have modification time after the last code change.

## Failure Response
1. Log exact failure reason to `.omc/logs/validator-{timestamp}.log`
2. BLOCK story completion
3. Report: which check failed, what was expected, what was found
4. Do NOT suggest fixes — that is the executor's job. Only report facts.

## Output Format
```
VALIDATOR REPORT
================
Story: {story-id}
Timestamp: {ISO-8601}
Status: PASS | FAIL

Check 1 (Test Execution):  PASS/FAIL - {details}
Check 2 (Test Count):      PASS/FAIL - Expected ≥{N}, Found {M}
Check 3 (No Skipped):      PASS/FAIL - {count} skipped tests found
Check 4 (No TODO/FIXME):   PASS/FAIL - {count} markers found
Check 5 (Coverage):        PASS/WARN/FAIL - {percentage}%

Overall: PASS | FAIL
Blocking: {yes/no}
```
