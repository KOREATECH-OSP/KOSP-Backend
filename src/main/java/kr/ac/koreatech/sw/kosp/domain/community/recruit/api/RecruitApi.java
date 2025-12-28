package kr.ac.koreatech.sw.kosp.domain.community.recruit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.global.security.annotation.AuthUser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Recruit", description = "모집 공고 관리 API")
public interface RecruitApi {

    @Operation(summary = "모집 공고 목록 조회", description = "전체 모집 공고 목록을 조회합니다.")
    @GetMapping
    ResponseEntity<RecruitListResponse> getList(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestParam Long boardId,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "모집 공고 상세 조회", description = "모집 공고 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    ResponseEntity<RecruitResponse> getOne(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    @Operation(summary = "모집 공고 작성", description = "새로운 모집 공고를 작성합니다.")
    @PostMapping
    ResponseEntity<Void> create(
        @Parameter(hidden = true) @AuthUser User user,
        @RequestBody @Valid RecruitRequest request
    );

    @Operation(summary = "모집 공고 수정", description = "기존 모집 공고를 수정합니다.")
    @PutMapping("/{id}")
    ResponseEntity<Void> update(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid RecruitRequest request
    );

    @Operation(summary = "모집 공고 삭제", description = "모집 공고를 삭제합니다.")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id
    );

    @Operation(summary = "모집 상태 변경", description = "모집 공고의 상태를 변경합니다.")
    @org.springframework.web.bind.annotation.PatchMapping("/{id}/status")
    ResponseEntity<Void> updateStatus(
        @Parameter(hidden = true) @AuthUser User user,
        @PathVariable Long id,
        @RequestBody @Valid kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitStatusRequest request
    );
}
