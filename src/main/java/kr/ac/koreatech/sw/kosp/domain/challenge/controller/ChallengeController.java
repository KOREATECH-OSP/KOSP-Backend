package kr.ac.koreatech.sw.kosp.domain.challenge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.koreatech.sw.kosp.domain.challenge.api.ChallengeApi;
import kr.ac.koreatech.sw.kosp.domain.challenge.dto.response.ChallengeListResponse;
import kr.ac.koreatech.sw.kosp.domain.challenge.service.ChallengeService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import kr.ac.koreatech.sw.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChallengeController implements ChallengeApi {

    private final ChallengeService challengeService;

    @Override
    @Permit(name = "challenges:read", permitAll = false, description = "도전 과제 목록 및 진행도 조회")
    public ResponseEntity<ChallengeListResponse> getChallenges(@AuthUser User user, Integer tier) {
        return ResponseEntity.ok(challengeService.getChallenges(user, tier));
    }
}
