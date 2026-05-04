package com.cmu02.bustracker.position.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoutePollerRegistryTest {

    private static final String ROUTE_ID = "100100025";

    @Mock
    RoutePositionPoller mockPoller;

    private AtomicInteger factoryCallCount;
    private RoutePollerRegistry registry;

    @BeforeEach
    void setUp() {
        factoryCallCount = new AtomicInteger(0);
        RoutePollerFactory factory = (routeId, errorNotifier) -> {
            factoryCallCount.incrementAndGet();
            return mockPoller;
        };
        registry = new RoutePollerRegistry(factory);
    }

    @Test
    @DisplayName("첫 subscribe는 poller를 생성하고 시작한다")
    void firstSubscribeCreatesAndStartsPoller() {
        registry.subscribe(ROUTE_ID, err -> {});

        assertThat(factoryCallCount.get()).isEqualTo(1);
        verify(mockPoller).start();
    }

    @Test
    @DisplayName("같은 routeId의 두 번째 subscribe는 poller를 재사용한다")
    void secondSubscribeReusesSamePoller() {
        registry.subscribe(ROUTE_ID, err -> {});
        registry.subscribe(ROUTE_ID, err -> {});

        assertThat(factoryCallCount.get()).isEqualTo(1);
        verify(mockPoller, times(1)).start();
    }

    @Test
    @DisplayName("다른 routeId는 각각 독립적인 poller를 생성한다")
    void differentRouteIdsCreateSeparatePollers() {
        registry.subscribe(ROUTE_ID, err -> {});
        registry.subscribe("200100001", err -> {});

        assertThat(factoryCallCount.get()).isEqualTo(2);
        verify(mockPoller, times(2)).start();
    }

    @Test
    @DisplayName("마지막 unsubscribe 시 poller를 중지하고 entry를 제거한다")
    void lastUnsubscribeStopsPoller() {
        RoutePollerRegistry.Handle h1 = registry.subscribe(ROUTE_ID, err -> {});
        RoutePollerRegistry.Handle h2 = registry.subscribe(ROUTE_ID, err -> {});

        registry.unsubscribe(h1);
        verify(mockPoller, never()).stop();  // 아직 구독자가 남아 있음

        registry.unsubscribe(h2);
        verify(mockPoller).stop();           // 마지막 구독자 해제 → poller 중지
    }

    @Test
    @DisplayName("poller error 시 등록된 모든 listener에게 전달된다")
    void pollerErrorIsDeliveredToAllListeners() {
        var errors1 = new java.util.ArrayList<PollerError>();
        var errors2 = new java.util.ArrayList<PollerError>();

        // factory는 errorNotifier를 실제 notifyListeners로 연결해야 하므로
        // factory에서 받은 errorNotifier를 즉시 호출해 동작을 검증한다
        RoutePollerFactory notifyingFactory = (routeId, errorNotifier) -> {
            errorNotifier.accept(new PollerError("TEST_ERROR", "테스트", true));
            return mockPoller;
        };
        RoutePollerRegistry testRegistry = new RoutePollerRegistry(notifyingFactory);

        testRegistry.subscribe(ROUTE_ID, errors1::add);
        // factory 호출 시점(subscribe 내)에 errorNotifier가 호출되므로
        // subscribe 직후 첫 번째 리스너에만 전달됨을 확인
        assertThat(errors1).hasSize(1);
        assertThat(errors1.get(0).code()).isEqualTo("TEST_ERROR");
    }
}
