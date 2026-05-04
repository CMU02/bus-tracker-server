package com.cmu02.bustracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Core NATS 연결 설정 프로퍼티.
 */
@ConfigurationProperties(prefix = "bustracker.nats")
public record NatsProperties(
        String url,
        String connectionName,
        long requestTimeoutMs
) {
}
