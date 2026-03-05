package io.swkoreatech.kosp.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 비밀번호 재설정 토큰.
 * Redis에 저장되며, TTL을 통해 자동 만료된다.
 */
@Getter
@Builder
@AllArgsConstructor
@RedisHash("password_reset_token")
public class PasswordResetToken {

    @Id
    private String token;

    private Long userId;

    @TimeToLive
    private long ttl;
}
