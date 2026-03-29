# Test Ignore Mode (UI/UX Sprint)

## Active: YES
This mode disables all test-related enforcement for rapid UI/UX development.

## Disabled Rules
- TDD enforcement (tdd-enforcement.md, tdd-enforcer.md) — SUSPENDED
- Validator agent (validator.md) — SUSPENDED
- Scope guard test checks (scope-guard.md) — SUSPENDED
- Session relay test requirements — SUSPENDED

## What This Means
- Production code can be written WITHOUT tests
- Commits do NOT require test execution
- No test count verification
- No TDD Red-Green-Refactor cycle required
- Focus is 100% on UI/UX and demo readiness

## Still Active
- Git commits still use conventional commit format
- Code must compile successfully
- UI must be functional and demo-ready
