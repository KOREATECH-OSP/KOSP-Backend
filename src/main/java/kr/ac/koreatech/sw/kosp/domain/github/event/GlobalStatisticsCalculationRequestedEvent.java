package kr.ac.koreatech.sw.kosp.domain.github.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class GlobalStatisticsCalculationRequestedEvent extends ApplicationEvent {

    private final String triggerSource;

    public GlobalStatisticsCalculationRequestedEvent(Object source, String triggerSource) {
        super(source);
        this.triggerSource = triggerSource;
    }
}
