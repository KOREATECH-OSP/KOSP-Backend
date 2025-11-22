package kr.ac.koreatech.sw.kosp.domain.auth.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "jwtStatus", timeToLive = 1800) // 30분 (1800초)
public class JwtToken {

    @Id
    private Integer id;

    private String accessToken;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private String refreshToken;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;
}

