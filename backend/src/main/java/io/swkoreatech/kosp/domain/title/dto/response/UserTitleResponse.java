package io.swkoreatech.kosp.domain.title.dto.response;

import java.time.LocalDateTime;

import io.swkoreatech.kosp.common.title.model.UserTitle;

/**
 * 유저 칭호 단건 응답 DTO.
 *
 * @param userTitleId  user_title PK
 * @param titleId      title PK
 * @param titleName    칭호명
 * @param description  칭호 설명
 * @param category     카테고리
 * @param rarity       희소도
 * @param iconUrl      아이콘 URL (nullable)
 * @param isDisplay    대표 칭호 여부
 * @param grantSource  지급 출처 (SYSTEM / ADMIN)
 * @param grantedAt    지급 시각
 */
public record UserTitleResponse(
    Long userTitleId,
    Long titleId,
    String titleName,
    String description,
    String category,
    String rarity,
    String iconUrl,
    boolean isDisplay,
    String grantSource,
    LocalDateTime grantedAt
) {
    public static UserTitleResponse from(UserTitle userTitle) {
        return new UserTitleResponse(
            userTitle.getId(),
            userTitle.getTitle().getId(),
            userTitle.getTitle().getName(),
            userTitle.getTitle().getDescription(),
            userTitle.getTitle().getCategory().name(),
            userTitle.getTitle().getRarity().name(),
            userTitle.getTitle().getIconUrl(),
            userTitle.isDisplay(),
            userTitle.getGrantSource().name(),
            userTitle.getGrantedAt()
        );
    }
}
