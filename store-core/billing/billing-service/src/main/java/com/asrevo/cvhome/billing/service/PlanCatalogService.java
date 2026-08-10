package com.asrevo.cvhome.billing.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.dto.PlanView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;
import com.asrevo.cvhome.billing.commons.errors.PlanPriceNotFoundException;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;

/**
 * Read access to the plan catalog.
 *
 * <p>
 * The catalog is the product's vocabulary and lives in the database rather than in an enum, so a new plan or a
 * changed ceiling is a configuration change rather than a release.
 * </p>
 */
public interface PlanCatalogService {

    /**
     * Every plan on sale, cheapest tier first.
     *
     * @param currency restrict the prices shown to one currency, or {@code null} for all of them
     */
    List<PlanView> listActivePlans(String currency);

    /**
     * @throws PlanNotFoundException when no active plan carries that code
     */
    PlanEntity requirePlanByCode(String code) throws PlanNotFoundException;

    /**
     * @throws PlanNotFoundException when the plan is absent — a dangling reference from a subscription row
     */
    PlanEntity requirePlan(PlanId planId) throws PlanNotFoundException;

    /**
     * Resolves a price that may still be bought.
     *
     * @throws PlanPriceNotFoundException when it is absent or withdrawn from sale
     */
    PlanPriceEntity requirePurchasablePrice(PlanPriceId planPriceId) throws PlanPriceNotFoundException;

    /**
     * Resolves a price for reading, including ones withdrawn from sale — an existing subscriber must still be shown
     * what they are paying after the catalog moves on.
     *
     * @throws PlanPriceNotFoundException when it is absent entirely
     */
    PlanPriceEntity requirePrice(PlanPriceId planPriceId) throws PlanPriceNotFoundException;

    /**
     * What a plan grants. An absent key means unlimited, so callers must not treat a missing entry as zero.
     */
    Map<EntitlementKey, EntitlementValue> entitlementsOf(PlanId planId);

    /**
     * Looks up a plan without insisting it exists — for rendering a subscription, where a null plan simply means the
     * store has never been on one.
     */
    Optional<PlanEntity> findPlan(PlanId planId);

    /**
     * Looks up a price without insisting it exists, including ones withdrawn from sale.
     */
    Optional<PlanPriceEntity> findPrice(PlanPriceId planPriceId);

    /**
     * The price a trial runs on: the cheapest active price of the lowest tier on sale.
     *
     * <p>
     * Derived rather than configured, so the trial cannot drift out of step with the catalog — there is no second
     * place to update when the free tier changes.
     * </p>
     */
    Optional<PlanPriceEntity> cheapestActivePrice();

}
