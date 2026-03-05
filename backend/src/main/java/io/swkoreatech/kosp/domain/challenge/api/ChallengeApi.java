package io.swkoreatech.kosp.domain.challenge.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.challenge.dto.response.ChallengeListResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

/**
 * 도전 과제 API 인터페이스.
 * 도전 과제 목록 및 진행도 조회 엔드포인트를 정의한다.
 */
@Tag(name = "Challenge", description = "도전 과제 API")
@RequestMapping("/v1/challenges")
public interface ChallengeApi {

    /**
     * 도전 과제 목록과 사용자 진행도를 조회한다.
     *
     * @param user 인증된 사용자
     * @param tier 필터링할 티어 (선택)
     * @return 도전 과제 목록 응답
     */
    @Operation(
        summary = "도전 과제 목록 및 진행도 조회",
        description = "모든 도전 과제와 사용자의 진행 상태를 조회합니다."
    )
    @GetMapping
    ResponseEntity<ChallengeListResponse> getChallenges(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam(required = false) Integer tier
    );
}
