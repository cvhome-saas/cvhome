package com.asrevo.cvhome.billing.commons;

/**
 * What a plan grants. Each key is either a ceiling ({@code MAX_*}, carrying a {@code limitValue}) or a capability
 * flag, never both — {@code billing.plan_entitlement} enforces that shape with a {@code CHECK}.
 *
 * <p>
 * Keys live here rather than in any one pod because the plan catalog is the product's vocabulary, not any single
 * service's. The pod that <em>enforces</em> a key owns the counter behind it: catalog counts products, merchant
 * counts accounts, checkout counts orders.
 * </p>
 */
public enum EntitlementKey {

    /** Ceiling on products in the store's catalog. */
    MAX_PRODUCTS,

    /** Ceiling on orders placed within the current calendar month. */
    MAX_ORDERS_MONTH,

    /** Ceiling on staff accounts with access to the store. */
    MAX_ACCOUNTS,

    /** Ceiling on uploaded media, in megabytes. */
    MAX_STORAGE_MB,

    /** Whether the store may bind its own domain. */
    CUSTOM_DOMAIN,

    /** Whether the analytics dashboards are available. */
    ANALYTICS,

    /** Whether support requests from this store are prioritised. */
    PRIORITY_SUPPORT;

    /**
     * Whether this key is a numeric ceiling rather than an on/off capability.
     */
    public boolean numeric() {
        return this == MAX_PRODUCTS || this == MAX_ORDERS_MONTH || this == MAX_ACCOUNTS || this == MAX_STORAGE_MB;
    }

}
