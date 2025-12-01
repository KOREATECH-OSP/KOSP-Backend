package kr.ac.koreatech.sw.kosp.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository;

    /**
     * 일반 로그인 (이메일 + 비밀번호)
     */
    @Transactional
    public void login(
        LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        // 1. 인증 토큰 생성
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()
        );

        // 2. AuthenticationManager에게 인증 위임 (실패 시 예외 발생)
        Authentication authentication = authenticationManager.authenticate(token);

        // 3. 인증 정보를 담을 SecurityContext 생성
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        // 4. 세션에 SecurityContext 저장
        securityContextRepository.saveContext(context, servletRequest, servletResponse);
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(HttpServletRequest request) {
        securityContextHolderStrategy.clearContext();
        request.getSession().invalidate();
    }

    /**
     * 사용자 정보 조회
     */
    public AuthMeResponse getUserInfo() {
        Authentication auth = securityContextHolderStrategy.getContext().getAuthentication();

        return new AuthMeResponse(
            auth.getName()
        );
    }
}
