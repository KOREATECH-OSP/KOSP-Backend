package io.swkoreatech.kosp.domain.user.api;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.auth.dto.response.AuthTokenResponse;
import io.swkoreatech.kosp.domain.user.dto.request.UserPasswordChangeRequest;
import io.swkoreatech.kosp.domain.user.dto.request.UserSignupRequest;
import io.swkoreatech.kosp.domain.user.dto.request.UserUpdateRequest;
import io.swkoreatech.kosp.domain.user.dto.response.MyApplicationListResponse;
import io.swkoreatech.kosp.domain.user.dto.response.MyPointHistoryResponse;
import io.swkoreatech.kosp.domain.user.dto.response.UserProfileResponse;
import io.swkoreatech.kosp.global.auth.annotation.Token;
import io.swkoreatech.kosp.global.auth.token.SignupToken;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import jakarta.validation.Valid;

/**
 * 사용자 관리 API.
 * 회원가입, 탈퇴, 정보 수정, 프로필 조회, 비밀번호 변경 등의 기능을 정의한다.
 */
@Tag(name = "User", description = "사용자 관리 API")
@RequestMapping("/v1/users")
public interface UserApi {

    /**
     * 회원가입 후 즉시 로그인하여 인증 토큰을 발급한다.
     *
     * @param request 회원가입 요청
     * @param token 회원가입 토큰
     * @return 인증 토큰 응답
     */
    @Operation(
        summary = "회원가입 및 즉시 로그인",
        description = """
            새 사용자 정보를 전달받아 내부적으로 User 엔티티를 생성하고 저장한 뒤,
            인증 모듈(AuthService)을 통해 즉시 로그인 처리까지 수행함.
            클라이언트는 이 API 한 번 호출로 회원가입과 Access/Refresh Token 발급 과정을 한 번에 수행할 수 있음.
            """
    )
    @PostMapping("/signup")
    ResponseEntity<AuthTokenResponse> signup(
        @RequestBody @Valid UserSignupRequest request,
        @Parameter(description = "회원가입 토큰 (JWS)", required = true, hidden = true) @Token SignupToken token
    );

    /**
     * 사용자 계정을 탈퇴(Soft Delete) 처리한다.
     *
     * @param user 인증된 사용자
     * @param userId 사용자 ID
     * @return 처리 결과
     */
    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자가 본인의 계정을 탈퇴(Soft Delete) 처리합니다.")
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId
    );

    /**
     * 사용자 정보를 수정한다.
     *
     * @param user 인증된 사용자
     * @param userId 사용자 ID
     * @param request 수정 요청
     * @return 처리 결과
     */
    @Operation(summary = "사용자 정보 수정", description = "자신의 사용자 정보를 수정합니다.")
    @PutMapping("/{userId}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @RequestBody @Valid UserUpdateRequest request
    );

    /**
     * 사용자 프로필을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 사용자 프로필 응답
     */
    @Operation(summary = "사용자 상세 조회 (타인)", description = "다른 사용자의 프로필을 조회합니다.")
    @GetMapping("/{userId}")
    ResponseEntity<UserProfileResponse> getProfile(
        @PathVariable Long userId
    );

    /**
     * 사용자의 비밀번호를 변경한다.
     *
     * @param user 인증된 사용자
     * @param request 비밀번호 변경 요청
     * @return 처리 결과
     */
    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    @PutMapping("/me/password")
    ResponseEntity<Void> updatePassword(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid UserPasswordChangeRequest request
    );

    /**
     * 로그인한 사용자의 지원 내역을 조회한다.
     *
     * @param user 인증된 사용자
     * @param filter RSQL 필터
     * @param pageable 페이지 정보
     * @return 지원 내역 응답
     */
    @Operation(
        summary = "본인 지원 내역 조회",
        description = "로그인한 사용자가 지원한 모집 공고 목록을 조회합니다. RSQL filter로 필터링 가능 (예: status==PENDING, status==ACCEPTED)"
    )
    @GetMapping("/me/applications")
    ResponseEntity<MyApplicationListResponse> getMyApplications(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(description = "RSQL 필터 (예: status==PENDING, status==ACCEPTED;createdAt=gt=2024-01-01)")
        @RequestParam(required = false) String filter,
        @Parameter(hidden = true) Pageable pageable
    );

    /**
     * 로그인한 사용자의 포인트 내역을 조회한다.
     *
     * @param user 인증된 사용자
     * @param pageable 페이지 정보
     * @return 포인트 내역 응답
     */
    @Operation(summary = "본인 포인트 내역 조회", description = "로그인한 사용자의 포인트 내역을 조회합니다.")
    @GetMapping("/me/points")
    ResponseEntity<MyPointHistoryResponse> getMyPointHistory(
        @Parameter(hidden = true) @AuthUser User user,
        @Parameter(hidden = true) Pageable pageable
    );
}
