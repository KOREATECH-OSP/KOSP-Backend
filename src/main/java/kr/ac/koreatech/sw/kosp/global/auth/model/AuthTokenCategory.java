package kr.ac.koreatech.sw.kosp.global.auth.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthTokenCategory {
    LOGIN("login"),
    SIGNUP("signup");

    private final String value;

    public static AuthTokenCategory fromValue(String value) {
        for (AuthTokenCategory category : values()) {
            if (category.getValue().equals(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown AuthTokenCategory: " + value);
    }
}
