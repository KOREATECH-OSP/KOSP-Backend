package kr.ac.koreatech.sw.kosp.domain.user.controller;

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
            .maxAge(60 * 60 * 24 * 14L)
            .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", jwtToken.getRefreshToken())
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(60 * 60 * 24 * 14L)
            .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
