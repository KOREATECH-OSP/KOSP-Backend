package io.swkoreatech.kosp.domain.season.eventlistener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.swkoreatech.kosp.common.season.model.Season;
import io.swkoreatech.kosp.common.season.model.enums.ScoreEventType;
import io.swkoreatech.kosp.common.season.repository.SeasonRepository;
import io.swkoreatech.kosp.common.season.repository.SeasonScoreEventLogRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.common.user.repository.UserRepository;
import io.swkoreatech.kosp.domain.community.article.event.ArticleCreatedEvent;
import io.swkoreatech.kosp.domain.season.service.SeasonScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 게시글 작성 이벤트 → 시즌 커뮤니티 점수 지급 이벤트 리스너.
 *
 * <p>게시글 1건당 0.5pt, 시즌 내 최대 10pt (20건)까지 지급한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonCommunityEventListener {

    private static final BigDecimal ARTICLE_SCORE = new BigDecimal("0.5000");
    private static final BigDecimal MAX_COMMUNITY_SCORE = new BigDecimal("10.0000");

    private final SeasonRepository seasonRepository;
    private final SeasonScoreEventLogRepository eventLogRepository;
    private final UserRepository userRepository;
    private final SeasonScoreService seasonScoreService;

    @Async
    @EventListener
    public void handle(ArticleCreatedEvent event) {
        try {
            processCommunityScore(event.getUserId(), event.getArticleId());
        } catch (Exception e) {
            log.error("[SeasonCommunity] 커뮤니티 점수 처리 중 오류. userId={}", event.getUserId(), e);
        }
    }

    private void processCommunityScore(Long userId, Long articleId) {
        Optional<Season> activeSeason = seasonRepository.findByIsActiveTrue();
        if (activeSeason.isEmpty()) {
            return;
        }

        Season season = activeSeason.get();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        // 이미 같은 articleId로 지급한 적 있는지 확인
        boolean alreadyGranted = eventLogRepository.existsBySeasonAndUserAndEventTypeAndSourceRefId(
            season, user, ScoreEventType.COMMUNITY, articleId);
        if (alreadyGranted) {
            return;
        }

        // 커뮤니티 점수 총합이 이미 최대치인지 확인
        var rankingScore = seasonScoreService.getOrCreate(season, user);
        if (rankingScore.getCommunityScore().compareTo(MAX_COMMUNITY_SCORE) >= 0) {
            log.debug("[SeasonCommunity] 커뮤니티 점수 한도 도달. userId={}", userId);
            return;
        }

        seasonScoreService.addScore(season, user, ScoreEventType.COMMUNITY,
            ARTICLE_SCORE, LocalDate.now(), articleId, "ARTICLE");

        log.debug("[SeasonCommunity] 커뮤니티 점수 지급. userId={}, articleId={}", userId, articleId);
    }
}
