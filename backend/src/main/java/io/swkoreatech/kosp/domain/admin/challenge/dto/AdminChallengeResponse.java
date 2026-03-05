package io.swkoreatech.kosp.domain.admin.challenge.dto;

import io.swkoreatech.kosp.common.challenge.model.Challenge;
import io.swkoreatech.kosp.common.challenge.model.ImageResourceType;

/**
 * 관리자 챌린지 상세 응답 DTO.
 *
 * @param id                챌린지 식별자
 * @param name              챌린지 이름
 * @param description       챌린지 설명
 * @param condition         챌린지 달성 조건 (SpEL 표현식)
 * @param tier              챌린지 등급
 * @param imageResource     이미지 리소스 경로
 * @param imageResourceType 이미지 리소스 타입
 * @param point             보상 포인트
 */
public record AdminChallengeResponse(
    Long id,
    String name,
    String description,
    String condition,
    Integer tier,
    String imageResource,
    ImageResourceType imageResourceType,
    Integer point
) {
    /**
     * {@link Challenge} 엔티티로부터 응답 DTO를 생성한다.
     *
     * @param challenge 챌린지 엔티티
     * @return 관리자 챌린지 응답 DTO
     */
    public static AdminChallengeResponse from(Challenge challenge) {
        return new AdminChallengeResponse(
            challenge.getId(),
            challenge.getName(),
            challenge.getDescription(),
            challenge.getCondition(),
            challenge.getTier(),
            challenge.getImageResource(),
            challenge.getImageResourceType(),
            challenge.getPoint()
        );
    }
}
