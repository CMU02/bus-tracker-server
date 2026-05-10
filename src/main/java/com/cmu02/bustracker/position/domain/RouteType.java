package com.cmu02.bustracker.position.domain;

/**
 * 서울 버스 노선 유형 분류.
 * Seoul API routeType 코드(0~9)를 도메인 enum으로 정규화한다.
 * 인식 불가능한 값은 UNKNOWN으로 표현한다.
 */
public enum RouteType {
    SHARED,      // 0 - 공용
    AIRPORT,     // 1 - 공항
    VILLAGE,     // 2 - 마을
    TRUNK,       // 3 - 간선
    BRANCH,      // 4 - 지선
    CIRCULAR,    // 5 - 순환
    WIDE_AREA,   // 6 - 광역
    INCHEON,     // 7 - 인천
    GYEONGGI,    // 8 - 경기
    ABOLISHED,   // 9 - 폐지
    UNKNOWN;

    public static RouteType fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        return switch (code) {
            case "0" -> SHARED;
            case "1" -> AIRPORT;
            case "2" -> VILLAGE;
            case "3" -> TRUNK;
            case "4" -> BRANCH;
            case "5" -> CIRCULAR;
            case "6" -> WIDE_AREA;
            case "7" -> INCHEON;
            case "8" -> GYEONGGI;
            case "9" -> ABOLISHED;
            default -> UNKNOWN;
        };
    }
}
