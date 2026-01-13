package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubContributionPattern;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionPatternCalculator {

    private final GithubCommitDetailRawRepository commitDetailRawRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;
    private final ObjectMapper objectMapper;

    /**
     * 기여 패턴 분석
     */
    public GithubContributionPattern calculate(String githubId) {
        log.info("Calculating contribution pattern for user: {}", githubId);

        List<GithubCommitDetailRaw> commits = commitDetailRawRepository.findByAuthorLogin(githubId);

        GithubContributionPattern pattern = GithubContributionPattern.create(githubId);

        if (commits.isEmpty()) {
            log.warn("No commits found for user: {}", githubId);
            return pattern;
        }

        // 저장소 통계 조회
        List<GithubRepositoryStatistics> repoStats = repositoryStatisticsRepository
            .findByContributorGithubId(githubId);

        // 1. 시간대 패턴 분석
        analyzeTimePattern(pattern, commits);

        // 2. 프로젝트 패턴 분석
        analyzeProjectPattern(pattern, commits, repoStats);

        // 3. 협업 패턴 분석
        analyzeCollaborationPattern(pattern, repoStats, githubId);

        log.info("Contribution pattern calculated for {}: Night Owl: {}, Initiator: {}, Independent: {}, Coworkers: {}",
            githubId, pattern.getNightOwlScore(), pattern.getInitiatorScore(), 
            pattern.getIndependentScore(), pattern.getTotalCoworkers());

        return pattern;
    }

    /**
     * 시간대 패턴 분석
     */
    private void analyzeTimePattern(GithubContributionPattern pattern, List<GithubCommitDetailRaw> commits) {
        // 시간대별 커밋 수 집계
        Map<Integer, Long> hourlyDistribution = commits.stream()
            .filter(c -> c.getAuthorDate() != null)
            .collect(Collectors.groupingBy(
                c -> c.getAuthorDate().getHour(),
                Collectors.counting()
            ));

        // Night Owl 점수 계산 (22:00-06:00)
        int nightCommits = (int) commits.stream()
            .filter(c -> c.getAuthorDate() != null)
            .filter(c -> {
                int hour = c.getAuthorDate().getHour();
                return hour >= 22 || hour < 6;
            })
            .count();

        int dayCommits = commits.size() - nightCommits;
        int nightOwlScore = calculateNightOwlScore(nightCommits, dayCommits);

        // JSON 변환
        String hourlyJson = convertToJson(hourlyDistribution);

        pattern.updateTimePattern(nightOwlScore, nightCommits, dayCommits, hourlyJson);
    }

    /**
     * 프로젝트 패턴 분석
     */
    private void analyzeProjectPattern(
        GithubContributionPattern pattern,
        List<GithubCommitDetailRaw> commits,
        List<GithubRepositoryStatistics> repoStats
    ) {
        // 저장소별 그룹화
        Map<String, List<GithubCommitDetailRaw>> commitsByRepo = commits.stream()
            .collect(Collectors.groupingBy(c -> c.getRepoOwner() + "/" + c.getRepoName()));

        int totalProjects = commitsByRepo.size();

        // Initiator 점수: 저장소 생성 후 1개월 이내 기여한 프로젝트 비율
        int earlyContributions = 0;
        
        for (GithubRepositoryStatistics repo : repoStats) {
            if (repo.getRepoCreatedAt() == null) continue;
            
            LocalDateTime oneMonthLater = repo.getRepoCreatedAt().plusMonths(1);
            
            boolean hasEarlyCommits = commits.stream()
                .filter(c -> c.getRepoOwner().equals(repo.getRepoOwner()))
                .filter(c -> c.getRepoName().equals(repo.getRepoName()))
                .filter(c -> c.getAuthorDate() != null)
                .filter(c -> c.getAuthorDate().isBefore(oneMonthLater))
                .findAny()
                .isPresent();
            
            if (hasEarlyCommits) {
                earlyContributions++;
            }
        }
        
        int initiatorScore = totalProjects > 0 
            ? (int) ((double) earlyContributions / totalProjects * 100) 
            : 0;

        // Independent 점수: 사용자 커밋이 전체의 80% 이상인 저장소
        long soloProjects = repoStats.stream()
            .filter(r -> r.getUserCommitsCount() != null && r.getTotalCommitsCount() != null)
            .filter(r -> r.getTotalCommitsCount() > 0)
            .filter(r -> {
                double userRatio = (double) r.getUserCommitsCount() / r.getTotalCommitsCount();
                return userRatio >= 0.8;
            })
            .map(r -> r.getRepoOwner() + "/" + r.getRepoName())
            .distinct()
            .count();

        int independentScore = totalProjects > 0 ? (int) (soloProjects * 100 / totalProjects) : 0;

        pattern.updateProjectPattern(
            initiatorScore,
            earlyContributions,
            independentScore,
            (int) soloProjects,
            totalProjects
        );
    }

    /**
     * 협업 패턴 분석
     */
    private void analyzeCollaborationPattern(
        GithubContributionPattern pattern,
        List<GithubRepositoryStatistics> repoStats,
        String githubId
    ) {
        // 협업 저장소: 사용자 커밋이 전체의 80% 미만인 저장소
        long collaborativeRepos = repoStats.stream()
            .filter(r -> r.getUserCommitsCount() != null && r.getTotalCommitsCount() != null)
            .filter(r -> r.getTotalCommitsCount() > 0)
            .filter(r -> {
                double userRatio = (double) r.getUserCommitsCount() / r.getTotalCommitsCount();
                return userRatio < 0.8;
            })
            .count();

        // 협업자 수 추정: 협업 저장소 수 * 평균 기여자 수 (간단하게 3명으로 가정)
        int estimatedCoworkers = (int) (collaborativeRepos * 2); // 자신 제외 평균 2명

        pattern.updateCollaborationPattern(estimatedCoworkers);
    }

    /**
     * Night Owl 점수 계산
     */
    private int calculateNightOwlScore(int nightCommits, int dayCommits) {
        if (nightCommits + dayCommits == 0) {
            return 0;
        }
        double percentage = (double) nightCommits / (nightCommits + dayCommits) * 100;
        return (int) Math.min(100, percentage * 2.5);
    }

    /**
     * Map을 JSON으로 변환
     */
    private String convertToJson(Map<Integer, Long> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert map to JSON", e);
            return "{}";
        }
    }
}
