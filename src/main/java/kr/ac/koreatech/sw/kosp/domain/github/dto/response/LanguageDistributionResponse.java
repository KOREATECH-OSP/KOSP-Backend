package kr.ac.koreatech.sw.kosp.domain.github.dto.response;

import java.math.BigDecimal;
import java.util.List;

import kr.ac.koreatech.sw.kosp.domain.github.model.GithubLanguageStatistics;
import lombok.Builder;

public record LanguageDistributionResponse(
    List<LanguageInfo> languages,
    Integer totalLanguages,
    String primaryLanguage
) {
    @Builder
    public record LanguageInfo(
        String name,
        Integer linesOfCode,
        BigDecimal percentage,
        Integer repositories,
        Integer commits
    ) {}

    public static LanguageDistributionResponse from(List<GithubLanguageStatistics> languageStats) {
        List<LanguageInfo> languages = languageStats.stream()
            .map(lang -> LanguageInfo.builder()
                .name(lang.getLanguage())
                .linesOfCode(lang.getLinesOfCode())
                .percentage(lang.getPercentage())
                .repositories(lang.getRepositories())
                .commits(lang.getCommits())
                .build())
            .toList();

        String primaryLanguage = languages.isEmpty() 
            ? null 
            : languages.get(0).name();

        return new LanguageDistributionResponse(
            languages,
            languages.size(),
            primaryLanguage
        );
    }
}
