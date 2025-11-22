package kr.ac.koreatech.sw.kosp.domain.auth.jwt.service;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.INVALID_TOKEN;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.auth.jwt.JwtTokenProvider;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.model.JwtToken;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.repository.JwtTokenRedisRepository;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenRedisRepository jwtTokenRedisRepository;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public JwtToken createJwtToken(Integer userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(String.valueOf(userId));
        String refreshToken = jwtTokenProvider.generateRefreshToken(String.valueOf(userId));

        JwtToken jwtToken = createJwtToken(userId, accessToken, refreshToken);
        return jwtTokenRedisRepository.save(jwtToken);
    }

    public JwtToken createJwtToken(Integer id, String accessToken, String refreshToken) {
        return JwtToken.builder()
            .id(id)
            .accessToken(accessToken)
            .accessTokenExpiration(accessTokenExpiration)
            .refreshToken(refreshToken)
            .refreshTokenExpiration(refreshTokenExpiration)
            .build();
    }

    public JwtToken issueAccessToken(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            log.error("🚨 유효하지 않은 refresh_token: {}", refreshToken);
            throw new GlobalException(INVALID_TOKEN.getMessage(), INVALID_TOKEN.getStatus());
        }

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        JwtToken jwtToken = jwtTokenRedisRepository.getById(Integer.parseInt(userId));

        if (jwtToken == null) {
            log.error("🚨 Redis에서 JwtToken를 찾을 수 없음: userId={}", userId);
            throw new GlobalException(INVALID_TOKEN.getMessage(), INVALID_TOKEN.getStatus());
        }

        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        log.info("✅ 새 access_token 발급: {}", accessToken);

        JwtToken updatedToken = JwtToken.builder()
            .id(jwtToken.getId())
            .accessToken(accessToken)
            .accessTokenExpiration(jwtToken.getAccessTokenExpiration())
            .refreshToken(jwtToken.getRefreshToken())
            .refreshTokenExpiration(jwtToken.getRefreshTokenExpiration())
            .build();

        return jwtTokenRedisRepository.save(updatedToken);
    }

    public JwtToken generateAndStoreJwt(Integer userId) {
        String accessToken = jwtTokenProvider.generateAccessToken(String.valueOf(userId));
        String refreshToken = jwtTokenProvider.generateRefreshToken(String.valueOf(userId));

        JwtToken jwtToken = createJwtToken(userId, accessToken, refreshToken);
        return jwtTokenRedisRepository.save(jwtToken);
    }
}
