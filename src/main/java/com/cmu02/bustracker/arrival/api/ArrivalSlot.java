package com.cmu02.bustracker.arrival.api;

/**
 * 도착 예정 단일 버스 슬롯 응답 DTO.
 * Seoul API 원본 필드명을 그대로 노출하지 않는다.
 */
public record ArrivalSlot(
        int slot,
        String vehicleId,
        String licensePlate,
        String arrivalMessage,
        int stopsAway,
        String busType
) {
}
