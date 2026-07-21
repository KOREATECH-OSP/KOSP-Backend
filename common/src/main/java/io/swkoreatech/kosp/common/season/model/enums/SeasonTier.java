package io.swkoreatech.kosp.common.season.model.enums;

/**
 * 시즌 랭킹 티어 열거형 (7단계, 백분위 기반).
 *
 * <p>기본 5티어(BRONZE~DIAMOND)는 <b>상대평가(백분위)</b>로 결정된다:</p>
 * <ul>
 *   <li>상위 0~20% → {@code DIAMOND}</li>
 *   <li>상위 20~40% → {@code PLATINUM}</li>
 *   <li>상위 40~60% → {@code GOLD}</li>
 *   <li>상위 60~80% → {@code SILVER}</li>
 *   <li>하위 20% → {@code BRONZE}</li>
 * </ul>
 *
 * <p>엘리트 2티어({@code MASTER}·{@code CHALLENGER})는 <b>다이아(상위 20%)이면서
 * 절대 총점 기준</b>도 넘어야 부여된다:</p>
 * <ul>
 *   <li>다이아 &amp; 총점 &ge; {@value #MASTER_MIN_SCORE} → {@code MASTER}</li>
 *   <li>다이아 &amp; 총점 &ge; {@value #CHALLENGER_MIN_SCORE} → {@code CHALLENGER}</li>
 * </ul>
 *
 * <p>백분위는 전체 인원이 필요하므로 티어 배정은 배치
 * ({@code SeasonRankingBatchService.assignTiers})에서 순위 계산 후 수행한다.</p>
 */
public enum SeasonTier {

    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND,
    MASTER,
    CHALLENGER;

    /** 다이아 상한 백분위 (상위 20%). */
    public static final double DIAMOND_MAX_PERCENTILE = 0.20;
    /** 플래티넘 상한 백분위 (상위 40%). */
    public static final double PLATINUM_MAX_PERCENTILE = 0.40;
    /** 골드 상한 백분위 (상위 60%). */
    public static final double GOLD_MAX_PERCENTILE = 0.60;
    /** 실버 상한 백분위 (상위 80%). */
    public static final double SILVER_MAX_PERCENTILE = 0.80;

    /** 마스터 승급 최소 총점 (다이아 조건과 함께 충족해야 함). */
    public static final double MASTER_MIN_SCORE = 40.0;
    /** 챌린저 승급 최소 총점 (다이아 조건과 함께 충족해야 함). */
    public static final double CHALLENGER_MIN_SCORE = 60.0;

    /**
     * 엘리트 티어(Master·Challenger) 여부.
     *
     * @return Master 또는 Challenger이면 {@code true}
     */
    public boolean isElite() {
        return this == MASTER || this == CHALLENGER;
    }

    /**
     * 백분위(순위/전체인원, 0.0~1.0)로 기본 티어(BRONZE~DIAMOND)를 결정한다.
     *
     * @param percentile 상위 백분위 (예: 0.15 = 상위 15%)
     * @return 기본 티어
     */
    public static SeasonTier fromPercentile(double percentile) {
        if (percentile <= DIAMOND_MAX_PERCENTILE) {
            return DIAMOND;
        }
        if (percentile <= PLATINUM_MAX_PERCENTILE) {
            return PLATINUM;
        }
        if (percentile <= GOLD_MAX_PERCENTILE) {
            return GOLD;
        }
        if (percentile <= SILVER_MAX_PERCENTILE) {
            return SILVER;
        }
        return BRONZE;
    }

    /**
     * 백분위 + 총점으로 최종 티어를 결정한다.
     *
     * <p>기본 티어가 다이아(상위 20%)이면서 절대 총점 기준을 넘으면 마스터/챌린저로 승급한다.
     * 총점이 아무리 높아도 상위 20% 밖이면 마스터/챌린저가 될 수 없다.</p>
     *
     * @param percentile 상위 백분위 (순위/전체인원)
     * @param totalScore 총점 (0.0~100.0)
     * @return 최종 티어
     */
    public static SeasonTier resolve(double percentile, double totalScore) {
        SeasonTier base = fromPercentile(percentile);
        if (base == DIAMOND) {
            if (totalScore >= CHALLENGER_MIN_SCORE) {
                return CHALLENGER;
            }
            if (totalScore >= MASTER_MIN_SCORE) {
                return MASTER;
            }
        }
        return base;
    }
}
