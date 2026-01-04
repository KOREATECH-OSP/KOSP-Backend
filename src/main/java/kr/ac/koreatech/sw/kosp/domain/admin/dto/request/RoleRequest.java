package kr.ac.koreatech.sw.kosp.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
    @NotBlank String name,
    String description
) {}
