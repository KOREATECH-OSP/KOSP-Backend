package kr.ac.koreatech.sw.kosp.domain.admin.point.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.ac.koreatech.sw.kosp.domain.admin.point.model.PointTransaction;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    Page<PointTransaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);

    Optional<PointTransaction> findFirstByUserOrderByCreatedAtDesc(User user);
}
