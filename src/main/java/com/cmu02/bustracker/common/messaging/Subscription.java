package com.cmu02.bustracker.common.messaging;

/**
 * 메시지 브로커 구독을 나타내는 인터페이스.
 * <p>
 * 브로커 종류(NATS Dispatcher, Kafka Consumer 등)에 관계없이
 * 구독 해제를 통일된 방식으로 처리할 수 있도록 추상화한다.
 * </p>
 */
public interface Subscription extends AutoCloseable {
    /**
     * 구독을 해제한다.
     * <p>
     * {@link AutoCloseable#close()}와 동일하지만,
     * checked exception을 던지지 않도록 재정의한다.
     * </p>
     */
    @Override
    void close();
}
