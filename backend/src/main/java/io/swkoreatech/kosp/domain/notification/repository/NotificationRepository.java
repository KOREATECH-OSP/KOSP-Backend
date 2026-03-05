package io.swkoreatech.kosp.domain.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.domain.notification.model.Notification;

/**
 * 알림 리포지토리.
 * 알림의 저장, 조회, 읽음 처리, 삭제 기능을 제공한다.
 */
public interface NotificationRepository extends CrudRepository<Notification, Long> {

    /** 사용자 ID로 알림 목록을 생성일시 내림차순 조회한다. */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /** 사용자 ID로 읽지 않은 알림 목록을 생성일시 내림차순 조회한다. */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /** 사용자 ID로 읽지 않은 알림 수를 조회한다. */
    long countByUserIdAndIsReadFalse(Long userId);

    /** 사용자의 모든 읽지 않은 알림을 읽음 처리한다. */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    /** 사용자의 모든 알림을 삭제한다. */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.id = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    /**
     * ID로 알림을 조회하고, 없으면 예외를 발생시킨다.
     *
     * @param id 알림 ID
     * @return 알림
     * @throws GlobalException 알림을 찾을 수 없는 경우
     */
    default Notification getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
