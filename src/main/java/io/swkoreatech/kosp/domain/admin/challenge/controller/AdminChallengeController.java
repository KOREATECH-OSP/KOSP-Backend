package io.swkoreatech.kosp.domain.admin.challenge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeResponse;
import io.swkoreatech.kosp.domain.admin.challenge.api.AdminChallengeApi;
import io.swkoreatech.kosp.domain.admin.challenge.dto.AdminChallengeListResponse;
import io.swkoreatech.kosp.domain.challenge.dto.request.ChallengeRequest;
import io.swkoreatech.kosp.domain.challenge.dto.response.SpelVariableResponse;
import io.swkoreatech.kosp.domain.challenge.service.ChallengeService;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminChallengeController implements AdminChallengeApi {

    private final ChallengeService challengeService;

    @Override
    @Permit(name = "admin:challenges:read", description = "챌린지 목록 조회")
    public ResponseEntity<AdminChallengeListResponse> getChallenges() {
        AdminChallengeListResponse response = challengeService.getAllChallenges();
        return ResponseEntity.ok(response);
    }

    @Override
    @Permit(name = "admin:challenges:read", description = "챌린지 단일 조회")
    public ResponseEntity<AdminChallengeResponse> getChallenge(Long challengeId) {
        AdminChallengeResponse response = challengeService.getChallenge(challengeId);
        return ResponseEntity.ok(response);
    }


    @Override
    @Permit(name = "admin:challenges:create", description = "챌린지 생성")
    public ResponseEntity<Void> createChallenge(ChallengeRequest request) {
        challengeService.createChallenge(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @Permit(name = "admin:challenges:update", description = "챌린지 수정")
    public ResponseEntity<Void> updateChallenge(Long challengeId, ChallengeRequest request) {
        challengeService.updateChallenge(challengeId, request);
        return ResponseEntity.ok().build();
    }

    @Override
    @Permit(name = "admin:challenges:delete", description = "챌린지 삭제")
    public ResponseEntity<Void> deleteChallenge(Long challengeId) {
        challengeService.deleteChallenge(challengeId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @Permit(name = "admin:challenges:read", description = "SpEL 변수 목록 조회")
    public ResponseEntity<SpelVariableResponse> getSpelVariables() {
        SpelVariableResponse response = challengeService.getSpelVariables();
        return ResponseEntity.ok(response);
    }
}
