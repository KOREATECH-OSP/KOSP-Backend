package io.swkoreatech.kosp.domain.admin.member.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.common.auth.model.Role;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * 관리자 사용자 목록 응답 DTO.
 *
 * @param users         사용자 정보 목록
 * @param totalElements 전체 사용자 수
 * @param totalPages    전체 페이지 수
 * @param currentPage   현재 페이지 번호
 * @param pageSize      페이지 크기
 */
public record AdminUserListResponse(
    List<UserInfo> users,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
    /**
     * {@link Page} 객체로부터 응답 DTO를 생성한다.
     *
     * @param page 사용자 페이지 객체
     * @return 관리자 사용자 목록 응답 DTO
     */
    public static AdminUserListResponse from(Page<User> page) {
        return new AdminUserListResponse(
            page.getContent().stream().map(UserInfo::from).toList(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    /**
     * 사용자 요약 정보.
     *
     * @param id              사용자 식별자
     * @param name            사용자 이름
     * @param kutEmail        한국기술교육대학교 이메일
     * @param kutId           학번 또는 사번
     * @param profileImageUrl 프로필 이미지 URL
     * @param introduction    자기소개
     * @param roles           보유 역할 이름 집합
     * @param isDeleted       탈퇴 여부
     * @param createdAt       가입 일시
     */
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
        /**
         * {@link User} 엔티티로부터 사용자 요약 정보를 생성한다.
         *
         * @param user 사용자 엔티티
         * @return 사용자 요약 정보
         */
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
