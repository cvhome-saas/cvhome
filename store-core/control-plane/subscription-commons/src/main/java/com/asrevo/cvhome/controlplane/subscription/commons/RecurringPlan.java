package com.asrevo.cvhome.controlplane.subscription.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecurringPlan {

    MONTH(1, 1), YEAR(12, .83333333f);

    private final int times;

    private final float factor;

}
