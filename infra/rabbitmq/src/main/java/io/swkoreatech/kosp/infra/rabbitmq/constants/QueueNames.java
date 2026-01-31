package io.swkoreatech.kosp.infra.rabbitmq.constants;

public final class QueueNames {
    public static final String CHALLENGE_EVALUATION = "challenge-evaluation-queue";
    public static final String CHALLENGE_COMPLETED = "challenge-completed-queue";
    public static final String POINT_CHANGED = "point-changed-queue";
    
    private QueueNames() {}
}
