package com.cmu02.bustracker.nats;

import com.cmu02.bustracker.position.domain.PositionSnapshot;
import tools.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * NATS subject를 구독하고 PositionSnapshot 단위 메시지로 변환해 핸들러에 전달한다.
 * 단일 Dispatcher를 가입자별로 새로 만들어 가입/해지 동작이 서로 영향을 주지 않게 한다.
 */
public class RoutePositionSubscriber {

    private static final Logger log = LoggerFactory.getLogger(RoutePositionSubscriber.class);

    private final RoutePositionPublisher.ConnectionSupplier connectionSupplier;
    private final ObjectMapper objectMapper;

    public RoutePositionSubscriber(RoutePositionPublisher.ConnectionSupplier connectionSupplier,
                                    ObjectMapper objectMapper) {
        this.connectionSupplier = connectionSupplier;
        this.objectMapper = objectMapper;
    }

    public Subscription subscribe(String routeId, Consumer<PositionSnapshot> handler) {
        Connection connection = connectionSupplier.get();
        Dispatcher dispatcher = connection.createDispatcher(message -> {
            try {
                PositionSnapshot snapshot = objectMapper.readValue(message.getData(), PositionSnapshot.class);
                handler.accept(snapshot);
            } catch (Exception e) {
                log.warn("snapshot dispatch 실패 routeId={}", routeId, e);
            }
        });
        String subject = NatsSubject.routeSnapshot(routeId);
        dispatcher.subscribe(subject);
        return new Subscription(routeId, dispatcher, connection);
    }

    public void unsubscribe(Subscription subscription) {
        if (subscription == null) {
            return;
        }
        try {
            subscription.connection().closeDispatcher(subscription.dispatcher());
        } catch (Exception e) {
            log.warn("NATS dispatcher 정리 실패 routeId={}", subscription.routeId(), e);
        }
    }

    public record Subscription(String routeId, Dispatcher dispatcher, Connection connection) {
    }
}
