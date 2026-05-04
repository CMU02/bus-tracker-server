@AGENTS.md

Claude-specific guardrail:

- Follow `.agents/rules/guardrails.md` before tool use.
- Do not bypass, edit, or disable `.claude/settings.json` or `.claude/hooks/*` unless the user explicitly requests guardrail changes.
- Do not commit, push, reset, clean, or delete files unless the user explicitly asks.
- Treat hook denials as hard blockers and explain the safer next step in Korean.

## Agent Model Strategy

작업 성격에 따라 아래 모델을 사용한다:

| 작업 유형 | 모델 | 대표 서브에이전트 |
|-----------|------|-----------------|
| 설계·계획·아키텍처 분석·코드 리뷰 | Opus 4.6 | `oh-my-claudecode:planner`, `oh-my-claudecode:architect`, `oh-my-claudecode:critic` |
| 기능 구현·코드 수정·디버깅 | Sonnet 4.6 | `coding-agent`, `oh-my-claudecode:executor`, `oh-my-claudecode:debugger` |
| git 커밋·문서 작성·반복 작업 | Haiku 4.5 | `oh-my-claudecode:git-master`, `oh-my-claudecode:writer` |

- Agent 도구 호출 시 반드시 `model` 파라미터를 위 전략에 맞게 명시한다.
- 메인 세션 기본 모델은 Sonnet 4.6으로 유지한다.
