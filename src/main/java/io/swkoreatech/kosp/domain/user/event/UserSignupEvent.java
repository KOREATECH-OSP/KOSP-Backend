package io.swkoreatech.kosp.domain.user.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserSignupEvent extends ApplicationEvent {
    
    private final Long userId;
    private final String githubLogin;
    
    public UserSignupEvent(Object source, Long userId, String githubLogin) {
        super(source);
        this.userId = userId;
        this.githubLogin = githubLogin;
    }
}
