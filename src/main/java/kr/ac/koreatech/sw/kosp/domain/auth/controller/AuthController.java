package kr.ac.koreatech.sw.kosp.domain.auth.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.annotation.UserId;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.request.UserSignInRequest;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.LoginResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.LogoutResponse;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.model.JwtToken;
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

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인")
    public ResponseEntity<Void> login(
        @RequestBody @Valid UserSignInRequest request,
        HttpServletResponse response
    ) {
        JwtToken jwtToken = authService.login(request);
        return cookieResponse(response, jwtToken);
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "JWT 토큰으로 현재 로그인한 사용자 정보 조회")
    public ResponseEntity<LoginResponse> getMyInfo(@UserId Integer userId) {
        LoginResponse response = authService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인한 사용자 로그아웃")
    public ResponseEntity<LogoutResponse> logout(
        @UserId Integer userId,
        HttpServletResponse response
    ) {
        authService.logout(userId);
        clearCookies(response);
        return ResponseEntity.ok(new LogoutResponse("로그아웃 되었습니다."));
    }

    private ResponseEntity<Void> cookieResponse(HttpServletResponse response, JwtToken jwtToken) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", jwtToken.getAccessToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(TimeUnit.MILLISECONDS.toSeconds(accessTokenExpiration))
            .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", jwtToken.getRefreshToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpiration))
            .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity.ok().build();
    }

    private void clearCookies(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("access_token", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }
}
