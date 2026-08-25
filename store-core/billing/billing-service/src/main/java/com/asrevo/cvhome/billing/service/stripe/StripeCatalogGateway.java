package com.asrevo.cvhome.billing.service.stripe;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeProductId;
import com.asrevo.cvhome.billing.commons.StripeRequestOperation;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.domain.PlanEntity;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;

import lombok.extern.slf4j.Slf4j;

/**
 * Publishes the local plan catalog to Stripe as products and prices.
 *
 * <p>
 * The database is the source of truth for what a plan grants; Stripe only needs to know what to charge. So this pushes
 * one way and writes the resulting ids back — it never reads a plan's shape out of Stripe.
 * </p>
 *
 * <p>
 * Both idempotency keys are derived purely from the catalog row, with no time component: publishing the same plan
 * twice must produce the same product, however far apart the attempts are. That is what makes the sync safe to run on
 * every boot.
 * </p>
 */
@Slf4j
@Component
public class StripeCatalogGateway extends StripeGatewaySupport {

    public StripeCatalogGateway(StripeCredentials credentials, StripeRequestRepository stripeRequestRepository,
                                StripeClient stripe) {
        super(credentials, stripeRequestRepository, stripe);
    }

    /**
     * Creates the Stripe product backing a plan.
     *
     * @throws BillingProviderUnavailableException Stripe could not be reached. No {@code CardException} branch: no
     *                                             money moves when publishing a catalog, so there is no refusal to
     *                                             tell apart from a fault.
     */
    public StripeProductId createProduct(PlanEntity plan) throws BillingProviderUnavailableException {
        String key = String.format("product:%s", plan.getCode());
        recordIntent(key, null, StripeRequestOperation.PRODUCT_CREATE);
        ProductCreateParams params = ProductCreateParams.builder()
                .setName(plan.getDisplayName())
                .setDescription(plan.getDescription())
                .build();
        try {
            Product product = stripe().products().create(params, options(key));
            recordCompletion(key, product.getId());
            log.info("Published plan {} to Stripe as product {}", plan.getCode(), product.getId());
            return new StripeProductId(product.getId());
        } catch (StripeException e) {
            throw BillingProviderUnavailableException.of(STRIPE, null, StripeRequestOperation.PRODUCT_CREATE,
                    e.getCode(), statusOf(e), e);
        }
    }

    /**
     * Creates the Stripe price backing one purchasable price of a plan.
     *
     * <p>
     * Stripe prices are immutable, which is why the local catalog mints a new row rather than editing an amount —
     * the two behaviours have to agree or a subscriber's charge would silently diverge from what the catalog says.
     * </p>
     *
     * @throws BillingProviderUnavailableException Stripe could not be reached
     */
    public StripePriceId createPrice(PlanEntity plan, PlanPriceEntity price)
            throws BillingProviderUnavailableException {
        String key = String.format("price:%s:%s:%s:%s", plan.getCode(), price.getCurrency().code(),
                price.getBillingInterval(), price.getUnitAmount());
        recordIntent(key, null, StripeRequestOperation.PRICE_CREATE);
        PriceCreateParams params = PriceCreateParams.builder()
                .setProduct(plan.getStripeProductId().id())
                .setCurrency(price.getCurrency().code().toLowerCase(Locale.ROOT))
                .setUnitAmount(price.getUnitAmount())
                .setRecurring(PriceCreateParams.Recurring.builder()
                        .setInterval(intervalOf(price))
                        .build())
                .build();
        try {
            Price created = stripe().prices().create(params, options(key));
            recordCompletion(key, created.getId());
            log.info("Published {} {} price of plan {} to Stripe as {}", price.getCurrency().code(),
                    price.getBillingInterval(), plan.getCode(), created.getId());
            return new StripePriceId(created.getId());
        } catch (StripeException e) {
            throw BillingProviderUnavailableException.of(STRIPE, null, StripeRequestOperation.PRICE_CREATE,
                    e.getCode(), statusOf(e), e);
        }
    }

    private PriceCreateParams.Recurring.Interval intervalOf(PlanPriceEntity price) {
        return switch (price.getBillingInterval()) {
            case MONTH -> PriceCreateParams.Recurring.Interval.MONTH;
            case YEAR -> PriceCreateParams.Recurring.Interval.YEAR;
        };
    }

}
