package com.cmu02.bustracker.seoul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 서울 getStaionByRoute 단일 정류장 항목.
 * 본 MVP에서는 routeId 유효성 검증과 lastSeq 산출에만 사용한다.
 */
public record SeoulStationItem(
        @JsonProperty("seq") String seq,
        @JsonProperty("section") String section,
        @JsonProperty("station") String station,
        @JsonProperty("stationNm") String stationNm,
        @JsonProperty("stationNo") String stationNo,
        @JsonProperty("arsId") String arsId,
        @JsonProperty("direction") String direction,
        @JsonProperty("gpsX") String gpsX,
        @JsonProperty("gpsY") String gpsY,
        @JsonProperty("busRouteAbrv") String busRouteAbrv,
        @JsonProperty("busRouteId") String busRouteId,
        @JsonProperty("busRouteNm") String busRouteNm,
        @JsonProperty("fullSectDist") String fullSectDist,
        @JsonProperty("routeType") String routeType,
        @JsonProperty("beginTm") String beginTm,
        @JsonProperty("lastTm") String lastTm,
        @JsonProperty("trnstnid") String trnstnid,
        @JsonProperty("posX") String posX,
        @JsonProperty("posY") String posY,
        @JsonProperty("sectSpd") String sectSpd,
        @JsonProperty("transYn") String transYn
) {
}
