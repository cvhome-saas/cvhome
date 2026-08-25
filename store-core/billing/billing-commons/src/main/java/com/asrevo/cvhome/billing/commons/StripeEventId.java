package com.asrevo.cvhome.billing.commons;

import com.asrevo.cvhome.commons.domain.Identifier;

/**
 * Identifies a Stripe webhook event. The primary key of {@code billing.processed_stripe_event}, which is what makes redelivery a no-op.
 *
 * <p>
 * Stripe ids are opaque strings minted by Stripe (here, {@code evt_}); they are never generated locally and never
 * parsed. Wrapping them keeps one from being passed where another is expected, which raw {@code String} would allow.
 * </p>
 */
public record StripeEventId(String id) implements Identifier {

    @Override
    public String getId() {
        return this.id;
    }

}
