package com.cmu02.bustracker.position.domain;

/**
 * 한 노선상의 단일 차량 위치 도메인 모델.
 * congestion, occupancy는 서울 API에서 자주 누락되므로 nullable로 둔다.
 */
public record VehiclePosition(
        String vehicleId,
        String plainNo,
        int sectionOrd,
        int stationSeq,
        boolean stopFlag,
        BusType busType,
        Integer congestion,
        Integer occupancy
) {
}
