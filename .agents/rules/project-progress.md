# Project Progress Checklist

이 파일은 BusTracker 구현 진행상황을 에이전트가 공유하기 위한 체크리스트입니다.

## 자동 체크 규칙

- `.claude/hooks/update-progress.ps1` 또는 `.claude/hooks/update-progress.sh`가 PostToolUse 단계에서 실행됩니다.
- `gradlew test`, `gradlew.bat test`, `./gradlew test`, `./gradlew.bat test` 명령이 성공하면 아래의 `구현 완료`와 `테스트 완료` 항목이 자동으로 체크됩니다.
- 자동 체크는 전체 테스트 명령의 성공만 신뢰합니다. 컴파일 실패, 테스트 실패, sandbox/network 실패는 자동 체크하지 않습니다.
- 구현 완료와 테스트 완료가 체크됐는데 PRD 항목이 비어 있으면 Stop hook이 응답 종료를 막고 Notion PRD 작성을 요구합니다.

## 현재 기능: SSE 기반 실시간 버스 위치 추적 백엔드

- [ ] 기획 완료 <!-- progress:auto:planning-complete -->
- [ ] 구현 완료 <!-- progress:auto:implementation-complete -->
- [ ] 테스트 완료 <!-- progress:auto:tests-complete -->
- [ ] 다음 기능 PRD Notion 작성 완료 <!-- progress:manual:notion-prd-complete -->

## 완료 기준

- 노선 검색 API가 동작합니다.
- 노선 정류장 조회 API가 동작합니다.
- routeId 기반 SSE 스트림이 `connected`, `snapshot`, `error`, `heartbeat` 이벤트 계약을 지킵니다.
- Core NATS subject `bus.position.route.{routeId}.snapshot`으로 위치 스냅샷이 발행됩니다.
- Redis는 노선 검색과 정류장 목록 캐시에만 사용됩니다.
- 전체 Gradle 테스트가 통과합니다.

## 다음 기능 PRD 작성 규칙

구현 완료와 테스트 완료 후 에이전트는 현재 Notion의 `Personal Project / BusTracker` 페이지 아래에 다음 기능 PRD를 작성해야 합니다.

PRD 제목 형식:

```text
PRD - <다음 기능명>
```

PRD 필수 섹션:

- 문제 정의
- 목표
- 사용자 흐름
- 기능 범위
- API 또는 UI 계약
- 비범위
- 성공 기준
- 테스트 기준

PRD 작성이 끝나면 이 파일의 `다음 기능 PRD Notion 작성 완료` 항목을 체크합니다.
