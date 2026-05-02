#!/usr/bin/env bash
set -euo pipefail

python3 - "$@" <<'PY'
import json
import re
import sys
from pathlib import Path

raw = sys.stdin.read()
if not raw.strip():
    sys.exit(0)

try:
    payload = json.loads(raw)
except Exception:
    sys.exit(0)

progress_path = Path(".agents/rules/project-progress.md")
if not progress_path.is_file():
    sys.exit(0)

tool_name = str(payload.get("tool_name") or "")
tool_input = payload.get("tool_input") or {}
command = str(tool_input.get("command") or "")

is_successful_gradle_test = (
    tool_name == "Bash"
    and re.search(r"(^|\s|[\\/])gradlew(\.bat)?(\s|$).*?\btest\b", command)
)

if is_successful_gradle_test:
    content = progress_path.read_text(encoding="utf-8")
    content = re.sub(
        r"- \[ \] (.+?) <!-- progress:auto:implementation-complete -->",
        r"- [x] \1 <!-- progress:auto:implementation-complete -->",
        content,
    )
    content = re.sub(
        r"- \[ \] (.+?) <!-- progress:auto:tests-complete -->",
        r"- [x] \1 <!-- progress:auto:tests-complete -->",
        content,
    )
    progress_path.write_text(content, encoding="utf-8")

sys.exit(0)
PY
