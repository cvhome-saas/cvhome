/**
 * The platform operator's view of billing.
 *
 * <p>
 * Separate from the store-scoped DTOs beside it because the audience is different and so is the cost of rendering
 * one. {@code SubscriptionView} is assembled by {@code SubscriptionMappers}, which resolves the plan, the price and
 * the entitlement map through {@code PlanCatalogService} — an <strong>uncached</strong> per-id read, two to four
 * round trips per row. That is right for one store's own page and wrong for a fifty-row register: the same shape
 * would be two hundred queries to draw a table that wants none of it. Everything here is flat and comes out of one
 * joined query.
 * </p>
 */
package com.asrevo.cvhome.billing.commons.dto.admin;
