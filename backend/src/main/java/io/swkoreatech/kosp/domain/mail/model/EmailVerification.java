package io.swkoreatech.kosp.domain.mail.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 인증 모델.
 * Redis에 저장되는 이메일 인증 코드 및 상태를 관리한다.
 */
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

    /** 이메일 인증을 완료 처리한다. */
    public void verify() {
        this.isVerified = true;
    }

    /**
     * TTL(만료 시간)을 갱신한다.
     *
     * @param ttl 새로운 TTL (초)
     */
    public void updateTtl(long ttl) {
        this.ttl = ttl;
    }
}
