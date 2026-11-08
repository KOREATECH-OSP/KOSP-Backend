package io.swkoreatech.kosp.domain.title.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.title.dto.response.TitleListResponse;
import io.swkoreatech.kosp.domain.title.dto.response.TitleProgressResponse;
import io.swkoreatech.kosp.domain.title.dto.response.UserTitleListResponse;
import io.swkoreatech.kosp.domain.title.dto.response.UserTitleResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

/**
 * 칭호 유저 API 인터페이스.
 */
@Tag(name = "Title", description = "칭호 조회 및 대표 칭호 설정 API")
@RequestMapping("/v1")
public interface TitleApi {

    /**
     * 내 칭호 목록을 조회한다.
     *
     * @param user 인증된 사용자
     * @return 보유 칭호 목록
     */
    @Operation(
        summary = "내 칭호 목록 조회",
        description = "로그인한 유저의 보유 칭호 목록을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/users/me/titles")
    ResponseEntity<UserTitleListResponse> getMyTitles(@AuthUser User user);

    /**
     * 내 미취득 칭호 진행도를 조회한다.
     *
     * @param user 인증된 사용자
     * @return 미취득 칭호별 진행도(현재값/목표값/달성률)
     */
    @Operation(
        summary = "미취득 칭호 진행도 조회",
        description = "로그인한 유저가 아직 취득하지 않은 활성 칭호의 조건별 현재값/목표값/달성률을 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/users/me/titles/progress")
    ResponseEntity<List<TitleProgressResponse>> getMyTitleProgress(@AuthUser User user);

    /**
     * 특정 유저의 칭호 목록을 조회한다 (공개).
     *
     * @param userId 조회 대상 유저 ID
     * @return 보유 칭호 목록
     */
    @Operation(
        summary = "유저 칭호 목록 조회",
        description = "특정 유저의 보유 칭호 목록을 공개 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    @GetMapping("/users/{userId}/titles")
    ResponseEntity<UserTitleListResponse> getUserTitles(
        @Parameter(description = "유저 ID") @PathVariable Long userId
    );

    /**
     * 대표 칭호를 설정한다.
     *
     * @param userTitleId 대표로 설정할 user_title ID
     * @param user        인증된 사용자
     * @return 설정된 칭호 정보
     */
    @Operation(
        summary = "대표 칭호 설정",
        description = "보유한 칭호 중 하나를 대표 칭호로 설정합니다. 기존 대표 칭호는 자동 해제됩니다."
    )
    @ApiResponse(responseCode = "200", description = "설정 성공")
    @ApiResponse(responseCode = "403", description = "본인 소유 칭호가 아님")
    @ApiResponse(responseCode = "404", description = "칭호를 찾을 수 없음")
    @PutMapping("/users/me/titles/{userTitleId}/display")
    ResponseEntity<UserTitleResponse> setDisplayTitle(
        @Parameter(description = "user_title ID") @PathVariable Long userTitleId,
        @AuthUser User user
    );

    /**
     * 활성화된 전체 칭호 목록을 달성 조건과 함께 조회한다 (공개).
     *
     * @return 전체 칭호 목록
     */
    @Operation(
        summary = "전체 칭호 목록 조회",
        description = "플랫폼에서 획득 가능한 모든 활성 칭호와 달성 조건을 반환합니다. 인증이 필요하지 않습니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/titles")
    ResponseEntity<TitleListResponse> getAllTitles();
}
