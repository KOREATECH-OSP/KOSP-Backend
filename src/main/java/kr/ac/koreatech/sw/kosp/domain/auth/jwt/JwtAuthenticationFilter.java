package kr.ac.koreatech.sw.kosp.domain.auth.jwt;

import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.BAD_REQUEST;
import static kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage.INVALID_TOKEN;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.annotation.UserId;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final HandlerMappingIntrospector handlerMappingIntrospector;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/swagger-ui") ||
            requestURI.startsWith("/v3/api-docs") ||
            requestURI.startsWith("/swagger-resources")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("request uri: {}", requestURI);

        boolean requiresJwt;
        HandlerExecutionChain handlerChainExec = findHandler(request);

        if (handlerChainExec != null && handlerChainExec.getHandler() instanceof HandlerMethod handlerMethod) {
            requiresJwt = Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(param -> param.hasParameterAnnotation(UserId.class));

            log.info("handlerMethod.getMethodParameters(): {}", Arrays.toString(handlerMethod.getMethodParameters()));
            log.info("requiresJwt: {}", requiresJwt);

            if (!requiresJwt) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        String accessToken = extractTokenFromCookie(request);

        if (accessToken != null && tokenProvider.validateToken(accessToken)) {
            // Access token이 유효한 경우
            String userId = tokenProvider.getUserIdFromToken(accessToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            throw new GlobalException(INVALID_TOKEN.getMessage(), INVALID_TOKEN.getStatus());
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            log.warn("🚨 쿠키가 존재하지 않음");
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("access_token")) {
                return cookie.getValue();
            }
        }
        log.warn("❌ access_token 쿠키 없음");
        return null;
    }

    private HandlerExecutionChain findHandler(HttpServletRequest request) {
        try {
            for (HandlerMapping mapping : handlerMappingIntrospector.getHandlerMappings()) {
                HandlerExecutionChain chain = mapping.getHandler(request);
                if (chain != null) {
                    return chain;
                }
            }
        } catch (Exception e) {
            throw new GlobalException(BAD_REQUEST.getMessage(), BAD_REQUEST.getStatus());
        }
        return null;
    }
}
