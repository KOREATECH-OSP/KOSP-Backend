package kr.ac.koreatech.sw.kosp.domain.admin.point.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.request.PointTransactionRequest;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response.PointTransactionResponse;
import kr.ac.koreatech.sw.kosp.domain.admin.point.model.PointTransaction;
import kr.ac.koreatech.sw.kosp.domain.admin.point.model.PointTransaction.TransactionType;
import kr.ac.koreatech.sw.kosp.domain.admin.point.repository.PointTransactionRepository;
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

    @Transactional
    public PointTransactionResponse changePoint(Long userId, PointTransactionRequest request, User admin) {
        validateNonZeroAmount(request.point());
        User user = findUser(userId);
        TransactionType type = determineTransactionType(request.point());
        Integer absoluteAmount = Math.abs(request.point());

        if (type == TransactionType.DEDUCT) {
            validateSufficientBalance(user, absoluteAmount);
        }

        return processTransaction(user, absoluteAmount, type, request.reason(), admin);
    }

    public PointHistoryResponse getPointHistory(Long userId, Pageable pageable) {
        User user = findUser(userId);
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return PointHistoryResponse.from(user, transactions);
    }

    private TransactionType determineTransactionType(Integer point) {
        if (point > 0) {
            return TransactionType.GRANT;
        }
        return TransactionType.DEDUCT;
    }

    private PointTransactionResponse processTransaction(User user, Integer amount, TransactionType type, String reason, User admin) {
        applyPointChange(user, amount, type);
        PointTransaction transaction = createTransaction(user, amount, type, reason, admin);
        pointTransactionRepository.save(transaction);
        return PointTransactionResponse.from(transaction);
    }

    private void applyPointChange(User user, Integer amount, TransactionType type) {
        if (type == TransactionType.GRANT) {
            user.addPoint(amount);
            return;
        }
        user.deductPoint(amount);
    }

    private PointTransaction createTransaction(User user, Integer amount, TransactionType type, String reason, User admin) {
        return PointTransaction.builder()
            .user(user)
            .amount(amount)
            .type(type)
            .reason(reason)
            .admin(admin)
            .balanceAfter(user.getPoint())
            .build();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_NOT_FOUND));
    }

    private void validateNonZeroAmount(Integer point) {
        if (point == null || point == 0) {
            throw new GlobalException(ExceptionMessage.INVALID_POINT_AMOUNT);
        }
    }

    private void validateSufficientBalance(User user, Integer amount) {
        if (user.getPoint() < amount) {
            throw new GlobalException(ExceptionMessage.INSUFFICIENT_POINTS);
        }
    }
}
