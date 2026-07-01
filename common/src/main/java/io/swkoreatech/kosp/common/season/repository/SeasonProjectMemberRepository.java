package io.swkoreatech.kosp.common.season.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonProject;
import io.swkoreatech.kosp.common.season.model.SeasonProjectMember;
import io.swkoreatech.kosp.common.user.model.User;

/**
 * {@link SeasonProjectMember} 엔티티 리포지토리.
 */
public interface SeasonProjectMemberRepository extends Repository<SeasonProjectMember, Long> {

    SeasonProjectMember save(SeasonProjectMember member);

    Optional<SeasonProjectMember> findById(Long id);

    List<SeasonProjectMember> findAllByProject(SeasonProject project);

    List<SeasonProjectMember> findAllByProjectAndScoreGrantedFalse(SeasonProject project);

    boolean existsByProjectAndUser(SeasonProject project, User user);

    /**
     * 특정 시즌에서 유저가 완료(점수 지급 완료)한 {@code minLevel} 이상 레벨의 프로젝트 수.
     * <p>엘리트 티어(Master/Challenger) 조건 평가에 사용한다.</p>
     *
     * @param user     대상 유저
     * @param season   대상 시즌
     * @param minLevel 최소 프로젝트 레벨
     * @return 조건을 만족하는 완료 프로젝트 수
     */
    @Query("SELECT COUNT(m) FROM SeasonProjectMember m "
        + "WHERE m.user = :user AND m.scoreGranted = true "
        + "AND m.project.season = :season AND m.project.projectLevel >= :minLevel")
    long countCompletedProjectsByLevel(
        @Param("user") User user,
        @Param("season") Season season,
        @Param("minLevel") int minLevel
    );

    default SeasonProjectMember getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.SEASON_PROJECT_MEMBER_NOT_FOUND));
    }
}
