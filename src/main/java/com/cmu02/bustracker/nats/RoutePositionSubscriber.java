package com.cmu02.bustracker.nats;

import com.cmu02.bustracker.common.messaging.MessageBrokerClient;
import com.cmu02.bustracker.common.messaging.MessageSerializer;
import com.cmu02.bustracker.common.messaging.Subscription;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * NATS subject를 구독하고 PositionSnapshot 단위 메시지로 변환해 핸들러에 전달한다.
 * 구독 해제는 반환된 {@link Subscription#close()}를 통해 수행한다.
 */
@RequiredArgsConstructor
public class RoutePositionSubscriber {

    private static final Logger log = LoggerFactory.getLogger(RoutePositionSubscriber.class);

    private final MessageBrokerClient broker;
    private final MessageSerializer serializer;

    public Subscription subscribe(String routeId, Consumer<PositionSnapshot> handler) {
        String subject = NatsSubject.routeSnapshot(routeId);
        return broker.subscribe(subject, message ->
                serializer.deserialize(message, PositionSnapshot.class)
                        .ifPresentOrElse(
                                handler::accept,
                                () -> log.warn("snapshot 역직렬화 실패 routeId={}", routeId)
                        )
        );
    }
}
