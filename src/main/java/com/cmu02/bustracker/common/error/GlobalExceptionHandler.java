package com.cmu02.bustracker.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * 전역 예외 처리기. SSE 엔드포인트 진입 전 routeId 유효성 오류 등을 HTTP 응답으로 변환한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRouteNotFound(RouteNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getCode().name(), e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(BusTrackerException.class)
    public ResponseEntity<ErrorResponse> handleBusTracker(BusTrackerException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getCode().name(), e.getMessage(), Instant.now()));
    }
}
