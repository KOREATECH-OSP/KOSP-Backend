package io.swkoreatech.kosp.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.domain.user.model.User;
import io.swkoreatech.kosp.global.dto.PageMeta;

public record MyPointHistoryResponse(
    Integer currentBalance,
    List<TransactionSummary> transactions,
    PageMeta meta
) {
    public record TransactionSummary(
        Long id,
        Integer amount,
        String type,
        String reason,
        Integer balanceAfter,
        LocalDateTime createdAt
    ) {
        public static TransactionSummary from(PointTransaction transaction) {
            return new TransactionSummary(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getReason(),
                transaction.getBalanceAfter(),
                transaction.getCreatedAt()
            );
        }
    }

    public static MyPointHistoryResponse from(User user, Page<PointTransaction> transactions) {
        List<TransactionSummary> summaries = transactions.getContent().stream()
            .map(TransactionSummary::from)
            .toList();

        return new MyPointHistoryResponse(
            user.getPoint(),
            summaries,
            PageMeta.from(transactions)
        );
    }
}
