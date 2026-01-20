package kr.ac.koreatech.sw.kosp.domain.community.team.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.community.team.model.Team;
import kr.ac.koreatech.sw.kosp.global.exception.ExceptionMessage;
import kr.ac.koreatech.sw.kosp.global.exception.GlobalException;

public interface TeamRepository extends Repository<Team, Long>, JpaSpecificationExecutor<Team> {

    Team save(Team team);
    Optional<Team> findById(Long id);
    Page<Team> findByNameContaining(String name, Pageable pageable);
    java.util.List<Team> findByNameContaining(String name);

    default Team getById(Long id) {
        return findById(id).orElseThrow(() -> new GlobalException(ExceptionMessage.NOT_FOUND));
    }

    Page<Team> findAll(Specification<Team> spec, Pageable pageable);
}
