package io.swkoreatech.kosp.domain.title.dto.response;

import java.util.Comparator;
import java.util.List;

import io.swkoreatech.kosp.common.title.model.UserTitle;

/**
 * 유저 칭호 목록 응답 DTO.
 *
 * @param titles     보유 칭호 전체 목록
 * @param subTitles  보조 칭호 목록 (대표 칭호 제외, 최대 3개 / lastDisplayedAt DESC → grantedAt DESC)
 * @param totalCount 총 보유 칭호 수
 */
public record UserTitleListResponse(
    List<UserTitleResponse> titles,
    List<UserTitleResponse> subTitles,
    int totalCount
) {
    public static UserTitleListResponse from(List<UserTitle> userTitles) {
        List<UserTitleResponse> responses = userTitles.stream()
            .map(UserTitleResponse::from)
            .toList();

        List<UserTitleResponse> sub = userTitles.stream()
            .filter(t -> !t.isDisplay())
            .sorted(Comparator
                .comparing(UserTitle::getLastDisplayedAt,
                    Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(UserTitle::getGrantedAt, Comparator.reverseOrder()))
            .limit(3)
            .map(UserTitleResponse::from)
            .toList();

        return new UserTitleListResponse(responses, sub, responses.size());
    }
}
