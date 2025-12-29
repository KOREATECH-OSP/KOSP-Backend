package kr.ac.koreatech.sw.kosp.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final UserDetailsService userDetailsService;
    private final kr.ac.koreatech.sw.kosp.domain.mail.service.EmailVerificationService emailVerificationService;

    @Transactional
    public void sendCertificationMail(String email) {
        emailVerificationService.sendCertificationMail(email);
    }

    @Transactional
    public void verifyCode(String email, String code) {
        emailVerificationService.verifyCode(email, code);
    }

    /**
     * 일반 로그인 (이메일 + 비밀번호)
     */
    public void login(
        String username,
        String password,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        Authentication authentication = authenticate(username, password);
        setAuthentication(authentication, servletRequest, servletResponse);
    }

    public void login(
        LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        login(request.email(), request.password(), servletRequest, servletResponse);
    }

    /**
     * 강제 로그인 (비밀번호 검증 없이 UserDetails를 신뢰하여 인증 컨텍스트 설정)
     * OAuth2 인증 등 외부 인증 성공 후, 비밀번호 재검증 없이 SecurityContext에 인증 정보를 저장할 때 사용
     */
    public void login(
        String username,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        // 1. UserDetails 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 2. 인증 토큰 생성 (비밀번호는 null)
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        setAuthentication(token, servletRequest, servletResponse);
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
        Object principal = auth.getPrincipal();

        if (principal instanceof kr.ac.koreatech.sw.kosp.domain.user.model.User user) {
            String profileImage = user.getGithubUser() != null ? user.getGithubUser().getGithubAvatarUrl() : null;
            return new AuthMeResponse(
                user.getId(),
                user.getKutEmail(),
                user.getName(),
                profileImage,
                user.getIntroduction()
            );
        }

        return new AuthMeResponse(null, null, auth.getName(), null, null);
    }

    private SecurityContext setAuthentication(Authentication authentication) {
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);
        return context;
    }

    private void setAuthentication(
        Authentication authentication,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        SecurityContext context = setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);
    }

    private Authentication authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        return authenticationManager.authenticate(token);
    }
}
