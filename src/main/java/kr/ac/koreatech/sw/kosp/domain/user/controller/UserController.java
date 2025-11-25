package kr.ac.koreatech.sw.kosp.domain.user.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.jwt.model.JwtToken;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignUpRequest;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(
        @RequestBody @Valid UserSignUpRequest request,
        HttpServletResponse response
    ) {
        JwtToken jwtToken = userService.signUp(request);
        return cookieResponse(response, jwtToken);
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

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
