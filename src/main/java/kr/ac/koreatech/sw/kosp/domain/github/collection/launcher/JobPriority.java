package kr.ac.koreatech.sw.kosp.domain.github.collection.launcher;

/**
 * Job 실행 우선순위.
 * HIGH: 회원가입 트리거, Rate Limit 해제 후 재시작
 * LOW: 정기 스케줄링
 */
public enum JobPriority {
    HIGH(0),
    LOW(1);

    private final int order;

    JobPriority(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
