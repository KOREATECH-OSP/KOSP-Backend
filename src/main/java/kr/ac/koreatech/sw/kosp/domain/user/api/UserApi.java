package kr.ac.koreatech.sw.kosp.domain.user.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.auth.dto.response.AuthTokenResponse;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserSignupRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserUpdateRequest;
import kr.ac.koreatech.sw.kosp.domain.user.dto.response.UserProfileResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;

@Tag(name = "User", description = "사용자 관리 API")
@RequestMapping("/v1/users")
public interface UserApi {

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
        @RequestBody @Valid UserSignupRequest request
    );

    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자가 본인의 계정을 탈퇴(Soft Delete) 처리합니다.")
    @DeleteMapping("/{userId}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId
    );

    @Operation(summary = "사용자 정보 수정", description = "자신의 사용자 정보를 수정합니다.")
    @PutMapping("/{userId}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long userId,
        @RequestBody @Valid UserUpdateRequest request
    );

    @Operation(summary = "사용자 상세 조회 (타인)", description = "다른 사용자의 프로필을 조회합니다.")
    @GetMapping("/{userId}")
    ResponseEntity<UserProfileResponse> getProfile(
        @PathVariable Long userId
    );

    @Operation(summary = "비밀번호 변경", description = "로그인한 사용자의 비밀번호를 변경합니다.")
    @PutMapping("/me/password")
    ResponseEntity<Void> updatePassword(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid kr.ac.koreatech.sw.kosp.domain.user.dto.request.UserPasswordChangeRequest request
    );
}
