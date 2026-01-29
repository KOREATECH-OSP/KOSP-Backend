package io.swkoreatech.kosp.global.security.filter;

import java.io.IOException;
import java.util.Collections;

import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import io.swkoreatech.kosp.global.auth.resolver.TokenHeaderResolver;
import io.swkoreatech.kosp.global.auth.token.AccessToken;
import io.swkoreatech.kosp.global.auth.token.JwtToken;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 인증 필터
 * Authorization 헤더의 ACCESS 토큰 처리
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final TokenHeaderResolver tokenHeaderResolver;
    
    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            boolean hasAuthHeader = request.getHeader("Authorization") != null;
            log.info("🔐 [AUTH] {} {} | Auth header: {}", method, uri, hasAuthHeader);
            
            String tokenString = extractFromHeader(request);
            
            if (tokenString.isBlank()) {
                log.info("🔐 [AUTH] {} {} | No token found", method, uri);
            } else {
                log.info("🔐 [AUTH] {} {} | Token extracted (length: {})", method, uri, tokenString.length());
            }
            
            if (!tokenString.isBlank()) {
                // ✅ JwtToken.from()으로 검증
                AccessToken token = JwtToken.from(AccessToken.class, tokenString);
                
                // SecurityContext 설정
                authenticateUser(token);
                log.info("🔐 [AUTH] {} {} | Authenticated user ID: {}", method, uri, token.getUserId());
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            log.warn("🔐 [AUTH] {} {} | Authentication failed: {}", method, uri, e.getMessage(), e);
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
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
