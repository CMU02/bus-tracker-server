package com.cmu02.bustracker.common.error;

/**
 * 서울 공공 API JSON 본문 파싱 실패를 표현하는 예외.
 */
public class SeoulApiParseException extends BusTrackerException {

    public SeoulApiParseException(String message, Throwable cause) {
        super(ErrorCode.SEOUL_API_PARSE_ERROR, message, cause);
    }
}
