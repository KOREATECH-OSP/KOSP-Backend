package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

/**
 * 조직 등록 안내 이메일 양식.
 *
 * <p>GitHub Organization이 K-OSP에 등록될 때 이메일이 공개된 미가입 멤버에게
 * K-OSP 가입을 안내하는 이메일 양식을 생성한다.
 */
public class OrganizationRegisteredForm implements EmailForm {

    private static final String PATH = "organization_registered";

    private final String githubUsername;
    private final String ownerName;
    private final String orgName;
    private final String clientUrl;

    public OrganizationRegisteredForm(String githubUsername, String ownerName, String orgName, String clientUrl) {
        this.githubUsername = githubUsername;
        this.ownerName = ownerName;
        this.orgName = orgName;
        this.clientUrl = clientUrl;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getContent() {
        return Map.of(
            "githubUsername", githubUsername,
            "ownerName", ownerName,
            "orgName", orgName,
            "contextPath", clientUrl
        );
    }

    /** {@inheritDoc} */
    @Override
    public String getSubject() {
        return String.format("[K-OSP] %s님이 %s 조직을 K-OSP에 등록했습니다.", ownerName, orgName);
    }

    /** {@inheritDoc} */
    @Override
    public String getFilePath() {
        return PATH;
    }
}
