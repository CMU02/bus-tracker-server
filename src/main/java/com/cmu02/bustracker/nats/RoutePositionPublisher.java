package com.cmu02.bustracker.nats;

import com.cmu02.bustracker.common.error.NatsPublishException;
import com.cmu02.bustracker.common.messaging.MessageBrokerClient;
import com.cmu02.bustracker.common.messaging.MessageSerializer;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PositionSnapshot을 NATS subject로 publish한다.
 * 직렬화와 브로커 통신은 각각 MessageSerializer, MessageBrokerClient에 위임한다.
 */
@RequiredArgsConstructor
public class RoutePositionPublisher {

    private static final Logger log = LoggerFactory.getLogger(RoutePositionPublisher.class);

    private final MessageBrokerClient broker;
    private final MessageSerializer serializer;

    public void publish(PositionSnapshot snapshot) {
        String subject = NatsSubject.routeSnapshot(snapshot.routeId());
        String payload = serializer.serialize(snapshot)
                .orElseThrow(() -> new NatsPublishException(snapshot.routeId(),
                        new IllegalStateException("snapshot 직렬화 실패")));
        try {
            broker.publish(subject, payload);
        } catch (Exception e) {
            log.warn("NATS publish 실패 routeId={} subject={}", snapshot.routeId(), subject, e);
            throw new NatsPublishException(snapshot.routeId(), e);
        }
    }
}
