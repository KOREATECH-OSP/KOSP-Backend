package io.swkoreatech.kosp.common.season.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시즌 랭킹 티어 열거형 (총 25단계).
 *
 * <p>Bronze4 ~ Diamond1(21단계)까지는 <b>점수형</b>이며 {@link #from(double)}으로 결정된다.
 * Master(4)·Challenger(1)는 <b>엘리트 구간</b>으로, 점수만으로는 도달할 수 없고
 * "상위 퍼센트 ∩ 성취 조건"을 만족해야 배치에서 승격된다
 * ({@code SeasonRankingBatchService.applyEliteTiers}).</p>
 */
@Getter
@RequiredArgsConstructor
public enum SeasonTier {

    BRONZE_4(0.0, 2.5),
    BRONZE_3(2.5, 5.0),
    BRONZE_2(5.0, 7.5),
    BRONZE_1(7.5, 10.0),

    SILVER_4(10.0, 12.5),
    SILVER_3(12.5, 15.0),
    SILVER_2(15.0, 17.5),
    SILVER_1(17.5, 20.0),

    GOLD_4(20.0, 23.75),
    GOLD_3(23.75, 27.5),
    GOLD_2(27.5, 31.25),
    GOLD_1(31.25, 35.0),

    PLATINUM_4(35.0, 40.0),
    PLATINUM_3(40.0, 45.0),
    PLATINUM_2(45.0, 50.0),
    PLATINUM_1(50.0, 55.0),

    DIAMOND_4(55.0, 60.0),
    DIAMOND_3(60.0, 65.0),
    DIAMOND_2(65.0, 70.0),
    DIAMOND_1(70.0, 75.0),

    MASTER_4(75.0, 78.75),
    MASTER_3(78.75, 82.5),
    MASTER_2(82.5, 86.25),
    MASTER_1(86.25, 90.0),

    CHALLENGER(90.0, Double.MAX_VALUE);

    private final double minScore;
    private final double maxScore;

    /**
     * 엘리트 티어(Master·Challenger) 여부.
     *
     * <p>엘리트 티어는 점수만으로는 도달할 수 없으며, 별도 조건 평가로만 부여된다.</p>
     *
     * @return Master 또는 Challenger이면 {@code true}
     */
    public boolean isElite() {
        return this == CHALLENGER
            || this == MASTER_1 || this == MASTER_2 || this == MASTER_3 || this == MASTER_4;
    }

    /**
     * 총점에 해당하는 <b>점수형 티어</b>를 반환한다.
     *
     * <p>점수형 상한은 {@code DIAMOND_1}이며, Master/Challenger는 이 메서드로 반환되지 않는다
     * (엘리트 승격은 배치의 {@code applyEliteTiers}가 담당).</p>
     *
     * @param totalScore 총점 (0.0 ~ 100.0)
     * @return 점수형 티어 (Bronze4 ~ Diamond1)
     */
    public static SeasonTier from(double totalScore) {
        SeasonTier[] tiers = values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            SeasonTier tier = tiers[i];
            if (tier.isElite()) {
                continue; // 점수만으로는 엘리트 티어에 도달 불가
            }
            if (totalScore >= tier.minScore) {
                return tier;
            }
        }
        return BRONZE_4;
    }
}
