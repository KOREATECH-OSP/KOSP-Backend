package io.swkoreatech.kosp.domain.challenge.controller;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.challenge.api.ChallengeApi;
import io.swkoreatech.kosp.domain.challenge.dto.response.ChallengeListResponse;
import io.swkoreatech.kosp.domain.challenge.service.ChallengeService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 도전 과제 컨트롤러.
 * {@link ChallengeApi}를 구현하여 도전 과제 관련 요청을 처리한다.
 */
@RestController
@RequiredArgsConstructor
public class ChallengeController implements ChallengeApi {

    private final ChallengeService challengeService;

    /** {@inheritDoc} */
    @Override
    @Permit(name = "challenges:read", permitAll = false, description = "도전 과제 목록 및 진행도 조회")
    public ResponseEntity<ChallengeListResponse> getChallenges(@AuthUser User user, Integer tier) {
        return ResponseEntity.ok(challengeService.getChallenges(user, tier));
    }
}
