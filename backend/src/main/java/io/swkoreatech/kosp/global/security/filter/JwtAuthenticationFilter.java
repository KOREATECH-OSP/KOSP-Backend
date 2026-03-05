package io.swkoreatech.kosp.global.security.filter;

import java.io.IOException;
import java.util.Collections;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.global.auth.resolver.TokenHeaderResolver;
import io.swkoreatech.kosp.global.auth.token.AccessToken;
import io.swkoreatech.kosp.global.auth.token.JwtToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 기반 인증 필터.
 * <p>HTTP 요청의 Authorization 헤더 또는 X-Access-Token 헤더에서 ACCESS 토큰을 추출하고,
 * 유효한 토큰이면 SecurityContext에 인증 정보를 설정한다.
 * 비동기 SSE 디스패치에서는 필터를 건너뛴다.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final TokenHeaderResolver tokenHeaderResolver;

    /** {@inheritDoc} */
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String tokenString = extractFromHeader(request);

            if (!tokenString.isBlank()) {
                // ✅ JwtToken.from()으로 검증
                AccessToken token = JwtToken.from(AccessToken.class, tokenString);

                // SecurityContext 설정
                authenticateUser(token);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.debug("Authentication failed: {}", e.getMessage());
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /** SSE 비동기 디스패치 시 JWT 검증을 건너뛴다. */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true; // Skip JWT validation on SSE async dispatches
    }

    private String extractFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        String headerName = tokenHeaderResolver.resolveHeaderName(AccessToken.class);
        String token = request.getHeader(headerName);

        if (StringUtils.hasText(token)) {
            return token;
        }

        return "";
    }

    private void authenticateUser(AccessToken token) {
        User user = userRepository.findById(token.getUserId())
            .orElseThrow(() -> new GlobalException(ExceptionMessage.AUTHENTICATION));

        if (user.isDeleted()) {
            throw new GlobalException(ExceptionMessage.AUTHENTICATION);
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(
            user, null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
