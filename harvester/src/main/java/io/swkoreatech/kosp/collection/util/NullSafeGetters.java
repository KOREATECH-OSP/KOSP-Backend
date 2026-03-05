package io.swkoreatech.kosp.collection.util;

/**
 * null 안전한 래퍼 타입-기본형 변환 유틸리티 클래스.
 *
 * <p>nullable Integer/Long 값을 int/long 기본형으로 안전하게 변환하며,
 * null인 경우 0을 기본값으로 반환한다.
 */
public final class NullSafeGetters {

    private NullSafeGetters() {
        throw new AssertionError("Utility class");
    }

    /**
     * 정수 값을 반환하되, null이면 0을 반환한다.
     *
     * @param value nullable 정수 값
     * @return 값 또는 null인 경우 0
     */
    public static int intOrZero(Integer value) {
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * long 값을 반환하되, null이면 0L을 반환한다.
     *
     * @param value nullable long 값
     * @return 값 또는 null인 경우 0L
     */
    public static long longOrZero(Long value) {
        if (value == null) {
            return 0L;
        }
        return value;
    }
}
