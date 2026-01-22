package io.swkoreatech.kosp.domain.github.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.domain.github.model.GithubContributionPattern;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ContributionPatternResponse(
    TimePatternInfo timePattern,
    ProjectPatternInfo projectPattern,
    CollaborationInfo collaboration
) {
    @Builder
    public record TimePatternInfo(
        NightOwlInfo nightOwl,
        List<HourlyCommit> hourlyDistribution
    ) {}

    @Builder
    public record NightOwlInfo(
        Integer score,
        String description,
        Integer nightCommits,
        Integer dayCommits,
        Double nightPercentage
    ) {}

    @Builder
    public record HourlyCommit(
        Integer hour,
        Integer commits
    ) {}

    @Builder
    public record ProjectPatternInfo(
        InitiatorInfo initiator,
        IndependentInfo independent
    ) {}

    @Builder
    public record InitiatorInfo(
        Integer score,
        String description,
        Integer earlyContributions,
        Integer totalProjects
    ) {}

    @Builder
    public record IndependentInfo(
        Integer score,
        String description,
        Integer soloProjects,
        Integer totalProjects
    ) {}

    @Builder
    public record CollaborationInfo(
        Integer totalCoworkers
    ) {}

    public static ContributionPatternResponse from(GithubContributionPattern pattern, ObjectMapper objectMapper) {
        // Night Owl 정보
        int totalCommits = pattern.getNightCommits() + pattern.getDayCommits();
        double nightPercentage = totalCommits > 0 
            ? (double) pattern.getNightCommits() / totalCommits * 100 
            : 0.0;

        String nightOwlDescription = pattern.getNightOwlScore() >= 70 
            ? "주로 밤 시간대(22:00-06:00)에 활동"
            : pattern.getNightOwlScore() >= 30
                ? "밤낮 고르게 활동"
                : "주로 낮 시간대(06:00-22:00)에 활동";

        NightOwlInfo nightOwl = NightOwlInfo.builder()
            .score(pattern.getNightOwlScore())
            .description(nightOwlDescription)
            .nightCommits(pattern.getNightCommits())
            .dayCommits(pattern.getDayCommits())
            .nightPercentage(Math.round(nightPercentage * 10.0) / 10.0)
            .build();

        // 시간대별 분포 파싱
        List<HourlyCommit> hourlyDistribution = parseHourlyDistribution(
            pattern.getHourlyDistribution(), 
            objectMapper
        );

        TimePatternInfo timePattern = TimePatternInfo.builder()
            .nightOwl(nightOwl)
            .hourlyDistribution(hourlyDistribution)
            .build();

        // Initiator 정보
        String initiatorDescription = pattern.getInitiatorScore() >= 70
            ? "프로젝트 초반에 주로 활동"
            : pattern.getInitiatorScore() >= 30
                ? "프로젝트 전 기간에 고르게 활동"
                : "프로젝트 후반에 주로 활동";

        InitiatorInfo initiator = InitiatorInfo.builder()
            .score(pattern.getInitiatorScore())
            .description(initiatorDescription)
            .earlyContributions(pattern.getEarlyContributions())
            .totalProjects(pattern.getTotalProjects())
            .build();

        // Independent 정보
        String independentDescription = pattern.getIndependentScore() >= 70
            ? "주로 혼자 작업하는 프로젝트에 기여"
            : pattern.getIndependentScore() >= 30
                ? "혼자 작업과 협업을 고르게 진행"
                : "주로 협업 프로젝트에 기여";

        IndependentInfo independent = IndependentInfo.builder()
            .score(pattern.getIndependentScore())
            .description(independentDescription)
            .soloProjects(pattern.getSoloProjects())
            .totalProjects(pattern.getTotalProjects())
            .build();

        ProjectPatternInfo projectPattern = ProjectPatternInfo.builder()
            .initiator(initiator)
            .independent(independent)
            .build();

        // 협업 정보
        CollaborationInfo collaboration = CollaborationInfo.builder()
            .totalCoworkers(pattern.getTotalCoworkers())
            .build();

        return new ContributionPatternResponse(timePattern, projectPattern, collaboration);
    }

    private static List<HourlyCommit> parseHourlyDistribution(String json, ObjectMapper objectMapper) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }

        try {
            Map<String, Integer> distribution = objectMapper.readValue(
                json, 
                new TypeReference<Map<String, Integer>>() {}
            );

            return distribution.entrySet().stream()
                .map(entry -> HourlyCommit.builder()
                    .hour(Integer.parseInt(entry.getKey()))
                    .commits(entry.getValue())
                    .build())
                .sorted((a, b) -> a.hour().compareTo(b.hour()))
                .toList();
        } catch (JsonProcessingException e) {
            log.error("Failed to parse hourly distribution JSON: {}", json, e);
            return List.of();
        }
    }
}
