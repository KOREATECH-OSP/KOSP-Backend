package io.swkoreatech.kosp.common.slack.client;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.swkoreatech.kosp.common.slack.model.SlackNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Slack 메시지 전송 클라이언트.
 *
 * <p>Slack Incoming Webhook을 통해 알림 메시지를 전송한다.
 * 전송 실패 시 최대 3회까지 자동 재시도하며,
 * 모든 재시도가 실패하면 에러 로그를 기록한다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlackClient {

    private final RestTemplate restTemplate;

    /**
     * Slack 알림 메시지를 전송한다.
     *
     * <p>전송 실패 시 1초 간격으로 최대 3회 재시도한다.</p>
     *
     * @param slackNotification 전송할 Slack 알림 정보
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void sendMessage(SlackNotification slackNotification) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        Map<String, Object> slackMessage = new HashMap<>();
        slackMessage.put("text", slackNotification.getContent());
        slackMessage.put("attachments", List.of(
            Map.of("color", SlackNotification.COLOR_GOOD)
        ));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(slackMessage, headers);
        restTemplate.postForObject(
            slackNotification.getSlackUrl(),
            request,
            String.class
        );
    }

    /**
     * Slack 메시지 전송 재시도가 모두 실패했을 때 호출되는 복구 메서드.
     *
     * @param e                 발생한 예외
     * @param slackNotification 전송에 실패한 Slack 알림 정보
     */
    @Recover
    public void slackRecovery(Exception e, SlackNotification slackNotification) {
        log.error("슬랙 메시지 전송에 실패했습니다. content: {}", slackNotification.getContent(), e);
    }
}
