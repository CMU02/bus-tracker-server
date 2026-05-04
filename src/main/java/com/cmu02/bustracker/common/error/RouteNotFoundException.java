package com.cmu02.bustracker.common.error;

/**
 * routeId에 해당하는 노선 정류장 데이터가 존재하지 않을 때 던진다.
 */
public class RouteNotFoundException extends BusTrackerException {

    public RouteNotFoundException(String routeId) {
        super(ErrorCode.ROUTE_NOT_FOUND, "Route not found routeId=" + routeId);
    }
}
