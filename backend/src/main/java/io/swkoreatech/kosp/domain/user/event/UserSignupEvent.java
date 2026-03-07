package io.swkoreatech.kosp.domain.user.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

/**
 * 사용자 회원가입 이벤트.
 * 회원가입 완료 후 GitHub 데이터 수집 등의 후속 처리를 트리거한다.
 */
@Getter
public class UserSignupEvent extends ApplicationEvent {

    private final Long userId;
    private final String githubLogin;

    /**
     * 회원가입 이벤트를 생성한다.
     *
     * @param source 이벤트 소스
     * @param userId 가입한 사용자 ID
     * @param githubLogin GitHub 로그인 ID
     */
    public UserSignupEvent(Object source, Long userId, String githubLogin) {
        super(source);
        this.userId = userId;
        this.githubLogin = githubLogin;
    }
}
