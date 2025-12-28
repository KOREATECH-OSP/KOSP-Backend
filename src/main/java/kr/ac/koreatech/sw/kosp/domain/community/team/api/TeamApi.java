package kr.ac.koreatech.sw.kosp.domain.community.team.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.request.TeamCreateRequest;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamDetailResponse;
import kr.ac.koreatech.sw.kosp.domain.community.team.dto.response.TeamListResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Team", description = "팀 관리 API")
public interface TeamApi {

    @Operation(summary = "팀 목록 조회", description = "팀 목록을 조회합니다.")
    @GetMapping("/teams")
    ResponseEntity<TeamListResponse> getList(
        @RequestParam(required = false, defaultValue = "") String search,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다.")
    @PostMapping("/teams")
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid TeamCreateRequest request
    );

    @Operation(summary = "팀 상세 조회", description = "팀 상세 정보를 조회합니다.")
    @GetMapping("/teams/{teamId}")
    ResponseEntity<TeamDetailResponse> getTeam(
        @PathVariable Long teamId
    );
}
