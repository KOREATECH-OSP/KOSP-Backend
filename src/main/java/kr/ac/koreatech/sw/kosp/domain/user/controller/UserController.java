package kr.ac.koreatech.sw.kosp.domain.user.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.user.api.UserApi;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserPasswordChangeRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.MyApplicationListResponse;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.MyPointHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.UserProfileResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.service.UserService;
import kr.ac.koreatech.sw.kosp.global.auth.annotation.Token;
import kr.ac.koreatech.sw.kosp.global.auth.token.SignupToken;
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

    @Override
    @PostMapping("/signup")
    @Permit(permitAll = true, description = "회원가입")
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
    @Permit(permitAll = true, description = "사용자 상세 조회")
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
        @org.springframework.web.bind.annotation.RequestParam(required = false) String status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(userService.getMyApplications(user, status, pageable));
    }

    @Override
    @Permit(description = "본인 포인트 내역 조회")
    public ResponseEntity<MyPointHistoryResponse> getMyPointHistory(@AuthUser User user, Pageable pageable) {
        return ResponseEntity.ok(userService.getMyPointHistory(user, pageable));
    }
}
