package io.swkoreatech.kosp.domain.point.eventlistener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.point.event.PointChangeEvent;
import io.swkoreatech.kosp.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventListener {

    private final PointService pointService;

    @EventListener
    @Transactional
    public void handlePointChange(PointChangeEvent event) {
        log.info("Processing point change: user={}, amount={}, source={}",
            event.user().getId(), event.amount(), event.source());

        pointService.changePoint(event.user(), event.amount(), event.reason(), event.source());
    }
}
