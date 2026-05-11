package com.bistro.domain.model.discount;

public enum DiscountPhase {
    ITEM_LEVEL(1),
    ORDER_LEVEL(2);

    private final int priority;

    DiscountPhase(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
