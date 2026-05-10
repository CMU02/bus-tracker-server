package com.cmu02.bustracker.common.error;

/**
 * 정류소 목록에서 요청한 stationId를 찾지 못했을 때 던지는 예외.
 */
public class StationNotFoundException extends BusTrackerException {

    public StationNotFoundException(String stationId) {
        super(ErrorCode.STATION_NOT_FOUND, "정류소를 찾을 수 없습니다: " + stationId);
    }
}
