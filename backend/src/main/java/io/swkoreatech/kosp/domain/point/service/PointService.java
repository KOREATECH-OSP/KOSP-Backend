package io.swkoreatech.kosp.domain.point.service;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.point.model.PointSource;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.domain.point.model.TransactionType;
import io.swkoreatech.kosp.domain.point.repository.PointTransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 포인트 서비스.
 * 포인트 변경 및 거래 기록 기능을 담당한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;

    /**
     * 사용자의 포인트를 변경하고 거래를 기록한다.
     *
     * @param user 대상 사용자
     * @param amount 변경 포인트 (양수: 지급, 음수: 차감)
     * @param reason 변경 사유
     * @param source 포인트 출처
     * @throws GlobalException 금액이 0이거나 잔액이 부족한 경우
     */
    public void changePoint(User user, Integer amount, String reason, PointSource source) {
        validateNonZeroAmount(amount);
        validateSufficientBalanceIfDeduct(user, amount);

        user.addPoint(amount);
        saveTransaction(user, amount, reason, source);
    }

    private void saveTransaction(User user, Integer amount, String reason, PointSource source) {
        TransactionType type = determineType(amount);
        PointTransaction transaction = PointTransaction.builder()
            .user(user)
            .amount(amount)
            .type(type)
            .source(source)
            .reason(reason)
            .balanceAfter(user.getPoint())
            .build();
        pointTransactionRepository.save(transaction);
    }

    private TransactionType determineType(Integer amount) {
        if (amount > 0) {
            return TransactionType.GRANT;
        }
        return TransactionType.DEDUCT;
    }

    private void validateNonZeroAmount(Integer amount) {
        if (amount == null || amount == 0) {
            throw new GlobalException(ExceptionMessage.INVALID_POINT_AMOUNT);
        }
    }

    private void validateSufficientBalanceIfDeduct(User user, Integer amount) {
        if (amount >= 0) {
            return;
        }
        if (user.getPoint() + amount < 0) {
            throw new GlobalException(ExceptionMessage.INSUFFICIENT_POINTS);
        }
    }
}
