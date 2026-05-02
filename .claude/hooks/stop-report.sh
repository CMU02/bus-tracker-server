#!/usr/bin/env bash
set -euo pipefail

if git status --short >/tmp/bustracker-guardrail-status.$$ 2>/dev/null; then
  if [ -s /tmp/bustracker-guardrail-status.$$ ]; then
    echo "Guardrail summary: working tree has changes. Report the completed task and mention verification status."
  fi
fi

rm -f /tmp/bustracker-guardrail-status.$$

progress_path=".agents/rules/project-progress.md"
if [ -f "$progress_path" ]; then
  if grep -q -- "- \[x\] 구현 완료" "$progress_path" \
    && grep -q -- "- \[x\] 테스트 완료" "$progress_path" \
    && ! grep -q -- "- \[x\] 다음 기능 PRD Notion 작성 완료" "$progress_path"; then
    echo "구현 완료와 테스트 완료가 체크됐지만 다음 기능 PRD가 Notion에 작성되지 않았습니다. BusTracker Notion 페이지에 다음 기능 PRD를 작성한 뒤 project-progress.md의 PRD 항목을 체크해주세요." >&2
    exit 2
  fi
fi

exit 0
