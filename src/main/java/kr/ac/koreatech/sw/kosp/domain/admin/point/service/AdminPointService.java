package kr.ac.koreatech.sw.kosp.domain.admin.point.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointTransactionResponse;
import kr.ac.koreatech.sw.kosp.domain.point.model.PointSource;
import kr.ac.koreatech.sw.kosp.domain.point.model.PointTransaction;
import kr.ac.koreatech.sw.kosp.domain.point.repository.PointTransactionRepository;
import kr.ac.koreatech.sw.kosp.domain.point.service.PointService;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPointService {

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final PointService pointService;

    @Transactional
    public PointTransactionResponse changePoint(Long userId, PointTransactionRequest request, User admin) {
        User user = findUser(userId);
        pointService.changePoint(user, request.point(), request.reason(), PointSource.ADMIN);

        PointTransaction lastTransaction = pointTransactionRepository
            .findFirstByUserOrderByCreatedAtDesc(user)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.POINT_TRANSACTION_NOT_FOUND));

        return PointTransactionResponse.from(lastTransaction);
    }

    public PointHistoryResponse getPointHistory(Long userId, Pageable pageable) {
        User user = findUser(userId);
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return PointHistoryResponse.from(user, transactions);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }
}
