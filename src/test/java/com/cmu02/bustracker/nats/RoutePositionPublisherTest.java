package com.cmu02.bustracker.nats;

import com.cmu02.bustracker.common.error.NatsPublishException;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import tools.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RoutePositionPublisherTest {

    private Connection connection;
    private RoutePositionPublisher publisher;

    @BeforeEach
    void setUp() {
        connection = mock(Connection.class);
        ObjectMapper mapper = new ObjectMapper();
        publisher = new RoutePositionPublisher(() -> connection, mapper);
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

        verify(connection).publish(eq("bus.position.route.100100025.snapshot"), any(byte[].class));
    }

    @Test
    @DisplayName("Connection.publish 실패는 NatsPublishException으로 변환된다")
    void wrapsPublishFailure() {
        PositionSnapshot snapshot = new PositionSnapshot(
                "100100025",
                OffsetDateTime.of(2026, 5, 2, 22, 30, 0, 0, ZoneOffset.ofHours(9)),
                15,
                List.of()
        );
        doThrow(new IllegalStateException("connection closed"))
                .when(connection).publish(any(String.class), any(byte[].class));

        assertThatThrownBy(() -> publisher.publish(snapshot))
                .isInstanceOf(NatsPublishException.class);
        assertThat(snapshot.routeId()).isEqualTo("100100025");
    }
}
