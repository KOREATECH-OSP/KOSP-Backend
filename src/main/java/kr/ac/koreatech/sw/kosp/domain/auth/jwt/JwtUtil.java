package kr.ac.koreatech.sw.kosp.domain.auth.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    public Map<String, String> parseHeaders(String token) throws JsonProcessingException {
        String header = token.split("\\.")[0];
        return new ObjectMapper().readValue(decodeHeader(header), new TypeReference<>() {});
    }

    public String decodeHeader(String token) {
        return new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
    }

    public Claims getTokenClaims(String token, PublicKey publicKey) {
        return Jwts.parser()
            .verifyWith(publicKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean validateIdToken(Claims claims, String expectedIssuer, String expectedAudience) {
        if (!claims.getIssuer().equals(expectedIssuer)) {
            return false;
        }
        Object audClaim = claims.get("aud");
        if (audClaim instanceof String) {
            if (!audClaim.equals(expectedAudience)) {
                return false;
            }
        } else if (audClaim instanceof List && !((List<?>) audClaim).contains(expectedAudience)) {
            return false;
        }

        return !claims.getExpiration().before(new java.util.Date()); // 토큰 만료 검증 실패
    }
}
