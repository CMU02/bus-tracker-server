package com.cmu02.bustracker.nats;

/**
 * NATS subject 이름 규칙을 한 곳에서 관리한다.
 * route snapshot subject 형식: bus.position.route.{routeId}.snapshot
 */
public final class NatsSubject {

    private NatsSubject() {
    }

    public static String routeSnapshot(String routeId) {
        return "bus.position.route." + routeId + ".snapshot";
    }
}
