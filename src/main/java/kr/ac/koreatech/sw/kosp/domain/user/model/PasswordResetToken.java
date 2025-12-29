package kr.ac.koreatech.sw.kosp.domain.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

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
