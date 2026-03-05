package io.swkoreatech.kosp.domain.community.board.api;

import io.swkoreatech.kosp.domain.community.board.dto.response.BoardListResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 게시판 API 인터페이스.
 * 게시판 메타데이터 조회 엔드포인트를 정의한다.
 */
@Tag(name = "Community - Board", description = "게시판 메타데이터 API")
@RequestMapping("/v1/community/boards")
public interface BoardApi {

    /**
     * 모든 게시판 목록을 조회한다.
     *
     * @return 게시판 목록 응답
     */
    @Operation(summary = "게시판 목록 조회", description = "모든 게시판 목록을 조회합니다.")
    @GetMapping
    ResponseEntity<BoardListResponse> getBoards();
}
