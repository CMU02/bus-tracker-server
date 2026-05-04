package com.cmu02.bustracker.common.error;

import lombok.Getter;

/**
 * BusTracker 도메인 공통 예외.
 * 호출 측이 ErrorCode 단위로 처리할 수 있도록 코드를 보존한다.
 */
@Getter
public class BusTrackerException extends RuntimeException {

    private final ErrorCode code;

    public BusTrackerException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public BusTrackerException(ErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
