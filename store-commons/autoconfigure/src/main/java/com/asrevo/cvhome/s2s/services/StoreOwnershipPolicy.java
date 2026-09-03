package com.asrevo.cvhome.s2s.services;

/**
 * Who checks that an org admin's store is really theirs.
 *
 * <p>
 * An org admin's token names the organization they administer and never the store their request names, so
 * something has to compare the two. Two things can: the shared permission gate, or the service itself.
 * </p>
 */
public enum StoreOwnershipPolicy {

    /**
     * The gate checks, and refuses when it cannot. The default, and what every service should want: a service
     * that has no store-to-organization lookup admits no org admin at all, so forgetting to wire one is a
     * locked door rather than an open one.
     */
    ENFORCED,

    /**
     * The service checks, and the gate stays out of it.
     *
     * <p>
     * For a service that owns the store records and can give a better answer than the gate can. Tenancy is the
     * one: it refuses a foreign store with "not found" rather than "forbidden", so that asking about somebody
     * else's store does not confirm the store exists — a distinction a permission gate, which can only say yes
     * or no, is unable to make. Choosing this is choosing to do the check yourself, and it is only honest where
     * that check demonstrably exists.
     * </p>
     */
    DELEGATED

}
