package io.swkoreatech.kosp.domain.point.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.domain.point.model.PointTransaction;
import io.swkoreatech.kosp.domain.user.model.User;

public interface PointTransactionRepository extends Repository<PointTransaction, Long> {

    PointTransaction save(PointTransaction pointTransaction);

    Page<PointTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);

    Optional<PointTransaction> findFirstByUserOrderByCreatedAtDesc(User user);
}
