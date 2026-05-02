# Guardrails

These rules define the safety model for AI agents working in BusTracker.

## Checkpoint 1: Project Rules

Before changing files or running commands, read:

- `AGENTS.md`
- `CLAUDE.md`
- `.agents/rules/*`

The agent must verify:

- The requested action matches the current task.
- The action does not violate architecture, API, testing, operations, or git rules.
- The action does not expose secrets.
- The action does not modify unrelated files.

## Checkpoint 2: Hook Inspection

Claude Code hooks under `.claude/settings.json` run before and after tool use.

PreToolUse must:

- Deny secret reads or writes.
- Deny destructive git cleanup/reset commands.
- Deny file writes outside the project root.
- Ask the user before modifying files.
- Ask the user before commit, push, tag, merge, or rebase commands.
- Ask the user before deleting files or running external network commands.

PostToolUse must:

- Check changed text files for simple style and secret risks.
- Report actionable failures to the agent.

## Checkpoint 3: User Approval

User approval is required for:

- File modifications.
- Destructive shell commands.
- Git history or remote operations.
- Dependency installation.
- External network access.
- Hook or rule changes.

Use narrow "always allow" permissions only. Do not request broad shell or scripting permissions.

## Absolute Deny List

Never perform these actions unless the user changes this rule explicitly:

- Print or commit `.env` contents.
- Commit credentials, tokens, API keys, private keys, or passwords.
- Run `git reset --hard`.
- Run `git clean -fd` or stronger variants.
- Delete `.git`.
- Modify files outside the project root.
- Disable hooks to bypass a safety check.
