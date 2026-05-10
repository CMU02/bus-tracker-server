package com.cmu02.bustracker.position.application;

import com.cmu02.bustracker.common.error.NatsPublishException;
import com.cmu02.bustracker.common.error.SeoulApiException;
import com.cmu02.bustracker.nats.RoutePositionPublisher;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import com.cmu02.bustracker.position.domain.StopFlag;
import com.cmu02.bustracker.seoul.client.SeoulBusPositionClient;
import com.cmu02.bustracker.seoul.dto.SeoulVehiclePositionItem;
import com.cmu02.bustracker.seoul.parser.SeoulBusJsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoutePositionPollerTest {

    private static final String ROUTE_ID = "100100025";

    @Mock
    SeoulBusPositionClient positionClient;
    @Mock
    SeoulBusJsonParser parser;
    @Mock
    RoutePositionPublisher publisher;
    @Mock
    ScheduledExecutorService scheduler;

    private final List<PollerError> capturedErrors = new ArrayList<>();
    private RoutePositionPoller poller;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-02T13:30:00Z"), ZoneId.of("Asia/Seoul"));
        poller = new RoutePositionPoller(
                ROUTE_ID, 50, 15,
                positionClient, parser, publisher,
                scheduler, fixedClock,
                capturedErrors::add
        );
    }

    @Test
    @DisplayName("정상 응답 시 올바른 routeId로 snapshot을 publish한다")
    void publishesSnapshotWithCorrectRouteId() {
        given(positionClient.getBusPosByRouteSt(ROUTE_ID, 1, 50))
                .willReturn("{\"msgHeader\":{\"headerCd\":\"0\"}}");
        given(parser.parseVehiclePositions(anyString())).willReturn(List.of(
                new SeoulVehiclePositionItem("V001", "서울70사1234", "5", "183", "0", "111700442", "20170810132951", "126.919766", "37.623303", "1", "111001109", "192917.8", "458196.8", "100100025")
        ));

        poller.pollOnce();

        ArgumentCaptor<PositionSnapshot> captor = ArgumentCaptor.forClass(PositionSnapshot.class);
        verify(publisher).publish(captor.capture());
        assertThat(captor.getValue().routeId()).isEqualTo(ROUTE_ID);
        assertThat(captor.getValue().vehicles()).hasSize(1);
        assertThat(captor.getValue().pollingIntervalSeconds()).isEqualTo(15);
    }

    @Test
    @DisplayName("Seoul API 오류 시 SEOUL_API_ERROR PollerError를 errorNotifier로 전달한다")
    void notifiesSeoulApiError() {
        given(positionClient.getBusPosByRouteSt(anyString(), anyInt(), anyInt()))
                .willReturn("{}");
        given(parser.parseVehiclePositions(anyString()))
                .willThrow(new SeoulApiException("4", "결과가 없습니다."));

        poller.pollOnce();

        assertThat(capturedErrors).hasSize(1);
        assertThat(capturedErrors.get(0).code()).isEqualTo("SEOUL_API_ERROR");
        assertThat(capturedErrors.get(0).recoverable()).isTrue();
    }

    @Test
    @DisplayName("NATS publish 실패 시 NATS_PUBLISH_ERROR PollerError를 errorNotifier로 전달한다")
    void notifiesNatsPublishError() {
        given(positionClient.getBusPosByRouteSt(anyString(), anyInt(), anyInt()))
                .willReturn("{}");
        given(parser.parseVehiclePositions(anyString())).willReturn(List.of());
        willThrow(new NatsPublishException(ROUTE_ID, new RuntimeException("connection closed")))
                .given(publisher).publish(any(PositionSnapshot.class));

        poller.pollOnce();

        assertThat(capturedErrors).hasSize(1);
        assertThat(capturedErrors.get(0).code()).isEqualTo("NATS_PUBLISH_ERROR");
        assertThat(capturedErrors.get(0).recoverable()).isTrue();
    }

    @Test
    @DisplayName("busType 코드가 올바른 enum으로 변환된다")
    void mapsSeoulBusTypeToEnum() {
        given(positionClient.getBusPosByRouteSt(anyString(), anyInt(), anyInt()))
                .willReturn("{}");
        given(parser.parseVehiclePositions(anyString())).willReturn(List.of(
                new SeoulVehiclePositionItem("V001", "번호판A", "1", null, "1", null, null, null, null, "1", null, null, null, null),
                new SeoulVehiclePositionItem("V002", "번호판B", "3", null, "0", null, null, null, null, "0", null, null, null, null)
        ));

        poller.pollOnce();

        ArgumentCaptor<PositionSnapshot> captor = ArgumentCaptor.forClass(PositionSnapshot.class);
        verify(publisher).publish(captor.capture());
        var vehicles = captor.getValue().vehicles();
        assertThat(vehicles.get(0).busType().name()).isEqualTo("LOW_FLOOR");
        assertThat(vehicles.get(0).stopFlag()).isEqualTo(StopFlag.ARRIVAL);
        assertThat(vehicles.get(1).busType().name()).isEqualTo("GENERAL");
        assertThat(vehicles.get(1).congestion()).isNull();  // getBusPosByRouteSt는 congetion 필드를 제공하지 않음
    }
}
