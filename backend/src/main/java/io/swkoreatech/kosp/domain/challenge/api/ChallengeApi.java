package io.swkoreatech.kosp.domain.challenge.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.challenge.dto.response.ChallengeListResponse;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

@Tag(name = "Challenge", description = "도전 과제 API")
@RequestMapping("/v1/challenges")
public interface ChallengeApi {

    @Operation(summary = "도전 과제 목록 및 진행도 조회", description = "모든 도전 과제와 사용자의 진행 상태를 조회합니다.")
    @GetMapping
    ResponseEntity<ChallengeListResponse> getChallenges(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam(required = false) Integer tier
    );
}
