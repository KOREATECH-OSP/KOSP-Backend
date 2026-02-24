package io.swkoreatech.kosp.domain.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
