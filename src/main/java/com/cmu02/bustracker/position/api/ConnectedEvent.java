package com.cmu02.bustracker.position.api;

/**
 * SSE 스트림이 열렸을 때 즉시 전송되는 connected 이벤트 payload.
 */
public record ConnectedEvent(String routeId, String message) {

    public static ConnectedEvent of(String routeId) {
        return new ConnectedEvent(routeId, "connected");
    }
}
