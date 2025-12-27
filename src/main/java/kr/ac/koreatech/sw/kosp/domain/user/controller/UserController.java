package kr.ac.koreatech.sw.kosp.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.service.AuthService;
import kr.ac.koreatech.sw.kosp.domain.user.api.UserApi;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;

import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final AuthService authService;

    @Override
    @PostMapping("/signup")
    @Permit(permitAll = true, description = "회원가입")
    public ResponseEntity<Void> signup(
        @RequestBody @Valid UserSignupRequest request,
        HttpServletRequest servletRequest,
        HttpServletResponse servletResponse
    ) {
        userService.signup(request);
        authService.login(request.kutEmail(), request.password(), servletRequest, servletResponse);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
