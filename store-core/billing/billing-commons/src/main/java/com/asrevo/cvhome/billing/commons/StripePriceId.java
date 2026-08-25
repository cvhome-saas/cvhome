package com.asrevo.cvhome.billing.commons;

import com.asrevo.cvhome.commons.domain.Identifier;

/**
 * Identifies a Stripe price, the remote counterpart of a {@link PlanPriceId}.
 *
 * <p>
 * Stripe ids are opaque strings minted by Stripe (here, {@code price_}); they are never generated locally and never
 * parsed. Wrapping them keeps one from being passed where another is expected, which raw {@code String} would allow.
 * </p>
 */
public record StripePriceId(String id) implements Identifier {

    @Override
    public String getId() {
        return this.id;
    }

}
