package io.swkoreatech.kosp.domain.mail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@AllArgsConstructor
@RedisHash("email_verification")
public class EmailVerification {
    @Id
    private String email;

    private String code;
    private String signupToken;

    private boolean isVerified;

    @TimeToLive
    private long ttl;

    public void verify() {
        this.isVerified = true;
    }

    public void updateTtl(long ttl) {
        this.ttl = ttl;
    }
}
