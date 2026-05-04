package com.cmu02.bustracker.position.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * routeId별 RoutePositionPoller 생명주기를 관리한다.
 *
 * <p>subscribe 호출이 처음이면 poller를 생성하고 시작한다.
 * unsubscribe로 마지막 구독이 해제되면 poller를 중지하고 entry를 제거한다.
 * 동시성 보장을 위해 ConcurrentHashMap.compute()로 entry를 원자적으로 조작한다.
 */
public class RoutePollerRegistry {

    private static final Logger log = LoggerFactory.getLogger(RoutePollerRegistry.class);

    private final RoutePollerFactory factory;
    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();

    public RoutePollerRegistry(RoutePollerFactory factory) {
        this.factory = factory;
    }

    /**
     * routeId에 대한 구독을 등록한다.
     * 첫 번째 구독이면 poller를 생성하고 시작한다.
     *
     * @throws com.cmu02.bustracker.common.error.RouteNotFoundException routeId가 유효하지 않을 때
     */
    public Handle subscribe(String routeId, Consumer<PollerError> listener) {
        entries.compute(routeId, (id, existing) -> {
            if (existing == null) {
                // listener를 먼저 등록한 뒤 poller를 생성해야 한다.
                // factory가 errorNotifier를 즉시 호출하더라도(예: 초기화 도중 발생한 에러)
                // listener가 이미 캡처된 리스트에 있어 알림을 수신할 수 있다.
                CopyOnWriteArrayList<Consumer<PollerError>> listeners = new CopyOnWriteArrayList<>();
                listeners.add(listener);
                // factory.create 내부에서 Seoul API를 호출해 routeId 검증 및 lastSeq 산출
                RoutePositionPoller poller = factory.create(id, err -> notifyListeners(listeners, err));
                Entry entry = new Entry(poller, listeners);
                entry.refCount++;
                poller.start();
                log.debug("poller entry 생성 routeId={}", id);
                return entry;
            }
            existing.listeners.add(listener);
            existing.refCount++;
            log.debug("구독 추가 routeId={} refCount={}", id, existing.refCount);
            return existing;
        });
        return new Handle(routeId, listener);
    }

    /**
     * 구독을 해제한다. 마지막 구독이면 poller를 중지하고 entry를 제거한다.
     */
    public void unsubscribe(Handle handle) {
        entries.compute(handle.routeId(), (id, entry) -> {
            if (entry == null) {
                return null;
            }
            entry.listeners.remove(handle.listener());
            entry.refCount--;
            log.debug("구독 해제 routeId={} refCount={}", id, entry.refCount);
            if (entry.refCount <= 0) {
                entry.poller.stop();
                log.debug("poller entry 제거 routeId={}", id);
                return null; // ConcurrentHashMap에서 key 제거
            }
            return entry;
        });
    }

    private void notifyListeners(List<Consumer<PollerError>> listeners, PollerError err) {
        for (Consumer<PollerError> listener : listeners) {
            try {
                listener.accept(err);
            } catch (Exception e) {
                log.warn("error listener 처리 실패", e);
            }
        }
    }

    private static class Entry {
        final RoutePositionPoller poller;
        final List<Consumer<PollerError>> listeners;
        int refCount = 0;

        Entry(RoutePositionPoller poller, List<Consumer<PollerError>> listeners) {
            this.poller = poller;
            this.listeners = listeners;
        }
    }

    /**
     * 구독 식별자. unsubscribe 시 사용한다.
     */
    public record Handle(String routeId, Consumer<PollerError> listener) {
    }
}
