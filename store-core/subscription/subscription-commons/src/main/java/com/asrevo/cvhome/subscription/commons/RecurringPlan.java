package com.asrevo.cvhome.subscription.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecurringPlan {
    MONTHLY(1, 1),
    YEARLY(12, 1.2f);
    private final int times;
    private final float factor;
}
