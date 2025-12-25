package kr.ac.koreatech.sw.kosp.domain.community.article.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.request.ArticleRequest;
import kr.ac.koreatech.sw.kosp.domain.community.article.dto.response.ArticleResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Article", description = "게시글 관리 API")
public interface ArticleApi {

    @Operation(summary = "게시글 목록 조회", description = "특정 게시판의 게시글 목록을 페이징하여 조회합니다.")
    ResponseEntity<ArticleListResponse> getList(
        Long boardId,
        @Parameter(hidden = true) Pageable pageable
    );

    @Operation(summary = "게시글 상세 조회", description = "게시글 정보를 조회합니다.")
    ResponseEntity<ArticleResponse> getOne(Long id);

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 작성합니다.")
    ResponseEntity<Void> create(ArticleRequest request);

    @Operation(summary = "게시글 수정", description = "작성자가 게시글을 수정합니다.")
    ResponseEntity<Void> update(Long id, ArticleRequest request);

    @Operation(summary = "게시글 삭제", description = "작성자가 게시글을 삭제합니다.")
    ResponseEntity<Void> delete(Long id);
}
