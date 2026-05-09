package com.cmu02.bustracker.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * NATS Connection의 생성/종료를 담당한다.
 * bustracker.nats.url 프로퍼티가 비어 있으면 빈으로 등록되지 않아 테스트 환경에서는 자동 비활성화된다.
 * Connection 자체를 외부에 노출하므로 publisher와 subscriber는 본 매니저에서 동일 Connection을 공유한다.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "bustracker.nats", name = "url")
public class NatsConnectionManager {

    @Value("${bustracker.nats.url}")
    private String url;

    @Value("${bustracker.nats.connectionName:}")
    private String connectionName;

    @Value("${bustracker.nats.requestTimeoutMs}")
    private long requestTimeoutMs;

    private Connection connection;

    @PostConstruct
    public void start() throws Exception {
        Options.Builder builder = new Options.Builder()
                .server(url)
                .connectionTimeout(Duration.ofMillis(requestTimeoutMs));
        if (connectionName != null && !connectionName.isBlank()) {
            builder.connectionName(connectionName);
        }
        connection = Nats.connect(builder.build());
        log.info("NATS connection established url={} name={}", url, connectionName);
    }

    @PreDestroy
    public void stop() throws Exception {
        if (connection != null) {
            connection.close();
            log.info("NATS connection closed");
        }
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("NATS connection이 초기화되지 않았습니다.");
        }
        return connection;
    }
}
