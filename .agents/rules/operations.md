# Operations and Configuration Rules

## Configuration

Use typed configuration properties for:

- Seoul public API service key.
- NATS URL and connection name.
- NATS request timeout.
- SSE polling interval.
- Route and station cache TTL values.

Do not read environment variables directly inside business services. Bind through Spring configuration.

## Secrets

Never commit:

- `.env`
- public data portal service keys
- JWT secrets
- database passwords
- NATS auth tokens

Logs must not include service keys or auth tokens.

## Docker Compose

The repository uses `infra/` for NATS files. Compose paths should reference:

- `infra/nats.Dockerfile`
- `infra/nats.conf`
- `infra/logs/nats`

Keep MySQL, Redis, and NATS on the same compose network for local development.

## Logging

Log these failures with enough context to diagnose the issue:

- Seoul API HTTP/client failure.
- Seoul API error code response.
- XML parsing failure.
- NATS connection, publish, or subscribe failure.
- SSE send failure or client disconnect.

Include routeId and subject when available. Do not include secrets.

## SSE Runtime Behavior

- Send `connected` immediately after opening a stream.
- Send `heartbeat` periodically to keep idle connections alive.
- Send `error` for recoverable failures and keep the stream open when possible.
- Clean up emitters, NATS subscriptions, and poller references on timeout, completion, and error.
