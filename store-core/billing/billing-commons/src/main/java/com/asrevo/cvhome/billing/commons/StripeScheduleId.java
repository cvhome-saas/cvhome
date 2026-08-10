package com.asrevo.cvhome.billing.commons;

import com.asrevo.cvhome.commons.domain.Identifier;

/**
 * Identifies a Stripe subscription schedule, used to defer a downgrade to the end of the paid period.
 *
 * <p>
 * Stripe ids are opaque strings minted by Stripe (here, {@code sub_sched_}); they are never generated locally and never
 * parsed. Wrapping them keeps one from being passed where another is expected, which raw {@code String} would allow.
 * </p>
 */
public record StripeScheduleId(String id) implements Identifier {

    @Override
    public String getId() {
        return this.id;
    }

}
