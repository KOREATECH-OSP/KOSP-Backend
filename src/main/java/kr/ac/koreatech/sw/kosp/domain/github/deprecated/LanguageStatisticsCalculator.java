package kr.ac.koreatech.sw.kosp.domain.github.deprecated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import kr.ac.koreatech.sw.kosp.domain.github.deprecated.GithubLanguageStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.model.GithubRepositoryStatistics;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.document.GithubCommitDetailRaw;
import kr.ac.koreatech.sw.kosp.domain.github.mongo.repository.GithubCommitDetailRawRepository;
import kr.ac.koreatech.sw.kosp.domain.github.repository.GithubRepositoryStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageStatisticsCalculator {

    private final GithubCommitDetailRawRepository commitDetailRawRepository;
    private final GithubRepositoryStatisticsRepository repositoryStatisticsRepository;

    /**
     * 언어별 통계 계산
     */
    public List<GithubLanguageStatistics> calculate(String githubId) {
        log.info("Calculating language statistics for user: {}", githubId);

        // 저장소 통계에서 언어 정보 조회
        List<GithubRepositoryStatistics> repoStats = repositoryStatisticsRepository
            .findByContributorGithubId(githubId);

        if (repoStats.isEmpty()) {
            log.warn("No repository statistics found for user: {}", githubId);
            return List.of();
        }

        // 커밋 데이터 조회
        List<GithubCommitDetailRaw> commits = commitDetailRawRepository.findByAuthorLogin(githubId);

        // 언어별 그룹화
        Map<String, LanguageData> languageMap = repoStats.stream()
            .filter(repo -> repo.getPrimaryLanguage() != null && !repo.getPrimaryLanguage().isEmpty())
            .collect(Collectors.groupingBy(
                GithubRepositoryStatistics::getPrimaryLanguage,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    repos -> calculateLanguageData(repos, commits)
                )
            ));

        // 총 라인 수 계산
        int totalLines = languageMap.values().stream()
            .mapToInt(data -> data.lines)
            .sum();

        // 엔티티 생성
        List<GithubLanguageStatistics> result = new ArrayList<>();

        for (Map.Entry<String, LanguageData> entry : languageMap.entrySet()) {
            LanguageData data = entry.getValue();
            
            BigDecimal percentage = totalLines > 0
                ? BigDecimal.valueOf((double) data.lines / totalLines * 100)
                    .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            GithubLanguageStatistics langStats = GithubLanguageStatistics.create(githubId, entry.getKey());
            langStats.updateStatistics(data.lines, percentage, data.repositories, data.commits);
            
            result.add(langStats);
        }

        // 라인 수 기준 내림차순 정렬
        result.sort((a, b) -> b.getLinesOfCode().compareTo(a.getLinesOfCode()));

        log.info("Language statistics calculated for {}: {} languages", githubId, result.size());

        return result;
    }

    /**
     * 특정 언어의 데이터 계산
     */
    private LanguageData calculateLanguageData(
        List<GithubRepositoryStatistics> repos,
        List<GithubCommitDetailRaw> allCommits
    ) {
        LanguageData data = new LanguageData();
        data.repositories = repos.size();

        // 해당 언어의 저장소들에 대한 커밋 필터링
        for (GithubRepositoryStatistics repo : repos) {
            List<GithubCommitDetailRaw> repoCommits = allCommits.stream()
                .filter(c -> c.getRepoOwner().equals(repo.getRepoOwner()))
                .filter(c -> c.getRepoName().equals(repo.getRepoName()))
                .toList();

            data.commits += repoCommits.size();
            data.lines += repoCommits.stream()
                .mapToInt(c -> c.getAdditions() + c.getDeletions())
                .sum();
        }

        return data;
    }

    /**
     * 언어별 데이터 홀더
     */
    private static class LanguageData {
        int lines = 0;
        int repositories = 0;
        int commits = 0;
    }
}
