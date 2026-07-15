package io.swkoreatech.kosp.domain.github.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.github.dto.response.GithubResumeProjectResponse;
import io.swkoreatech.kosp.global.security.annotation.AuthUser;

/**
 * GitHub → 이력서 프로젝트 가져오기 API.
 *
 * <p>로그인 사용자의 GitHub 저장소 통계를 이력서 프로젝트로 채워 넣을 수 있는 형태로 제공한다.</p>
 */
@Tag(name = "GitHub", description = "GitHub 관련 API")
public interface GithubResumeApi {

    @Operation(
        summary = "내 GitHub 저장소 목록 (이력서 가져오기용)",
        description = "로그인 사용자의 저장소 통계를 이력서 프로젝트 항목 형태로 반환합니다. "
            + "소유 저장소 우선, 스타/최근 커밋 순으로 정렬됩니다. GitHub 미연동 시 빈 목록."
    )
    @GetMapping("/v1/users/me/github/repositories")
    ResponseEntity<List<GithubResumeProjectResponse>> getMyRepositoriesForResume(
        @Parameter(hidden = true) @AuthUser User user
    );
}
