package io.swkoreatech.kosp.common.title.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.title.model.Title;
import io.swkoreatech.kosp.common.title.model.UserTitle;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link UserTitle} 엔티티 데이터 접근 레포지토리.
 */
public interface UserTitleRepository extends Repository<UserTitle, Long> {

    UserTitle save(UserTitle userTitle);

    Optional<UserTitle> findById(Long id);

    /**
     * 유저의 회수되지 않은 칭호 목록을 조회한다.
     */
    List<UserTitle> findAllByUserAndIsRevokedFalse(User user);

    /**
     * 유저의 특정 칭호 보유 여부를 확인한다 (회수 포함).
     */
    Optional<UserTitle> findByUserAndTitleAndIsRevokedFalse(User user, Title title);

    /**
     * 유저의 현재 대표 칭호를 조회한다.
     */
    Optional<UserTitle> findByUserAndIsDisplayTrueAndIsRevokedFalse(User user);

    /**
     * 유저의 대표 칭호 해제 대상 목록을 조회한다 (is_display=true 이면서 특정 id가 아닌 것).
     * 트랜잭션 내 대표 칭호 단일 보장에 사용한다.
     */
    List<UserTitle> findAllByUserAndIsDisplayTrueAndIsRevokedFalse(User user);

    /**
     * 여러 유저의 대표 칭호를 한 번에 조회한다 (목록 화면 N+1 방지용).
     *
     * @param userIds 대상 유저 ID 목록
     * @return 회수되지 않은 대표 칭호 목록 (칭호 마스터 페치)
     */
    @Query("SELECT ut FROM UserTitle ut JOIN FETCH ut.title "
        + "WHERE ut.user.id IN :userIds AND ut.isDisplay = true AND ut.isRevoked = false")
    List<UserTitle> findAllDisplayTitlesByUserIds(@Param("userIds") Collection<Long> userIds);

    default UserTitle getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.USER_TITLE_NOT_FOUND));
    }
}
