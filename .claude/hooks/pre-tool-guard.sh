#!/usr/bin/env bash
set -euo pipefail

python3 - "$@" <<'PY'
import json
import os
import re
import sys
from pathlib import Path

raw = sys.stdin.read()
if not raw.strip():
    sys.exit(0)

try:
    payload = json.loads(raw)
except Exception:
    print("Guardrail failed: hook input JSON could not be parsed.", file=sys.stderr)
    sys.exit(2)

project_root = os.environ.get("CLAUDE_PROJECT_DIR") or payload.get("cwd") or os.getcwd()
root = Path(project_root).resolve()

def decision(kind: str, reason: str) -> None:
    print(json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": kind,
            "permissionDecisionReason": reason,
        }
    }, ensure_ascii=False))
    sys.exit(0)

def resolve_path(value: str | None) -> Path | None:
    if not value:
        return None
    path = Path(value)
    if not path.is_absolute():
        path = root / path
    return path.resolve()

def in_project(value: str | None) -> bool:
    path = resolve_path(value)
    if path is None:
        return False
    return path == root or root in path.parents

def is_secret_path(value: str | None) -> bool:
    if not value:
        return False
    normalized = value.replace("\\", "/").lower()
    name = Path(normalized).name
    return (
        name == ".env"
        or name.startswith(".env.")
        or name.endswith(".pem")
        or name.endswith(".key")
        or name in {"id_rsa", "id_ed25519"}
        or "/.git/" in normalized
    )

tool_name = str(payload.get("tool_name") or "")
tool_input = payload.get("tool_input") or {}

if re.match(r"^(Edit|Write|MultiEdit)$", tool_name):
    file_path = tool_input.get("file_path") or tool_input.get("path")

    if not in_project(file_path):
        decision("deny", f"프로젝트 루트 밖 파일 수정은 차단됩니다: {file_path}")

    if is_secret_path(file_path):
        decision("deny", f"민감 파일 또는 Git 내부 파일 수정은 차단됩니다: {file_path}")

    normalized_path = str(file_path or "").replace("\\", "/").lower()
    if ".claude/settings" in normalized_path or ".claude/hooks/" in normalized_path:
        decision("ask", f"Claude hook 설정 또는 hook 스크립트 변경입니다. 사용자 승인이 필요합니다: {file_path}")

    decision("ask", f"파일 변경 작업입니다. 변경 의도를 확인한 뒤 승인해주세요: {file_path}")

if tool_name == "Bash":
    command = str(tool_input.get("command") or "")
    lower = command.lower()

    if re.search(r"(cat|type|get-content|gc)\s+.*\.env(\s|$)", lower) or re.search(r"(cat|type|get-content|gc)\s+.*(id_rsa|id_ed25519|\.pem|\.key)", lower):
        decision("deny", "민감 파일 내용을 출력하는 명령은 차단됩니다.")

    if re.search(r"git\s+reset\s+--hard", lower) or re.search(r"git\s+clean\s+-[a-z]*f[a-z]*d|git\s+clean\s+-[a-z]*d[a-z]*f", lower):
        decision("deny", "파괴적인 Git reset/clean 명령은 차단됩니다.")

    if re.search(r"(rm\s+-rf\s+[/\\]|remove-item\s+.*\.git|rm\s+.*\.git|rmdir\s+.*\.git)", lower):
        decision("deny", "루트 또는 .git 삭제 위험이 있는 명령은 차단됩니다.")

    if re.search(r"git\s+(commit|push|tag|merge|rebase)\b", lower):
        decision("ask", "Git 이력 또는 원격 저장소에 영향을 주는 명령입니다. 사용자 승인이 필요합니다.")

    if re.search(r"\b(remove-item|rm|del|rmdir)\b", lower):
        decision("ask", "삭제 명령입니다. 대상과 영향 범위를 확인한 뒤 승인해주세요.")

    if re.search(r"\b(curl|wget|invoke-webrequest|iwr|invoke-restmethod|irm)\b", lower):
        decision("ask", "외부 네트워크 요청 명령입니다. 목적과 대상 URL을 확인한 뒤 승인해주세요.")

    if re.search(r"\.claude[\\/](settings|hooks)", lower):
        decision("ask", "Claude hook 설정 또는 스크립트에 접근하는 명령입니다. 사용자 승인이 필요합니다.")

sys.exit(0)
PY
