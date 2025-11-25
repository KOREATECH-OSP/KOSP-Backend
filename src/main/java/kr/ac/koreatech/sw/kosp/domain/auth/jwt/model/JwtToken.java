package kr.ac.koreatech.sw.kosp.domain.auth.jwt.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "jwtStatus", timeToLive = 1800) // 30분 (1800초)
public class JwtToken {

    @Id
    private Integer id;

    private String accessToken;

    private long accessTokenExpiration;

    private String refreshToken;

    private long refreshTokenExpiration;
}

