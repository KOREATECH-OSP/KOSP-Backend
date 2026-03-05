package io.swkoreatech.kosp.common.slack.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Slack 알림 메시지 팩토리.
 *
 * <p>다양한 이벤트(회원가입, 챌린지 완료 등)에 대한
 * Slack 알림 메시지를 생성하는 유틸리티 컴포넌트이다.</p>
 */
@Component
public class SlackNotificationFactory {

    private final String eventNotificationUrl;

    /**
     * SlackNotificationFactory를 생성한다.
     *
     * @param eventNotificationUrl Slack 이벤트 알림 Webhook URL
     */
    public SlackNotificationFactory(
        @Value("${slack.event-notification-url}") String eventNotificationUrl
    ) {
        this.eventNotificationUrl = eventNotificationUrl;
    }

    /**
     * 사용자 가입 알림 메시지를 생성한다.
     *
     * @param email 가입한 사용자의 이메일
     * @return Slack 알림 객체
     */
    public SlackNotification generateUserSignupNotification(String email) {
        return SlackNotification.builder()
            .slackUrl(eventNotificationUrl)
            .text(String.format("`%s`님이 가입하셨습니다.", email))
            .build();
    }

    /**
     * 챌린지 완료 알림 메시지를 생성한다.
     *
     * @param userName      챌린지를 완료한 사용자 이름
     * @param challengeName 완료된 챌린지 이름
     * @return Slack 알림 객체
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
