package io.swkoreatech.kosp.domain.admin.role.dto.response;

import io.swkoreatech.kosp.common.auth.model.Policy;

import java.util.List;

/**
 * 정책(Policy) 상세 응답 DTO.
 *
 * @param id          정책 식별자
 * @param name        정책 이름
 * @param description 정책 설명
 * @param permissions 정책에 할당된 권한 목록
 */
public record PolicyDetailResponse(
    Long id,
    String name,
    String description,
    List<PermissionResponse> permissions
) {
    /**
     * {@link Policy} 엔티티로부터 상세 응답 DTO를 생성한다.
     *
     * @param policy 정책 엔티티
     * @return 정책 상세 응답 DTO
     */
    public static PolicyDetailResponse from(Policy policy) {
        List<PermissionResponse> permissions = policy.getPermissions().stream()
            .map(PermissionResponse::from)
            .toList();
        return new PolicyDetailResponse(
            policy.getId(),
            policy.getName(),
            policy.getDescription(),
            permissions
        );
    }
}
