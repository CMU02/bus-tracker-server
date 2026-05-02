# API Contract Rules

## Public Endpoints

Use `/api/v1` for MVP endpoints.

### Route Search

`GET /api/v1/routes?query=146`

Return a JSON array of route summaries:

- `routeId`
- `routeName`
- `startStationName`
- `endStationName`
- `routeType`
- `term`

Validation:

- Reject blank `query` with HTTP 400.
- Return an empty array when Seoul API returns no matches.

### Station List

`GET /api/v1/routes/{routeId}/stations`

Return a JSON array ordered by station sequence:

- `routeId`
- `sequence`
- `sectionId`
- `stationId`
- `stationName`
- `stationNumber`
- `arsId`
- `direction`
- `gpsX`
- `gpsY`

Validation:

- Reject blank `routeId` with HTTP 400.
- Return HTTP 404 when no station data exists for the route.

### Position Stream

`GET /api/v1/routes/{routeId}/positions/stream`

Response content type:

- `text/event-stream`

Events:

- `connected`: stream opened successfully.
- `snapshot`: current route vehicle positions.
- `error`: recoverable upstream, parsing, or messaging failure.
- `heartbeat`: connection keepalive.

`snapshot` payload:

- `routeId`
- `timestamp`
- `pollingIntervalSeconds`
- `vehicles`

Vehicle payload:

- `vehicleId`
- `plainNo`
- `sectionOrd`
- `stationSeq`
- `stopFlag`
- `busType`
- `congestion`
- `occupancy`

Use nullable fields when Seoul API omits optional values.

## Error Response Shape

For non-SSE HTTP errors, use a consistent JSON body:

- `code`
- `message`
- `timestamp`

Keep messages useful for developers, but do not expose secrets such as service keys.

## External API Rules

Seoul public API responses should be parsed from XML into internal DTOs before mapping to public API DTOs.

Required Seoul APIs:

- `getBusRouteList?serviceKey=...&strSrch={query}`
- `getStaionByRoute?serviceKey=...&busRouteId={routeId}`
- `getBusPosByRouteSt?serviceKey=...&busRouteId={routeId}&startOrd=1&endOrd={lastSeq}`

Never pass raw Seoul API XML directly to clients.
