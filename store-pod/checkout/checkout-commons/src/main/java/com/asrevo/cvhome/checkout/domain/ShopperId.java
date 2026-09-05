package com.asrevo.cvhome.checkout.domain;

/**
 * The shopper as cua names them: the {@code sub} claim of a shopper token (an account id, not a username — with one
 * realm per store four demo shoppers are all called {@code user}). Joined to {@code customer_account.cua_external_id}.
 */
public record ShopperId(String sub) {

    public ShopperId {
        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("shopper id must not be blank");
        }
    }
}
