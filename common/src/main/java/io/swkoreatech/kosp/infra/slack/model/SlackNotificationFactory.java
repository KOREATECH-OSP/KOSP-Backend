package io.swkoreatech.kosp.infra.slack.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlackNotificationFactory {

    private final String eventNotificationUrl;

    public SlackNotificationFactory(
        @Value("${slack.event-notification-url}") String eventNotificationUrl
    ) {
        this.eventNotificationUrl = eventNotificationUrl;
    }

    /**
     * 사용자 가입 알림
     */
    public SlackNotification generateUserSignupNotification(String email) {
        return SlackNotification.builder()
            .slackUrl(eventNotificationUrl)
            .text(String.format("`%s`님이 가입하셨습니다.", email))
            .build();
    }

    /**
     * 챌린지 완료 알림
     */
    public SlackNotification generateChallengeCompletedNotification(
        String userName,
        String challengeName
    ) {
        return SlackNotification.builder()
            .slackUrl(eventNotificationUrl)
            .text(String.format("`%s`님이 `%s` 챌린지를 완료했습니다!", userName, challengeName))
            .build();
    }
}
