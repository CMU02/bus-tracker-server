# BusTracker Agent Guide

This project is a Spring Boot backend for real-time Seoul bus location tracking.
Agents working in this repository should keep changes focused on the MVP:

- Search Seoul bus routes by bus number.
- Fetch route station order and coordinates.
- Stream route vehicle positions through SSE without client refresh.
- Use Core NATS as the internal publish/subscribe layer for route snapshots.
- Cache route and station lookup data with Redis.
- Do not persist vehicle position history in the database for the MVP.
- Do not add Spring Security or JWT until the core tracking feature is complete.

## Rule Files

Read these project rules before making changes:

- `.agents/rules/architecture.md`
- `.agents/rules/api-contracts.md`
- `.agents/rules/git-convention.md`
- `.agents/rules/guardrails.md`
- `.agents/rules/testing.md`
- `.agents/rules/operations.md`

## Working Style

- Prefer small, behavior-focused changes.
- Write or update tests for new behavior before production code.
- Keep backend API contracts stable once introduced.
- Avoid speculative abstractions and unrelated refactors.
- Surface blockers clearly, especially external API, NATS, Redis, and Gradle environment issues.

## korea-response

# [Korean Response Guidelines]

## Default Response Language

- All responses must be written in Korean.

- Technical terms should include both Korean and English together (e.g., 함수(function), 변수(variable)).

- Code comments should also be written in Korean.

## Response Style

- Use polite and respectful tone (e.g., ~습니다, ~해주세요).

- Maintain a friendly and helpful tone at all times.

- Provide clear and easy-to-understand explanations for technical topics.

## Code-Related Responses

- Explanations of code should be written in Korean.

- Keep variable and function names in English (standard programming convention).

- Error messages should be explained in Korean.

- All code comments must be written in Korean.

## Exceptions

- Even when a question is asked in English, respond in Korean by default.

- Only use another language when the user explicitly requests it.

## Reporting

- Do not create unnecessary documents.
- Summarize and report on the last task completed.

## Current Technical Baseline

- Java 21
- Spring Boot 4
- Spring MVC
- OpenFeign
- Redis
- MySQL
- Core NATS through `io.nats:jnats`
- Gradle wrapper

## Useful Commands

```powershell
.\gradlew.bat test
```

If Gradle cannot download or use its wrapper cache, report the exact failure instead of changing wrapper files.
