package io.swkoreatech.kosp.domain.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import io.swkoreatech.kosp.domain.auth.dto.response.AuthTokenResponse;
import io.swkoreatech.kosp.domain.user.api.UserApi;
import io.swkoreatech.kosp.domain.user.dto.request.UserPasswordChangeRequest;
import io.swkoreatech.kosp.domain.user.dto.request.UserSignupRequest;
import io.swkoreatech.kosp.domain.user.dto.request.UserUpdateRequest;
import io.swkoreatech.kosp.domain.user.dto.response.MyApplicationListResponse;
import io.swkoreatech.kosp.domain.user.dto.response.MyPointHistoryResponse;
import io.swkoreatech.kosp.domain.user.dto.response.UserProfileResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.service.UserService;
import io.swkoreatech.kosp.global.auth.annotation.Token;
import io.swkoreatech.kosp.global.auth.token.SignupToken;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @PostMapping("/signup")
    @Permit(permitAll = true, name = "users:signup", description = "회원가입")
    public ResponseEntity<AuthTokenResponse> signup(
        @RequestBody @Valid UserSignupRequest request,
        @Token SignupToken token
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.signup(request, token));
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
    @Permit(description = "회원 탈퇴")
    public ResponseEntity<Void> delete(@AuthUser User user, Long userId) {
        if (!user.getId().equals(userId)) {
            throw new GlobalException(ExceptionMessage.FORBIDDEN);
        }
        userService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(permitAll = true, name = "users:profile", description = "사용자 상세 조회")
    public ResponseEntity<UserProfileResponse> getProfile(Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @Override
    @Permit(description = "비밀번호 변경")
    public ResponseEntity<Void> updatePassword(@AuthUser User user, @Valid UserPasswordChangeRequest request) {
        userService.changePassword(user.getId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(description = "본인 지원 내역 조회")
    public ResponseEntity<MyApplicationListResponse> getMyApplications(
        @AuthUser User user,
        @RequestParam(required = false) String filter,
        Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getMyApplications(user, filter, pageable));
    }

    @Override
    @Permit(description = "본인 포인트 내역 조회")
    public ResponseEntity<MyPointHistoryResponse> getMyPointHistory(@AuthUser User user, Pageable pageable) {
        return ResponseEntity.ok(userService.getMyPointHistory(user, pageable));
    }
}
