package kr.ac.koreatech.sw.kosp.domain.user.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserSignupEvent extends ApplicationEvent {
    
    private final String githubLogin;
    
    public UserSignupEvent(Object source, String githubLogin) {
        super(source);
        this.githubLogin = githubLogin;
    }
}
