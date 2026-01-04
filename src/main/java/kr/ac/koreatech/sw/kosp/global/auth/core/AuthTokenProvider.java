package kr.ac.koreatech.sw.kosp.global.auth.core;

import java.util.Map;

public interface AuthTokenProvider<T> {
    T createAuthToken(String id, Map<String, Object> claims, long expireTime);
    T convertAuthToken(String token);
}
