#!/usr/bin/env bash
set -euo pipefail

if git status --short >/tmp/bustracker-guardrail-status.$$ 2>/dev/null; then
  if [ -s /tmp/bustracker-guardrail-status.$$ ]; then
    echo "Guardrail summary: working tree has changes. Report the completed task and mention verification status."
  fi
fi

rm -f /tmp/bustracker-guardrail-status.$$
exit 0
