package kr.ac.koreatech.sw.kosp.domain.community.board.controller;

import kr.ac.koreatech.sw.kosp.domain.community.board.api.BoardApi;
import kr.ac.koreatech.sw.kosp.domain.community.board.dto.response.BoardListResponse;
import kr.ac.koreatech.sw.kosp.domain.community.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/boards")
public class BoardController implements BoardApi {

    private final BoardService boardService;

    @Override
    @GetMapping
    public ResponseEntity<BoardListResponse> getBoards() {
        BoardListResponse response = boardService.getBoards();
        return ResponseEntity.ok(response);
    }
}
