package io.swkoreatech.kosp.domain.community.board.controller;

import io.swkoreatech.kosp.domain.community.board.api.BoardApi;
import io.swkoreatech.kosp.domain.community.board.dto.response.BoardListResponse;
import io.swkoreatech.kosp.domain.community.board.service.BoardService;
import io.swkoreatech.kosp.global.security.annotation.Permit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * 게시판 컨트롤러.
 * {@link BoardApi}를 구현하여 게시판 관련 요청을 처리한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/boards")
public class BoardController implements BoardApi {

    private final BoardService boardService;

    /** {@inheritDoc} */
    @Override
    @GetMapping
    @Permit(permitAll = true, name = "boards:list", description = "게시판 목록 조회")
    public ResponseEntity<BoardListResponse> getBoards() {
        BoardListResponse response = boardService.getBoards();
        return ResponseEntity.ok(response);
    }
}
