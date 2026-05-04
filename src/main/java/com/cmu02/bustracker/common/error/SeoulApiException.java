package com.cmu02.bustracker.common.error;

/**
 * 서울 공공 API 호출 또는 응답 파싱 실패를 표현하는 예외.
 * Seoul API의 headerCd가 정상이 아닐 때 던진다.
 */
public class SeoulApiException extends BusTrackerException {

    private final String headerCd;

    public SeoulApiException(String headerCd, String headerMsg) {
        super(ErrorCode.SEOUL_API_ERROR, "Seoul API error headerCd=" + headerCd + " headerMsg=" + headerMsg);
        this.headerCd = headerCd;
    }

    public SeoulApiException(String headerCd, String headerMsg, Throwable cause) {
        super(ErrorCode.SEOUL_API_ERROR, "Seoul API error headerCd=" + headerCd + " headerMsg=" + headerMsg, cause);
        this.headerCd = headerCd;
    }

    public String getHeaderCd() {
        return headerCd;
    }
}
