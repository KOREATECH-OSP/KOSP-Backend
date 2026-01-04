package kr.ac.koreatech.sw.kosp.global.security.filter;

import java.io.IOException;

import io.jsonwebtoken.Claims;

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
import kr.ac.koreatech.sw.kosp.domain.auth.service.UserDetailsServiceImpl;
import kr.ac.koreatech.sw.kosp.global.auth.core.AuthToken;
import kr.ac.koreatech.sw.kosp.global.auth.provider.LoginTokenProvider;
import kr.ac.koreatech.sw.kosp.global.auth.model.AuthTokenCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final LoginTokenProvider loginTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String token = resolveToken(request);
        
        if (token != null) {
            authenticateIfValid(token);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateIfValid(String tokenStr) {
        AuthToken<?> token = (AuthToken<?>) loginTokenProvider.convertAuthToken(tokenStr);
        if (!token.validate()) {
            return;
        }
        processAuthentication(token, tokenStr);
    }

    private void processAuthentication(AuthToken<?> token, String tokenStr) {
        Claims claims = (Claims) token.getData();
        String categoryStr = claims.get("category", String.class);
        if (categoryStr == null) {
            return;
        }
        setSecurityContext(claims.getSubject(), categoryStr, tokenStr);
    }

    private void setSecurityContext(String id, String categoryStr, String tokenStr) {
        AuthTokenCategory category = AuthTokenCategory.fromValue(categoryStr);
        var userDetails = userDetailsService.loadUser(id, category);
        Authentication auth = new UsernamePasswordAuthenticationToken(
            userDetails, tokenStr, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
