package io.swkoreatech.kosp.global.auth.token;

import lombok.Getter;
import lombok.Setter;

/**
 * 토큰 타입 정의
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

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    /**
     * category 문자열로부터 TokenType 조회
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
