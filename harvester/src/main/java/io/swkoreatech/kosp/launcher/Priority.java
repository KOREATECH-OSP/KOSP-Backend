package io.swkoreatech.kosp.launcher;

public enum Priority {
    HIGH(1),
    LOW(10);
    
    private final int order;
    
    Priority(int order) {
        this.order = order;
    }
    
    public int getOrder() {
        return order;
    }
}
