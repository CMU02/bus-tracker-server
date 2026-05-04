package com.cmu02.bustracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SSE polling 및 heartbeat 주기를 정의하는 설정 프로퍼티.
 * - intervalSeconds: 위치 snapshot 폴링 주기
 * - heartbeatSeconds: SSE 연결 유지 신호 주기
 */
@ConfigurationProperties(prefix = "bustracker.sse")
public record SseProperties(int intervalSeconds, int heartbeatSeconds) {
}
