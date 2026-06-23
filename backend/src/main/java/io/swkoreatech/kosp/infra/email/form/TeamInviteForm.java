package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * 팀 초대 이메일 양식.
 *
 * <p>팀 초대 시 팀명, 초대자, 피초대자 이름, 서버 URL, 초대 ID를 포함한 이메일 양식을 생성한다.
 */
@RequiredArgsConstructor
public class TeamInviteForm implements EmailForm {

    private final String teamName;
    private final String inviterName;
    private final String inviteeName;
    private final String serverUrl;
    private final Long inviteId;

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getContent() {
        return Map.of(
            "teamName", teamName,
            "inviterName", inviterName,
            "inviteeName", inviteeName,
            "contextPath", serverUrl,
            "inviteId", String.valueOf(inviteId)
        );
    }

    /** {@inheritDoc} */
    @Override
    public String getSubject() {
        return String.format("[K-OSP] %s님이 %s 팀에 초대했습니다.", inviterName, teamName);
    }

    /** {@inheritDoc} */
    @Override
    public String getFilePath() {
        return "team_invite";
    }
}
