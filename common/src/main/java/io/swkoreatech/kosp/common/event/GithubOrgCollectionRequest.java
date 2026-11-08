package io.swkoreatech.kosp.common.event;

/**
 * GitHub 조직 레포지토리 수집 요청 이벤트.
 *
 * <p>특정 조직의 GitHub 레포지토리 통계를 수집하도록 요청할 때 사용된다.</p>
 *
 * @param organizationId K-OSP 내 조직 ID
 * @param orgName        GitHub 조직 이름
 * @param githubOrgId    GitHub 조직 numeric ID
 * @param adminUserId    조직을 등록한 관리자의 K-OSP 사용자 ID (토큰 조회용)
 */
public record GithubOrgCollectionRequest(
    Long organizationId,
    String orgName,
    Long githubOrgId,
    Long adminUserId
) {
}
