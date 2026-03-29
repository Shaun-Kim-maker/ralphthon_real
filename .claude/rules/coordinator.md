# Coordinator Agent Rules

## Purpose
Prevent conflicts when multiple stories or prompts execute in sequence or parallel.
Ensure dependency order is respected and git state stays clean between milestones.

## When It Runs
- Before starting each story/prompt phase
- During parallel execution (if multiple agents are active)
- After each git commit, before moving to next story

## Coordination Checks

### Check 1: Git State Clean
Before starting any new story:
```
git status --porcelain
```
- If output is non-empty → BLOCK. Uncommitted changes from previous story.
- Resolution: commit or stash before proceeding.

### Check 2: Dependency Order
Story execution order MUST follow priority:
- P0 stories complete before P1 stories begin
- P1 stories complete before P2 stories begin
- Within same priority: execute in story ID order (US-001 before US-002)

Dependency matrix for this project:
- US-001 (Ambiguity Analysis) → blocks all other stories (foundational)
- US-002, US-003 (Test Specs) → can run in parallel after US-001
- US-004, US-005 (Test Specs) → can run after US-002 and US-003
- US-006 (Prompts) → requires US-002 through US-005 complete
- US-007 (Verification) → can run in parallel with US-002+
- US-008 (Master Doc) → requires ALL other stories complete

### Check 3: File Conflict Prevention
Before starting a story, check its expected file scope:
- US-001: `.omc/harness/ambiguity-analysis.md`
- US-002: `.omc/harness/test-specs/domain-tests.md`
- US-003: `.omc/harness/test-specs/data-tests.md`
- US-004: `.omc/harness/test-specs/ui-tests.md`
- US-005: `.omc/harness/test-specs/integration-tests.md`
- US-006: `.omc/harness/prompts/`
- US-007: `.omc/harness/verification-system.md`, `.claude/rules/`
- US-008: `.omc/harness/README.md`

If two concurrent stories would modify the same file → PAUSE the lower-priority story.

### Check 4: No Merge Conflicts
After every commit:
```
git log --oneline -3
```
Verify the commit history is linear. If ralph is running in multiple sessions:
```
git pull --rebase origin main
```
If rebase fails → STOP. Alert for manual resolution.

### Check 5: Milestone Completion Verification
Before transitioning from story N to story N+1:
1. All acceptance criteria for story N are met (cross-reference prd.json)
2. Git commit exists with conventional commit message referencing the story
3. Validator agent has reported PASS for story N

## Failure Response
1. Log conflict details to `.omc/logs/coordinator-{timestamp}.log`
2. PAUSE the conflicting story (do not proceed)
3. Attempt auto-resolution:
   - For dirty git state: `git stash` then retry
   - For dependency violation: reorder execution
   - For merge conflict: attempt `git rebase`, escalate if it fails
4. If auto-resolution fails: STOP and report

## Output Format
```
COORDINATOR REPORT
==================
Current Story: {story-id}
Previous Story: {story-id} - {COMPLETE/INCOMPLETE}
Timestamp: {ISO-8601}

Check 1 (Git Clean):       PASS/FAIL
Check 2 (Dependencies):    PASS/FAIL - {blocking story}
Check 3 (File Conflicts):  PASS/FAIL - {conflicting files}
Check 4 (Merge State):     PASS/FAIL
Check 5 (Milestone):       PASS/FAIL - {missing criteria}

Transition: ALLOWED | BLOCKED
Reason: {if blocked}
```
