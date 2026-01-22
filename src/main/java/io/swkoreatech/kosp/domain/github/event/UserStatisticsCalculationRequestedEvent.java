package io.swkoreatech.kosp.domain.github.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class UserStatisticsCalculationRequestedEvent extends ApplicationEvent {

    private final String githubLogin;

    public UserStatisticsCalculationRequestedEvent(Object source, String githubLogin) {
        super(source);
        this.githubLogin = githubLogin;
    }
}
