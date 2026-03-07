package io.swkoreatech.kosp.infra.rabbitmq.constants;

/**
 * RabbitMQ 큐, 익스체인지, 데드 레터 관련 이름 상수를 정의하는 유틸리티 클래스.
 *
 * <p>인스턴스 생성을 방지하기 위해 {@code final} 클래스로 선언되며,
 * 모든 상수는 {@code public static final}로 제공된다.</p>
 */
public final class QueueNames {

    /** 데드 레터 익스체인지 설정에 사용되는 RabbitMQ 인자 키 */
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    /** 데드 레터 라우팅 키 설정에 사용되는 RabbitMQ 인자 키 */
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    /** 챌린지 평가 큐 이름 */
    public static final String CHALLENGE_EVALUATION = "challenge-evaluation-queue";

    /** 챌린지 완료 큐 이름 */
    public static final String CHALLENGE_COMPLETED = "challenge-completed-queue";

    /** 포인트 변경 큐 이름 */
    public static final String POINT_CHANGED = "point-changed-queue";

    /** GitHub 활동 수집 큐 이름 */
    public static final String GITHUB_COLLECTION = "github-collection-queue";

    /** GitHub 활동 수집 익스체인지 이름 */
    public static final String GITHUB_COLLECTION_EXCHANGE = "github-collection-exchange";

    private QueueNames() {
    }
}
