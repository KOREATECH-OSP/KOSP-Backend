package kr.ac.koreatech.sw.kosp.domain.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.EmailVerificationRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(
    name = "Auth",
    description = "인증 및 세션 관리와 관련된 API 모음임. 로그인, 로그아웃, 내 정보 조회 기능을 제공함."
)
@RequestMapping("/v1/auth")
public interface AuthApi {

    @PostMapping("/login")
    @Operation(
        summary = "일반 로그인",
        description = "이메일과 비밀번호(SHA-256 해시)를 전달받아 로그인 처리함."
    )
    ResponseEntity<Void> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    );

    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "현재 로그인한 사용자의 세션/토큰을 무효화하여 로그아웃 처리함."
    )
    ResponseEntity<Void> logout(HttpServletRequest request);

    @GetMapping("/me")
    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인된 사용자의 기본 정보를 조회하여 반환함."
    )
    ResponseEntity<AuthMeResponse> getMyInfo();

    @Operation(summary = "이메일 인증 코드 발송", description = "입력한 이메일로 인증 코드를 발송합니다.")
    ResponseEntity<Void> sendCertificationMail(@Parameter(description = "이메일 정보") @RequestBody @Valid EmailRequest request);

    @Operation(summary = "이메일 인증 코드 검증", description = "이메일로 발송된 인증 코드를 검증합니다.")
    ResponseEntity<Void> verifyCode(@Parameter(description = "이메일 및 인증 코드 정보") @RequestBody @Valid EmailVerificationRequest request);
}
