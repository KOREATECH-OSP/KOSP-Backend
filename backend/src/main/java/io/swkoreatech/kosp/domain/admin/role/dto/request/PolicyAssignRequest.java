package io.swkoreatech.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 역할에 정책을 할당하기 위한 요청 DTO.
 *
 * @param policyName 할당할 정책 이름
 */
public record PolicyAssignRequest(
    @NotBlank String policyName
) {}
