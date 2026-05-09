package com.cmu02.bustracker.nats;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * NATS subject 이름 규칙을 한 곳에서 관리한다.
 * route snapshot subject 형식: bus.position.route.{routeId}.snapshot
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NatsSubject {
    public static String routeSnapshot(String routeId) {
        return "bus.position.route." + routeId + ".snapshot";
    }
}
