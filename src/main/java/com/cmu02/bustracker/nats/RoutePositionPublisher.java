package com.cmu02.bustracker.nats;

import com.cmu02.bustracker.common.error.NatsPublishException;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import tools.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionSnapshot을 NATS subject로 publish한다.
 * 본 클래스는 직접 빈으로 등록하지 않고, NATS connection 가용 시점에 NatsBeansConfig에서 주입한다.
 * 실패는 NatsPublishException으로 wrapping해 호출 측이 recoverable error로 처리하도록 한다.
 */
public class RoutePositionPublisher {

    private static final Logger log = LoggerFactory.getLogger(RoutePositionPublisher.class);

    private final ConnectionSupplier connectionSupplier;
    private final ObjectMapper objectMapper;

    public RoutePositionPublisher(ConnectionSupplier connectionSupplier, ObjectMapper objectMapper) {
        this.connectionSupplier = connectionSupplier;
        this.objectMapper = objectMapper;
    }

    public void publish(PositionSnapshot snapshot) {
        String subject = NatsSubject.routeSnapshot(snapshot.routeId());
        try {
            byte[] payload = objectMapper.writeValueAsBytes(snapshot);
            Connection connection = connectionSupplier.get();
            connection.publish(subject, payload);
        } catch (Exception e) {
            log.warn("NATS publish failed routeId={} subject={}", snapshot.routeId(), subject, e);
            throw new NatsPublishException(snapshot.routeId(), e);
        }
    }

    /**
     * Connection 조회를 함수 인터페이스로 추상화해 테스트에서 stub 주입을 단순화한다.
     */
    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get();
    }
}
