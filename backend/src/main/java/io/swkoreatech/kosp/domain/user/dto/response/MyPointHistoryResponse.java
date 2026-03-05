package io.swkoreatech.kosp.domain.user.dto.response;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.global.dto.PageMeta;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 본인 포인트 내역 응답 DTO.
 *
 * @param currentBalance 현재 포인트 잔액
 * @param transactions 포인트 거래 내역 목록
 * @param meta 페이지 메타 정보
 */
public record MyPointHistoryResponse(
    Integer currentBalance,
    List<TransactionSummary> transactions,
    PageMeta meta
) {
    /**
     * 포인트 거래 요약 정보.
     *
     * @param id 거래 ID
     * @param amount 거래 금액
     * @param type 거래 유형 (EARN, SPEND)
     * @param reason 거래 사유
     * @param balanceAfter 거래 후 잔액
     * @param createdAt 거래 일시
     */
    public record TransactionSummary(
        Long id,
        Integer amount,
        String type,
        String reason,
        Integer balanceAfter,
        LocalDateTime createdAt
    ) {
        /**
         * PointTransaction 엔티티로부터 거래 요약 정보를 생성한다.
         *
         * @param transaction 포인트 거래 엔티티
         * @return 거래 요약 응답
         */
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

    /**
     * 사용자와 포인트 거래 페이지로부터 포인트 내역 응답을 생성한다.
     *
     * @param user 사용자 엔티티
     * @param transactions 포인트 거래 페이지
     * @return 포인트 내역 응답
     */
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
