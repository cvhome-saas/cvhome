package com.asrevo.cvhome.billing.commons;

import com.asrevo.cvhome.commons.domain.Identifier;

/**
 * Identifies a Stripe product, the remote counterpart of a {@link PlanId}.
 *
 * <p>
 * Stripe ids are opaque strings minted by Stripe (here, {@code prod_}); they are never generated locally and never
 * parsed. Wrapping them keeps one from being passed where another is expected, which raw {@code String} would allow.
 * </p>
 */
public record StripeProductId(String id) implements Identifier {

    @Override
    public String getId() {
        return this.id;
    }

}
