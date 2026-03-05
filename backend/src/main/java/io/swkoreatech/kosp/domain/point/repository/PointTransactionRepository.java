package io.swkoreatech.kosp.domain.point.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.point.model.PointTransaction;

/**
 * 포인트 거래 리포지토리.
 * 포인트 거래 내역의 저장 및 조회 기능을 제공한다.
 */
public interface PointTransactionRepository extends Repository<PointTransaction, Long> {

    /** 포인트 거래를 저장한다. */
    PointTransaction save(PointTransaction pointTransaction);

    /** 사용자의 포인트 거래 내역을 생성일시 내림차순 페이지 조회한다. */
    Page<PointTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /** 사용자의 포인트 거래 내역을 생성일시 내림차순 조회한다. */
    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);

    /** 사용자의 가장 최근 포인트 거래를 조회한다. */
    Optional<PointTransaction> findFirstByUserOrderByCreatedAtDesc(User user);
}
