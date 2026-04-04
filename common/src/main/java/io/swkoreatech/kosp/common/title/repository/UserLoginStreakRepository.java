package io.swkoreatech.kosp.common.title.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.title.model.UserLoginStreak;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link UserLoginStreak} 엔티티 데이터 접근 레포지토리.
 */
public interface UserLoginStreakRepository extends Repository<UserLoginStreak, Long> {

    UserLoginStreak save(UserLoginStreak streak);

    /**
     * 유저의 로그인 streak 정보를 조회한다.
     */
    Optional<UserLoginStreak> findByUser(User user);
}
