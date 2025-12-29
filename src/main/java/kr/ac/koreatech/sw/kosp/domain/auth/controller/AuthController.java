package kr.ac.koreatech.sw.kosp.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.api.AuthApi;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailVerificationRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;

import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final kr.ac.koreatech.sw.kosp.domain.user.service.UserPasswordService userPasswordService;

    @Override
    @PostMapping("/login")
    @Permit(permitAll = true, description = "로그인")
    public ResponseEntity<Void> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        authService.login(request, servletRequest, servletResponse);

        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/logout")
    @Permit(description = "로그아웃") // Default permitAll=false
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/me")
    @Permit(description = "내 정보 조회")
    public ResponseEntity<AuthMeResponse> getMyInfo() {
        return ResponseEntity.ok(authService.getUserInfo());
    }

    @Override
    @PostMapping("/email/verify")
    @Permit(permitAll = true, description = "이메일 인증 코드 발송")
    public ResponseEntity<Void> sendCertificationMail(@RequestBody @Valid EmailRequest request) {
        authService.sendCertificationMail(request.email());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/email/verify/confirm")
    @Permit(permitAll = true, description = "이메일 인증 코드 검증")
    public ResponseEntity<Void> verifyCode(@RequestBody @Valid EmailVerificationRequest request) {
        authService.verifyCode(request.email(), request.code());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/password/reset")
    @Permit(permitAll = true, description = "비밀번호 재설정 메일 발송")
    public ResponseEntity<Void> sendPasswordResetMail(@RequestBody @Valid EmailRequest request) {
        userPasswordService.sendPasswordResetMail(request.email());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/password/reset/confirm")
    @Permit(permitAll = true, description = "비밀번호 재설정")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid kr.ac.koreatech.sw.kosp.domain.auth.dto.request.PasswordResetRequest request) {
        userPasswordService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
