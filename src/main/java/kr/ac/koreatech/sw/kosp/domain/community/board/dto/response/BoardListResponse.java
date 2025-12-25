package kr.ac.koreatech.sw.kosp.domain.community.board.dto.response;

import java.util.List;
import java.util.stream.Collectors;
import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import lombok.Getter;

@Getter
public class BoardListResponse {
    private final List<BoardResponse> boards;

    private BoardListResponse(List<BoardResponse> boards) {
        this.boards = boards;
    }

    public static BoardListResponse from(List<Board> boards) {
        List<BoardResponse> responses = boards.stream()
            .map(BoardResponse::from)
            .collect(Collectors.toList());
        return new BoardListResponse(responses);
    }
}
