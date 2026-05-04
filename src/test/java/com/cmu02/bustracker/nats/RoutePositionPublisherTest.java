package com.cmu02.bustracker.nats;

import com.cmu02.bustracker.common.error.NatsPublishException;
import com.cmu02.bustracker.common.messaging.JacksonMessageSerializer;
import com.cmu02.bustracker.common.messaging.MessageBrokerClient;
import com.cmu02.bustracker.common.messaging.MessageSerializer;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RoutePositionPublisherTest {

    private MessageBrokerClient broker;
    private RoutePositionPublisher publisher;

    @BeforeEach
    void setUp() {
        broker = mock(MessageBrokerClient.class);
        MessageSerializer serializer = new JacksonMessageSerializer(new ObjectMapper());
        publisher = new RoutePositionPublisher(broker, serializer);
    }

    @Test
    @DisplayName("snapshot은 bus.position.route.{routeId}.snapshot subject로 publish된다")
    void publishesToExpectedSubject() {
        PositionSnapshot snapshot = new PositionSnapshot(
                "100100025",
                OffsetDateTime.of(2026, 5, 2, 22, 30, 0, 0, ZoneOffset.ofHours(9)),
                15,
                List.of()
        );

        publisher.publish(snapshot);

        verify(broker).publish(eq("bus.position.route.100100025.snapshot"), anyString());
    }

    @Test
    @DisplayName("MessageBrokerClient.publish 실패는 NatsPublishException으로 변환된다")
    void wrapsPublishFailure() {
        PositionSnapshot snapshot = new PositionSnapshot(
                "100100025",
                OffsetDateTime.of(2026, 5, 2, 22, 30, 0, 0, ZoneOffset.ofHours(9)),
                15,
                List.of()
        );
        doThrow(new RuntimeException("connection closed"))
                .when(broker).publish(anyString(), anyString());

        assertThatThrownBy(() -> publisher.publish(snapshot))
                .isInstanceOf(NatsPublishException.class);
    }
}
