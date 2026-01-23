package io.swkoreatech.kosp.domain.point.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.domain.point.model.PointSource;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.domain.point.model.TransactionType;
import io.swkoreatech.kosp.domain.point.repository.PointTransactionRepository;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.exception.ExceptionMessage;
import io.swkoreatech.kosp.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointTransactionRepository pointTransactionRepository;

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
