package kr.ac.koreatech.sw.kosp.domain.admin.member.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record AdminUserListResponse(
    List<UserInfo> users,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
    public record UserInfo(
        Long id,
        String name,
        String kutEmail,
        String kutId,
        String profileImageUrl,
        String introduction,
        Set<String> roles,
        boolean isDeleted,
        LocalDateTime createdAt
    ) {
    }
}
