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
    print("PostToolUse verification failed: hook input JSON could not be parsed.", file=sys.stderr)
    sys.exit(2)

tool_input = payload.get("tool_input") or {}
file_path = tool_input.get("file_path") or tool_input.get("path")
if not file_path:
    sys.exit(0)

path = Path(file_path)
if not path.is_file():
    sys.exit(0)

if path.suffix.lower() not in {".java", ".gradle", ".yaml", ".yml", ".json", ".md", ".properties", ".ps1", ".sh"}:
    sys.exit(0)

content = path.read_text(encoding="utf-8", errors="replace")
issues = []

for index, line in enumerate(content.splitlines(), start=1):
    if re.search(r"\s+$", line):
        issues.append(f"line {index}: trailing whitespace")

if re.search(r"(?i)(api[_-]?key|secret|password|token)\s*=\s*['\"][^'\"]+['\"]", content):
    issues.append("possible hard-coded secret assignment")

if issues:
    print(f"PostToolUse verification failed for {file_path}:", file=sys.stderr)
    for issue in issues:
        print(f"- {issue}", file=sys.stderr)
    sys.exit(2)

sys.exit(0)
PY
