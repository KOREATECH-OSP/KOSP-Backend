package kr.ac.koreatech.sw.kosp.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.api.AuthApi;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.CheckMemberIdRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailVerificationRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.GithubTokenRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.PasswordResetRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.CheckMemberIdResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.GithubVerificationResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.service.AuthService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserPasswordService;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.auth.token.LoginToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.RefreshToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.SignupToken;
import kr.ac.koreatech.sw.kosp.global.host.ServerURL;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final UserPasswordService userPasswordService;
    private final UserService userService;

    @Override
    @PostMapping("/github/exchange")
    @Permit(permitAll = true, description = "Github 토큰 교환 (회원가입용)")
    public ResponseEntity<GithubVerificationResponse> exchangeGithubToken(
        @RequestBody @Valid GithubTokenRequest request
    ) {
        String verificationToken = authService.exchangeGithubTokenForSignup(request.githubAccessToken());
        return ResponseEntity.ok(new GithubVerificationResponse(verificationToken));
    }

    @Override
    @GetMapping("/verify/identity")
    @Permit(permitAll = true, description = "학번/사번 중복 확인")
    public ResponseEntity<CheckMemberIdResponse> checkMemberId(
        CheckMemberIdRequest request
    ) {
        return ResponseEntity.ok(userService.checkMemberIdAvailability(request.id()));
    }

    @Override
    @GetMapping("/verify/token/signup")
    @Permit(permitAll = true, description = "회원가입 토큰 검증")
    public ResponseEntity<Void> validateSignupToken(SignupToken token) {
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/verify/token/login")
    @Permit(permitAll = true, description = "로그인 토큰 검증")
    public ResponseEntity<Void> validateLoginToken(LoginToken token) {
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/verify/email")
    @Permit(permitAll = true, description = "이메일 인증 코드 발송")
    public ResponseEntity<Void> sendCertificationMail(
        @RequestBody @Valid EmailRequest request
    ) {
        authService.sendCertificationMail(request.email(), request.signupToken());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/verify/email/confirm")
    @Permit(permitAll = true, description = "이메일 인증 코드 검증")
    public ResponseEntity<Object> verifyCode(
        @RequestBody @Valid EmailVerificationRequest request
    ) {
        String newSignupToken = authService.verifyCode(request.email(), request.code());
        if (newSignupToken != null) {
            return ResponseEntity.ok(java.util.Map.of("signupToken", newSignupToken));
        }
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/login")
    @Permit(permitAll = true, description = "로그인")
    public ResponseEntity<AuthTokenResponse> login(
        @RequestBody @Valid LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Override
    @GetMapping("/me")
    @Permit(description = "내 정보 조회")
    public ResponseEntity<AuthMeResponse> getMyInfo(@AuthUser User user) {
        return ResponseEntity.ok(authService.getUserInfo(user));
    }

    @Override
    @PostMapping("/reset/password")
    @Permit(permitAll = true, description = "비밀번호 재설정 메일 발송")
    public ResponseEntity<Void> sendPasswordResetMail(
        @RequestBody @Valid EmailRequest request,
        @ServerURL String serverUrl
    ) {
        userPasswordService.sendPasswordResetMail(request.email(), serverUrl);
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/reset/password/confirm")
    @Permit(permitAll = true, description = "비밀번호 재설정")
    public ResponseEntity<Void> resetPassword(
        @RequestBody @Valid PasswordResetRequest request) {
        userPasswordService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @Override
    @PostMapping("/login/github")
    @Permit(permitAll = true, description = "Github 로그인")
    public ResponseEntity<AuthTokenResponse> loginWithGithub(
        @RequestBody @Valid GithubTokenRequest request
    ) {
        return ResponseEntity.ok(authService.loginWithGithub(request.githubAccessToken()));
    }

    @Override
    @PostMapping("/reissue")
    @Permit(permitAll = true, description = "토큰 재발급")
    public ResponseEntity<AuthTokenResponse> reissue(
        RefreshToken token
    ) {
        return ResponseEntity.ok(authService.reissue(token));
    }

    @Override
    @PostMapping("/logout")
    @Permit(description = "로그아웃")
    public ResponseEntity<Void> logout(
        @AuthUser User user
    ) {
        authService.logout(user.getId());
        return ResponseEntity.ok().build();
    }
}
