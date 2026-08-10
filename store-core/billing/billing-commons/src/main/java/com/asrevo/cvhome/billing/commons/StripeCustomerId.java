package com.asrevo.cvhome.billing.commons;

import com.asrevo.cvhome.commons.domain.Identifier;

/**
 * Identifies a Stripe customer. One per org: billing details and payment methods are org-level, while each store has
 * its own subscription underneath.
 *
 * <p>
 * Stripe ids are opaque strings minted by Stripe (here, {@code cus_}); they are never generated locally and never
 * parsed. Wrapping them keeps one from being passed where another is expected, which raw {@code String} would allow.
 * </p>
 */
public record StripeCustomerId(String id) implements Identifier {

    @Override
    public String getId() {
        return this.id;
    }

}
