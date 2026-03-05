package io.swkoreatech.kosp.domain.community.board.dto.response;

import io.swkoreatech.kosp.domain.community.board.model.Board;

import java.util.List;

/**
 * 게시판 목록 응답 DTO.
 *
 * @param boards 게시판 응답 목록
 */
public record BoardListResponse(
    List<BoardResponse> boards
) {
    /**
     * 게시판 엔티티 목록으로부터 응답 객체를 생성한다.
     *
     * @param boards 게시판 엔티티 목록
     * @return 게시판 목록 응답
     */
    public static BoardListResponse from(List<Board> boards) {
        List<BoardResponse> responses = boards.stream()
            .map(BoardResponse::from)
            .toList();
        return new BoardListResponse(responses);
    }
}
