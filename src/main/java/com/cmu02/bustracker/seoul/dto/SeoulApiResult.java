package com.cmu02.bustracker.seoul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 서울 공공 API 응답 공통 envelope.
 * msgHeader 검증 후 msgBody의 itemList를 도메인 변환 단계에 넘긴다.
 */
public record SeoulApiResult<T>(
        @JsonProperty("msgHeader") SeoulApiHeader header,
        @JsonProperty("msgBody") SeoulApiBody<T> body
) {
}
