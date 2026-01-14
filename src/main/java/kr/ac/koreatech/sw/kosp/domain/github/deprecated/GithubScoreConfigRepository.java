package kr.ac.koreatech.sw.kosp.domain.github.deprecated;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.ac.koreatech.sw.kosp.domain.github.deprecated.GithubScoreConfig;

public interface GithubScoreConfigRepository extends Repository<GithubScoreConfig, Long> {

    GithubScoreConfig save(GithubScoreConfig config);

    Optional<GithubScoreConfig> findById(Long id);

    Optional<GithubScoreConfig> findByActive(Boolean active);

    Optional<GithubScoreConfig> findByConfigName(String configName);

    List<GithubScoreConfig> findAll();

    boolean existsByConfigName(String configName);
}
