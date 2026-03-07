package io.swkoreatech.kosp.domain.admin.point.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import io.swkoreatech.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import io.swkoreatech.kosp.domain.point.event.PointChangeEvent;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.domain.point.repository.PointTransactionRepository;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 포인트 관리 서비스.
 * <p>관리자 권한으로 포인트 변경 및 거래 내역 조회 비즈니스 로직을 처리한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 사용자의 포인트를 변경한다.
     *
     * @param userId  사용자 식별자
     * @param request 포인트 변경 요청 DTO
     */
    @Transactional
    public void changePoint(Long userId, PointTransactionRequest request) {
        User user = userRepository.getById(userId);
        eventPublisher.publishEvent(PointChangeEvent.fromAdmin(user, request.point(), request.reason()));
    }

    /**
     * 사용자의 포인트 거래 내역을 조회한다.
     *
     * @param userId   사용자 식별자
     * @param pageable 페이지 정보
     * @return 포인트 내역 응답 DTO
     */
    public PointHistoryResponse getPointHistory(Long userId, Pageable pageable) {
        User user = userRepository.getById(userId);
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return PointHistoryResponse.from(user, transactions);
    }
}
