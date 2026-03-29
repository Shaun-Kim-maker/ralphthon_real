#!/bin/bash
# competition-runner.sh — Zero-intervention autonomous competition executor
# Usage: bash competition-runner.sh
#
# This script orchestrates 7 Claude Code sessions.
# Each session gets a FRESH context window.
# RALPH_BACKLOG.md is the handoff mechanism between sessions.

set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_DIR/.omc/logs"
mkdir -p "$LOG_DIR"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
MASTER_LOG="$LOG_DIR/competition-$TIMESTAMP.log"

log() {
  echo "[$(date '+%H:%M:%S')] $1" | tee -a "$MASTER_LOG"
}

check_remaining() {
  local remaining
  remaining=$(grep -c '^\- \[ \]' "$PROJECT_DIR/RALPH_BACKLOG.md" 2>/dev/null || echo "0")
  echo "$remaining"
}

run_phase() {
  local phase_num=$1
  local phase_prompt="$PROJECT_DIR/prompts/phase-${phase_num}.md"
  local phase_log="$LOG_DIR/phase-${phase_num}-$TIMESTAMP.log"

  if [ ! -f "$phase_prompt" ]; then
    log "WARN: Phase $phase_num prompt not found at $phase_prompt, skipping"
    return 0
  fi

  local remaining
  remaining=$(check_remaining)
  if [ "$remaining" -eq 0 ]; then
    log "All milestones complete! Stopping early."
    return 0
  fi

  log "=== PHASE $phase_num START (${remaining} milestones remaining) ==="

  local prompt_content
  prompt_content=$(cat "$phase_prompt")

  # First attempt — fresh session, full permissions, no human interaction
  local exit_code=0
  claude -p "$prompt_content" \
    --permission-mode bypassPermissions \
    --max-budget-usd 50 \
    > "$phase_log" 2>&1 || exit_code=$?

  if [ "$exit_code" -eq 0 ]; then
    log "=== PHASE $phase_num COMPLETE (attempt 1) ==="
  else
    log "WARN: Phase $phase_num attempt 1 exited with code $exit_code, retrying..."

    # Second attempt (retry once)
    local retry_log="$LOG_DIR/phase-${phase_num}-retry-$TIMESTAMP.log"
    exit_code=0
    claude -p "$prompt_content" \
      --permission-mode bypassPermissions \
      --max-budget-usd 50 \
      > "$retry_log" 2>&1 || exit_code=$?

    if [ "$exit_code" -eq 0 ]; then
      log "=== PHASE $phase_num COMPLETE (attempt 2) ==="
      phase_log="$retry_log"
    else
      log "WARN: Phase $phase_num attempt 2 also failed (code $exit_code). Checking progress and moving on."
      # Even on error, milestones may have been completed — do not abort
    fi
  fi

  # Tail the phase log into master log for visibility
  log "--- Phase $phase_num output (last 20 lines) ---"
  tail -20 "$phase_log" >> "$MASTER_LOG" 2>/dev/null || true
  log "--- End Phase $phase_num output ---"

  # Commit any changes left uncommitted by the session
  cd "$PROJECT_DIR"
  if ! git diff --quiet || ! git diff --cached --quiet; then
    log "Committing uncommitted changes after phase $phase_num..."
    git add -A
    git commit -m "feat: Phase ${phase_num} auto-commit checkpoint [runner]"
    git push origin master || log "WARN: push failed after phase $phase_num, will retry next phase"
  else
    log "No uncommitted changes after phase $phase_num"
  fi

  return 0
}

# ============================================================
# MAIN EXECUTION
# ============================================================

log "========================================"
log "  Competition Runner Started"
log "  Project: $PROJECT_DIR"
log "  Timestamp: $TIMESTAMP"
log "========================================"
log "Milestones remaining at start: $(check_remaining)"

for phase in 1 2 3 4 5 6 7; do
  run_phase "$phase"

  remaining=$(check_remaining)
  log "After Phase $phase: $remaining milestones remaining"

  if [ "$remaining" -eq 0 ]; then
    log "ALL MILESTONES COMPLETE — stopping runner early."
    break
  fi

  # Brief pause between sessions to let git settle
  sleep 3
done

log "========================================"
log "  Competition Runner Finished"
log "  Final milestone count: $(check_remaining) remaining"
log "========================================"

# Final verification — use gradlew.bat on Windows Git Bash
log "Running final test suite..."
cd "$PROJECT_DIR"
if [ -f "./gradlew.bat" ]; then
  ./gradlew.bat test --no-daemon 2>&1 | tail -15 >> "$MASTER_LOG" || log "WARN: Final test run exited non-zero"
elif [ -f "./gradlew" ]; then
  ./gradlew test --no-daemon 2>&1 | tail -15 >> "$MASTER_LOG" || log "WARN: Final test run exited non-zero"
else
  log "WARN: No gradlew found, skipping final test run"
fi

log "DONE — see full log at: $MASTER_LOG"
