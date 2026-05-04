package com.cmu02.bustracker.common.messaging;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 메시지 브로커에 대한 발행/구독 공통 인터페이스.
 * <p>
 * 현재는 NATS 구현체만 존재하지만, 향후 Kafka 등 다른 브로커로
 * 전환할 때 구현체만 교체하면 되도록 DIP(의존성 역전 원칙)를 적용한다.
 * </p>
 */
public interface MessageBrokerClient {

    void publish(String topic, String message);

    /**
     * 지정한 토픽을 구독하고, 수신된 메시지를 처리하는 핸들러를 등록한다.
     *
     * @param topic   구독할 토픽 이름
     * @param handler 수신된 메시지 본문을 처리하는 콜백 함수
     * @return 구독 해제에 사용할 수 있는 {@link Subscription} 객체
     */
    Subscription subscribe(String topic, Consumer<String> handler);

    /**
     * Queue Group 기반 구독. 동일 그룹 내 하나의 인스턴스만 메시지를 수신한다.
     * <p>Request-Reply 패턴에서 중복 처리를 방지하는 데 사용한다.</p>
     *
     * @param topic      구독할 토픽 이름
     * @param queueGroup 큐 그룹 이름
     * @param handler    수신된 메시지를 처리하는 콜백 (NATS Message 전체를 전달)
     * @return 구독 해제에 사용할 수 있는 {@link Subscription} 객체
     */
    Subscription subscribeWithReply(String topic, String queueGroup,
                                    java.util.function.BiConsumer<String, ReplyCallback> handler);

    /**
     * Request-Reply 패턴으로 메시지를 발행하고 응답을 기다린다.
     *
     * @param topic   요청 토픽
     * @param payload 요청 본문 (JSON 문자열)
     * @param timeout 응답 대기 최대 시간
     * @return 응답 본문, 타임아웃 시 {@link Optional#empty()}
     */
    Optional<String> request(String topic, String payload, Duration timeout);

    /**
     * Reply 콜백 — 구독 핸들러에서 응답을 보낼 때 사용
     */
    @FunctionalInterface
    interface ReplyCallback {
        void reply(String response);
    }
}
