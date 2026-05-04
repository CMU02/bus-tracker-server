package com.cmu02.bustracker.position.api;

/**
 * poller 오류를 SSE error 이벤트로 전달할 때 사용하는 payload.
 * recoverable=true이면 클라이언트는 연결을 유지하고 다음 snapshot을 기다린다.
 */
public record PositionErrorEvent(String routeId, String code, String message, boolean recoverable) {
}
