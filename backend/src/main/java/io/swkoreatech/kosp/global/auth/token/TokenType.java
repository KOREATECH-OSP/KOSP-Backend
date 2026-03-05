package io.swkoreatech.kosp.global.auth.token;

import lombok.Getter;
import lombok.Setter;

/**
 * JWT 토큰 타입을 정의하는 열거형.
 * <p>ACCESS, REFRESH, SIGNUP 세 가지 토큰 타입을 지원하며,
 * 각 타입별 만료 시간을 관리한다.</p>
 */
@Getter
public enum TokenType {

    ACCESS,
    REFRESH,
    SIGNUP,
    ;

    public static final String CLAIM = "category";

    @Setter
    private long expiration;

    /** 토큰 타입 이름을 소문자로 반환한다. */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    /**
     * 카테고리 문자열로부터 해당하는 TokenType을 조회한다.
     *
     * @param category 카테고리 문자열 (대소문자 무관)
     * @return 일치하는 TokenType
     * @throws IllegalArgumentException 일치하는 토큰 타입이 없는 경우
     */
    public static TokenType fromCategory(String category) {
        for (TokenType type : values()) {
            if (type.toString().equalsIgnoreCase(category)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown token category: " + category);
    }
}
