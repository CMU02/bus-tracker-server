# Architecture Rules

## Product Direction

BusTracker solves the refresh-driven bus tracking UX problem. The backend should expose route and position data so a client can show where each bus is along the route, not only when it may arrive.

## MVP Boundaries

Include:

- Seoul bus public API integration.
- Route search by bus number.
- Route station list lookup.
- Route-based SSE position streaming.
- Core NATS subject publish/subscribe for position snapshots.
- Redis caching for route and station lookup data.

Exclude for now:

- Frontend UI implementation.
- Spring Security and JWT.
- Position history persistence.
- NATS JetStream.
- Prometheus/Actuator metrics unless explicitly requested.

## Package Shape

Use feature-oriented packages under `com.cmu02.bustracker`:

- `route`: public route search and station lookup API.
- `position`: vehicle position snapshots and SSE streaming.
- `seoul`: Seoul public API client and JSON parsing.
- `nats`: NATS connection and route snapshot messaging.
- `config`: typed application properties and infrastructure beans.
- `common`: shared exceptions and response/error utilities only when reused.

Keep each class responsible for one job. For example, do not mix JSON parsing, polling, and SSE session management in one service.

## Data Flow

1. Client searches routes with a bus number.
2. Backend calls Seoul route API and caches the route search result.
3. Client requests stations for a selected route.
4. Backend calls Seoul station API and caches the station list.
5. Client opens an SSE stream for the route.
6. Stream manager starts or reuses a route poller.
7. Poller fetches Seoul bus position snapshots and publishes to NATS.
8. SSE subscriber forwards NATS snapshot messages to the client.
9. When the last stream closes, clean up route poller and NATS subscription.

## NATS Rules

- Use Core NATS only.
- Subject format: `bus.position.route.{routeId}.snapshot`.
- Do not introduce JetStream in MVP code.
- Message payload should be JSON matching the SSE `snapshot` contract.
- Keep NATS-specific types out of controller DTOs.

## Persistence Rules

- Do not store position snapshots in MySQL.
- Use Redis only for route search and station list caches.
- Keep cache keys explicit and versionable, for example `route:search:v1:{query}`.
