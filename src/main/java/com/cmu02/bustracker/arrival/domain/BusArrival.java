package com.cmu02.bustracker.arrival.domain;

import com.cmu02.bustracker.position.domain.BusType;

/**
 * 정류소 기준 단일 도착 예정 버스 도메인 모델.
 * slot은 1(첫번째 버스) 또는 2(두번째 버스)이다.
 * vehId가 null·blank·"0"인 슬롯은 ArrivalService에서 제외한 뒤 이 모델을 생성한다.
 */
public record BusArrival(
        int slot,
        String vehicleId,
        String licensePlate,
        String arrivalMessage,
        int stopsAway,
        BusType busType
) {
}
