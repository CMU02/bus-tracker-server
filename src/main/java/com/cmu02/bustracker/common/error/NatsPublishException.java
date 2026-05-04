package com.cmu02.bustracker.common.error;

/**
 * NATS publish 실패를 표현하는 예외.
 * Connection 단절, 직렬화 실패 등을 모두 본 예외로 묶어 호출 측에서 recoverable 처리한다.
 */
public class NatsPublishException extends BusTrackerException {

    public NatsPublishException(String routeId, Throwable cause) {
        super(ErrorCode.NATS_PUBLISH_ERROR, "NATS publish failed routeId=" + routeId, cause);
    }
}
