package kr.ac.koreatech.sw.kosp.domain.community.board.dto.response;

import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import lombok.Builder;

@Builder
public record BoardResponse(
    Long id,
    String name,
    String description,
    Boolean isRecruitAllowed,
    Boolean isNotice
) {
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
