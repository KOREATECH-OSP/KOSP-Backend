package kr.ac.koreatech.sw.kosp.domain.auth.service;

import kr.ac.koreatech.sw.kosp.global.config.redis.RedisConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionAdminService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Call this method after modifying any Role, Policy, or Permission for a user.
     * @param username (kutEmail) of the affected user.
     */
    @Transactional
    public void publishPermissionChange(String username) {
        log.info("Publishing permission change event for user: {}", username);
        redisTemplate.convertAndSend(RedisConfig.SECURITY_REFRESH_CHANNEL, username);
    }
}
