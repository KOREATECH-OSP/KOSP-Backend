package io.swkoreatech.kosp.infra.email.form;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * 미가입자 팀 초대(가입 유도) 이메일 양식.
 *
 * <p>{@code team_invite.html} 템플릿을 공유하되, {@code pending=true} 플래그로 CTA를
 * "회원가입하고 팀 합류하기" 단일 버튼(회원가입 링크)으로 전환한다.</p>
 */
@RequiredArgsConstructor
public class TeamInvitePendingForm implements EmailForm {

    private final String teamName;
    private final String inviterName;
    private final String email;
    private final String clientUrl;

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getContent() {
        String signupUrl = clientUrl + "/signup?email="
            + URLEncoder.encode(email, StandardCharsets.UTF_8);
        return Map.of(
            "teamName", teamName,
            "inviterName", inviterName,
            "contextPath", clientUrl,
            "pending", "true",
            "signupUrl", signupUrl
        );
    }

    /** {@inheritDoc} */
    @Override
    public String getSubject() {
        return String.format("[K-OSP] %s님이 %s 팀에 초대했습니다. 지금 K-OSP에 가입하세요!",
            inviterName, teamName);
    }

    /** {@inheritDoc} */
    @Override
    public String getFilePath() {
        return "team_invite";
    }
}
