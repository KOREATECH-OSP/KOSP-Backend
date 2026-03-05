package io.swkoreatech.kosp.domain.admin.point.dto.response;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * 포인트 내역 응답 DTO.
 *
 * @param userId         사용자 식별자
 * @param userName       사용자 이름
 * @param currentBalance 현재 포인트 잔액
 * @param transactions   포인트 거래 내역 목록
 * @param totalElements  전체 거래 수
 * @param totalPages     전체 페이지 수
 * @param currentPage    현재 페이지 번호
 * @param pageSize       페이지 크기
 */
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
    /**
     * 포인트 거래 상세 정보.
     *
     * @param transactionId 거래 식별자
     * @param amount        거래 금액
     * @param type          거래 유형
     * @param reason        거래 사유
     * @param balanceAfter  거래 후 잔액
     * @param source        거래 출처
     * @param createdAt     거래 일시
     */
    public record TransactionInfo(
        Long transactionId,
        Integer amount,
        String type,
        String reason,
        Integer balanceAfter,
        String source,
        java.time.LocalDateTime createdAt
    ) {
        /**
         * {@link PointTransaction} 엔티티로부터 거래 정보를 생성한다.
         *
         * @param transaction 포인트 거래 엔티티
         * @return 포인트 거래 상세 정보
         */
        public static TransactionInfo from(PointTransaction transaction) {
            return new TransactionInfo(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType().name(),
                transaction.getReason(),
                transaction.getBalanceAfter(),
                transaction.getSource().name(),
                transaction.getCreatedAt()
            );
        }
    }

    /**
     * 사용자와 포인트 거래 페이지로부터 응답 DTO를 생성한다.
     *
     * @param user         사용자 엔티티
     * @param transactions 포인트 거래 페이지 객체
     * @return 포인트 내역 응답 DTO
     */
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
