package com.asrevo.cvhome.billing.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.repository.PlanPriceRepository;
import com.asrevo.cvhome.billing.repository.PlanRepository;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Puts a seeded store into the state a test needs, through the real repositories.
 *
 * <p>
 * Arranged in Java rather than in {@code data-test-stores.sql} because the plan catalog is written by
 * {@code PlanCatalogSeeder} during start-up, after {@code spring.sql.init} has run, with ids it mints itself. A
 * fixture that named a plan id in SQL would either break the foreign key or go stale the moment
 * {@code plan-catalog.yml} changed; reading the ids back is the only way to stay true to whatever the catalog
 * actually is.
 * </p>
 *
 * <p>
 * Every method is idempotent and scoped to one store, because all the integration tests share a single context and a
 * single database — a helper that reset something global would make the suite order-dependent.
 * </p>
 */
public class BillingFixtures {

    /** The Stripe subscription a store is bound to, so a plan change has something to act on. */
    public static final String PROVIDER_SUBSCRIPTION = "sub_integration_test";

    private final PlanRepository planRepository;

    private final PlanPriceRepository priceRepository;

    private final StoreSubscriptionRepository subscriptionRepository;

    public BillingFixtures(PlanRepository planRepository, PlanPriceRepository priceRepository,
                           StoreSubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.priceRepository = priceRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Gives every active price a Stripe id.
     *
     * <p>
     * {@code catalog.stripe-sync-enabled} is off in tests, so nothing publishes the catalog and every price's
     * {@code stripe_price_id} is null — which {@code checkout} correctly reports as "not purchasable". Filling them
     * in here is what makes the purchase paths reachable without letting a test near Stripe.
     * </p>
     */
    public void publishPrices() {
        for (PlanPriceEntity price : activePrices()) {
            if (price.getStripePriceId() == null) {
                priceRepository.save(price.publishedAs(new StripePriceId("price_" + price.getId().getId())));
            }
        }
    }

    /** Active plans, cheapest tier first — the same order the public catalog renders in. */
    public List<PlanEntity> plans() {
        return planRepository.findAllByActiveTrueOrderByTierAsc();
    }

    /** Every active price, cheapest first. */
    public List<PlanPriceEntity> activePrices() {
        return plans().stream()
                .map(plan -> priceRepository.findAllByPlanIdAndActiveTrue(plan.getId()))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(PlanPriceEntity::getUnitAmount)
                        .thenComparing(it -> it.getId().getId().toString()))
                .toList();
    }

    /** The cheapest active price, which is what a downgrade aims at. */
    public PlanPriceEntity cheapestPrice() {
        return activePrices().getFirst();
    }

    /** The dearest active price, which is what an upgrade aims at. */
    public PlanPriceEntity dearestPrice() {
        return activePrices().getLast();
    }

    /**
     * Makes {@code store} a paying subscriber on {@code price}, bound to a provider subscription.
     *
     * <p>
     * Written straight onto the row rather than through the state machine: the store may be in any state a previous
     * test left it in, and a test's arrange step should not have to be a legal transition from that.
     * </p>
     */
    public StoreSubscriptionEntity active(String store, PlanPriceEntity price) {
        StoreMerchantId id = new StoreMerchantId(store);
        subscriptionRepository.deleteById(id);
        StoreSubscriptionEntity entity = StoreSubscriptionEntity.pending(id, orgOf(store));
        try {
            entity.activate(price.getPlanId(), price.getId(), Instant.now(),
                    Instant.now().plus(30L, ChronoUnit.DAYS));
        } catch (Exception e) {
            throw new IllegalStateException("PENDING to ACTIVE is a legal transition", e);
        }
        entity.bindProvider(new StripeCustomerId("cus_integration_test"),
                new StripeSubscriptionId(PROVIDER_SUBSCRIPTION + ":" + store));
        return subscriptionRepository.save(entity);
    }

    /** Returns {@code store} to never-having-paid, so a test that needs a blank row gets one. */
    public StoreSubscriptionEntity pending(String store) {
        StoreMerchantId id = new StoreMerchantId(store);
        subscriptionRepository.deleteById(id);
        return subscriptionRepository.save(StoreSubscriptionEntity.pending(id, orgOf(store)));
    }

    /** Writes a subscription back, for a test that needs to bind provider ids of its own. */
    public StoreSubscriptionEntity save(StoreSubscriptionEntity entity) {
        return subscriptionRepository.save(entity);
    }

    public StoreSubscriptionEntity read(String store) {
        return subscriptionRepository.findById(new StoreMerchantId(store)).orElseThrow();
    }

    /**
     * Which tenant a seeded store belongs to, from its id.
     *
     * <p>
     * The fixture ids are deliberately prefixed — {@code b111…} for org A, {@code b222…} for the neighbour — so that
     * a test reading one can see at a glance which side of the tenant boundary it is on.
     * </p>
     */
    private static ManagerOrgId orgOf(String store) {
        return new ManagerOrgId(store.startsWith("b222") ? BillingApiSupport.ORG_B : BillingApiSupport.ORG_A);
    }

}
