@AGENTS.md

Claude-specific guardrail:

- Follow `.agents/rules/guardrails.md` before tool use.
- Do not bypass, edit, or disable `.claude/settings.json` or `.claude/hooks/*` unless the user explicitly requests guardrail changes.
- Do not commit, push, reset, clean, or delete files unless the user explicitly asks.
- Treat hook denials as hard blockers and explain the safer next step in Korean.
