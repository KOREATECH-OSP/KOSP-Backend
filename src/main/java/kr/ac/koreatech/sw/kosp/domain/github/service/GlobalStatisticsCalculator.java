package kr.ac.koreatech.sw.kosp.domain.github.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubGlobalStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubGlobalStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GlobalStatisticsCalculator {

    private final GithubUserStatisticsRepository userStatisticsRepository;
    private final GithubGlobalStatisticsRepository globalStatisticsRepository;

    @Transactional
    public void calculateAndSave() {
        log.info("Starting global statistics calculation...");

        Object[] result = userStatisticsRepository.getGlobalAverages();

        if (result == null || result.length == 0 || result[0] == null) {
            log.warn("No user statistics available to calculate global averages.");
            return;
        }

        Object[] stats = (Object[]) result[0];
        
        // JPQL AVG returns Double, COUNT returns Long
        Double avgCommits = stats[0] != null ? (Double) stats[0] : 0.0;
        Double avgPrs = stats[1] != null ? (Double) stats[1] : 0.0;
        Double avgIssues = stats[2] != null ? (Double) stats[2] : 0.0;
        Double avgStars = stats[3] != null ? (Double) stats[3] : 0.0;
        Long count = stats[4] != null ? (Long) stats[4] : 0L;

        GithubGlobalStatistics globalStats = GithubGlobalStatistics.create(
            avgCommits,
            avgStars,
            avgPrs,
            avgIssues,
            count.intValue()
        );

        globalStatisticsRepository.save(globalStats);

        log.info("Global statistics calculated and saved. Users: {}, Commits: {}, Stars: {}, PRs: {}, Issues: {}",
            count, avgCommits, avgStars, avgPrs, avgIssues);
    }
}
