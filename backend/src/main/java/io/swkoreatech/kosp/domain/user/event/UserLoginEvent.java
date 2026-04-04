package io.swkoreatech.kosp.domain.user.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

/**
 * 사용자 로그인 이벤트.
 *
 * <p>로그인 성공 시 발행되며, 로그인 streak 업데이트 및 향후 확장 처리에 사용된다.
 * {@code UserLoginStreakEventListener}가 이 이벤트를 수신해 streak를 갱신한다.</p>
 */
@Getter
public class UserLoginEvent extends ApplicationEvent {

    private final Long userId;

    /**
     * 로그인 이벤트를 생성한다.
     *
     * @param source 이벤트 소스
     * @param userId 로그인한 사용자 ID
     */
    public UserLoginEvent(Object source, Long userId) {
        super(source);
        this.userId = userId;
    }
}
