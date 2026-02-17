package io.swkoreatech.kosp.infra.rabbitmq.constants;

public final class QueueNames {

    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    public static final String CHALLENGE_EVALUATION = "challenge-evaluation-queue";
    public static final String CHALLENGE_COMPLETED = "challenge-completed-queue";
    public static final String POINT_CHANGED = "point-changed-queue";

    public static final String GITHUB_COLLECTION = "github-collection-queue";
    public static final String GITHUB_COLLECTION_EXCHANGE = "github-collection-exchange";

    private QueueNames() {
    }
}
