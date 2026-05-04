package com.cmu02.bustracker.seoul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 서울 공공 API 응답 공통 헤더.
 * headerCd가 "0"이면 정상으로 간주한다.
 */
public record SeoulApiHeader(
        @JsonProperty("headerCd") String headerCd,
        @JsonProperty("headerMsg") String headerMsg,
        @JsonProperty("itemCount") int itemCount
) {
    public static final String SUCCESS_CODE = "0";

    public boolean isSuccess() {
        return SUCCESS_CODE.equals(headerCd);
    }
}
