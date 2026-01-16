package kr.ac.koreatech.sw.kosp.domain.community.board.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.community.board.dto.response.BoardListResponse;

@Tag(name = "Board", description = "게시판 메타데이터 API")
@RequestMapping("/v1/community/boards")
public interface BoardApi {

    @Operation(summary = "게시판 목록 조회", description = "모든 게시판 목록을 조회합니다.")
    @GetMapping
    ResponseEntity<BoardListResponse> getBoards();
}
