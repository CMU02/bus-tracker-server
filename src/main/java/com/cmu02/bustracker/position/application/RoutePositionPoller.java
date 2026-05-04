package com.cmu02.bustracker.position.application;

import com.cmu02.bustracker.common.error.NatsPublishException;
import com.cmu02.bustracker.common.error.SeoulApiException;
import com.cmu02.bustracker.common.error.SeoulApiParseException;
import com.cmu02.bustracker.nats.RoutePositionPublisher;
import com.cmu02.bustracker.position.domain.BusType;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import com.cmu02.bustracker.position.domain.VehiclePosition;
import com.cmu02.bustracker.seoul.client.SeoulBusPositionClient;
import com.cmu02.bustracker.seoul.dto.SeoulVehiclePositionItem;
import com.cmu02.bustracker.seoul.parser.SeoulBusJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 단일 routeId에 대해 Seoul 버스 위치 API를 주기적으로 폴링하고
 * 결과를 NATS subject로 publish한다.
 *
 * <p>스케줄러 직접 테스트는 복잡하므로, 단위 테스트에서는 {@link #pollOnce()}를 직접 호출해 동작을 검증한다.
 */
public class RoutePositionPoller {

    private static final Logger log = LoggerFactory.getLogger(RoutePositionPoller.class);

    private final String routeId;
    private final int lastSeq;
    private final int intervalSeconds;
    private final SeoulBusPositionClient positionClient;
    private final SeoulBusJsonParser parser;
    private final RoutePositionPublisher publisher;
    private final ScheduledExecutorService scheduler;
    private final Clock clock;
    private final Consumer<PollerError> errorNotifier;

    private volatile ScheduledFuture<?> future;

    public RoutePositionPoller(
            String routeId,
            int lastSeq,
            int intervalSeconds,
            SeoulBusPositionClient positionClient,
            SeoulBusJsonParser parser,
            RoutePositionPublisher publisher,
            ScheduledExecutorService scheduler,
            Clock clock,
            Consumer<PollerError> errorNotifier) {
        this.routeId = routeId;
        this.lastSeq = lastSeq;
        this.intervalSeconds = intervalSeconds;
        this.positionClient = positionClient;
        this.parser = parser;
        this.publisher = publisher;
        this.scheduler = scheduler;
        this.clock = clock;
        this.errorNotifier = errorNotifier;
    }

    public synchronized void start() {
        if (future != null) {
            return;
        }
        future = scheduler.scheduleAtFixedRate(this::pollOnce, 0, intervalSeconds, TimeUnit.SECONDS);
        log.info("poller 시작 routeId={} intervalSeconds={}", routeId, intervalSeconds);
    }

    public synchronized void stop() {
        if (future != null) {
            future.cancel(false);
            future = null;
            log.info("poller 중지 routeId={}", routeId);
        }
    }

    /**
     * 단일 폴링 틱 실행. 테스트에서 직접 호출 가능하도록 package-private.
     */
    void pollOnce() {
        try {
            String json = positionClient.getBusPosByRouteSt(routeId, 1, lastSeq);
            List<SeoulVehiclePositionItem> items = parser.parseVehiclePositions(json);
            PositionSnapshot snapshot = buildSnapshot(items);
            publisher.publish(snapshot);
        } catch (SeoulApiException | SeoulApiParseException e) {
            log.warn("Seoul API 오류 routeId={}", routeId, e);
            errorNotifier.accept(new PollerError("SEOUL_API_ERROR", "서울 버스 위치 API 호출에 실패했습니다.", true));
        } catch (NatsPublishException e) {
            log.warn("NATS publish 실패 routeId={}", routeId, e);
            errorNotifier.accept(new PollerError("NATS_PUBLISH_ERROR", "메시지 발행에 실패했습니다.", true));
        } catch (Exception e) {
            log.error("예기치 않은 폴링 오류 routeId={}", routeId, e);
            errorNotifier.accept(new PollerError("INTERNAL_ERROR", "내부 오류가 발생했습니다.", true));
        }
    }

    private PositionSnapshot buildSnapshot(List<SeoulVehiclePositionItem> items) {
        List<VehiclePosition> vehicles = items.stream()
                .map(this::toVehiclePosition)
                .toList();
        return new PositionSnapshot(routeId, OffsetDateTime.now(clock), intervalSeconds, vehicles);
    }

    private VehiclePosition toVehiclePosition(SeoulVehiclePositionItem item) {
        return new VehiclePosition(
                item.vehId(),
                item.plainNo(),
                parseIntSafe(item.sectOrd()),
                parseIntSafe(item.stOrd()),
                "1".equals(item.stopFlag()),
                BusType.fromCode(item.busType()),
                parseIntOrNull(item.congetion()),
                null  // occupancy: Seoul API에서 제공하지 않음
        );
    }

    private int parseIntSafe(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("정수 변환 실패 routeId={} value={}", routeId, value);
            return 0;
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
