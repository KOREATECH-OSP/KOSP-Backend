package kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response;

import java.time.LocalDateTime;

import kr.ac.koreatech.sw.kosp.domain.admin.point.model.PointTransaction;

public record PointTransactionResponse(
    Long transactionId,
    Long userId,
    String userName,
    Integer amount,
    String type,
    String reason,
    Integer balanceAfter,
    String adminName,
    LocalDateTime createdAt
) {
    public static PointTransactionResponse from(PointTransaction transaction) {
        return new PointTransactionResponse(
            transaction.getId(),
            transaction.getUser().getId(),
            transaction.getUser().getName(),
            transaction.getAmount(),
            transaction.getType().name(),
            transaction.getReason(),
            transaction.getBalanceAfter(),
            transaction.getAdmin().getName(),
            transaction.getCreatedAt()
        );
    }
}
