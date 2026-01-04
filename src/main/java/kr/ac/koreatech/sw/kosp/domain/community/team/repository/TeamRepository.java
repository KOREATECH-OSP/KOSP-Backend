package kr.ac.koreatech.sw.kosp.domain.community.team.repository;

import java.util.Optional;
import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface TeamRepository extends Repository<Team, Long> {

    Team save(Team team);
    Optional<Team> findById(Long id);
    Page<Team> findByNameContaining(String name, Pageable pageable);

    default Team getById(Long id) {
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }
}
