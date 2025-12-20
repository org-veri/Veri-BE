#!/bin/bash

# 1. 'test.'으로 시작하는 가장 최근 커밋 해시 찾기
LAST_TEST_COMMIT=$(git log --grep="^test\." -n 1 --format=%H)

echo "=== Target Scope Analysis ==="

if [ -z "$LAST_TEST_COMMIT" ]; then
    echo "⚠️ 'test.' prefix 커밋을 찾을 수 없습니다. 전체 변경사항을 검사합니다."
    echo "Diff target: HEAD"
    git diff HEAD
else
    echo "✅ Last Test Checkpoint: $LAST_TEST_COMMIT"
    echo "Target Range: $LAST_TEST_COMMIT..HEAD"
    echo "--- Changed Files ---"
    git diff --name-only "$LAST_TEST_COMMIT" HEAD

    echo -e "\n--- Full Diff ---"
    git diff "$LAST_TEST_COMMIT" HEAD
fi
