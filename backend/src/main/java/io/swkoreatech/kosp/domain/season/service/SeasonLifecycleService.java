package io.swkoreatech.kosp.domain.season.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.model.SeasonRankingSnapshot;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingScoreRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingSnapshotRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시즌 종료 자동화 서비스.
 *
 * <p>매일 새벽(랭킹 배치 이후)에 활성 시즌의 종료일을 확인하여, 종료일이 지났으면 시즌을
 * 자동으로 마감한다. 마감 시 다음을 수행한다:</p>
 * <ol>
 *   <li>티어 칭호 최종 동기화 ({@link SeasonTierTitleService}) — 최종 티어 기념 칭호 확정</li>
 *   <li>유저별 최종 점수·티어·순위 스냅샷 저장 ({@link SeasonRankingSnapshot})</li>
 *   <li>시즌 비활성화 ({@code is_active = false}) — 이후 랭킹 배치가 이 시즌을 건드리지 않음</li>
 * </ol>
 *
 * <p>시즌이 비활성화되면 {@link SeasonRankingBatchService}가 스킵하므로, 마감 시점의
 * 티어 칭호가 그대로 최종 상태로 남는다(다음 시즌 시작 시 새 시즌 기준으로 재동기화).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonLifecycleService {

    private final SeasonRepository seasonRepository;
    private final SeasonRankingScoreRepository rankingScoreRepository;
    private final SeasonRankingSnapshotRepository snapshotRepository;
    private final SeasonTierTitleService seasonTierTitleService;

    /**
     * 활성 시즌의 종료 여부를 확인하고, 종료일이 지났으면 시즌을 마감한다.
     *
     * <p>랭킹 배치(새벽 4시) 이후에 실행되도록 4시 30분에 실행한다.</p>
     */
    @Scheduled(cron = "0 30 4 * * *")
    public void runSeasonEndCheck() {
        Optional<Season> activeSeason = seasonRepository.findByIsActiveTrue();
        if (activeSeason.isEmpty()) {
            return;
        }
        Season season = activeSeason.get();
        if (!LocalDate.now().isAfter(season.getEndDate())) {
            return; // 아직 시즌 진행 중
        }
        log.info("[SeasonEnd] 시즌 종료 감지. seasonId={}, name={}, endDate={}",
            season.getId(), season.getName(), season.getEndDate());
        finalizeSeason(season);
    }

    /**
     * 시즌을 마감한다: 티어 칭호 최종 동기화 → 스냅샷 저장 → 시즌 비활성화.
     */
    @Transactional
    public void finalizeSeason(Season season) {
        // 1) 최종 티어 칭호 동기화 (마감 시점 기준으로 확정)
        seasonTierTitleService.syncTierTitles(season);

        // 2) 유저별 최종 스냅샷 저장 (중복 방지)
        List<SeasonRankingScore> scores = rankingScoreRepository.findAllBySeason(season);
        int saved = 0;
        for (SeasonRankingScore score : scores) {
            if (snapshotRepository.existsBySeasonAndUser(season, score.getUser())) {
                continue;
            }
            snapshotRepository.save(SeasonRankingSnapshot.builder()
                .season(season)
                .user(score.getUser())
                .finalScore(score.getTotalScore())
                .finalTier(score.getTier())
                .finalRank(score.getRankInSeason() != null ? score.getRankInSeason() : 0)
                .build());
            saved++;
        }

        // 3) 시즌 비활성화
        season.deactivate();
        seasonRepository.save(season);

        log.info("[SeasonEnd] 시즌 마감 완료. seasonId={}, 스냅샷 저장={}", season.getId(), saved);
    }
}
