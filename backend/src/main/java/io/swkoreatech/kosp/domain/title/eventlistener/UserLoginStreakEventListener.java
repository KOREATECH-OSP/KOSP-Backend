package io.swkoreatech.kosp.domain.title.eventlistener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.domain.title.service.UserLoginStreakService;
import io.swkoreatech.kosp.domain.user.event.UserLoginEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 유저 로그인 이벤트를 수신해 streak를 갱신하는 이벤트 리스너.
 *
 * <p>비동기({@code @Async})로 처리하여 로그인 응답 지연을 방지한다.
 * streak 갱신 실패가 로그인 흐름에 영향을 주지 않도록 예외를 내부에서 처리한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoginStreakEventListener {

    private final UserLoginStreakService userLoginStreakService;

    /**
     * 로그인 이벤트를 수신해 streak를 비동기로 갱신한다.
     *
     * @param event 로그인 이벤트
     */
    @Async
    @EventListener
    public void handle(UserLoginEvent event) {
        try {
            userLoginStreakService.recordLogin(event.getUserId());
        } catch (Exception e) {
            // streak 실패가 로그인 응답을 깨지 않도록 예외 흡수 (운영 로그로 확인)
            log.error("[Streak] userId={} streak 갱신 실패. 로그인 흐름에는 영향 없음. error={}",
                event.getUserId(), e.getMessage(), e);
        }
    }
}
