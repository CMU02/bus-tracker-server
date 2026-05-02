# Code Style Rules

이 문서는 BusTracker 백엔드의 코드 스타일과 설계 원칙을 정의합니다.

## 기본 원칙

- 코드는 작고 명확하게 작성합니다.
- 새 추상화는 실제 중복 제거, 책임 분리, 테스트 용이성 개선이 있을 때만 추가합니다.
- 기능 구현과 리팩터링(refactoring)을 가능한 분리합니다.
- 외부 API, NATS, Redis, SSE 같은 기술 세부사항을 도메인 모델에 섞지 않습니다.
- 코드 주석(comment)은 한국어로 작성합니다.
- 클래스(class), 함수(function), 변수(variable) 이름은 Java 관례에 맞춰 영어로 작성합니다.

## Java 스타일

- Java 21 문법을 사용할 수 있지만, 팀원이 읽기 어려운 과한 문법 사용은 피합니다.
- 불변 데이터 전달에는 record를 우선 고려합니다.
- null을 반환하지 말고 빈 컬렉션(collection), Optional, 명확한 예외(exception)를 사용합니다.
- public method는 입력값 검증 책임을 명확히 합니다.
- magic string과 magic number는 의미 있는 상수로 분리합니다.
- 한 클래스가 250줄을 넘으면 책임 분리 후보로 봅니다.
- 한 method가 40줄을 넘으면 분리 후보로 봅니다.

## Spring 스타일

- 생성자 주입(constructor injection)을 사용합니다.
- field injection은 사용하지 않습니다.
- `@ConfigurationProperties`로 설정값을 바인딩합니다.
- service에서 `System.getenv`를 직접 호출하지 않습니다.
- controller는 요청 validation, service 호출, response mapping에 집중합니다.
- business rule은 controller가 아니라 application service에 둡니다.
- 외부 시스템 client는 interface 역할이 드러나도록 별도 패키지에 둡니다.

## DTO와 Domain

- public API response DTO와 외부 API DTO를 분리합니다.
- Seoul API JSON field 이름을 public API에 그대로 노출하지 않습니다.
- domain model은 route, station, position 같은 BusTracker 언어를 사용합니다.
- NATS message payload는 domain model에서 바로 만들지 말고 mapper 또는 publisher 계층에서 변환합니다.

## Error Handling

- 예외는 원인을 보존합니다.
- 외부 API 실패, JSON parsing 실패, NATS 실패는 서로 다른 error code로 구분합니다.
- 사용자에게 노출되는 error message에는 secret, token, service key를 포함하지 않습니다.
- recoverable SSE 오류는 가능한 경우 `error` 이벤트로 전달하고 stream을 유지합니다.

## Logging

- 로그에는 routeId, subject, external API name처럼 문제 추적에 필요한 식별자를 포함합니다.
- service key, password, token, raw `.env` 값은 절대 로그로 남기지 않습니다.
- catch 후 무시하지 않습니다. 복구하거나, 변환해서 던지거나, 명확히 로그로 남깁니다.

## Test Style

- 테스트 이름은 동작을 설명합니다.
- 테스트는 구현 세부사항보다 observable behavior를 검증합니다.
- 외부 API 호출은 실제 네트워크 대신 fixture 또는 test double을 사용합니다.
- 실제 공공데이터 key, NATS token, DB password를 테스트에 넣지 않습니다.
- 새 parser, service, poller, controller 동작에는 테스트를 먼저 작성합니다.

## SOLID Guidelines

SOLID 원칙은 적절하게 따릅니다. 단, 원칙을 이유로 불필요한 추상화나 과한 계층을 만들지 않습니다.

### SRP: Single Responsibility Principle

- 한 class는 하나의 변경 이유만 가져야 합니다.
- JSON parsing, API 호출, SSE 전송, NATS publish를 한 class에 섞지 않습니다.
- 예: `SeoulBusJsonParser`는 JSON parsing만 담당하고 HTTP 호출은 client가 담당합니다.

### OCP: Open/Closed Principle

- 확장이 예상되는 지점만 닫힌 구현보다 interface를 고려합니다.
- 서울 외 지역 API를 당장 구현하지 않는다면 provider abstraction을 과하게 만들지 않습니다.
- MVP에서는 Seoul API 기준으로 단순하게 만들고, 두 번째 지역 연동이 생길 때 확장 포인트를 분리합니다.

### LSP: Liskov Substitution Principle

- interface 구현체는 같은 입력에 대해 동일한 계약을 지켜야 합니다.
- cache fallback, fake client, test double이 production client와 다른 예외 계약을 갖지 않게 합니다.

### ISP: Interface Segregation Principle

- 큰 service interface 하나보다 작은 역할별 interface를 선호합니다.
- 읽기 전용 route lookup과 position streaming을 같은 interface에 묶지 않습니다.
- 단, 구현체가 하나뿐이고 분리 이점이 없으면 interface를 만들지 않아도 됩니다.

### DIP: Dependency Inversion Principle

- application service는 구체적인 외부 client 구현보다 역할 중심 abstraction에 의존합니다.
- 외부 시스템 세부사항은 `seoul`, `nats`, `config` 패키지에 가둡니다.
- Spring bean wiring은 config에서 처리하고 business code는 생성자 주입으로 의존성을 받습니다.

## Formatting

- Gradle/Spring 기본 포맷과 현재 파일 스타일을 따릅니다.
- import wildcard는 사용하지 않습니다.
- 불필요한 주석, 사용하지 않는 import, 죽은 코드는 추가하지 않습니다.
- Markdown과 YAML은 trailing whitespace 없이 작성합니다.
