package io.swkoreatech.kosp.common.trigger.model;

public enum TriggerPriority {
    HIGH(1),
    LOW(10);

    private final int order;

    TriggerPriority(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
