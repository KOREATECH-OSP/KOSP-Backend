package io.swkoreatech.kosp.domain.admin.member.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.user.model.User;

public record AdminUserListResponse(
    List<UserInfo> users,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
    public static AdminUserListResponse from(Page<User> page) {
        return new AdminUserListResponse(
            page.getContent().stream().map(UserInfo::from).toList(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

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
        public static UserInfo from(User user) {
            String profileImageUrl = user.getGithubUser() != null
                ? user.getGithubUser().getGithubAvatarUrl()
                : null;
            Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
            return new UserInfo(
                user.getId(),
                user.getName(),
                user.getKutEmail(),
                user.getKutId(),
                profileImageUrl,
                user.getIntroduction(),
                roleNames,
                user.isDeleted(),
                user.getCreatedAt()
            );
        }
    }
}
