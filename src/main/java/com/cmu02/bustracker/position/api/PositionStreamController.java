package com.cmu02.bustracker.position.api;

import com.cmu02.bustracker.position.application.PositionStreamService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

/**
 * SSE 기반 실시간 버스 위치 스트리밍 엔드포인트.
 *
 * <p>NATS가 설정되지 않은 환경에서는 503을 반환한다.
 * routeId가 유효하지 않으면 GlobalExceptionHandler가 404로 응답한다.
 */
@RestController
@RequestMapping("/api/v1/routes")
public class PositionStreamController {

    private final Optional<PositionStreamService> streamService;

    public PositionStreamController(Optional<PositionStreamService> streamService) {
        this.streamService = streamService;
    }

    @GetMapping(value = "/{routeId}/positions/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String routeId) {
        return streamService
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE, "NATS가 설정되지 않아 위치 스트리밍을 사용할 수 없습니다."))
                .openStream(routeId);
    }
}
