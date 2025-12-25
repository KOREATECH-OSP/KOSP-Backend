package kr.ac.koreatech.sw.kosp.domain.community.board.dto.response;

import kr.ac.koreatech.sw.kosp.domain.community.board.model.Board;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final Boolean isRecruitmentAllowed;

    public static BoardResponse from(Board board) {
        return BoardResponse.builder()
            .id(board.getId())
            .name(board.getName())
            .description(board.getDescription())
            .isRecruitmentAllowed(board.isRecruitmentAllowed())
            .build();
    }
}
