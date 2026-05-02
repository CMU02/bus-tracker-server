# Project Structure Rules

이 문서는 BusTracker 백엔드의 권장 파일 구조를 정의합니다. 새 기능을 추가할 때는 이 구조를 우선 따르고, 구조를 벗어나야 하면 이유를 작업 보고에 남깁니다.

## 기본 원칙

- 기능(feature) 중심으로 패키지를 나눕니다.
- controller, service, DTO만으로 큰 패키지를 만들지 않습니다.
- 서울 공공 API, NATS, Redis 같은 외부 기술은 도메인 코드와 분리합니다.
- MVP에서는 위치 이력 저장을 하지 않으므로 position entity/repository를 만들지 않습니다.
- Spring Security/JWT 관련 패키지는 인증 기능을 시작하기 전까지 만들지 않습니다.
- 테스트 파일은 main 패키지 구조를 그대로 따라갑니다.

## 권장 루트 구조

```text
bustracker/
  AGENTS.md
  CLAUDE.md
  build.gradle
  compose.yaml
  infra/
    nats.Dockerfile
    nats.conf
  .agents/
    rules/
  .claude/
    hooks/
  src/
    main/
      java/com/cmu02/bustracker/
      resources/
    test/
      java/com/cmu02/bustracker/
```

## Java 패키지 구조

```text
com.cmu02.bustracker/
  BusTrackerApplication.java

  common/
    error/
      ErrorCode.java
      ErrorResponse.java
      GlobalExceptionHandler.java
    time/
      ClockConfig.java

  config/
    CacheConfig.java
    FeignConfig.java
    NatsConfig.java
    SseConfig.java
    SeoulBusProperties.java
    NatsProperties.java
    CacheProperties.java

  route/
    api/
      RouteController.java
      RouteResponse.java
      StationResponse.java
    application/
      RouteService.java
    domain/
      BusRoute.java
      BusStation.java
    cache/
      RouteCache.java
      StationCache.java

  position/
    api/
      PositionStreamController.java
      PositionSnapshotEvent.java
      PositionErrorEvent.java
    application/
      PositionStreamService.java
      RoutePositionPoller.java
      RoutePollerRegistry.java
    domain/
      PositionSnapshot.java
      VehiclePosition.java

  seoul/
    client/
      SeoulBusRouteClient.java
      SeoulBusPositionClient.java
    parser/
      SeoulBusJsonParser.java
    dto/
      SeoulRouteItem.java
      SeoulStationItem.java
      SeoulVehiclePositionItem.java
      SeoulApiResult.java

  nats/
    NatsConnectionManager.java
    RoutePositionPublisher.java
    RoutePositionSubscriber.java
    NatsSubject.java
```

## 패키지 책임

### `route`

버스 노선 검색과 정류장 목록 조회를 담당합니다.

- public API는 `route/api`에 둡니다.
- 비즈니스 흐름은 `route/application`에 둡니다.
- 외부 API DTO를 controller 응답으로 직접 반환하지 않습니다.
- Redis 캐시는 `route/cache`에 둡니다.

### `position`

routeId 기반 실시간 위치 스트리밍을 담당합니다.

- SSE endpoint는 `position/api`에 둡니다.
- poller lifecycle과 stream lifecycle은 `position/application`에 둡니다.
- 위치 스냅샷과 차량 위치 모델은 `position/domain`에 둡니다.
- NATS 타입이나 Seoul API JSON DTO를 domain 모델에 섞지 않습니다.

### `seoul`

서울 공공 API 호출과 JSON parsing을 담당합니다.

- Feign client는 `seoul/client`에 둡니다.
- JSON parsing은 `seoul/parser`에 둡니다.
- 서울 API 원본 필드 대응 DTO는 `seoul/dto`에 둡니다.
- public API 응답 형식은 이 패키지에 두지 않습니다.

### `nats`

Core NATS 연결과 subject publish/subscribe를 담당합니다.

- subject 생성 규칙은 `NatsSubject` 한 곳에 둡니다.
- subject 형식은 `bus.position.route.{routeId}.snapshot`을 사용합니다.
- JetStream 관련 파일은 MVP에서 만들지 않습니다.

### `config`

Spring bean과 typed properties를 담당합니다.

- 환경변수는 service에서 직접 읽지 않습니다.
- `@ConfigurationProperties`로 bind한 뒤 주입합니다.
- 설정 클래스는 외부 시스템 연결과 properties 등록에 집중합니다.

### `common`

두 개 이상의 기능 패키지에서 재사용되는 코드만 둡니다.

- 단일 기능에서만 쓰는 예외(exception), DTO, helper는 해당 기능 패키지 안에 둡니다.
- `common`을 잡다한 유틸리티 보관소로 만들지 않습니다.

## 테스트 구조

```text
src/test/java/com/cmu02/bustracker/
  route/
    api/
    application/
    cache/
  position/
    api/
    application/
  seoul/
    parser/
  nats/
```

테스트 이름은 동작을 드러내도록 작성합니다.

예:

- `SeoulBusJsonParserTest`
- `RouteServiceTest`
- `PositionStreamControllerTest`
- `RoutePollerRegistryTest`
- `RoutePositionPublisherTest`

## 리소스 구조

```text
src/main/resources/
  application.yaml
  application-dev.yaml
  application-prod.yaml

src/test/resources/
  seoul/
    route-list-success.json
    station-list-success.json
    bus-position-success.json
    api-error.json
```

테스트 fixture에는 실제 service key, token, password를 넣지 않습니다.

## 금지 구조

다음 구조는 만들지 않습니다.

```text
controller/
service/
repository/
dto/
util/
```

위처럼 기술 계층만으로 루트 패키지를 나누면 BusTracker의 route, position, seoul, nats 책임 경계가 흐려집니다.

## 구조 변경 규칙

- 새 루트 패키지는 실제 기능 경계가 생겼을 때만 추가합니다.
- 파일이 250줄을 넘거나 책임이 두 개 이상이면 분리 후보로 봅니다.
- 구조 변경은 기능 구현과 섞지 말고 가능한 별도 커밋으로 분리합니다.
