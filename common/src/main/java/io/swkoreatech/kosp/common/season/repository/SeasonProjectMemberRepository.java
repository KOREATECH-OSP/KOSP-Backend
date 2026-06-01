package io.swkoreatech.kosp.common.season.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
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

    default SeasonProjectMember getById(Long id) {
        return findById(id)
            .orElseThrow(() -> new GlobalException(ExceptionMessage.SEASON_PROJECT_MEMBER_NOT_FOUND));
    }
}
