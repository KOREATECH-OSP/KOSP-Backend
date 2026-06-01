package io.swkoreatech.kosp.domain.title.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.swkoreatech.kosp.common.title.model.Title;
import io.swkoreatech.kosp.common.title.model.TitleCondition;
import io.swkoreatech.kosp.common.title.model.UserTitle;
import io.swkoreatech.kosp.common.title.model.enums.TitleGrantSource;
import io.swkoreatech.kosp.common.title.repository.TitleConditionRepository;
import io.swkoreatech.kosp.common.title.repository.TitleRepository;
import io.swkoreatech.kosp.common.title.repository.UserTitleRepository;
import io.swkoreatech.kosp.common.user.model.User;
import io.swkoreatech.kosp.domain.title.evaluator.TitleConditionEvaluator;
import io.swkoreatech.kosp.domain.title.evaluator.UserTitleContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 칭호 조건 평가 핵심 서비스.
 *
 * <p>유저 1명에 대해 활성화된 모든 칭호의 조건을 평가하고,
 * 조건 충족 시 중복 검사 후 칭호를 지급한다.
 * 중복 지급 방지는 DB partial unique index와 이 서비스의 소프트 체크로 이중 보호.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TitleEvaluationService {

    private final TitleRepository titleRepository;
    private final TitleConditionRepository titleConditionRepository;
    private final UserTitleRepository userTitleRepository;
    private final UserTitleContextBuilder contextBuilder;
    private final List<TitleConditionEvaluator> evaluators;

    // evaluator를 conditionType 기준으로 빠르게 조회하기 위한 캐시
    private Map<io.swkoreatech.kosp.common.title.model.enums.TitleConditionType, TitleConditionEvaluator> evaluatorMap;

    /**
     * 모든 활성 유저를 대상으로 칭호를 평가한다.
     * TitleBatchService에서 호출하며, 1회 배치 실행 단위이다.
     *
     * @param users 평가 대상 유저 목록
     * @return 이번 실행에서 지급된 총 칭호 수
     */
    @Transactional
    public int evaluateAll(List<User> users) {
        List<Title> activeTitles = titleRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
        if (activeTitles.isEmpty()) {
            log.warn("[TitleBatch] 활성화된 칭호가 없습니다. 배치를 건너뜁니다.");
            return 0;
        }

        Map<Long, List<TitleCondition>> conditionsByTitle = activeTitles.stream()
            .collect(Collectors.toMap(
                Title::getId,
                titleConditionRepository::findAllByTitle
            ));

        int totalGranted = 0;
        for (User user : users) {
            try {
                totalGranted += evaluateForUser(user, activeTitles, conditionsByTitle);
            } catch (Exception e) {
                // 개별 유저 평가 실패 시 전체 배치를 중단하지 않음
                log.error("[TitleBatch] userId={} 평가 중 오류 발생. 해당 유저를 건너뜁니다. error={}",
                    user.getId(), e.getMessage(), e);
            }
        }
        return totalGranted;
    }

    /**
     * 단일 유저에 대해 모든 칭호 조건을 평가한다.
     * 개별 유저 단위 트랜잭션으로, 유저 간 격리를 보장한다.
     */
    @Transactional
    public int evaluateForUser(User user) {
        List<Title> activeTitles = titleRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc();
        Map<Long, List<TitleCondition>> conditionsByTitle = activeTitles.stream()
            .collect(Collectors.toMap(
                Title::getId,
                titleConditionRepository::findAllByTitle
            ));
        return evaluateForUser(user, activeTitles, conditionsByTitle);
    }

    private int evaluateForUser(
        User user,
        List<Title> activeTitles,
        Map<Long, List<TitleCondition>> conditionsByTitle
    ) {
        UserTitleContext context = contextBuilder.build(user);
        int grantedCount = 0;

        for (Title title : activeTitles) {
            // 이미 보유 중인 칭호는 건너뜀 (중복 지급 방지)
            if (userTitleRepository.findByUserAndTitleAndIsRevokedFalse(user, title).isPresent()) {
                continue;
            }

            List<TitleCondition> conditions = conditionsByTitle.getOrDefault(title.getId(), List.of());
            if (conditions.isEmpty()) {
                log.warn("[TitleBatch] titleId={} ({}) 에 조건이 없습니다. 건너뜁니다.",
                    title.getId(), title.getName());
                continue;
            }

            if (allConditionsMet(context, conditions)) {
                grantTitle(user, title);
                grantedCount++;
                log.info("[TitleBatch] userId={} → 칭호 지급: {} (titleId={})",
                    user.getId(), title.getName(), title.getId());
            }
        }
        return grantedCount;
    }

    private boolean allConditionsMet(UserTitleContext context, List<TitleCondition> conditions) {
        return conditions.stream().allMatch(condition -> {
            TitleConditionEvaluator evaluator = getEvaluatorMap().get(condition.getConditionType());
            if (evaluator == null) {
                // 평가기가 없는 조건 유형은 미충족으로 처리 - TODO 로그로 운영 확인 가능
                log.warn("[TitleBatch] conditionType={} 에 대응하는 평가기가 없습니다.",
                    condition.getConditionType());
                return false;
            }
            return evaluator.evaluate(context, condition.getThresholdValue());
        });
    }

    private void grantTitle(User user, Title title) {
        UserTitle userTitle = UserTitle.builder()
            .user(user)
            .title(title)
            .isDisplay(false)
            .grantSource(TitleGrantSource.SYSTEM)
            .grantedAt(LocalDateTime.now())
            .build();
        userTitleRepository.save(userTitle);
    }

    private Map<io.swkoreatech.kosp.common.title.model.enums.TitleConditionType, TitleConditionEvaluator> getEvaluatorMap() {
        if (evaluatorMap == null) {
            evaluatorMap = evaluators.stream()
                .collect(Collectors.toMap(TitleConditionEvaluator::supports, Function.identity()));
        }
        return evaluatorMap;
    }
}
