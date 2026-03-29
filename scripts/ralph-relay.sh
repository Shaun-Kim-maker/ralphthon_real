#!/bin/bash
# Ralph Marathon Relay - Auto Session Restart Script
# 세션이 종료되면 자동으로 다음 Phase를 시작하는 릴레이 스크립트
#
# 사용법: bash scripts/ralph-relay.sh
# 중단: Ctrl+C (SIGINT)

BACKLOG="RALPH_BACKLOG.md"
CHECKPOINT=".omc/state/relay-checkpoint.md"
MAX_SESSIONS=7
SESSION=1
MAX_RETRIES=2

echo "========================================="
echo " Ralph Marathon Relay - START"
echo " Max Sessions: $MAX_SESSIONS"
echo "========================================="

while [ $SESSION -le $MAX_SESSIONS ]; do
    echo ""
    echo "-----------------------------------------"
    echo " Session $SESSION / $MAX_SESSIONS"
    echo " $(date '+%Y-%m-%dT%H:%M:%S')"
    echo "-----------------------------------------"

    # Check if all milestones are done
    REMAINING=$(grep -c "^\- \[ \]" "$BACKLOG" 2>/dev/null || echo "0")
    if [ "$REMAINING" -eq 0 ]; then
        echo "All milestones complete! Exiting relay."
        break
    fi
    echo "Remaining milestones: $REMAINING"

    # Get next milestone for prompt
    NEXT_MILESTONE=$(grep -m1 "^\- \[ \]" "$BACKLOG" 2>/dev/null | head -1)
    echo "Next: $NEXT_MILESTONE"

    # Clean up uncommitted changes from crashed session
    DIRTY=$(git status --porcelain 2>/dev/null | wc -l)
    if [ "$DIRTY" -gt 0 ]; then
        echo "Dirty state detected from previous session. Stashing..."
        git stash push -m "relay-session-$SESSION-recovery"
    fi

    # Run Claude Code session
    PROMPT="RALPH_BACKLOG.md 읽고 첫 번째 미완료 마일스톤부터 TDD 루프 시작. Phase 끝나면 세션 종료. git stash가 있으면 git stash pop 후 이어서 진행."

    claude --dangerously-skip-permissions -p "$PROMPT" 2>&1
    EXIT_CODE=$?

    echo ""
    echo "Session $SESSION exited with code: $EXIT_CODE"

    # Check progress after session
    NEW_REMAINING=$(grep -c "^\- \[ \]" "$BACKLOG" 2>/dev/null || echo "0")
    COMPLETED=$((REMAINING - NEW_REMAINING))
    echo "Milestones completed this session: $COMPLETED"

    # If no progress was made, retry up to MAX_RETRIES then skip
    if [ "$COMPLETED" -eq 0 ]; then
        echo "WARNING: No progress made in session $SESSION"
        RETRY=0
        while [ $RETRY -lt $MAX_RETRIES ]; do
            RETRY=$((RETRY + 1))
            echo "Retry $RETRY / $MAX_RETRIES..."
            claude --dangerously-skip-permissions -p "$PROMPT" 2>&1
            NEW_REMAINING2=$(grep -c "^\- \[ \]" "$BACKLOG" 2>/dev/null || echo "0")
            if [ "$NEW_REMAINING2" -lt "$NEW_REMAINING" ]; then
                echo "Progress made on retry. Continuing."
                break
            fi
        done
        if [ "$RETRY" -ge "$MAX_RETRIES" ]; then
            echo "STUCK: No progress after $MAX_RETRIES retries. Skipping current milestone."
            # Mark current milestone as SKIP
            STUCK_LINE=$(grep -n -m1 "^\- \[ \]" "$BACKLOG" | cut -d: -f1)
            if [ -n "$STUCK_LINE" ]; then
                sed -i "${STUCK_LINE}s/- \[ \]/- [SKIP] relay-stuck:/" "$BACKLOG"
            fi
        fi
    fi

    # Brief pause between sessions
    echo "Waiting 5 seconds before next session..."
    sleep 5

    SESSION=$((SESSION + 1))
done

echo ""
echo "========================================="
echo " All Sessions Complete - Starting Final Android Studio Test"
echo " $(date '+%Y-%m-%dT%H:%M:%S')"
echo "========================================="

# =============================================
# FINAL STAGE: Android Studio Integration Test
# 모든 Phase 완료 후 에뮬레이터에서 통합 테스트
# =============================================

ADB="$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
EMULATOR="$LOCALAPPDATA/Android/Sdk/emulator/emulator.exe"

# 1. Check emulator
DEVICE=$("$ADB" devices 2>/dev/null | grep "device$" | head -1 | cut -f1)
if [ -z "$DEVICE" ]; then
    echo "No emulator running. Starting Pixel_9_Pro..."
    "$EMULATOR" -avd Pixel_9_Pro -no-audio &
    EMULATOR_PID=$!
    echo "Waiting for emulator boot (max 120s)..."
    "$ADB" wait-for-device
    sleep 30
    DEVICE=$("$ADB" devices 2>/dev/null | grep "device$" | head -1 | cut -f1)
fi
echo "Device: $DEVICE"

# 2. Build and install app
echo "Building debug APK..."
./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5
if [ $? -ne 0 ]; then
    echo "FAIL: Debug build failed. Delegating fix to Claude..."
    claude --dangerously-skip-permissions -p "assembleDebug 빌드 실패. 에러 수정 후 다시 빌드." 2>&1
fi

echo "Installing on emulator..."
./gradlew.bat installDebug --no-daemon 2>&1 | tail -3

# 3. Run connectedAndroidTest
echo "Running connectedAndroidTest..."
./gradlew.bat connectedDebugAndroidTest --no-daemon 2>&1 | tail -20
ANDROID_TEST_EXIT=$?

if [ $ANDROID_TEST_EXIT -ne 0 ]; then
    echo "androidTest FAILED. Delegating bug regression to Claude..."
    REGRESSION_RETRY=0
    MAX_REGRESSION=3
    while [ $REGRESSION_RETRY -lt $MAX_REGRESSION ]; do
        REGRESSION_RETRY=$((REGRESSION_RETRY + 1))
        echo "Regression fix attempt $REGRESSION_RETRY / $MAX_REGRESSION"
        claude --dangerously-skip-permissions -p "connectedAndroidTest 실패. 실패 로그 분석 후 원인 Phase 코드 수정. unit test 회귀 확인 후 다시 connectedAndroidTest 실행." 2>&1
        ./gradlew.bat connectedDebugAndroidTest --no-daemon 2>&1 | tail -10
        if [ $? -eq 0 ]; then
            echo "androidTest PASSED on retry $REGRESSION_RETRY"
            break
        fi
    done
fi

# 4. Launch app on emulator
echo "Launching app..."
"$ADB" -s "$DEVICE" shell am start -n com.ralphthon.app/.MainActivity 2>&1

echo ""
echo "========================================="
echo " Ralph Marathon Relay - ALL COMPLETE"
echo " $(date '+%Y-%m-%dT%H:%M:%S')"
echo " Milestones remaining: $(grep -c '^\- \[ \]' $BACKLOG 2>/dev/null || echo 0)"
echo " Android Test: $([ $ANDROID_TEST_EXIT -eq 0 ] && echo 'PASSED' || echo 'FAILED')"
echo "========================================="
