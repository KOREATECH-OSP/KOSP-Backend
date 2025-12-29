package kr.ac.koreatech.sw.kosp.domain.github.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubAnalysisResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "GitHub", description = "GitHub 관련 API")
@RequestMapping("/v1/github/users")
public interface GithubApi {

    @Operation(summary = "GitHub 활동 분석 조회", description = "특정 유저의 GitHub 활동 분석 결과(활동 시간대, 협업 성향 등)를 조회합니다.")
    @GetMapping("/{username}/analysis")
    ResponseEntity<GithubAnalysisResponse> getGithubAnalysis(
        @Parameter(description = "GitHub 사용자 이름(로그인 ID)", required = true) 
        @PathVariable String username
    );
}
