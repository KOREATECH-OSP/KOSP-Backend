package io.swkoreatech.kosp.domain.title.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.title.model.UserLoginStreak;
import io.swkoreatech.kosp.common.title.repository.UserLoginStreakRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유저 로그인 streak 관리 서비스.
 *
 * <p>{@code UserLoginEvent}를 수신한 {@code UserLoginStreakEventListener}에서 호출된다.
 * streak 데이터가 없는 유저는 신규 생성하고, 있으면 날짜 기준으로 갱신한다.</p>
 *
 * <p>연결 지점: {@code AuthService.login()} / {@code AuthService.loginWithGithub()}
 * → {@code UserLoginEvent} 발행 → {@code UserLoginStreakEventListener.handle()}
 * → 이 서비스의 {@link #recordLogin(Long)} 호출</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginStreakService {

    private final UserLoginStreakRepository userLoginStreakRepository;
    private final UserRepository userRepository;

    /**
     * 유저의 로그인 streak를 갱신한다.
     *
     * @param userId 로그인한 유저 ID
     */
    @Transactional
    public void recordLogin(Long userId) {
        User user = userRepository.getById(userId);
        LocalDate today = LocalDate.now();

        UserLoginStreak streak = userLoginStreakRepository.findByUser(user)
            .orElseGet(() -> {
                log.info("[Streak] 신규 streak 레코드 생성. userId={}", userId);
                return UserLoginStreak.builder().user(user).build();
            });

        streak.recordLogin(today);
        userLoginStreakRepository.save(streak);

        log.debug("[Streak] userId={} streak 갱신: currentStreak={}, longestStreak={}",
            userId, streak.getCurrentStreak(), streak.getLongestStreak());
    }
}
