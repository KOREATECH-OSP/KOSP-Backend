package io.swkoreatech.kosp.domain.admin.role.dto.response;

import io.swkoreatech.kosp.common.auth.model.Policy;

/**
 * 정책(Policy) 응답 DTO.
 *
 * @param id          정책 식별자
 * @param name        정책 이름
 * @param description 정책 설명
 */
public record PolicyResponse(
    Long id,
    String name,
    String description
) {
    /**
     * {@link Policy} 엔티티로부터 응답 DTO를 생성한다.
     *
     * @param policy 정책 엔티티
     * @return 정책 응답 DTO
     */
    public static PolicyResponse from(Policy policy) {
        return new PolicyResponse(
            policy.getId(),
            policy.getName(),
            policy.getDescription()
        );
    }
}
