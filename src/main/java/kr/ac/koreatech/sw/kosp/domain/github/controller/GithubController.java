package kr.ac.koreatech.sw.kosp.domain.github.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GithubController implements kr.ac.koreatech.sw.kosp.domain.github.api.GithubApi {

    private final kr.ac.koreatech.sw.kosp.domain.github.service.GithubService githubService;

    @Override
    public ResponseEntity<kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubAnalysisResponse> getGithubAnalysis(String username) {
        return ResponseEntity.ok(githubService.getAnalysis(username));
    }
}
