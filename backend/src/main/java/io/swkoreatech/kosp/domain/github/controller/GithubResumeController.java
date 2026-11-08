package io.swkoreatech.kosp.domain.github.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.github.api.GithubResumeApi;
import io.swkoreatech.kosp.domain.github.dto.response.GithubResumeProjectResponse;
import io.swkoreatech.kosp.domain.github.service.GithubStatisticsService;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;
import io.swkoreatech.kosp.global.security.annotation.Permit;
import lombok.RequiredArgsConstructor;

/**
 * GitHub → 이력서 프로젝트 가져오기 컨트롤러.
 * {@link GithubResumeApi}의 구현체.
 */
@RestController
@RequiredArgsConstructor
public class GithubResumeController implements GithubResumeApi {

    private final GithubStatisticsService githubStatisticsService;

    @Override
    @Permit(description = "내 GitHub 저장소 목록 (이력서 가져오기용)")
    public ResponseEntity<List<GithubResumeProjectResponse>> getMyRepositoriesForResume(@AuthUser User user) {
        return ResponseEntity.ok(githubStatisticsService.getMyRepositoriesForResume(user));
    }
}
