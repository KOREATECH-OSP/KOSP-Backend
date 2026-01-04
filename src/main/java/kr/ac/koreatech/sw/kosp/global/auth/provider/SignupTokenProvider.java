package kr.ac.koreatech.sw.kosp.global.auth.provider;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthToken;
import kr.ac.koreatech.sw.kosp.global.auth.model.AuthTokenCategory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SignupTokenProvider extends JwtAuthTokenProvider {

    @Value("${jwt.expiration-time.signup-token}")
    private long signupExpiration;

    public SignupTokenProvider(
        @Value("${jwt.secret-key}") String secret
    ) {
        super(secret);
    }

    /**
     * 회원가입 JWS 토큰 생성
     * @param subject JWT subject (예: githubId)
     * @param claims 모든 GitHub attributes + encryptedGithubToken
     */
    public AuthToken<Claims> createSignupToken(String subject, Map<String, Object> claims) {
        // category 추가
        Map<String, Object> enrichedClaims = new java.util.HashMap<>(claims);
        enrichedClaims.put("category", AuthTokenCategory.SIGNUP.getValue());
        
        return createAuthToken(subject, enrichedClaims, signupExpiration);
    }

    /**
     * 회원가입 JWS 토큰 파싱 및 검증
     */
    public AuthToken<Claims> parseSignupToken(String token) {
        return convertAuthToken(token);
    }
}
