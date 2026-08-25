package com.asrevo.cvhome.billing.service.stripe;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeRequestOperation;
import com.asrevo.cvhome.billing.commons.errors.BillingProviderUnavailableException;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionChangeRejectedException;
import com.asrevo.cvhome.billing.config.StripeCredentials;
import com.asrevo.cvhome.billing.domain.PlanPriceEntity;
import com.asrevo.cvhome.billing.repository.StripeRequestRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.extern.slf4j.Slf4j;

/**
 * Starts a subscription by sending the customer to Stripe's hosted checkout.
 *
 * <p>
 * Nothing local changes here. The subscription becomes real when Stripe says the money moved, which arrives as
 * {@code invoice.payment_succeeded} — treating the redirect as success would activate stores that abandoned the
 * payment page.
 * </p>
 */
@Slf4j
@Component
public class StripeCheckoutGateway extends StripeGatewaySupport {

    private static final String STORE_METADATA_KEY = "storeId";

    private static final String ORG_METADATA_KEY = "orgId";

    private static final String PRICE_METADATA_KEY = "planPriceId";

    public StripeCheckoutGateway(StripeCredentials credentials, StripeRequestRepository stripeRequestRepository) {
        super(credentials, stripeRequestRepository);
    }

    /**
     * Creates a subscription-mode checkout session and returns where to send the customer.
     *
     * <p>
     * The store id travels twice, deliberately: as {@code client_reference_id} on the session, and in the
     * subscription's metadata. The first is how {@code checkout.session.completed} is attributed; the second is how
     * every later {@code customer.subscription.*} event is, since those carry no reference to the session that
     * created them.
     * </p>
     *
     * @throws SubscriptionChangeRejectedException  Stripe refused outright — the customer must fix something before
     *                                              retrying
     * @throws BillingProviderUnavailableException  Stripe could not be reached, so nothing was decided
     */
    public String createSubscriptionSession(StoreMerchantId store, ManagerOrgId org, StripeCustomerId customer,
                                            PlanPriceEntity price, String successUrl, String cancelUrl)
            throws SubscriptionChangeRejectedException, BillingProviderUnavailableException {
        String key = idempotencyKey(StripeRequestOperation.CHECKOUT_SESSION_CREATE,
                String.format("%s:%s", store.getId(), price.getId().getId()));
        recordIntent(key, store, StripeRequestOperation.CHECKOUT_SESSION_CREATE);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customer.id())
                .setClientReferenceId(store.getId().toString())
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPrice(price.getStripePriceId().id())
                        .build())
                .setSubscriptionData(SessionCreateParams.SubscriptionData.builder()
                        .putMetadata(STORE_METADATA_KEY, store.getId().toString())
                        .putMetadata(ORG_METADATA_KEY, org.getId().toString())
                        .putMetadata(PRICE_METADATA_KEY, price.getId().getId().toString())
                        .build())
                .build();

        try {
            Session session = Session.create(params, options(key));
            recordCompletion(key, session.getId());
            log.info("Opened Stripe checkout {} for store {}", session.getId(), store);
            return session.getUrl();
        } catch (CardException e) {
            // The one Stripe failure that is an answer rather than a fault: the card was refused. Retrying this
            // request unchanged will be refused again, so the customer is told, not the operator.
            throw SubscriptionChangeRejectedException.of(STRIPE, store, price.getId(), e.getCode(), statusOf(e), e);
        } catch (StripeException e) {
            // Everything else — no connection, rate limited, our key rejected, a request we built wrong — settles
            // nothing. Calling any of those a refusal would tell the customer their card failed when it never ran.
            throw BillingProviderUnavailableException.of(STRIPE, store,
                    StripeRequestOperation.CHECKOUT_SESSION_CREATE, e.getCode(), statusOf(e), e);
        }
    }

}
