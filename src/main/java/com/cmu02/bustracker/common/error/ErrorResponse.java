package com.cmu02.bustracker.common.error;

import java.time.Instant;

/**
 * SSE 외 HTTP 오류 응답에 사용하는 공통 JSON 바디.
 * service key, token, password를 message에 포함하지 않는다.
 */
public record ErrorResponse(String code, String message, Instant timestamp) {
}
