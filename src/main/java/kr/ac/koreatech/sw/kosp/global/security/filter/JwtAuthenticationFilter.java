package kr.ac.koreatech.sw.kosp.global.security.filter;

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
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.auth.resolver.TokenHeaderResolver;
import kr.ac.koreatech.sw.kosp.global.auth.token.AccessToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.JwtToken;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
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
    
    private String extractFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return tokenHeaderResolver.resolveHeaderName(AccessToken.class);
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
