package io.swkoreatech.kosp.common.slack.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Slack 알림 메시지 모델.
 *
 * <p>Slack Webhook URL과 전송할 메시지 내용을 담는 값 객체이다.</p>
 */
@Getter
public class SlackNotification {

    /** Slack 첨부 메시지의 "good" 색상 코드 */
    public static final String COLOR_GOOD = "good";

    private final String slackUrl;
    private final String content;

    @Builder
    private SlackNotification(
        String slackUrl,
        String text
    ) {
        this.slackUrl = slackUrl;
        this.content = text;
    }
}
