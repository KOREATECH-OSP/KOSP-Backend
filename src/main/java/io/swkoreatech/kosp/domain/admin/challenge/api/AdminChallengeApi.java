package io.swkoreatech.kosp.domain.admin.challenge.api;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeResponse;
import jakarta.validation.Valid;
import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeListResponse;
import io.swkoreatech.kosp.domain.challenge.dto.request.ChallengeRequest;

@Tag(name = "Admin - Challenge", description = "관리자 전용 챌린지 관리 API")
@RequestMapping("/v1/admin/challenges")
public interface AdminChallengeApi {

    @Operation(summary = "챌린지 목록 조회", description = "관리자 권한으로 모든 챌린지 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<AdminChallengeListResponse> getChallenges();

    @Operation(summary = "챌린지 단일 조회", description = "관리자 권한으로 특정 챌린지의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    @GetMapping("/{challengeId}")
    ResponseEntity<AdminChallengeResponse> getChallenge(
        @Parameter(description = "챌린지 ID") @PathVariable Long challengeId
    );


    @Operation(summary = "챌린지 생성", description = "관리자 권한으로 새로운 챌린지를 생성합니다. (SpEL 조건식 검증 포함)")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @PostMapping
    ResponseEntity<Void> createChallenge(@RequestBody @Valid ChallengeRequest request);

    @Operation(summary = "챌린지 수정", description = "관리자 권한으로 챌린지 정보를 수정합니다. SpEL 조건을 변경하는 경우 유효성을 검증합니다.")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @PutMapping("/{challengeId}")
    ResponseEntity<Void> updateChallenge(
        @Parameter(description = "챌린지 ID") @PathVariable Long challengeId,
        @RequestBody @Valid ChallengeRequest request
    );

    @Operation(summary = "챌린지 삭제", description = "관리자 권한으로 챌린지를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{challengeId}")
    ResponseEntity<Void> deleteChallenge(@PathVariable Long challengeId);
}
