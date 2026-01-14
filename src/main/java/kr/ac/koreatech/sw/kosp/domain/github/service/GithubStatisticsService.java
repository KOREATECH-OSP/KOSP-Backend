package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ActivityTimelineResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionOverviewResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.ContributionPatternResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubContributionComparisonResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubContributionScoreResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubMonthlyActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubOverallHistoryResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentActivityResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubRecentContributionsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GithubSummaryResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.GlobalStatisticsResponse;
// import kr.ac.koreatech.sw.kosp.domain.github.dto.response.LanguageDistributionResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.RepositoryStatsResponse;
import kr.ac.koreatech.sw.kosp.domain.github.dto.response.YearlyAnalysisResponse;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubGlobalStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubContributionPattern;
// import kr.ac.koreatech.sw.kosp.domain.github.model.GithubLanguageStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubMonthlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubYearlyStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubTimelineData;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubTimelineDataRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubContributionPatternRepository;
// import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubLanguageStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubMonthlyStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubGlobalStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubYearlyStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.user.model.User;
import kr.ac.koreatech.sw.kosp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubStatisticsService {

    private final UserStatisticsCalculator userStatisticsCalculator;
    private final MonthlyStatisticsCalculator monthlyStatisticsCalculator;
    private final RepositoryStatisticsCalculator repositoryStatisticsCalculator;
    private final ContributionPatternCalculator contributionPatternCalculator;
    private final YearlyStatisticsCalculator yearlyStatisticsCalculator;
    // private final LanguageStatisticsCalculator languageStatisticsCalculator;
    private final GithubScoreCalculator scoreCalculator;
    
    private final UserRepository userRepository;
    private final GithubUserStatisticsRepository userStatisticsRepository;
    private final GithubMonthlyStatisticsRepository monthlyStatisticsRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;
    private final GithubContributionPatternRepository contributionPatternRepository;
    private final GithubYearlyStatisticsRepository yearlyStatisticsRepository;
    private final GithubGlobalStatisticsRepository globalStatisticsRepository;
    // private final GithubLanguageStatisticsRepository languageStatisticsRepository;
    private final GithubTimelineDataRepository timelineDataRepository;
    
    private final ObjectMapper objectMapper;

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
                statistics.getTotalForksReceived(),
                statistics.getNightCommits(),
                statistics.getDayCommits()
            );
            existing.updateDataPeriod(statistics.getDataPeriodStart(), statistics.getDataPeriodEnd());
            existing.updateDetailedScore(
                statistics.getMainRepoScore(),
                statistics.getOtherRepoScore(),
                statistics.getPrIssueScore(),
                statistics.getReputationScore()
            );
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
     * 사용자의 모든 통계 계산 및 저장 (전체 + 월별 + 저장소별 + 기여 패턴 + 연도별 + 언어 + 점수)
     */
    @Transactional
    public void calculateAndSaveAllStatistics(String githubId) {
        log.info("========================================");
        log.info("Starting comprehensive statistics calculation for user: {}", githubId);
        
        try {
            // 1. 사용자 전체 통계
            log.info("[1/7] Calculating user statistics...");
            calculateAndSaveUserStatistics(githubId);
            
            // 2. 월별 통계
            log.info("[2/7] Calculating monthly statistics...");
            calculateAndSaveMonthlyStatistics(githubId);
            
            // 3. 저장소별 통계
            log.info("[3/7] Calculating repository statistics...");
            calculateAndSaveRepositoryStatistics(githubId);
            
            // 4. 기여 패턴 분석
            log.info("[4/7] Calculating contribution pattern...");
            GithubContributionPattern pattern = contributionPatternCalculator.calculate(githubId);
            contributionPatternRepository.save(pattern);
            log.info("Contribution pattern saved: Night Owl={}, Initiator={}, Independent={}", 
                pattern.getNightOwlScore(), pattern.getInitiatorScore(), pattern.getIndependentScore());
            
            // 5. 연도별 통계
            log.info("[5/7] Calculating yearly statistics...");
            List<GithubYearlyStatistics> yearlyStats = yearlyStatisticsCalculator.calculateAll(githubId);
            yearlyStatisticsRepository.saveAll(yearlyStats);
            log.info("Saved {} yearly statistics", yearlyStats.size());
            
            // 6. 언어 분포 통계
            log.info("[6/7] Skipping language statistics (Deprecated)...");
            // List<GithubLanguageStatistics> langStats = languageStatisticsCalculator.calculate(githubId);
            // languageStatisticsRepository.saveAll(langStats);
            // log.info("Saved {} language statistics", langStats.size());
            
            // 7. 점수 계산 및 업데이트
            log.info("[7/7] Calculating and updating scores...");
            calculateAndUpdateScore(githubId);
            
            log.info("✅ All statistics calculated and saved successfully for user: {}", githubId);
            log.info("========================================");
        } catch (Exception e) {
            log.error("❌ Failed to calculate all statistics for user: {}", githubId, e);
            log.error("========================================");
            throw e;
        }
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

    // ==================== REST API Query Methods ====================

    /**
     * 사용자 GitHub 통계 요약 조회
     */
    @Transactional(readOnly = true)
    public GithubSummaryResponse getSummary(Long userId) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();
        
        GithubUserStatistics stats = userStatisticsRepository.findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("GitHub 통계를 찾을 수 없습니다."));

        return GithubSummaryResponse.builder()
            .githubId(stats.getGithubId())
            .totalCommits(stats.getTotalCommits())
            .totalLines(stats.getTotalLines())
            .totalAdditions(stats.getTotalAdditions())
            .totalDeletions(stats.getTotalDeletions())
            .totalPrs(stats.getTotalPrs())
            .totalIssues(stats.getTotalIssues())
            .ownedReposCount(stats.getOwnedReposCount())
            .contributedReposCount(stats.getContributedReposCount())
            .totalStarsReceived(stats.getTotalStarsReceived())
            .totalScore(stats.getTotalScore())
            .calculatedAt(stats.getCalculatedAt())
            .dataPeriodStart(stats.getDataPeriodStart())
            .dataPeriodEnd(stats.getDataPeriodEnd())
            .build();
    }

    /**
     * 사용자 월별 활동 추이 조회
     */
    @Transactional(readOnly = true)
    public GithubMonthlyActivityResponse getMonthlyActivity(
        Long userId,
        Integer startYear,
        Integer startMonth,
        Integer endYear,
        Integer endMonth
    ) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        YearMonth start = calculateStartYearMonth(startYear, startMonth);
        YearMonth end = calculateEndYearMonth(endYear, endMonth);

        List<GithubMonthlyStatistics> statistics = monthlyStatisticsRepository
            .findByGithubIdAndYearMonthBetween(
                githubId,
                start.getYear(), start.getMonthValue(),
                end.getYear(), end.getMonthValue()
            );

        List<GithubMonthlyActivityResponse.MonthlyActivity> activities = statistics.stream()
            .map(stat -> GithubMonthlyActivityResponse.MonthlyActivity.builder()
                .year(stat.getYear())
                .month(stat.getMonth())
                .commitsCount(stat.getCommitsCount())
                .linesCount(stat.getLinesCount())
                .prsCount(stat.getPrsCount())
                .issuesCount(stat.getIssuesCount())
                .build())
            .collect(Collectors.toList());

        return GithubMonthlyActivityResponse.builder()
            .activities(activities)
            .build();
    }

    /**
     * 사용자 최근 기여 활동 조회
     */
    @Transactional(readOnly = true)
    public GithubRecentContributionsResponse getRecentContributions(Long userId, Integer limit) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        List<GithubRepositoryStatistics> repositories = repositoryStatisticsRepository
            .findTopNByContributorGithubIdOrderByLastCommitDateDesc(githubId, limit);

        List<GithubRecentContributionsResponse.RecentRepository> recentRepos = repositories.stream()
            .map(repo -> GithubRecentContributionsResponse.RecentRepository.builder()
                .repoOwner(repo.getRepoOwner())
                .repoName(repo.getRepoName())
                .stargazersCount(repo.getStargazersCount())
                .userCommitsCount(repo.getUserCommitsCount())
                .userPrsCount(repo.getUserPrsCount())
                .userIssuesCount(repo.getUserIssuesCount())
                .lastCommitDate(repo.getLastCommitDate())
                .primaryLanguage(repo.getPrimaryLanguage())
                .build())
            .collect(Collectors.toList());

        return GithubRecentContributionsResponse.builder()
            .repositories(recentRepos)
            .build();
    }

    // ==================== Private Helper Methods ====================

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private YearMonth calculateStartYearMonth(Integer startYear, Integer startMonth) {
        if (startYear != null && startMonth != null) {
            return YearMonth.of(startYear, startMonth);
        }
        return YearMonth.now().minusYears(1);
    }

    private YearMonth calculateEndYearMonth(Integer endYear, Integer endMonth) {
        if (endYear != null && endMonth != null) {
            return YearMonth.of(endYear, endMonth);
        }
        return YearMonth.now();
    }

    /**
     * 전체 기여 내역 요약 조회
     */
    @Transactional(readOnly = true)
    public ContributionOverviewResponse getContributionOverview(Long userId) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        GithubUserStatistics statistics = userStatisticsRepository.findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("통계 정보를 찾을 수 없습니다."));

        return ContributionOverviewResponse.from(statistics);
    }

    /**
     * 기여 성향 분석 조회
     */
    @Transactional(readOnly = true)
    public ContributionPatternResponse getContributionPattern(Long userId) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        GithubContributionPattern pattern = contributionPatternRepository.findByGithubId(githubId)
            .orElseGet(() -> {
                // 캐시 없으면 계산
                GithubContributionPattern calculated = contributionPatternCalculator.calculate(githubId);
                return contributionPatternRepository.save(calculated);
            });

        return ContributionPatternResponse.from(pattern, objectMapper);
    }

    /**
     * 연도별 분석 조회
     */
    @Transactional(readOnly = true)
    public YearlyAnalysisResponse getYearlyAnalysis(Long userId, int year) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        GithubYearlyStatistics statistics = yearlyStatisticsRepository.findByGithubIdAndYear(githubId, year)
            .orElseGet(() -> {
                // 캐시 없으면 계산
                GithubYearlyStatistics calculated = yearlyStatisticsCalculator.calculate(githubId, year);
                return yearlyStatisticsRepository.save(calculated);
            });

        // Best 저장소 통계 조회
        GithubRepositoryStatistics bestRepoStats = null;
        if (statistics.getBestRepoOwner() != null && statistics.getBestRepoName() != null) {
            bestRepoStats = repositoryStatisticsRepository
                .findByRepoOwnerAndRepoNameAndContributorGithubId(
                    statistics.getBestRepoOwner(),
                    statistics.getBestRepoName(),
                    githubId
                )
                .orElse(null);
        }

        return YearlyAnalysisResponse.from(statistics, bestRepoStats);
    }

    /**
     * 저장소별 통계 조회
     */
    @Transactional(readOnly = true)
    public RepositoryStatsResponse getRepositoryStats(Long userId, int limit, String sortBy) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        // 저장소 통계 조회
        List<GithubRepositoryStatistics> repositories = repositoryStatisticsRepository
            .findByContributorGithubId(githubId);

        // 정렬
        repositories = repositories.stream()
            .sorted((a, b) -> {
                return switch (sortBy.toLowerCase()) {
                    case "stars" -> Integer.compare(
                        b.getStargazersCount() != null ? b.getStargazersCount() : 0,
                        a.getStargazersCount() != null ? a.getStargazersCount() : 0
                    );
                    case "lines" -> {
                        int aLines = (a.getUserCommitsCount() != null ? a.getUserCommitsCount() : 0) * 100;
                        int bLines = (b.getUserCommitsCount() != null ? b.getUserCommitsCount() : 0) * 100;
                        yield Integer.compare(bLines, aLines);
                    }
                    default -> Integer.compare(
                        b.getUserCommitsCount() != null ? b.getUserCommitsCount() : 0,
                        a.getUserCommitsCount() != null ? a.getUserCommitsCount() : 0
                    );
                };
            })
            .limit(limit)
            .toList();

        // 메인 저장소 키 추출 (상위 3개)
        List<String> mainRepoKeys = repositories.stream()
            .limit(3)
            .map(r -> r.getRepoOwner() + "/" + r.getRepoName())
            .toList();

        return RepositoryStatsResponse.from(repositories, mainRepoKeys);
    }

    /**
     * 언어 분포 조회 (Deprecated)
     */
    @Transactional(readOnly = true)
    public void getLanguageDistribution(Long userId) {
        // User user = findUserById(userId);
        // String githubId = user.getGithubUser().getGithubLogin();

        // List<GithubLanguageStatistics> languageStats = languageStatisticsRepository
        //     .findByGithubIdOrderByLinesOfCodeDesc(githubId);

        // if (languageStats.isEmpty()) {
        //     // 캐시 없으면 계산
        //     languageStats = languageStatisticsCalculator.calculate(githubId);
        //     languageStats = languageStatisticsRepository.saveAll(languageStats);
        // }

        // return LanguageDistributionResponse.from(languageStats);
        throw new UnsupportedOperationException("Language statistics are deprecated.");
    }

    /**
     * 활동 타임라인 조회
     */
    @Transactional(readOnly = true)
    public ActivityTimelineResponse getActivityTimeline(Long userId, int limit, int days) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        // 타임라인 데이터 조회 (전체)
        List<GithubTimelineData> timelineData = timelineDataRepository
            .findByGithubId(githubId);

        ActivityTimelineResponse response = ActivityTimelineResponse.from(timelineData);
        
        // limit 적용
        if (response.timeline().size() > limit) {
            List<ActivityTimelineResponse.TimelineItem> limitedItems = response.timeline()
                .stream()
                .limit(limit)
                .toList();
            return new ActivityTimelineResponse(limitedItems, limitedItems.size());
        }
        
        return response;
    }

    /**
     * 1. 최근 기여활동 조회
     */
    @Transactional(readOnly = true)
    public List<GithubRecentActivityResponse> getRecentActivityDetails(Long userId) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        List<GithubRepositoryStatistics> repoStats = repositoryStatisticsRepository
            .findByContributorGithubId(githubId);

        return repoStats.stream()
            .sorted((a, b) -> b.getLastCommitDate().compareTo(a.getLastCommitDate()))
            .limit(10) // Reasonable limit matching spec intent
            .map(GithubRecentActivityResponse::from)
            .collect(Collectors.toList());
    }

    /**
     * 2. 전체 기여 내역 조회
     */
    @Transactional(readOnly = true)
    public GithubOverallHistoryResponse getOverallHistoryDetails(Long userId) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        GithubUserStatistics userStats = userStatisticsRepository.findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("GitHub 통계를 찾을 수 없습니다."));

        return GithubOverallHistoryResponse.from(userStats);
    }

    /**
     * 3. 기여내역 비교 조회
     */
    @Transactional(readOnly = true)
    public GithubContributionComparisonResponse getComparisonDetails(Long userId) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        GithubUserStatistics userStats = userStatisticsRepository.findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("GitHub 통계를 찾을 수 없습니다."));

        GithubGlobalStatistics globalStats = globalStatisticsRepository.findTopByOrderByCalculatedAtDesc()
            .orElse(null);

        return new GithubContributionComparisonResponse(
            globalStats != null ? globalStats.getAvgCommitCount() : 0.0,
            globalStats != null ? globalStats.getAvgStarCount() : 0.0,
            globalStats != null ? globalStats.getAvgPrCount() : 0.0,
            globalStats != null ? globalStats.getAvgIssueCount() : 0.0,
            userStats.getTotalCommits(),
            userStats.getTotalStarsReceived(),
            userStats.getTotalPrs(),
            userStats.getTotalIssues()
        );
    }

    /**
     * 4. GitHub 기여점수 조회
     */
    @Transactional(readOnly = true)
    public GithubContributionScoreResponse getScoreDetails(Long userId) {
        User user = findUserById(userId);
        String githubId = user.getGithubUser().getGithubLogin();

        GithubUserStatistics userStats = userStatisticsRepository.findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("GitHub 통계를 찾을 수 없습니다."));

        return GithubContributionScoreResponse.from(userStats);
    }

    /**
     * 전체 사용자 평균 통계 조회
     */
    @Transactional(readOnly = true)
    public GlobalStatisticsResponse getGlobalStatistics() {
        return globalStatisticsRepository.findTopByOrderByCalculatedAtDesc()
            .map(GlobalStatisticsResponse::from)
            .orElseGet(() -> GlobalStatisticsResponse.from(null));
    }
}
