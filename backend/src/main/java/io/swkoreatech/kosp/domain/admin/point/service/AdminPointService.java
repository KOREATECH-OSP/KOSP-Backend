package io.swkoreatech.kosp.domain.admin.point.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import io.swkoreatech.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import io.swkoreatech.kosp.domain.point.event.PointChangeEvent;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.domain.point.repository.PointTransactionRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void changePoint(Long userId, PointTransactionRequest request) {
        User user = userRepository.getById(userId);
        eventPublisher.publishEvent(PointChangeEvent.fromAdmin(user, request.point(), request.reason()));
    }

    public PointHistoryResponse getPointHistory(Long userId, Pageable pageable) {
        User user = userRepository.getById(userId);
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return PointHistoryResponse.from(user, transactions);
    }
}
