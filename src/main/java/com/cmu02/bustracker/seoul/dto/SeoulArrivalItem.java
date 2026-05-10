package com.cmu02.bustracker.seoul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 서울 getArrInfoByRoute 단일 정류소 도착예정 항목.
 * 첫번째(1)·두번째(2) 버스 정보가 하나의 항목에 포함된다.
 */
public record SeoulArrivalItem(
        @JsonProperty("stId") String stId,
        @JsonProperty("stNm") String stNm,
        @JsonProperty("ord") String ord,
        @JsonProperty("busRouteId") String busRouteId,
        @JsonProperty("rtNm") String rtNm,
        @JsonProperty("vehId1") String vehId1,
        @JsonProperty("plainNo1") String plainNo1,
        @JsonProperty("busType1") String busType1,
        @JsonProperty("arrmsg1") String arrmsg1,
        @JsonProperty("arrprevstationcnt1") String arrprevstationcnt1,
        @JsonProperty("rerdie1") String rerdie1,
        @JsonProperty("vehId2") String vehId2,
        @JsonProperty("plainNo2") String plainNo2,
        @JsonProperty("busType2") String busType2,
        @JsonProperty("arrmsg2") String arrmsg2,
        @JsonProperty("arrprevstationcnt2") String arrprevstationcnt2,
        @JsonProperty("rerdie2") String rerdie2
) {
}
