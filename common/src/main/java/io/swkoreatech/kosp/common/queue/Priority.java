package io.swkoreatech.kosp.common.queue;

public enum Priority {
    HIGH(0L),
    LOW(1_000_000_000L);

    private final long offset;

    Priority(long offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }
}
