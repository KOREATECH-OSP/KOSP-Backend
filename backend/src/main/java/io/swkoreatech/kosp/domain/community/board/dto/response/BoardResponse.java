package io.swkoreatech.kosp.domain.community.board.dto.response;

import io.swkoreatech.kosp.domain.community.board.model.Board;

import lombok.Builder;

/**
 * 게시판 응답 DTO.
 *
 * @param id 게시판 ID
 * @param name 게시판 이름
 * @param description 게시판 설명
 * @param isRecruitAllowed 모집글 허용 여부
 * @param isNotice 공지사항 게시판 여부
 */
@Builder
public record BoardResponse(
    Long id,
    String name,
    String description,
    Boolean isRecruitAllowed,
    Boolean isNotice
) {
    /**
     * 게시판 엔티티로부터 응답 객체를 생성한다.
     *
     * @param board 게시판 엔티티
     * @return 게시판 응답
     */
    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
            .id(board.getId())
            .name(board.getName())
            .description(board.getDescription())
            .isRecruitAllowed(board.isRecruitAllowed())
            .isNotice(board.isNotice())
            .build();
    }
}
