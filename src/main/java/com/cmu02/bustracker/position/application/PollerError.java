package com.cmu02.bustracker.position.application;

/**
 * RoutePositionPoller가 한 폴링 틱에서 발생한 오류를 나타낸다.
 * recoverable=true이면 다음 틱에서 자동 회복을 시도하며 SSE error 이벤트로 전달한다.
 */
public record PollerError(String code, String message, boolean recoverable) {
}
