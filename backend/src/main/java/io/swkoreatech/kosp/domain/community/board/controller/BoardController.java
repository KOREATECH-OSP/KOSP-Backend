package io.swkoreatech.kosp.domain.community.board.controller;

import io.swkoreatech.kosp.domain.community.board.api.BoardApi;
import io.swkoreatech.kosp.domain.community.board.dto.response.BoardListResponse;
import io.swkoreatech.kosp.domain.community.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swkoreatech.kosp.global.security.annotation.Permit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/community/boards")
public class BoardController implements BoardApi {

    private final BoardService boardService;

    @Override
    @GetMapping
    @Permit(permitAll = true, name = "boards:list", description = "게시판 목록 조회")
    public ResponseEntity<BoardListResponse> getBoards() {
        BoardListResponse response = boardService.getBoards();
        return ResponseEntity.ok(response);
    }
}
