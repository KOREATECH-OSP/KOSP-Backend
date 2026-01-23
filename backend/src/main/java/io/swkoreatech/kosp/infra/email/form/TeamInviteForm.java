package io.swkoreatech.kosp.infra.email.form;

import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TeamInviteForm implements EmailForm {

    private final String teamName;
    private final String inviterName;
    private final String serverUrl;
    private final Long inviteId;

    @Override
    public Map<String, String> getContent() {
        return Map.of(
            "teamName", teamName,
            "inviterName", inviterName,
            "contextPath", serverUrl, // Use contextPath to match convention in templates if needed, or serverUrl
            "inviteId", String.valueOf(inviteId)
        );
    }

    @Override
    public String getSubject() {
        return String.format("[KOSP] %s 팀에 초대되었습니다.", teamName);
    }

    @Override
    public String getFilePath() {
        return "team_invite";
    }
}
