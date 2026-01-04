package kr.ac.koreatech.sw.kosp.domain.community.board.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.koreatech.sw.kosp.domain.community.board.dto.response.BoardListResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Board", description = "게시판 메타데이터 API")
public interface BoardApi {

    @Operation(summary = "게시판 목록 조회", description = "모든 게시판 목록을 조회합니다.")
    ResponseEntity<BoardListResponse> getBoards();
}
