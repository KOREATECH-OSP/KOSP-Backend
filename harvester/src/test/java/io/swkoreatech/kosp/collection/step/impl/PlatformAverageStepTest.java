package io.swkoreatech.kosp.collection.step.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

import io.swkoreatech.kosp.job.StepCompletionListener;
import io.swkoreatech.kosp.statistics.model.PlatformStatistics;
import io.swkoreatech.kosp.domain.github.repository.GithubUserStatisticsRepository;
import io.swkoreatech.kosp.statistics.repository.PlatformStatisticsRepository;

@DisplayName("PlatformAverageStep 단위 테스트")
@ExtendWith(MockitoExtension.class)
class PlatformAverageStepTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private GithubUserStatisticsRepository statisticsRepository;

    @Mock
    private PlatformStatisticsRepository platformStatisticsRepository;

    @Mock
    private StepCompletionListener stepCompletionListener;

    @InjectMocks
    private PlatformAverageStep platformAverageStep;

    @Nested
    @DisplayName("getStepName 메서드")
    class GetStepNameTest {

        @Test
        @DisplayName("올바른 스텝 이름을 반환한다")
        void returnsCorrectStepName() {
            String stepName = platformAverageStep.getStepName();

            assertThat(stepName).isEqualTo("platformAverageStep");
        }
    }

    @Nested
    @DisplayName("getStep 메서드")
    class GetStepTest {

        @Test
        @DisplayName("Step 인스턴스를 반환한다")
        void returnsStepInstance() {
            Step step = platformAverageStep.getStep();

            assertThat(step).isNotNull();
        }
    }

    @Nested
    @DisplayName("shouldRecompute 메서드")
    class ShouldRecomputeTest {

        @Test
        @DisplayName("델타가 임계값 미만일 때 재계산하지 않는다")
        void doesNotRecompute_whenDeltaBelowThreshold() throws Exception {
            PlatformStatistics platformStats = createPlatformStats(100);
            when(statisticsRepository.count()).thenReturn(105L);
            when(platformStatisticsRepository.getOrCreate("GLOBAL")).thenReturn(platformStats);

            invokeExecute();

            verify(platformStatisticsRepository, never()).save(any());
        }

        @Test
        @DisplayName("델타가 임계값 이상일 때 재계산한다")
        void recomputes_whenDeltaAboveThreshold() throws Exception {
            PlatformStatistics platformStats = createPlatformStats(100);
            when(statisticsRepository.count()).thenReturn(110L);
            when(platformStatisticsRepository.getOrCreate("GLOBAL")).thenReturn(platformStats);
            when(statisticsRepository.findAverageCommits()).thenReturn(BigDecimal.valueOf(50));
            when(statisticsRepository.findAveragePrs()).thenReturn(BigDecimal.valueOf(10));
            when(statisticsRepository.findAverageIssues()).thenReturn(BigDecimal.valueOf(5));
            when(statisticsRepository.findAverageStars()).thenReturn(BigDecimal.valueOf(100));

            invokeExecute();

            verify(platformStatisticsRepository).save(platformStats);
        }
    }

    @Nested
    @DisplayName("updateAverages 메서드")
    class UpdateAveragesTest {

        @Test
        @DisplayName("SQL 집계 결과로 플랫폼 통계를 업데이트한다")
        void updatesAverages_withAggregateResults() throws Exception {
            PlatformStatistics platformStats = createPlatformStats(50);
            BigDecimal avgCommits = BigDecimal.valueOf(42.5);
            BigDecimal avgPrs = BigDecimal.valueOf(8.3);
            BigDecimal avgIssues = BigDecimal.valueOf(3.7);
            BigDecimal avgStars = BigDecimal.valueOf(150.2);

            when(statisticsRepository.count()).thenReturn(100L);
            when(platformStatisticsRepository.getOrCreate("GLOBAL")).thenReturn(platformStats);
            when(statisticsRepository.findAverageCommits()).thenReturn(avgCommits);
            when(statisticsRepository.findAveragePrs()).thenReturn(avgPrs);
            when(statisticsRepository.findAverageIssues()).thenReturn(avgIssues);
            when(statisticsRepository.findAverageStars()).thenReturn(avgStars);

            invokeExecute();

            verify(platformStatisticsRepository).save(platformStats);
            assertThat(platformStats.getAvgCommitCount()).isEqualByComparingTo(avgCommits);
            assertThat(platformStats.getAvgPrCount()).isEqualByComparingTo(avgPrs);
            assertThat(platformStats.getAvgIssueCount()).isEqualByComparingTo(avgIssues);
            assertThat(platformStats.getAvgStarCount()).isEqualByComparingTo(avgStars);
            assertThat(platformStats.getTotalUserCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("null 평균값은 0으로 처리한다")
        void treatsNullAverages_asZero() throws Exception {
            PlatformStatistics platformStats = createPlatformStats(50);

            when(statisticsRepository.count()).thenReturn(100L);
            when(platformStatisticsRepository.getOrCreate("GLOBAL")).thenReturn(platformStats);
            when(statisticsRepository.findAverageCommits()).thenReturn(null);
            when(statisticsRepository.findAveragePrs()).thenReturn(null);
            when(statisticsRepository.findAverageIssues()).thenReturn(null);
            when(statisticsRepository.findAverageStars()).thenReturn(null);

            invokeExecute();

            verify(platformStatisticsRepository).save(platformStats);
            assertThat(platformStats.getAvgCommitCount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(platformStats.getAvgPrCount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(platformStats.getAvgIssueCount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(platformStats.getAvgStarCount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    private void invokeExecute() throws Exception {
        Method executeMethod = PlatformAverageStep.class.getDeclaredMethod("execute");
        executeMethod.setAccessible(true);
        executeMethod.invoke(platformAverageStep);
    }

    private PlatformStatistics createPlatformStats(int totalUserCount) {
        PlatformStatistics stats = PlatformStatistics.create("GLOBAL");
        stats.updateAverages(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            totalUserCount
        );
        return stats;
    }
}
