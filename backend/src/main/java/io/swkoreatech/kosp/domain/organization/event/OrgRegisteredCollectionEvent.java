package io.swkoreatech.kosp.domain.organization.event;

import org.springframework.context.ApplicationEvent;

/**
 * 조직 등록 완료 후 레포지토리 수집을 트리거하는 Spring 이벤트.
 */
public class OrgRegisteredCollectionEvent extends ApplicationEvent {

    private final Long organizationId;
    private final String orgName;
    private final Long githubOrgId;
    private final Long adminUserId;

    public OrgRegisteredCollectionEvent(
        Object source,
        Long organizationId,
        String orgName,
        Long githubOrgId,
        Long adminUserId
    ) {
        super(source);
        this.organizationId = organizationId;
        this.orgName = orgName;
        this.githubOrgId = githubOrgId;
        this.adminUserId = adminUserId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getOrgName() {
        return orgName;
    }

    public Long getGithubOrgId() {
        return githubOrgId;
    }

    public Long getAdminUserId() {
        return adminUserId;
    }
}
