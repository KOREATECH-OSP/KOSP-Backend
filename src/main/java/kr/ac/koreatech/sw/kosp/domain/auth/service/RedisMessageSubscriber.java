package kr.ac.koreatech.sw.kosp.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageSubscriber implements MessageListener {

    private final UserRefreshManager userRefreshManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String username = new String(message.getBody());
        log.info("Received security refresh signal for user: {}", username);
        userRefreshManager.markAsDirty(username);
    }
}
