package io.swkoreatech.kosp.domain.title.dto.response;

import java.util.List;

import io.swkoreatech.kosp.common.title.model.UserTitle;

/**
 * 유저 칭호 목록 응답 DTO.
 *
 * @param titles     보유 칭호 목록
 * @param totalCount 총 보유 칭호 수
 */
public record UserTitleListResponse(
    List<UserTitleResponse> titles,
    int totalCount
) {
    public static UserTitleListResponse from(List<UserTitle> userTitles) {
        List<UserTitleResponse> responses = userTitles.stream()
            .map(UserTitleResponse::from)
            .toList();
        return new UserTitleListResponse(responses, responses.size());
    }
}
