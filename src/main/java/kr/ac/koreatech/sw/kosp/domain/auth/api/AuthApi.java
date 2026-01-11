package kr.ac.koreatech.sw.kosp.domain.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.auth.annotation.Token;
import kr.ac.koreatech.sw.kosp.global.auth.token.AccessToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.RefreshToken;
import kr.ac.koreatech.sw.kosp.global.auth.token.SignupToken;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;

@Tag(
    name = "Auth",
    description = "인증 및 세션 관리와 관련된 API 모음임. 로그인, 로그아웃, 내 정보 조회 기능을 제공함."
)
@RequestMapping("/v1/auth")
public interface AuthApi {

    @Operation(summary = "Github 토큰 교환 (회원가입용)", description = "Github Access Token을 검증하고 회원가입용 토큰(JWS)을 발급합니다.")
    @PostMapping("/github/exchange")
    ResponseEntity<GithubVerificationResponse> exchangeGithubToken(
        @Parameter(description = "Github Access Token") @RequestBody @Valid GithubTokenRequest request
    );

    @Operation(summary = "학번/사번 중복 확인", description = "회원가입 시 학번 또는 사번의 중복 여부와 형식을 확인합니다. (학번: 10자리, 사번: 6/8자리)")
    @GetMapping("/verify/identity")
    ResponseEntity<CheckMemberIdResponse> checkMemberId(
        @Valid CheckMemberIdRequest request
    );

    @Operation(summary = "회원가입 토큰 검증", description = "회원가입 폼 진입 전 JWS 토큰의 유효성을 검증합니다. 위변조된 토큰은 접근을 차단합니다.")
    @GetMapping("/verify/token/signup")
    ResponseEntity<Void> validateSignupToken(
        @Parameter(description = "회원가입 토큰 (JWS)", required = true, hidden = true) @Token SignupToken token
    );

    @Operation(summary = "로그인 토큰 검증", description = "Access Token의 유효성을 검증합니다. 만료되거나 위변조된 토큰은 차단합니다.")
    @GetMapping("/verify/token/login")
    ResponseEntity<Void> validateLoginToken(
        @Parameter(description = "Access Token (JWS)", required = true, hidden = true) @Token AccessToken token
    );

    @Operation(summary = "이메일 인증 코드 발송", description = "회원가입을 위해 이메일로 인증 코드를 발송합니다.")
    @PostMapping("/verfiy/email")
    ResponseEntity<Void> sendCertificationMail(
        @Parameter(description = "이메일 정보")
        @RequestBody @Valid EmailRequest request,
        @Parameter(description = "회원가입 토큰 (JWS)", required = true, hidden = true) @Token SignupToken token
    );

    @Operation(summary = "이메일 인증 코드 검증", description = "이메일로 발송된 인증 코드를 검증합니다.")
    @PostMapping("/verify/email/confirm")
    ResponseEntity<Object> verifyCode(
        @Parameter(description = "이메일 및 인증 코드 정보")
        @RequestBody @Valid EmailVerificationRequest request
    );

    @PostMapping("/login")
    @Operation(
        summary = "일반 로그인",
        description = "이메일과 비밀번호를 전달받아 로그인 처리함. Access Token과 Refresh Token을 반환함."
    )
    ResponseEntity<AuthTokenResponse> login(
        @RequestBody @Valid LoginRequest request
    );

    @Operation(summary = "Github 로그인", description = "Github Access Token을 사용하여 로그인하고 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/login/github")
    ResponseEntity<AuthTokenResponse> loginWithGithub(
        @Parameter(description = "Github Access Token") @RequestBody @Valid GithubTokenRequest request
    );

    @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다.")
    @PostMapping("/reissue")
    ResponseEntity<AuthTokenResponse> reissue(
        @Parameter(description = "Refresh Token", required = true, hidden = true) @Token RefreshToken token
    );

    @GetMapping("/me")
    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인된 사용자의 기본 정보를 조회하여 반환함."
    )
    ResponseEntity<AuthMeResponse> getMyInfo(
        @Parameter(hidden = true) User user
    );

    @Operation(summary = "비밀번호 재설정 메일 발송", description = "비밀번호 재설정을 위해 이메일로 링크를 발송합니다.")
    @PostMapping("/reset/password")
    ResponseEntity<Void> sendPasswordResetMail(
        @Parameter(description = "이메일 정보") @RequestBody @Valid EmailRequest request,
        @Parameter(hidden = true) String serverUrl
    );

    @Operation(summary = "비밀번호 재설정", description = "발송된 링크의 토큰을 사용하여 비밀번호를 재설정합니다.")
    @PostMapping("/reset/password/confirm")
    ResponseEntity<Void> resetPassword(
        @Parameter(description = "토큰 및 새 비밀번호") @RequestBody @Valid PasswordResetRequest request);

    @Operation(summary = "로그아웃", description = "서버에서 Refresh Token을 삭제하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
        @Parameter(hidden = true) @AuthUser User user
    );
}
