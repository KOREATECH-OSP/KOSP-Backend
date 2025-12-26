package kr.ac.koreatech.sw.kosp.domain.community.board.dto.response;

import java.util.List;

import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;

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
