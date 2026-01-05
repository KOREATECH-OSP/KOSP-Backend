package kr.ac.koreatech.sw.kosp.domain.admin.challenge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.admin.challenge.api.AdminChallengeApi;
import kr.ac.koreatech.sw.kosp.domain.admin.challenge.dto.AdminChallengeListResponse;
import kr.ac.koreatech.sw.kosp.domain.challenge.dto.request.ChallengeRequest;
import kr.ac.koreatech.sw.kosp.domain.challenge.service.ChallengeService;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
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
}
