# BusTracker

서울 버스의 실시간 위치를 SSE(Server-Sent Events)로 스트리밍하는 백엔드 서비스입니다.

기존의 새로고침 기반 버스 위치 확인 방식을 개선하여, 클라이언트가 별도 요청 없이도 노선상 각 버스의 현재 위치를 지속적으로 수신할 수 있게 합니다.

---

## 주요 기능

- **노선 검색**: 버스 번호로 서울 노선 목록 조회
- **정류장 조회**: 노선 ID 기반 경유 정류장 순서 및 좌표 조회
- **실시간 위치 스트림**: SSE를 통해 노선별 차량 위치를 지속적으로 전달
- **내부 메시지 버스**: Core NATS로 poller와 SSE 스트림 계층 분리
- **Redis 캐싱**: 노선·정류장 조회 결과를 캐시하여 외부 API 부하 감소

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4, Spring MVC |
| 외부 API 호출 | OpenFeign |
| 메시지 브로커 | Core NATS (`io.nats:jnats`) |
| 캐시 | Redis |
| 데이터베이스 | MySQL |
| 빌드 | Gradle Wrapper |
| 인프라 | Docker Compose |

---

## 아키텍처

### 데이터 흐름

```
클라이언트
  │
  ├─ GET /api/v1/routes?query=146          ── RouteController
  │                                              └─ RouteService → 서울 getBusRouteList API
  │                                                               └─ Redis 캐시
  │
  ├─ GET /api/v1/routes/{routeId}/stations ── RouteController
  │                                              └─ RouteService → 서울 getStaionByRoute API
  │                                                               └─ Redis 캐시
  │
  └─ GET /api/v1/routes/{routeId}/positions/stream  ── PositionStreamController (SSE)
         │
         ├─ RoutePollerRegistry
         │    └─ RoutePositionPoller (15초 주기)
         │         └─ 서울 getBusPosByRouteSt API
         │              └─ RoutePositionPublisher → NATS subject
         │                   bus.position.route.{routeId}.snapshot
         │
         └─ RoutePositionSubscriber
              └─ NATS subscribe → SseEmitter로 snapshot 이벤트 전달
```

### SSE 이벤트 종류

| 이벤트 | 설명 |
|--------|------|
| `connected` | 스트림 연결 성공 시 최초 1회 전송 |
| `snapshot` | 현재 노선상 차량 위치 목록 (15초 주기) |
| `heartbeat` | 연결 유지 신호 (30초 주기) |
| `error` | 상위 API·NATS·파싱 오류 발생 시 |

### 패키지 구조

```
com.cmu02.bustracker/
  ├── BusTrackerApplication.java
  │
  ├── route/           # 노선 검색 · 정류장 조회 공개 API
  │   ├── api/
  │   ├── application/
  │   ├── domain/
  │   └── cache/
  │
  ├── position/        # SSE 스트리밍 · 차량 위치 도메인
  │   ├── api/
  │   ├── application/
  │   └── domain/
  │
  ├── seoul/           # 서울 공공 API 클라이언트 · 파서
  │   ├── client/
  │   ├── parser/
  │   └── dto/
  │
  ├── nats/            # Core NATS 연결 · Pub/Sub
  │
  ├── config/          # Spring 빈 설정 · typed properties
  │
  └── common/          # 공유 예외 · 에러 응답
```

---

## API 명세

### 노선 검색

```
GET /api/v1/routes?query={버스번호}
```

**응답 예시**

```json
[
  {
    "routeId": "100100118",
    "routeName": "146",
    "startStationName": "개화역",
    "endStationName": "중곡동",
    "routeType": "3",
    "term": "5"
  }
]
```

### 정류장 목록 조회

```
GET /api/v1/routes/{routeId}/stations
```

**응답 예시**

```json
[
  {
    "routeId": "100100118",
    "sequence": 1,
    "stationId": "112000202",
    "stationName": "개화역",
    "stationNumber": "13285",
    "arsId": "13285",
    "direction": "중곡동",
    "gpsX": "126.9107",
    "gpsY": "37.5813"
  }
]
```

### 실시간 위치 스트림 (SSE)

```
GET /api/v1/routes/{routeId}/positions/stream
Content-Type: text/event-stream
```

**`snapshot` 이벤트 페이로드**

```json
{
  "routeId": "100100118",
  "timestamp": "2026-05-09T10:30:00+09:00",
  "pollingIntervalSeconds": 15,
  "vehicles": [
    {
      "vehicleId": "111033352",
      "plainNo": "서울75사2644",
      "sectionOrd": 3,
      "stationSeq": 3,
      "stopFlag": false,
      "busType": "GENERAL",
      "congestion": null,
      "occupancy": null
    }
  ]
}
```

### 에러 응답 형식 (비SSE)

```json
{
  "code": "ROUTE_NOT_FOUND",
  "message": "노선을 찾을 수 없습니다.",
  "timestamp": "2026-05-09T10:30:00+09:00"
}
```

---

## 로컬 실행

### 사전 요구사항

- Java 21
- Docker & Docker Compose
- 공공데이터포털 서울 버스 API 인증키 ([신청](https://www.data.go.kr))

### 1. 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다.

```dotenv
# 데이터베이스
DB_HOST=localhost
DB_PORT=3306
DB_SCHEMA=bustracker
DB_USERNAME=bustracker
DB_PASSWORD=bustracker

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# NATS
NATS_URL=nats://localhost:4222
NATS_CONNECTION_NAME=bustracker-dev
NATS_AUTH_TOKEN=your_nats_token

# 공공데이터포털 서울 버스 API 인증키
PUBLIC_DATA_PORTAL_ENCODE_KEY=your_api_key_here
```

### 2. 인프라 실행

```bash
docker compose up -d
```

| 서비스 | 포트 |
|--------|------|
| MySQL | 3306 |
| Redis | 6379 |
| NATS (클라이언트) | 4222 |
| NATS (모니터링) | 8222 |

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 실행 프로파일은 `dev`입니다.

### 4. 테스트

```bash
./gradlew test
```

---

## NATS Subject 규칙

```
bus.position.route.{routeId}.snapshot
```

- Core NATS만 사용합니다. JetStream은 MVP 범위 외입니다.
- 메시지 페이로드는 SSE `snapshot` 이벤트와 동일한 JSON 형식입니다.

---

## MVP 제외 항목

다음 기능은 현재 버전에서 제공하지 않습니다.

- 프론트엔드 UI
- Spring Security / JWT 인증
- 차량 위치 이력 저장
- NATS JetStream
- Prometheus / Actuator 메트릭
