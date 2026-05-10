package com.cmu02.bustracker.position.domain;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 한 노선의 단일 폴링 시점에 모은 차량 위치 묶음.
 * SSE snapshot 이벤트 payload, NATS publish payload의 단일 source of truth로 사용한다.
 */
public record PositionSnapshot(
        String routeId,
        RouteType routeType,
        OffsetDateTime timestamp,
        int pollingIntervalSeconds,
        List<VehiclePosition> vehicles
) {
}
