package com.cmu02.bustracker.common.error;

/**
 * BusTracker 도메인 에러 코드.
 * 외부 API 실패, JSON parsing 실패, NATS 실패는 서로 다른 코드로 구분한다.
 */
public enum ErrorCode {
    SEOUL_API_ERROR,
    SEOUL_API_PARSE_ERROR,
    NATS_PUBLISH_ERROR,
    NATS_SUBSCRIBE_ERROR,
    ROUTE_NOT_FOUND,
    INVALID_REQUEST,
    INTERNAL_ERROR
}
