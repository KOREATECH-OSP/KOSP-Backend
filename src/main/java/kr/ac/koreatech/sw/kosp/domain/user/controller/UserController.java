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
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.UserProfileResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

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

    @Override
    @Permit(permitAll = false, description = "사용자 정보 수정")
    public ResponseEntity<Void> update(@AuthUser User user, Long userId, UserUpdateRequest request) {
        if (!user.getId().equals(userId)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        userService.update(userId, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(permitAll = true, description = "사용자 상세 조회")
    public ResponseEntity<UserProfileResponse> getProfile(Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}
