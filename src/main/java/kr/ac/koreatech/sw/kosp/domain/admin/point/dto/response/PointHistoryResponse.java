package kr.ac.koreatech.sw.kosp.domain.admin.point.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import kr.ac.koreatech.sw.kosp.domain.admin.point.model.PointTransaction;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public record PointHistoryResponse(
    Long userId,
    String userName,
    Integer currentBalance,
    List<TransactionInfo> transactions,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {
    public record TransactionInfo(
        Long transactionId,
        Integer amount,
        String type,
        String reason,
        Integer balanceAfter,
        String adminName,
        java.time.LocalDateTime createdAt
    ) {
        public static TransactionInfo from(PointTransaction transaction) {
            String adminName = null;
            if (transaction.getAdmin() != null) {
                adminName = transaction.getAdmin().getName();
            }
            return new TransactionInfo(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getReason(),
                transaction.getBalanceAfter(),
                adminName,
                transaction.getCreatedAt()
            );
        }
    }

    public static PointHistoryResponse from(User user, Page<PointTransaction> transactions) {
        List<TransactionInfo> transactionInfos = transactions.getContent().stream()
            .map(TransactionInfo::from)
            .toList();

        return new PointHistoryResponse(
            user.getId(),
            user.getName(),
            user.getPoint(),
            transactionInfos,
            transactions.getTotalElements(),
            transactions.getTotalPages(),
            transactions.getNumber(),
            transactions.getSize()
        );
    }
}
