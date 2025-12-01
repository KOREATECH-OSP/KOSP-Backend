package kr.ac.koreatech.sw.kosp.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.LoginRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthMeResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인")
    public ResponseEntity<Void> login(
        @RequestBody @Valid LoginRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        authService.login(request, servletRequest, servletResponse);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자 로그아웃")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자 정보 조회")
    public ResponseEntity<AuthMeResponse> getMyInfo() {
        AuthMeResponse response = authService.getUserInfo();
        return ResponseEntity.ok(response);
    }

}
