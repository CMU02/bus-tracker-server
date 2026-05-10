package com.cmu02.bustracker.position.domain;

public enum StopFlag {
    IN_PROGRESS, // 운행 중
    ARRIVAL, // 도착
    UNKNOWN;

    public static StopFlag fromCode(String code) {
        if (code == null) return UNKNOWN;

        return switch (code) {
            case "0" -> IN_PROGRESS;
            case "1" -> ARRIVAL;
            default -> UNKNOWN;
        };
    }
}
