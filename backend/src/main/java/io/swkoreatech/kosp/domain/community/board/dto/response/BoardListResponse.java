package io.swkoreatech.kosp.domain.community.board.dto.response;

import io.swkoreatech.kosp.domain.community.board.model.Board;

import java.util.List;

public record BoardListResponse(
    List<BoardResponse> boards
) {
    public static BoardListResponse from(List<Board> boards) {
        List<BoardResponse> responses = boards.stream()
            .map(BoardResponse::from)
            .toList();
        return new BoardListResponse(responses);
    }
}
