package com.cmu02.bustracker.common.util;

/**
 * 문자열 파싱 공통 유틸리티.
 * <p>Seoul API 응답값은 숫자임에도 null 또는 빈 문자열로 오는 경우가 있어
 * 안전한 변환 메서드를 한 곳에서 관리한다.</p>
 */
public final class ParsingUtils {

    private ParsingUtils() {
    }

    /**
     * 문자열을 int로 변환한다. null, 빈 문자열, 변환 실패 시 0을 반환한다.
     */
    public static int parseIntSafe(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 문자열을 Integer로 변환한다. null, 빈 문자열, 변환 실패 시 null을 반환한다.
     */
    public static Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
