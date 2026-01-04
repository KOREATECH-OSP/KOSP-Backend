package kr.ac.koreatech.sw.kosp.domain.admin.role.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PermissionAssignRequest(
    @NotBlank(message = "권한 이름은 필수입니다.")
    String permissionName
) {
}
