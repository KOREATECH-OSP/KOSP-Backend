package kr.ac.koreatech.sw.kosp.domain.community.recruit.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.request.RecruitRequest;
import kr.ac.koreatech.sw.kosp.domain.community.recruit.dto.response.RecruitResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Recruit", description = "모집 공고 관리 API")
public interface RecruitApi {

    @Operation(summary = "모집 공고 목록 조회", description = "특정 게시판의 모집 공고 목록을 페이징하여 조회합니다.")
    ResponseEntity<RecruitListResponse> getList(
        Long boardId,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "모집 공고 상세 조회", description = "모집 공고 정보를 조회합니다.")
    ResponseEntity<RecruitResponse> getOne(Long id);

    @Operation(summary = "모집 공고 생성", description = "새로운 모집 공고를 작성합니다.")
    ResponseEntity<Void> create(RecruitRequest request);

    @Operation(summary = "모집 공고 수정", description = "작성자가 모집 공고를 수정합니다.")
    ResponseEntity<Void> update(Long id, RecruitRequest request);

    @Operation(summary = "모집 공고 삭제", description = "작성자가 모집 공고를 삭제합니다.")
    ResponseEntity<Void> delete(Long id);
}
