package kr.ac.koreatech.sw.kosp.domain.admin.member.dto.request;

public record AdminUserUpdateRequest(
    String name,
    String introduction,
    String profileImageUrl
) {
}
