package com.cmu02.bustracker.position.application;

import com.cmu02.bustracker.common.messaging.Subscription;
import com.cmu02.bustracker.nats.RoutePositionSubscriber;
import com.cmu02.bustracker.position.api.ConnectedEvent;
import com.cmu02.bustracker.position.api.PositionErrorEvent;
import com.cmu02.bustracker.position.domain.PositionSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * routeId 기반 SSE 스트림 생명주기를 담당한다.
 *
 * <ul>
 *   <li>poller 구독 시작 (RoutePollerRegistry)</li>
 *   <li>NATS snapshot 구독 (RoutePositionSubscriber)</li>
 *   <li>connected 이벤트 즉시 전송</li>
 *   <li>heartbeat 스케줄 등록</li>
 *   <li>emitter 종료 시 모든 리소스 정리</li>
 * </ul>
 */
@Slf4j
@RequiredArgsConstructor
public class PositionStreamService {

    private final RoutePollerRegistry registry;
    private final RoutePositionSubscriber subscriber;
    private final TaskScheduler heartbeatScheduler;
    private final int intervalSeconds;
    private final int heartbeatSeconds;
    private final ObjectMapper objectMapper;

    /**
     * routeId에 대한 SSE 스트림을 열고 SseEmitter를 반환한다.
     *
     * @throws com.cmu02.bustracker.common.error.RouteNotFoundException routeId가 유효하지 않을 때
     */
    public SseEmitter openStream(String routeId) {
        SseEmitter emitter = new SseEmitter(0L);

        // registry.subscribe 내부에서 Seoul API로 routeId를 검증한다.
        // RouteNotFoundException 발생 시 여기서 전파되어 GlobalExceptionHandler가 404로 응답한다.
        RoutePollerRegistry.Handle handle = registry.subscribe(routeId,
                err -> sendError(emitter, routeId, err));

        Subscription natsSub;
        try {
            natsSub = subscriber.subscribe(routeId, snap -> sendSnapshot(emitter, snap));
        } catch (Exception e) {
            log.error("NATS 구독 실패 routeId={}", routeId, e);
            registry.unsubscribe(handle);
            emitter.completeWithError(e);
            return emitter;
        }

        // 연결 확인 즉시 connected 이벤트 전송
        sendConnected(emitter, routeId);

        // heartbeat 주기 스케줄 등록 (snapshot이 없을 때도 연결 유지 신호 제공)
        Instant firstHeartbeat = Instant.now().plusSeconds(heartbeatSeconds);
        Duration heartbeatPeriod = Duration.ofSeconds(heartbeatSeconds);
        ScheduledFuture<?> heartbeat = heartbeatScheduler.scheduleAtFixedRate(
                () -> sendHeartbeat(emitter, routeId),
                firstHeartbeat,
                heartbeatPeriod);

        Runnable cleanup = buildCleanup(heartbeat, natsSub, handle);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(t -> cleanup.run());

        log.info("SSE 스트림 열림 routeId={}", routeId);
        return emitter;
    }

    private Runnable buildCleanup(
            ScheduledFuture<?> heartbeat,
            Subscription natsSub,
            RoutePollerRegistry.Handle handle) {
        return () -> {
            heartbeat.cancel(false);
            natsSub.close();
            registry.unsubscribe(handle);
            log.info("SSE 스트림 정리 routeId={}", handle.routeId());
        };
    }

    private void sendConnected(SseEmitter emitter, String routeId) {
        sendEvent(emitter, "connected", ConnectedEvent.of(routeId));
    }

    private void sendSnapshot(SseEmitter emitter, PositionSnapshot snapshot) {
        sendEvent(emitter, "snapshot", snapshot);
    }

    private void sendError(SseEmitter emitter, String routeId, PollerError err) {
        sendEvent(emitter, "error", new PositionErrorEvent(routeId, err.code(), err.message(), err.recoverable()));
    }

    private void sendHeartbeat(SseEmitter emitter, String routeId) {
        sendEvent(emitter, "heartbeat", Map.of("routeId", routeId));
    }

    private void sendEvent(SseEmitter emitter, String name, Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            emitter.send(SseEmitter.event()
                    .name(name)
                    .data(json, MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            // 클라이언트가 이미 끊긴 경우 send가 IOException을 던질 수 있다.
            // completeWithError로 emitter를 종료하면 onError 핸들러가 cleanup을 수행한다.
            log.warn("SSE send 실패 event={}", name, e);
            emitter.completeWithError(e);
        }
    }
}
