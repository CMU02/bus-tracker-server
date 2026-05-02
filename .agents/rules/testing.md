# Testing Rules

## Default Approach

Use test-first changes for new behavior. A new service, parser, controller, or stream manager should have a focused test that fails before the implementation is added.

## Test Types

Unit tests:

- Seoul XML parser normal response.
- Empty Seoul response.
- Seoul API error code response.
- Missing optional fields.
- Route and station cache key/TTL behavior.
- Route poller publishes snapshots to the expected NATS subject.
- SSE stream cleanup decreases route subscription reference counts.

Web MVC tests:

- `GET /api/v1/routes?query=146` returns route summaries.
- Blank route query returns HTTP 400.
- `GET /api/v1/routes/{routeId}/stations` returns ordered stations.
- Missing route stations returns HTTP 404.
- `GET /api/v1/routes/{routeId}/positions/stream` returns `text/event-stream` and emits `connected`.

Integration checks:

- Docker Compose starts MySQL, Redis, and NATS.
- A real public data key can search a route and open an SSE stream in the dev profile.
- NATS failure produces structured logs and an SSE `error` event where possible.

## Test Data

Keep Seoul XML samples small and local to parser tests unless reused.

Do not use real public data portal keys in tests, source files, or committed fixtures.

## Gradle

Run:

```powershell
.\gradlew.bat test
```

If the sandbox blocks Gradle wrapper cache creation or network downloads, report the failure and continue only with static verification.
