package kr.ac.koreatech.sw.kosp.domain.github.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubUserStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubPRRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubPRRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubUserStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubScoreCalculator {

    private final GithubUserStatisticsRepository userStatisticsRepository;
    private final GithubRepositoryStatisticsRepository repoStatisticsRepository;
    private final GithubPRRawRepository prRawRepository;

    public BigDecimal calculate(String githubId) {
        log.info("Calculating GitHub score for user: {}", githubId);

        // 1. 사용자 통계 조회
        GithubUserStatistics userStats = userStatisticsRepository
            .findByGithubId(githubId)
            .orElseThrow(() -> new IllegalArgumentException("User statistics not found: " + githubId));

        // 2. 저장소 통계 조회
        List<GithubRepositoryStatistics> repoStats = repoStatisticsRepository
            .findByContributorGithubId(githubId);

        // 3. 활동 수준 계산 (최대 3점) - Repository 기준 Bucket
        double activityLevel = calculateActivityLevel(repoStats);
        log.debug("Activity level score: {}", activityLevel);

        // 4. 활동 다양성 계산 (최대 1점) - Repo Count Bucket
        double diversity = calculateDiversity(userStats);
        log.debug("Diversity score: {}", diversity);

        // 5. 활동 영향성 계산 (최대 5점) - Impact Bonus
        double impact = calculateImpact(githubId, userStats, repoStats);
        log.debug("Impact score: {}", impact);

        // 총점 계산 (각 항목은 이미 Max Cap이 적용되어 있음)
        double totalScore = activityLevel + diversity + impact;

        // 세부 점수 업데이트
        userStats.updateDetailedScore(
            BigDecimal.valueOf(activityLevel),
            BigDecimal.valueOf(diversity),
            BigDecimal.valueOf(impact),
            BigDecimal.ZERO // Reputation Score (Reserved)
        );
        userStatisticsRepository.save(userStats);

        log.info("Total score for {}: {} (activity: {}, diversity: {}, impact: {})",
            githubId, totalScore, activityLevel, diversity, impact);

        return BigDecimal.valueOf(totalScore)
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 4.1 활동 수준 (최대 3점)
     * 기여한 저장소 중 가장 높은 점수를 적용한다.
     * 3점 | 커밋 100회 이상 AND PR 20회 이상
     * 2점 | 커밋 30회 이상 AND PR 5회 이상
     * 1점 | 커밋 5회 이상 OR PR 1회 이상
     */
    private double calculateActivityLevel(List<GithubRepositoryStatistics> repoStats) {
        double maxScore = 0.0;

        for (GithubRepositoryStatistics repo : repoStats) {
            int commits = repo.getUserCommitsCount() != null ? repo.getUserCommitsCount() : 0;
            int prs = repo.getUserPrsCount() != null ? repo.getUserPrsCount() : 0;

            if (commits >= 100 && prs >= 20) {
                return 3.0; // 최고 점수 도달 시 즉시 리턴
            } else if (commits >= 30 && prs >= 5) {
                maxScore = Math.max(maxScore, 2.0);
            } else if (commits >= 5 || prs >= 1) {
                maxScore = Math.max(maxScore, 1.0);
            }
        }

        return maxScore;
    }

    /**
     * 4.2 활동 다양성 (최대 1점)
     * 기여한 저장소 개수를 기준으로 산출한다.
     * 1.0점 | 기여 저장소 10개 이상
     * 0.7점 | 기여 저장소 5~9개
     * 0.4점 | 기여 저장소 2~4개
     */
    private double calculateDiversity(GithubUserStatistics userStats) {
        int repoCount = userStats.getContributedReposCount();

        if (repoCount >= 10) {
            return 1.0;
        } else if (repoCount >= 5) {
            return 0.7;
        } else if (repoCount >= 2) {
            return 0.4;
        } else {
            return 0.0;
        }
    }

    /**
     * 4.3 활동 영향성 (최대 5점, 보너스)
     * +2점   | 본인 소유 저장소 중 스타 100개 이상 보유
     * +1.5점 | 스타 1,000개 이상 외부 저장소에 PR 병합
     * +1점   | 본인 PR로 해결된 이슈 10개 이상 (To be implemented)
     * +0.5점 | 포크한 저장소에 원본 PR 병합 (To be implemented)
     */
    private double calculateImpact(String githubId, GithubUserStatistics userStats, List<GithubRepositoryStatistics> repoStats) {
        double impactScore = 0.0;

        // 1. 본인 소유 저장소 중 스타 100개 이상 (+2.0)
        boolean hasPopularOwnedRepo = repoStats.stream()
            .anyMatch(repo -> 
                Boolean.TRUE.equals(repo.getIsOwned()) && 
                repo.getStargazersCount() != null && 
                repo.getStargazersCount() >= 100
            );
        
        if (hasPopularOwnedRepo) {
            impactScore += 2.0;
        }

        // 2. 스타 1,000개 이상 외부 저장소에 PR 병합 (+1.5)
        // 외부 저장소(Not Owned)에 대한 Merged PR 조회
        // 이를 위해 GithubPRRaw를 뒤져야 함.
        if (hasMergedPrToPopularExternalRepo(githubId)) {
            impactScore += 1.5;
        }

        // 3. 본인 PR로 해결된 이슈 10개 이상 (+1.0)
        // TODO: Closing Issues 수집 로직 추가 후 구현
        
        // 4. 포크한 저장소에 원본 PR 병합 (+0.5)
        // TODO: Fork 여부 확인 로직 정교화 후 구현

        return Math.min(impactScore, 5.0);
    }

    private boolean hasMergedPrToPopularExternalRepo(String githubId) {
        try {
            // 이 유저가 작성한 모든 PR 조회
            // 주의: 성능 이슈가 있을 수 있으므로 추후 최적화 필요 (Aggregation 등)
            // 현재는 단순 구현: 모든 PR을 가져와서 필터링
            // 실제로는 Repository 레벨에서 필터링 쿼리를 짜는 게 좋음
            List<GithubPRRaw> userPrs = prRawRepository.findByAuthorLogin(githubId)
                .collectList()
                .block();
            
            if (userPrs == null) return false;
            
            return userPrs.stream().anyMatch(pr -> {
                // 1. Merged 상태인지 확인
                Map<String, Object> prData = pr.getPrData();
                if (prData == null) return false;
                
                String state = (String) prData.get("state");
                boolean isMerged = "MERGED".equalsIgnoreCase(state) || 
                                   (prData.get("merged") != null && (Boolean) prData.get("merged"));
                
                if (!isMerged) return false;

                // 2. Base Repo의 스타 수가 1000개 이상인지 확인
                // 구조: base -> repo -> stargazers_count
                Map<String, Object> base = (Map<String, Object>) prData.get("base");
                if (base == null) return false;
                
                Map<String, Object> repo = (Map<String, Object>) base.get("repo");
                if (repo == null) return false;
                
                Integer stars = (Integer) repo.get("stargazers_count");
                
                // 3. 외부 저장소인지 확인 (내 소유가 아님)
                Map<String, Object> owner = (Map<String, Object>) repo.get("owner");
                String ownerLogin = (String) owner.get("login");
                boolean isExternal = !githubId.equalsIgnoreCase(ownerLogin);
                
                return isExternal && stars != null && stars >= 1000;
            });
            
        } catch (Exception e) {
            log.error("Error calculating external repo impact for user: {}", githubId, e);
            return false;
        }
    }
}
