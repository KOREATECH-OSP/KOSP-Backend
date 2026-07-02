package io.swkoreatech.kosp.domain.season.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.SeasonRankingScore;
import io.swkoreatech.kosp.common.season.model.enums.SeasonTier;
import io.swkoreatech.kosp.common.season.repository.SeasonRankingScoreRepository;
import io.swkoreatech.kosp.common.title.model.Title;
import io.swkoreatech.kosp.common.title.model.UserTitle;
import io.swkoreatech.kosp.common.title.model.enums.TitleGrantSource;
import io.swkoreatech.kosp.common.title.repository.TitleRepository;
import io.swkoreatech.kosp.common.title.repository.UserTitleRepository;
import io.swkoreatech.kosp.common.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 시즌 랭킹 티어 칭호 자동 동기화 서비스.
 *
 * <p>유저의 현재 시즌 티어(패밀리)에 대응하는 칭호를 자동 지급하고,
 * 티어가 내려가면 기존 티어 칭호를 자동 회수한다. 즉 유저는 시즌 진행 중
 * <b>현재 티어 패밀리에 해당하는 칭호 1개</b>만 보유한다.</p>
 *
 * <p>티어 패밀리는 7종이며, 각각 아래 칭호 코드에 매핑된다 (V13/V21 시드).</p>
 * <ul>
 *   <li>BRONZE_* → {@code SEASON_BRONZE}</li>
 *   <li>SILVER_* → {@code SEASON_SILVER}</li>
 *   <li>GOLD_* → {@code SEASON_GOLD}</li>
 *   <li>PLATINUM_* → {@code SEASON_PLATINUM}</li>
 *   <li>DIAMOND_* → {@code SEASON_DIAMOND}</li>
 *   <li>MASTER_* → {@code SEASON_MASTER}</li>
 *   <li>CHALLENGER → {@code SEASON_CHALLENGER}</li>
 * </ul>
 *
 * <p>자동 지급분은 {@link TitleGrantSource#SEASON_TIER}로 기록되며, 회수 시에도
 * 이 출처로 지급된 칭호만 대상으로 한다(관리자 수동 지급분은 건드리지 않는다).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonTierTitleService {

    /** 티어 패밀리 → 칭호 코드 매핑 (7종). */
    static final Map<String, String> TIER_TITLE_CODE_BY_FAMILY = Map.of(
        "BRONZE", "SEASON_BRONZE",
        "SILVER", "SEASON_SILVER",
        "GOLD", "SEASON_GOLD",
        "PLATINUM", "SEASON_PLATINUM",
        "DIAMOND", "SEASON_DIAMOND",
        "MASTER", "SEASON_MASTER",
        "CHALLENGER", "SEASON_CHALLENGER"
    );

    private static final List<String> TIER_TITLE_CODES = List.copyOf(TIER_TITLE_CODE_BY_FAMILY.values());

    private final SeasonRankingScoreRepository rankingScoreRepository;
    private final TitleRepository titleRepository;
    private final UserTitleRepository userTitleRepository;

    /**
     * 주어진 시즌의 모든 유저에 대해 티어 칭호를 현재 티어에 맞게 동기화한다.
     *
     * @param season 대상 시즌
     */
    @Transactional
    public void syncTierTitles(Season season) {
        List<Title> tierTitles = titleRepository.findAllByCodeIn(TIER_TITLE_CODES);
        if (tierTitles.size() < TIER_TITLE_CODES.size()) {
            log.warn("[TierTitle] 티어 칭호 시드가 일부 누락됨. 조회={}, 기대={}",
                tierTitles.size(), TIER_TITLE_CODES.size());
        }
        Map<String, Title> titleByCode = tierTitles.stream()
            .collect(Collectors.toMap(Title::getCode, Function.identity()));

        List<SeasonRankingScore> scores = rankingScoreRepository.findAllBySeason(season);
        int granted = 0;
        int revoked = 0;
        for (SeasonRankingScore score : scores) {
            try {
                int[] delta = syncForUser(score.getUser(), score.getTier(), tierTitles, titleByCode);
                granted += delta[0];
                revoked += delta[1];
            } catch (Exception e) {
                log.warn("[TierTitle] 유저 동기화 실패 스킵. userId={}, error={}",
                    score.getUser().getId(), e.getMessage());
            }
        }
        log.info("[TierTitle] 티어 칭호 동기화 완료. seasonId={}, 지급={}, 회수={}",
            season.getId(), granted, revoked);
    }

    /**
     * 단일 유저의 티어 칭호를 현재 티어에 맞게 동기화한다.
     *
     * @return {@code [지급 수, 회수 수]}
     */
    private int[] syncForUser(User user, SeasonTier tier, List<Title> tierTitles, Map<String, Title> titleByCode) {
        String targetCode = titleCodeOf(tier);
        int granted = 0;
        int revoked = 0;

        for (Title title : tierTitles) {
            boolean isTarget = title.getCode().equals(targetCode);
            Optional<UserTitle> held = userTitleRepository.findByUserAndTitleAndIsRevokedFalse(user, title);

            if (isTarget) {
                if (held.isEmpty()) {
                    grantTierTitle(user, title);
                    granted++;
                }
            } else if (held.isPresent() && held.get().getGrantSource() == TitleGrantSource.SEASON_TIER) {
                // 현재 티어가 아닌 티어 칭호 중, 티어 자동 지급분만 회수 (관리자 지급분은 유지)
                held.get().revokeBySystem();
                revoked++;
            }
        }
        return new int[] {granted, revoked};
    }

    private void grantTierTitle(User user, Title title) {
        UserTitle userTitle = UserTitle.builder()
            .user(user)
            .title(title)
            .isDisplay(false)
            .grantSource(TitleGrantSource.SEASON_TIER)
            .grantedAt(LocalDateTime.now())
            .build();
        userTitleRepository.save(userTitle);
    }

    /**
     * 시즌 티어에 해당하는 티어 칭호 코드를 반환한다.
     *
     * @param tier 시즌 티어 (예: {@code DIAMOND_2})
     * @return 티어 칭호 코드 (예: {@code SEASON_DIAMOND})
     */
    static String titleCodeOf(SeasonTier tier) {
        String family = tier.name().split("_")[0];
        return TIER_TITLE_CODE_BY_FAMILY.getOrDefault(family, "SEASON_BRONZE");
    }
}
