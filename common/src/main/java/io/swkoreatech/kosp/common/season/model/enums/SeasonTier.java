package io.swkoreatech.kosp.common.season.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 시즌 랭킹 티어 열거형.
 *
 * <p>총점에 따라 Bronze4 ~ Challenger 까지 29단계로 구분된다.</p>
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
     * 총점에 해당하는 티어를 반환한다.
     *
     * @param totalScore 총점 (0.0 ~ 100.0)
     * @return 해당 티어
     */
    public static SeasonTier from(double totalScore) {
        SeasonTier[] tiers = values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            if (totalScore >= tiers[i].minScore) {
                return tiers[i];
            }
        }
        return BRONZE_4;
    }
}
