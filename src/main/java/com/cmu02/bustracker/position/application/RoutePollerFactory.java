package com.cmu02.bustracker.position.application;

import java.util.function.Consumer;

/**
 * routeId에 대한 RoutePositionPoller 생성 책임을 추상화한다.
 * RoutePollerRegistry가 실제 생성 방식을 알지 않아도 되도록 분리했으며,
 * 테스트에서는 이 인터페이스를 stub해 스케줄러 동작 없이 레지스트리 로직만 검증한다.
 */
@FunctionalInterface
public interface RoutePollerFactory {

    /**
     * @throws com.cmu02.bustracker.common.error.RouteNotFoundException routeId에 해당하는 정류장이 없을 때
     */
    RoutePositionPoller create(String routeId, Consumer<PollerError> errorNotifier);
}
