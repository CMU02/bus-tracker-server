package com.cmu02.bustracker.common.error;

/**
 * BusTracker 도메인 에러 코드.
 * 외부 API 실패, JSON parsing 실패, NATS 실패는 서로 다른 코드로 구분한다.
 */
public enum ErrorCode {
    SEOUL_API_ERROR, // 서울 공공 API 호출 실패
    SEOUL_API_PARSE_ERROR, // 서울 공공 API 응답 JSON 파싱 실패
    NATS_PUBLISH_ERROR, // NATS 메시지 발행 실패
    NATS_SUBSCRIBE_ERROR, // NATS 메시지 구독 실패
    ROUTE_NOT_FOUND, // 요청한 버스 노선을 찾을 수 없음
    STATION_NOT_FOUND, // 요청한 노선의 정류장 정보를 찾을 수 없음
    INVALID_REQUEST, // 요청 파라미터 또는 경로 값이 유효하지 않음
    INTERNAL_ERROR // 서버 내부 처리 실패
}
