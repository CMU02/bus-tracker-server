package com.cmu02.bustracker.position.domain;

/**
 * 서울 버스 차종 분류.
 * Seoul API busType 코드(0/1/2)를 도메인 enum으로 정규화한다.
 * 인식 불가능한 값은 UNKNOWN으로 표현한다.
 */
public enum BusType {
    GENERAL,
    LOW_FLOOR,
    ARTICULATED,
    UNKNOWN;

    public static BusType fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        return switch (code) {
            case "0" -> GENERAL;
            case "1" -> LOW_FLOOR;
            case "2" -> ARTICULATED;
            default -> UNKNOWN;
        };
    }
}
