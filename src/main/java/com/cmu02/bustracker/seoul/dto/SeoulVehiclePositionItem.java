package com.cmu02.bustracker.seoul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 서울 getBusPosByRouteSt 단일 차량 위치 항목.
 * Seoul API 원본 필드명을 그대로 노출하므로, 외부 API에 직접 전달하지 않는다.
 * 숫자 필드도 원본이 문자열로 내려오므로 String으로 받고 변환은 도메인 매핑 단계에서 수행한다.
 */
public record SeoulVehiclePositionItem(
        @JsonProperty("vehId") String vehId,
        @JsonProperty("plainNo") String plainNo,
        @JsonProperty("sectOrd") String sectOrd,
        @JsonProperty("sectDist") String sectDist,
        @JsonProperty("stopFlag") String stopFlag,
        @JsonProperty("sectionId") String sectionId,
        @JsonProperty("dataTm") String dataTm,
        @JsonProperty("tmX") String tmX,
        @JsonProperty("tmY") String tmY,
        @JsonProperty("busType") String busType,
        @JsonProperty("lastStnId") String lastStnId,
        @JsonProperty("posX") String posX,
        @JsonProperty("posY") String posY,
        @JsonProperty("routeId") String routeId
) {
}
