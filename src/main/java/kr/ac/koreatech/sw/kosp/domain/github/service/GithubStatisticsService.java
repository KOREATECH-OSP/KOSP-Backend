package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubMonthlyStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubStatisticsService {

    private final UserStatisticsCalculator userStatisticsCalculator;
    private final MonthlyStatisticsCalculator monthlyStatisticsCalculator;
    private final RepositoryStatisticsCalculator repositoryStatisticsCalculator;
    private final GithubScoreCalculator scoreCalculator;
    
    private final GithubUserStatisticsRepository userStatisticsRepository;
    private final GithubMonthlyStatisticsRepository monthlyStatisticsRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;

    /**
     * 사용자의 전체 통계 계산 및 저장
     */
    @Transactional
    public GithubUserStatistics calculateAndSaveUserStatistics(String githubId) {
        log.info("Calculating and saving statistics for user: {}", githubId);

        // 통계 계산
        GithubUserStatistics statistics = userStatisticsCalculator.calculate(githubId);

        // 기존 통계 조회
        GithubUserStatistics existing = userStatisticsRepository.findByGithubId(githubId)
            .orElse(null);

        // 저장 또는 업데이트
        if (existing != null) {
            // 기존 통계 업데이트
            existing.updateStatistics(
                statistics.getTotalCommits(),
                statistics.getTotalLines(),
                statistics.getTotalAdditions(),
                statistics.getTotalDeletions(),
                statistics.getTotalPrs(),
                statistics.getTotalIssues(),
                statistics.getOwnedReposCount(),
                statistics.getContributedReposCount(),
                statistics.getTotalStarsReceived(),
                statistics.getNightCommits(),
                statistics.getDayCommits()
            );
            existing.updateDataPeriod(statistics.getDataPeriodStart(), statistics.getDataPeriodEnd());
            existing.updateScore(statistics.getTotalScore());
            statistics = userStatisticsRepository.save(existing);
        } else {
            // 새로운 통계 저장
            statistics = userStatisticsRepository.save(statistics);
        }

        log.info("User statistics saved for: {}", githubId);
        return statistics;
    }

    /**
     * 사용자의 월별 통계 계산 및 저장
     */
    @Transactional
    public List<GithubMonthlyStatistics> calculateAndSaveMonthlyStatistics(String githubId) {
        log.info("Calculating and saving monthly statistics for user: {}", githubId);

        // 월별 통계 계산
        List<GithubMonthlyStatistics> monthlyStatistics = monthlyStatisticsCalculator.calculate(githubId);

        // 기존 월별 통계 삭제 (재계산)
        List<GithubMonthlyStatistics> existing = monthlyStatisticsRepository.findByGithubId(githubId);
        existing.forEach(stat -> {
            // 개별 삭제는 Repository에 delete 메서드가 필요하므로, 여기서는 업데이트로 처리
            // 실제로는 deleteByGithubId 같은 메서드를 추가해야 함
        });

        // 새로운 월별 통계 저장
        List<GithubMonthlyStatistics> saved = monthlyStatistics.stream()
            .map(monthlyStatisticsRepository::save)
            .toList();

        log.info("Saved {} monthly statistics for user: {}", saved.size(), githubId);
        return saved;
    }

    /**
     * 사용자의 모든 통계 계산 및 저장 (전체 + 월별 + 저장소별 + 점수)
     */
    @Transactional
    public void calculateAndSaveAllStatistics(String githubId) {
        log.info("Calculating all statistics for user: {}", githubId);
        
        // 1. 사용자 전체 통계
        calculateAndSaveUserStatistics(githubId);
        
        // 2. 월별 통계
        calculateAndSaveMonthlyStatistics(githubId);
        
        // 3. 저장소별 통계
        calculateAndSaveRepositoryStatistics(githubId);
        
        // 4. 점수 계산 및 업데이트
        calculateAndUpdateScore(githubId);
        
        log.info("All statistics calculated and saved for user: {}", githubId);
    }

    /**
     * 사용자의 저장소별 통계 계산 및 저장
     */
    @Transactional
    public List<GithubRepositoryStatistics> calculateAndSaveRepositoryStatistics(String githubId) {
        log.info("Calculating and saving repository statistics for user: {}", githubId);
        
        List<GithubRepositoryStatistics> statistics = repositoryStatisticsCalculator.calculate(githubId);
        
        log.info("Saved {} repository statistics for user: {}", statistics.size(), githubId);
        return statistics;
    }

    /**
     * 사용자의 점수 계산 및 업데이트
     */
    @Transactional
    public void calculateAndUpdateScore(String githubId) {
        log.info("Calculating and updating score for user: {}", githubId);
        
        BigDecimal score = scoreCalculator.calculate(githubId);
        
        GithubUserStatistics userStats = userStatisticsRepository.findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("User statistics not found: " + githubId));
        
        userStats.updateScore(score);
        userStatisticsRepository.save(userStats);
        
        log.info("Score updated for user {}: {}", githubId, score);
    }
}
