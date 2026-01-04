package kr.ac.koreatech.sw.kosp.domain.github.mongo.repository;

import kr.ac.koreatech.sw.kosp.domain.github.mongo.model.GithubTrend;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface GithubTrendRepository extends Repository<GithubTrend, String> {
    GithubTrend save(GithubTrend trend);
    List<GithubTrend> findByGithubIdOrderByPeriodAsc(Long githubId);
}
